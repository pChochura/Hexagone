package com.pointlessgames.hexagone.game

import androidx.lifecycle.ViewModel
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow
import kotlin.random.Random

internal class GameViewModel : ViewModel() {

    private val _gridState = MutableStateFlow<List<HexagonCell>>(emptyList())
    val gridState: StateFlow<List<HexagonCell>> = _gridState.asStateFlow()

    private val _previewState = MutableStateFlow<List<PreviewCell>>(emptyList())
    val previewState: StateFlow<List<PreviewCell>> = _previewState.asStateFlow()

    private val _pendingMerge = MutableStateFlow<MergeTransition?>(null)
    val pendingMerge: StateFlow<MergeTransition?> = _pendingMerge.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _bestScore = MutableStateFlow(0)
    val bestScore: StateFlow<Int> = _bestScore.asStateFlow()

    private val _level = MutableStateFlow(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _highestValue = MutableStateFlow(1)
    val highestValue: StateFlow<Int> = _highestValue.asStateFlow()

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

    private val columns = 5
    private val rows = 4
    private var idCounter = 0
    private var lastLevel = 1

    init {
        generateInitialGrid()
        generateInitialPreview()
        updateLevel()
        checkValidMoves()
    }

    private fun updateLevel() {
        val currentScore = _score.value
        var lvl = 1
        while (currentScore >= 20 * (2.0.pow(lvl) - 1)) {
            lvl++
        }
        
        if (lvl > lastLevel) {
            lastLevel = lvl
            _perkOptions.value = List(3) { Perk.entries.random() }
        }

        _level.value = lvl
        val highest = _gridState.value.maxOfOrNull { it.value } ?: 1
        _highestValue.value = highest
    }

    private fun generateInitialGrid() {
        val cells = mutableListOf<HexagonCell>()
        val startX = Random.nextInt(columns)
        val startY = Random.nextInt(rows)
        val neighbors = getNeighbors(startX, startY)
        
        if (neighbors.isNotEmpty()) {
            val (nx, ny) = neighbors.random()
            val startValue = Random.nextInt(1, 3)
            cells.add(HexagonCell("cell_${idCounter++}", startX, startY, startValue))
            cells.add(HexagonCell("cell_${idCounter++}", nx, ny, startValue))
        }

        val count = Random.nextInt(2, 4)
        repeat(count) {
            val occupied = cells.map { it.x to it.y }.toSet()
            val empty = mutableListOf<Pair<Int, Int>>()
            for (y in 0 until rows) {
                for (x in 0 until columns) {
                    if (x to y !in occupied) empty.add(x to y)
                }
            }
            if (empty.isNotEmpty()) {
                val (rx, ry) = empty.random()
                cells.add(HexagonCell("cell_${idCounter++}", rx, ry, Random.nextInt(1, 3)))
            }
        }
        _gridState.value = cells
    }

    private fun generateInitialPreview() {
        _previewState.value = pickRandomPreviews(_gridState.value, emptyList(), 3)
    }

    private var previewIdCounter = 0

    private fun pickRandomPreviews(
        currentGrid: List<HexagonCell>,
        existingPreviews: List<PreviewCell>,
        count: Int
    ): List<PreviewCell> {
        val spawnPool = if (currentGrid.isEmpty()) listOf(1, 2) else currentGrid.map { it.value }.distinct()
        val newPreviews = existingPreviews.toMutableList()
        val needed = count - newPreviews.size
        
        if (needed <= 0) return newPreviews.mapIndexed { index, p -> p.copy(rank = index) }

        repeat(needed) {
            val currentOccupied = (currentGrid.map { it.x to it.y } + newPreviews.map { it.x to it.y }).toSet()
            val emptyPositions = mutableListOf<Pair<Int, Int>>()
            for (y in 0 until rows) {
                for (x in 0 until columns) {
                    if (x to y !in currentOccupied) emptyPositions.add(x to y)
                }
            }

            if (emptyPositions.isNotEmpty()) {
                val value = spawnPool.random()
                val matchingPositions = emptyPositions.filter { pos ->
                    getNeighbors(pos.first, pos.second).any { (nx, ny) ->
                        currentGrid.any { it.x == nx && it.y == ny && it.value == value }
                    }
                }
                val finalPos = if (matchingPositions.isNotEmpty() && Random.nextFloat() < 0.6f) matchingPositions.random() else emptyPositions.random()
                newPreviews.add(PreviewCell("preview_${previewIdCounter++}", finalPos.first, finalPos.second, value, newPreviews.size))
            }
        }
        return newPreviews.mapIndexed { index, p -> p.copy(rank = index) }
    }

    fun onEmptySpaceClicked(x: Int, y: Int) {
        if (_pendingMerge.value != null) return

        val perk = _activePerk.value
        val selectedId = _selectedCellId.value

        if (perk == Perk.MOVE_TILE && selectedId != null) {
            _gridState.value = _gridState.value.map { 
                if (it.id == selectedId) it.copy(x = x, y = y) else it
            }
            consumePerk(Perk.MOVE_TILE)
            _activePerk.value = null
            _selectedCellId.value = null
            checkValidMoves()
            return
        }

        val currentState = _gridState.value
        if (currentState.any { it.x == x && it.y == y }) return

        val neighborCoords = getNeighbors(x, y)
        val neighborCells = currentState.filter { cell ->
            neighborCoords.any { it.first == cell.x && it.second == cell.y }
        }

        if (perk == Perk.FUSION && neighborCells.isNotEmpty()) {
            val vMax = neighborCells.maxOf { it.value }
            val n = neighborCells.size
            val newValue = vMax + n - 1

            _gridState.value = currentState.map { cell ->
                if (neighborCells.any { it.id == cell.id }) cell.copy(x = x, y = y) else cell
            }
            _pendingMerge.value = MergeTransition(x, y, neighborCells, newValue)
            consumePerk(Perk.FUSION)
            _activePerk.value = null
            return
        }

        val valuesToMove = neighborCells.groupBy { it.value }.filter { it.value.size > 1 }.keys

        if (valuesToMove.isNotEmpty()) {
            val mergingCells = neighborCells.filter { it.value in valuesToMove }
            val vMax = mergingCells.maxOf { it.value }
            val n = mergingCells.size
            val k = mergingCells.distinctBy { it.value }.size
            val newValue = vMax + n - k

            _gridState.value = currentState.map { cell ->
                if (mergingCells.any { it.id == cell.id }) cell.copy(x = x, y = y) else cell
            }
            _pendingMerge.value = MergeTransition(x, y, mergingCells, newValue)
        }
    }

    fun onCellClicked(cell: HexagonCell) {
        if (_pendingMerge.value != null) return

        when (_activePerk.value) {
            Perk.MOVE_TILE -> {
                _selectedCellId.value = cell.id
            }
            Perk.REMOVE_TILE -> {
                _gridState.value = _gridState.value.filter { it.id != cell.id }
                consumePerk(Perk.REMOVE_TILE)
                _activePerk.value = null
                checkValidMoves()
            }
            else -> {}
        }
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
            Perk.MOVE_TILE -> {
                _activePerk.value = Perk.MOVE_TILE
            }
            Perk.REMOVE_TILE -> {
                _activePerk.value = Perk.REMOVE_TILE
            }
            Perk.FUSION -> {
                _activePerk.value = Perk.FUSION
            }
        }
    }

    fun onPerkSelected(perk: Perk) {
        _collectedPerks.value = _collectedPerks.value + perk
        _perkOptions.value = emptyList()
        checkValidMoves()
    }

    fun onRestartClicked() {
        _score.value = 0
        _isGameOver.value = false
        _isStuck.value = false
        _collectedPerks.value = emptyList()
        _perkOptions.value = emptyList()
        _activePerk.value = null
        _selectedCellId.value = null
        lastLevel = 1
        generateInitialGrid()
        generateInitialPreview()
        updateLevel()
        checkValidMoves()
    }

    fun onMergeAnimationFinished() {
        val merge = _pendingMerge.value ?: return
        _pendingMerge.value = null

        val stateAfterMerge = _gridState.value.filter { cell ->
            merge.mergingCells.none { it.id == cell.id }
        } + HexagonCell("cell_${idCounter++}", merge.targetX, merge.targetY, merge.newValue)

        _gridState.value = stateAfterMerge
        _score.value += merge.newValue * merge.mergingCells.size
        if (_score.value > _bestScore.value) _bestScore.value = _score.value

        spawnFromQueue(stateAfterMerge)
        checkValidMoves()
    }

    private fun checkValidMoves() {
        val currentState = _gridState.value
        if (isMovePossible(currentState)) {
            _isStuck.value = false
            _isGameOver.value = false
            return
        }

        // If player is currently choosing a level-up perk, defer the stuck/gameover state.
        // Selecting a perk will trigger this check again.
        if (_perkOptions.value.isNotEmpty()) {
            _isStuck.value = false
            _isGameOver.value = false
            return
        }

        if (_collectedPerks.value.isNotEmpty()) {
            _isStuck.value = true
            _isGameOver.value = false
        } else {
            _isStuck.value = false
            _isGameOver.value = true
        }
    }

    private fun isMovePossible(grid: List<HexagonCell>): Boolean {
        val occupied = grid.map { it.x to it.y }.toSet()
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                if (x to y !in occupied) {
                    val neighbors = getNeighbors(x, y)
                    val neighborCells = grid.filter { cell -> neighbors.any { it.first == cell.x && it.second == cell.y } }
                    if (neighborCells.groupBy { it.value }.any { it.value.size >= 2 }) return true
                }
            }
        }
        return false
    }

    private fun spawnFromQueue(currentState: List<HexagonCell>) {
        val currentPreviews = _previewState.value
        if (currentPreviews.isEmpty()) return

        val spawnableIndex = currentPreviews.indexOfFirst { p -> currentState.none { it.x == p.x && it.y == p.y } }

        val (newState, consumedCount) = if (spawnableIndex != -1) {
            val p = currentPreviews[spawnableIndex]
            (currentState + HexagonCell("cell_${idCounter++}", p.x, p.y, p.value)) to (spawnableIndex + 1)
        } else {
            val occupied = currentState.map { it.x to it.y }.toSet()
            val empty = mutableListOf<Pair<Int, Int>>()
            for (y in 0 until rows) for (x in 0 until columns) if (x to y !in occupied) empty.add(x to y)
            if (empty.isNotEmpty()) {
                val (rx, ry) = empty.random()
                val pool = if (currentState.isEmpty()) listOf(1, 2) else currentState.map { it.value }.distinct()
                (currentState + HexagonCell("cell_${idCounter++}", rx, ry, pool.random())) to currentPreviews.size
            } else currentState to 0
        }

        val remaining = currentPreviews.drop(consumedCount).filter { p -> newState.none { it.x == p.x && it.y == p.y } }
        _gridState.value = newState
        _previewState.value = pickRandomPreviews(newState, remaining, 3)
        updateLevel()
    }

    private fun getNeighbors(x: Int, y: Int): List<Pair<Int, Int>> {
        val potential = if (x % 2 == 0) {
            listOf(x to y-1, x to y+1, x-1 to y-1, x-1 to y, x+1 to y-1, x+1 to y)
        } else {
            listOf(x to y-1, x to y+1, x-1 to y, x-1 to y+1, x+1 to y, x+1 to y+1)
        }
        return potential.filter { (nx, ny) -> nx in 0 until columns && ny in 0 until rows }
    }
}
