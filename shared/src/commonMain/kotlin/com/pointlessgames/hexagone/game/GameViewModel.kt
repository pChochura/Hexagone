package com.pointlessgames.hexagone.game

import androidx.lifecycle.ViewModel
import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class GameViewModel : ViewModel() {

    private val engine = GameEngine()

    private val _gridState = MutableStateFlow<List<HexagonCell>>(emptyList())
    val gridState: StateFlow<List<HexagonCell>> = _gridState.asStateFlow()

    private val _previewState = MutableStateFlow<List<PreviewCell>>(emptyList())
    val previewState: StateFlow<List<PreviewCell>> = _previewState.asStateFlow()

    private val _pendingMerge = MutableStateFlow<MergeTransition?>(null)
    val pendingMerge: StateFlow<MergeTransition?> = _pendingMerge.asStateFlow()

    private val _hoveredMerge = MutableStateFlow<MergeTransition?>(null)
    val hoveredMerge: StateFlow<MergeTransition?> = _hoveredMerge.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _bestScore = MutableStateFlow(0)
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    private val _level = MutableStateFlow(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _highestValue = MutableStateFlow(1)
    val highestValue: StateFlow<Int> = _highestValue.asStateFlow()

    private val _combo = MutableStateFlow(0)
    val combo: StateFlow<Int> = _combo.asStateFlow()

    private val _isStuck = MutableStateFlow(false)
    val isStuck: StateFlow<Boolean> = _isStuck.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver: StateFlow<Boolean> = _isGameOver.asStateFlow()

    private val _collectedPerks = MutableStateFlow<List<Perk>>(emptyList())
    val collectedPerks: StateFlow<List<Perk>> = _collectedPerks.asStateFlow()

    private val _perkOptions = MutableStateFlow<List<Perk>>(emptyList())
    val perkOptions: StateFlow<List<Perk>> = _perkOptions.asStateFlow()

    private val _activePerk = MutableStateFlow<Perk?>(null)
    val activePerk: StateFlow<Perk?> = _activePerk.asStateFlow()

    private val _selectedCellId = MutableStateFlow<String?>(null)
    val selectedCellId: StateFlow<String?> = _selectedCellId.asStateFlow()

    private var lastLevel = 1

    init {
        restartGame()
    }

    private fun updateLevel() {
        val currentScore = _score.value
        val lvl = engine.calculateLevel(currentScore)

        if (lvl > lastLevel) {
            lastLevel = lvl
            _perkOptions.value = List(3) { Perk.entries.random() }
        }

        _level.value = lvl
        val highest = _gridState.value.maxOfOrNull { it.value } ?: 1
        _highestValue.value = highest
    }

    fun onEmptySpaceClicked(x: Int, y: Int) {
        if (_pendingMerge.value != null || _isGameOver.value || _isStuck.value || _perkOptions.value.isNotEmpty()) return
        _hoveredMerge.value = null

        val perk = _activePerk.value
        val selectedId = _selectedCellId.value
        val previewAtPos = _previewState.value.find { it.x == x && it.y == y }

        if (perk != null && perk != Perk.FUSION && previewAtPos != null) {
            if (selectedId == previewAtPos.id) {
                _selectedCellId.value = null
                return
            }
            if (selectedId == null || perk == Perk.REMOVE_TILE || (perk == Perk.SWAP_TILES && selectedId != previewAtPos.id)) {
                onPreviewClicked(previewAtPos)
                return
            }
        }

        if (perk == Perk.MOVE_TILE && selectedId != null) {
            moveTile(selectedId, x, y)
            return
        }

        val currentState = _gridState.value
        if (currentState.any { it.x == x && it.y == y }) return

        val merge = if (perk == Perk.FUSION) {
            engine.calculateFusion(x, y, currentState)
        } else {
            engine.calculateMerge(x, y, currentState)
        }

        if (merge != null) {
            _gridState.value = currentState.map { cell ->
                if (merge.mergingCells.any { it.id == cell.id }) cell.copy(x = x, y = y) else cell
            }
            _pendingMerge.value = merge
            if (perk == Perk.FUSION) {
                consumePerk(Perk.FUSION)
                _activePerk.value = null
            }
        }
    }

    fun onEmptySpaceTouchDown(x: Int, y: Int) {
        if (_pendingMerge.value != null || _isGameOver.value || _isStuck.value || _perkOptions.value.isNotEmpty()) return
        val perk = _activePerk.value
        if (perk != null && perk != Perk.CHAIN_MERGE && perk != Perk.FUSION) return

        val currentState = _gridState.value
        if (currentState.any { it.x == x && it.y == y }) return

        _hoveredMerge.value = if (perk == Perk.FUSION) {
            engine.calculateFusion(x, y, currentState)
        } else {
            engine.calculateMerge(x, y, currentState)
        }
    }

    fun onEmptySpaceTouchUp() {
        _hoveredMerge.value = null
    }

    private fun moveTile(selectedId: String, x: Int, y: Int) {
        val cellToMove = _gridState.value.find { it.id == selectedId }
        if (cellToMove != null) {
            _gridState.value = _gridState.value.map {
                if (it.id == selectedId) it.copy(x = x, y = y) else it
            }
            finishPerkAction(Perk.MOVE_TILE)
            return
        }

        val previewToMove = _previewState.value.find { it.id == selectedId }
        if (previewToMove != null) {
            _previewState.value = _previewState.value.map {
                if (it.id == selectedId) it.copy(x = x, y = y) else it
            }
            finishPerkAction(Perk.MOVE_TILE)
        }
    }

    private fun finishPerkAction(perk: Perk) {
        consumePerk(perk)
        _activePerk.value = null
        _selectedCellId.value = null
        checkValidMoves()
    }

    fun onPreviewClicked(preview: PreviewCell) {
        if (_pendingMerge.value != null || _isGameOver.value || _isStuck.value || _perkOptions.value.isNotEmpty()) return

        when (_activePerk.value) {
            Perk.MOVE_TILE -> {
                _selectedCellId.value = preview.id
            }
            Perk.REMOVE_TILE -> {
                val remaining = _previewState.value.filter { it.id != preview.id }
                _previewState.value = engine.pickRandomPreviews(_gridState.value, remaining, 3)
                finishPerkAction(Perk.REMOVE_TILE)
            }
            Perk.SWAP_TILES -> {
                val selectedId = _selectedCellId.value
                if (selectedId == null) {
                    _selectedCellId.value = preview.id
                } else if (selectedId != preview.id) {
                    swapTiles(selectedId, preview.id)
                }
            }
            else -> {}
        }
    }

    fun onCellClicked(cell: HexagonCell) {
        if (_pendingMerge.value != null || _isGameOver.value || _isStuck.value || _perkOptions.value.isNotEmpty()) return

        when (val perk = _activePerk.value) {
            Perk.MOVE_TILE -> {
                if (_selectedCellId.value == cell.id) {
                    _selectedCellId.value = null
                } else {
                    _selectedCellId.value = cell.id
                }
            }
            Perk.REMOVE_TILE -> {
                _gridState.value = _gridState.value.filter { it.id != cell.id }
                finishPerkAction(Perk.REMOVE_TILE)
            }
            Perk.SWAP_TILES -> {
                val selectedId = _selectedCellId.value
                if (selectedId == null) {
                    _selectedCellId.value = cell.id
                } else if (selectedId == cell.id) {
                    _selectedCellId.value = null
                } else {
                    swapTiles(selectedId, cell.id)
                }
            }
            else -> {}
        }
    }

    private fun swapTiles(id1: String, id2: String) {
        val grid = _gridState.value
        val previews = _previewState.value

        val cell1 = grid.find { it.id == id1 }
        val cell2 = grid.find { it.id == id2 }
        val preview1 = previews.find { it.id == id1 }
        val preview2 = previews.find { it.id == id2 }

        // Determine coordinates
        val x1 = cell1?.x ?: preview1?.x ?: return
        val y1 = cell1?.y ?: preview1?.y ?: return
        val x2 = cell2?.x ?: preview2?.x ?: return
        val y2 = cell2?.y ?: preview2?.y ?: return

        if (cell1 != null) {
            _gridState.value = _gridState.value.map {
                if (it.id == id1) it.copy(x = x2, y = y2) else it
            }
        } else if (preview1 != null) {
            _previewState.value = _previewState.value.map {
                if (it.id == id1) it.copy(x = x2, y = y2) else it
            }
        }

        if (cell2 != null) {
            _gridState.value = _gridState.value.map {
                if (it.id == id2) it.copy(x = x1, y = y1) else it
            }
        } else if (preview2 != null) {
            _previewState.value = _previewState.value.map {
                if (it.id == id2) it.copy(x = x1, y = y1) else it
            }
        }

        finishPerkAction(Perk.SWAP_TILES)
    }

    private fun consumePerk(perk: Perk) {
        val perkIndex = _collectedPerks.value.indexOf(perk)
        if (perkIndex != -1) {
            val newList = _collectedPerks.value.toMutableList()
            newList.removeAt(perkIndex)
            _collectedPerks.value = newList
        }
    }

    fun onUsePerkClicked(perk: Perk) {
        if (_activePerk.value == perk) {
            _activePerk.value = null
            _selectedCellId.value = null
            return
        }

        _isStuck.value = false
        when (perk) {
            Perk.ADVANCE_QUEUE -> {
                consumePerk(Perk.ADVANCE_QUEUE)
                spawnFromQueue(_gridState.value)
                checkValidMoves()
            }
            Perk.MOVE_TILE -> _activePerk.value = Perk.MOVE_TILE
            Perk.REMOVE_TILE -> _activePerk.value = Perk.REMOVE_TILE
            Perk.FUSION -> _activePerk.value = Perk.FUSION
            Perk.SWAP_TILES -> _activePerk.value = Perk.SWAP_TILES
            Perk.CHAIN_MERGE -> _activePerk.value = Perk.CHAIN_MERGE
        }
    }

    fun onPerkSelected(perk: Perk) {
        _collectedPerks.value = _collectedPerks.value + perk
        _perkOptions.value = emptyList()
        checkValidMoves()
    }

    fun onRestartClicked() {
        restartGame()
    }

    private fun restartGame() {
        _score.value = 0
        _isGameOver.value = false
        _isStuck.value = false
        _collectedPerks.value = emptyList()
        _perkOptions.value = emptyList()
        _activePerk.value = null
        _selectedCellId.value = null
        lastLevel = 1
        _gridState.value = engine.generateInitialGrid()
        _previewState.value = engine.pickRandomPreviews(_gridState.value, emptyList(), 3)
        updateLevel()
        checkValidMoves()
    }

    fun onMergeAnimationFinished() {
        val merge = _pendingMerge.value ?: return
        _pendingMerge.value = null

        val comboMultiplier = _combo.value + 1
        _combo.value = comboMultiplier
        
        val addedScore = merge.newValue * merge.totalCells * comboMultiplier
        _score.value += addedScore
        if (_score.value > _bestScore.value) _bestScore.value = _score.value

        val stateAfterMerge = _gridState.value.filter { cell ->
            merge.mergingCells.none { it.id == cell.id } && (cell.x != merge.targetX || cell.y != merge.targetY)
        } + engine.createCell(merge.targetX, merge.targetY, merge.newValue)

        _gridState.value = stateAfterMerge
        
        // Check for chain merge ONLY if the CHAIN_MERGE perk is active
        val chainMerge = if (_activePerk.value == Perk.CHAIN_MERGE) {
            engine.calculateMerge(merge.targetX, merge.targetY, stateAfterMerge)
        } else null

        if (chainMerge != null) {
            // Update gridState to move neighbors of the chain merge to the target position
            _gridState.value = stateAfterMerge.map { cell ->
                if (chainMerge.mergingCells.any { it.id == cell.id }) cell.copy(x = merge.targetX, y = merge.targetY) else cell
            }
            _pendingMerge.value = chainMerge
        } else {
            if (_activePerk.value == Perk.CHAIN_MERGE) {
                consumePerk(Perk.CHAIN_MERGE)
                _activePerk.value = null
            }
            _combo.value = 0
            spawnFromQueue(stateAfterMerge)
            checkValidMoves()
        }
    }

    private fun checkValidMoves() {
        val currentState = _gridState.value
        if (engine.isMovePossible(currentState)) {
            _isStuck.value = false
            _isGameOver.value = false
            return
        }

        if (_perkOptions.value.isNotEmpty()) {
            _isStuck.value = false
            _isGameOver.value = false
            return
        }

        val actionablePerks = _collectedPerks.value.filter { it.canSaveFromStuck }
        if (actionablePerks.isNotEmpty()) {
            _isStuck.value = true
            _isGameOver.value = false
        } else {
            _isStuck.value = false
            _isGameOver.value = true
        }
    }

    private fun spawnFromQueue(currentState: List<HexagonCell>) {
        val (newState, newPreviews) = engine.spawnFromQueue(currentState, _previewState.value)
        _gridState.value = newState
        _previewState.value = newPreviews
        updateLevel()
    }

    fun getLevelProgress(): Float {
        return engine.getLevelProgress(_score.value, _level.value)
    }
}
