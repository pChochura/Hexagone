package com.pointlessgames.hexagone.game

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PreviewCell
import com.pointlessgames.hexagone.game.model.ScorePopup
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

internal data class GameUiState(
    val grid: List<HexagonCell> = emptyList(),
    val preview: List<PreviewCell> = emptyList(),
    val pendingMerge: MergeTransition? = null,
    val hoveredMerge: MergeTransition? = null,
    val score: Int = 0,
    val bestScore: Int = 0,
    val level: Int = 1,
    val highestValue: Int = 1,
    val combo: Int = 0,
    val isBusy: Boolean = false,
    val isStuck: Boolean = false,
    val isGameOver: Boolean = false,
    val collectedPerks: List<Perk> = emptyList(),
    val perkOptions: List<Perk> = emptyList(),
    val activePerk: Perk? = null,
    val selectedCellId: String? = null,
    val particles: List<Particle> = emptyList(),
    val scorePopups: List<ScorePopup> = emptyList()
)

internal data class GameState(
    val grid: List<HexagonCell>,
    val preview: List<PreviewCell>,
    val score: Int,
    val level: Int,
    val highestValue: Int,
    val combo: Int,
    val collectedPerks: List<Perk>
)

internal class GameViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val engine = GameEngine()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var lastLevel = 1
    private var stateHistory = mutableListOf<GameState>()

    init {
        viewModelScope.launch {
            val best = settingsRepository.getBestScore()
            _uiState.update { it.copy(bestScore = best) }
        }
        restartGame()
        startAnimationLoop()
    }

    private fun startAnimationLoop() {
        viewModelScope.launch {
            while (true) {
                delay(16)
                val dt = 0.016f

                _uiState.update { state ->
                    val nextParticles = if (state.particles.isNotEmpty()) {
                        state.particles.mapNotNull { p ->
                            if (p.life <= 0) null
                            else p.copy(
                                x = p.x + p.vx * dt,
                                y = p.y + p.vy * dt,
                                life = p.life - dt * 2f,
                            )
                        }
                    } else state.particles

                    val nextPopups = if (state.scorePopups.isNotEmpty()) {
                        state.scorePopups.mapNotNull { s ->
                            if (s.life <= 0) null
                            else s.copy(
                                y = s.y - dt * 100f,
                                life = s.life - dt * 1.2f
                            )
                        }
                    } else state.scorePopups

                    state.copy(
                        particles = nextParticles,
                        scorePopups = nextPopups
                    )
                }
            }
        }
    }

    fun addParticles(newParticles: List<Particle>) {
        _uiState.update { it.copy(particles = it.particles + newParticles) }
    }

    fun addScorePopup(x: Float, y: Float, score: Int, color: Color) {
        val newPopup = ScorePopup(
            id = Random.nextLong(),
            x = x,
            y = y,
            score = score,
            life = 1f,
            color = color
        )
        _uiState.update { it.copy(scorePopups = it.scorePopups + newPopup) }
    }

    private fun saveState() {
        val state = _uiState.value
        val currentState = GameState(
            grid = state.grid,
            preview = state.preview,
            score = state.score,
            level = state.level,
            highestValue = state.highestValue,
            combo = state.combo,
            collectedPerks = state.collectedPerks
        )
        stateHistory.add(currentState)
        if (stateHistory.size > 10) stateHistory.removeAt(0)
    }

    private fun undoLastMove(): Boolean {
        if (stateHistory.isEmpty()) return false
        val previousState = stateHistory.removeAt(stateHistory.size - 1)
        
        _uiState.update { state ->
            state.copy(
                grid = previousState.grid,
                preview = previousState.preview,
                score = previousState.score,
                level = previousState.level,
                highestValue = previousState.highestValue,
                combo = previousState.combo,
                collectedPerks = previousState.collectedPerks,
                pendingMerge = null,
                activePerk = null,
                selectedCellId = null
            )
        }
        
        lastLevel = previousState.level
        checkValidMoves()
        return true
    }

    private fun updateLevel() {
        val currentScore = _uiState.value.score
        val lvl = engine.calculateLevel(currentScore)

        _uiState.update { state ->
            var nextPerkOptions = state.perkOptions
            if (lvl > lastLevel) {
                lastLevel = lvl
                nextPerkOptions = Perk.entries.shuffled().take(3)
            }

            val highest = state.grid.maxOfOrNull { it.value } ?: 1
            state.copy(
                level = lvl,
                highestValue = highest,
                perkOptions = nextPerkOptions
            )
        }
    }

    fun onEmptySpaceClicked(x: Int, y: Int) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || state.isStuck || state.perkOptions.isNotEmpty()) return
        
        saveState()
        _uiState.update { it.copy(hoveredMerge = null) }

        val perk = state.activePerk
        val selectedId = state.selectedCellId
        val previewAtPos = state.preview.find { it.x == x && it.y == y }

        if (perk != null && perk != Perk.FUSION && perk != Perk.CHAIN_MERGE && previewAtPos != null) {
            if (selectedId == previewAtPos.id) {
                _uiState.update { it.copy(selectedCellId = null) }
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

        if (state.grid.any { it.x == x && it.y == y }) return

        val merge = if (perk == Perk.FUSION) {
            engine.calculateFusion(x, y, state.grid)
        } else {
            engine.calculateMerge(x, y, state.grid)
        }

        if (merge != null) {
            _uiState.update { currentState ->
                currentState.copy(
                    grid = currentState.grid.map { cell ->
                        if (merge.mergingCells.any { it.id == cell.id }) cell.copy(x = x, y = y) else cell
                    },
                    pendingMerge = merge,
                    isBusy = true
                )
            }
            if (perk == Perk.FUSION) {
                consumePerk(Perk.FUSION)
            }
        } else {
            if (perk == Perk.CHAIN_MERGE) {
                finishPerkAction(Perk.CHAIN_MERGE)
                _uiState.update { it.copy(activePerk = null) }
            }
        }
    }

    fun onEmptySpaceTouchDown(x: Int, y: Int) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || state.isStuck || state.perkOptions.isNotEmpty()) return
        
        val perk = state.activePerk
        if (perk != null && perk != Perk.CHAIN_MERGE && perk != Perk.FUSION) return

        if (state.grid.any { it.x == x && it.y == y }) return

        val merge = if (perk == Perk.FUSION) {
            engine.calculateFusion(x, y, state.grid)
        } else {
            engine.calculateMerge(x, y, state.grid)
        }
        _uiState.update { it.copy(hoveredMerge = merge) }
    }

    fun onEmptySpaceTouchUp() {
        _uiState.update { it.copy(hoveredMerge = null) }
    }

    private fun moveTile(selectedId: String, x: Int, y: Int) {
        saveState()
        _uiState.update { state ->
            val cellToMove = state.grid.find { it.id == selectedId }
            if (cellToMove != null) {
                state.copy(
                    grid = state.grid.map { if (it.id == selectedId) it.copy(x = x, y = y) else it }
                )
            } else {
                val previewToMove = state.preview.find { it.id == selectedId }
                if (previewToMove != null) {
                    state.copy(
                        preview = state.preview.map { if (it.id == selectedId) it.copy(x = x, y = y) else it }
                    )
                } else state
            }
        }

        finishPerkAction(Perk.MOVE_TILE)
    }

    private fun finishPerkAction(perk: Perk) {
        consumePerk(perk)
        _uiState.update { it.copy(activePerk = null, selectedCellId = null) }
        checkValidMoves()
    }

    fun onPreviewClicked(preview: PreviewCell) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || state.isStuck || state.perkOptions.isNotEmpty()) return

        when (val perk = state.activePerk) {
            Perk.MOVE_TILE -> {
                _uiState.update { it.copy(selectedCellId = preview.id) }
            }
            Perk.REMOVE_TILE -> {
                saveState()
                val remaining = state.preview.filter { it.id != preview.id }
                _uiState.update { it.copy(preview = engine.pickRandomPreviews(it.grid, remaining, 3)) }
                finishPerkAction(Perk.REMOVE_TILE)
            }
            Perk.SWAP_TILES -> {
                val selectedId = state.selectedCellId
                if (selectedId == null) {
                    _uiState.update { it.copy(selectedCellId = preview.id) }
                } else if (selectedId != preview.id) {
                    swapTiles(selectedId, preview.id)
                }
            }
            else -> {}
        }
    }

    fun onCellClicked(cell: HexagonCell) {
        val state = _uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || state.isStuck || state.perkOptions.isNotEmpty()) return

        when (val perk = state.activePerk) {
            Perk.MOVE_TILE -> {
                _uiState.update { it.copy(selectedCellId = if (it.selectedCellId == cell.id) null else cell.id) }
            }
            Perk.REMOVE_TILE -> {
                saveState()
                _uiState.update { it.copy(grid = it.grid.filter { it.id != cell.id }) }
                finishPerkAction(Perk.REMOVE_TILE)
            }
            Perk.SWAP_TILES -> {
                val selectedId = state.selectedCellId
                if (selectedId == null) {
                    _uiState.update { it.copy(selectedCellId = cell.id) }
                } else if (selectedId == cell.id) {
                    _uiState.update { it.copy(selectedCellId = null) }
                } else {
                    saveState()
                    swapTiles(selectedId, cell.id)
                }
            }
            else -> {}
        }
    }

    private fun swapTiles(id1: String, id2: String) {
        val state = _uiState.value
        val cell1 = state.grid.find { it.id == id1 }
        val cell2 = state.grid.find { it.id == id2 }
        val preview1 = state.preview.find { it.id == id1 }
        val preview2 = state.preview.find { it.id == id2 }

        val x1 = cell1?.x ?: preview1?.x ?: return
        val y1 = cell1?.y ?: preview1?.y ?: return
        val x2 = cell2?.x ?: preview2?.x ?: return
        val y2 = cell2?.y ?: preview2?.y ?: return

        _uiState.update { currentState ->
            currentState.copy(
                grid = currentState.grid.map {
                    when (it.id) {
                        id1 -> it.copy(x = x2, y = y2)
                        id2 -> it.copy(x = x1, y = y1)
                        else -> it
                    }
                },
                preview = currentState.preview.map {
                    when (it.id) {
                        id1 -> it.copy(x = x2, y = y2)
                        id2 -> it.copy(x = x1, y = y1)
                        else -> it
                    }
                }
            )
        }
        finishPerkAction(Perk.SWAP_TILES)
    }

    private fun consumePerk(perk: Perk) {
        _uiState.update { state ->
            val perkIndex = state.collectedPerks.indexOf(perk)
            if (perkIndex != -1) {
                val newList = state.collectedPerks.toMutableList()
                newList.removeAt(perkIndex)
                state.copy(collectedPerks = newList)
            } else state
        }
    }

    fun onUsePerkClicked(perk: Perk) {
        val state = _uiState.value
        if (state.activePerk == perk) {
            _uiState.update { it.copy(activePerk = null, selectedCellId = null) }
            return
        }

        _uiState.update { it.copy(isStuck = false, isGameOver = false) }
        when (perk) {
            Perk.ADVANCE_QUEUE -> {
                saveState()
                consumePerk(Perk.ADVANCE_QUEUE)
                spawnFromQueue(_uiState.value.grid)
            }
            Perk.UNDO -> {
                if (undoLastMove()) {
                    consumePerk(Perk.UNDO)
                }
            }
            else -> _uiState.update { it.copy(activePerk = perk) }
        }
    }

    fun onPerkSelected(perk: Perk) {
        _uiState.update { it.copy(collectedPerks = it.collectedPerks + perk, perkOptions = emptyList()) }
        checkValidMoves()
    }

    fun onRestartClicked() {
        restartGame()
    }

    private fun restartGame() {
        stateHistory.clear()
        lastLevel = 1
        val initialGrid = engine.generateInitialGrid()
        _uiState.value = GameUiState(
            grid = initialGrid,
            preview = engine.pickRandomPreviews(initialGrid, emptyList(), 3),
            bestScore = _uiState.value.bestScore
        )
        updateLevel()
        checkValidMoves()
    }

    fun onMergeAnimationFinished() {
        val merge = _uiState.value.pendingMerge ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true, pendingMerge = null) }

            val currentState = _uiState.value
            val comboMultiplier = currentState.combo + 1
            val addedScore = merge.newValue * merge.totalCells * comboMultiplier
            
            val nextBestScore = if (currentState.score + addedScore > currentState.bestScore) currentState.score + addedScore else currentState.bestScore
            if (nextBestScore > currentState.bestScore) {
                settingsRepository.setBestScore(nextBestScore)
            }
            
            val nextCombo = if (merge.uniqueGroups > 1 || currentState.activePerk == Perk.CHAIN_MERGE) {
                currentState.combo + (if (currentState.activePerk == Perk.CHAIN_MERGE) 1 else 0) + (merge.uniqueGroups - 1)
            } else if (currentState.activePerk != Perk.FUSION) 0 else currentState.combo

            val stateAfterMerge = currentState.grid.filter { cell ->
                merge.mergingCells.none { it.id == cell.id } && (cell.x != merge.targetX || cell.y != merge.targetY)
            } + engine.createCell(merge.targetX, merge.targetY, merge.newValue)

            _uiState.update { it.copy(
                score = it.score + addedScore,
                bestScore = nextBestScore,
                combo = nextCombo,
                grid = stateAfterMerge
            ) }

            val chainMerge = if (_uiState.value.activePerk == Perk.CHAIN_MERGE) {
                engine.calculateMerge(merge.targetX, merge.targetY, stateAfterMerge)
            } else null

            if (chainMerge != null) {
                // Update gridState to move neighbors of the chain merge to the target position
                _uiState.update { it.copy(
                    grid = stateAfterMerge.map { cell ->
                        if (chainMerge.mergingCells.any { it.id == cell.id }) cell.copy(x = merge.targetX, y = merge.targetY) else cell
                    },
                    pendingMerge = chainMerge
                ) }
            } else {
                val activePerk = _uiState.value.activePerk
                if (activePerk != null && activePerk != Perk.FUSION) {
                    consumePerk(activePerk)
                }
                _uiState.update { it.copy(activePerk = null) }
                spawnFromQueue(stateAfterMerge)
            }
        }
    }

    private fun checkValidMoves() {
        val state = _uiState.value
        val isPossible = engine.isMovePossible(state.grid)
        val hasPerkOptions = state.perkOptions.isNotEmpty()
        val actionablePerks = state.collectedPerks.filter { it.canSaveFromStuck }

        _uiState.update { 
            if (isPossible || hasPerkOptions) {
                it.copy(isStuck = false, isGameOver = false)
            } else if (actionablePerks.isNotEmpty()) {
                it.copy(isStuck = true, isGameOver = false)
            } else {
                it.copy(isStuck = false, isGameOver = true)
            }
        }
    }

    private fun spawnFromQueue(currentState: List<HexagonCell>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val (newState, newPreviews) = engine.spawnFromQueue(currentState, _uiState.value.preview)
            _uiState.update { it.copy(grid = newState, preview = newPreviews) }
            updateLevel()
            checkValidMoves()
            _uiState.update { it.copy(isBusy = false) }
        }
    }

    fun getLevelProgress(): Float {
        val state = _uiState.value
        return engine.getLevelProgress(state.score, state.level)
    }
}
