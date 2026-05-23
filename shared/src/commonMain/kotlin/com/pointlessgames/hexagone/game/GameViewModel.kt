package com.pointlessgames.hexagone.game

import androidx.lifecycle.ViewModel
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _level = MutableStateFlow(4)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val columns = 5
    private val rows = 4
    private var idCounter = 0

    init {
        generateInitialGrid()
        generateInitialPreview()
    }

    private fun generateInitialGrid() {
        val cells = mutableListOf<HexagonCell>()
        
        // 1. Guaranteed move: Pick two adjacent random cells and give them same value
        val startX = Random.nextInt(columns)
        val startY = Random.nextInt(rows)
        val neighbors = getNeighbors(startX, startY)
        
        if (neighbors.isNotEmpty()) {
            val (nx, ny) = neighbors.random()
            val startValue = Random.nextInt(1, 3)
            cells.add(HexagonCell("cell_${idCounter++}", startX, startY, startValue))
            cells.add(HexagonCell("cell_${idCounter++}", nx, ny, startValue))
        }

        // 2. Add 3-5 more random tiles to the board
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
                
                // Smart positioning: Prefer spots near tiles with the same value
                val matchingPositions = emptyPositions.filter { pos ->
                    getNeighbors(pos.first, pos.second).any { (nx, ny) ->
                        currentGrid.any { it.x == nx && it.y == ny && it.value == value }
                    }
                }
                
                val finalPos = if (matchingPositions.isNotEmpty() && Random.nextFloat() < 0.6f) {
                    matchingPositions.random()
                } else {
                    emptyPositions.random()
                }
                
                newPreviews.add(PreviewCell("preview_${previewIdCounter++}", finalPos.first, finalPos.second, value, newPreviews.size))
            }
        }
        
        return newPreviews.mapIndexed { index, p -> p.copy(rank = index) }
    }

    fun onEmptySpaceClicked(x: Int, y: Int) {
        if (_pendingMerge.value != null) return // Ignore clicks while animating

        val currentState = _gridState.value
        if (currentState.any { it.x == x && it.y == y }) return

        val neighborCoords = getNeighbors(x, y)
        val neighborCells = currentState.filter { cell ->
            neighborCoords.any { it.first == cell.x && it.second == cell.y }
        }

        val valuesToMove = neighborCells.groupBy { it.value }
            .filter { it.value.size > 1 }
            .keys

        if (valuesToMove.isNotEmpty()) {
            val mergingCells = neighborCells.filter { it.value in valuesToMove }
            
            val vMax = mergingCells.maxOf { it.value }
            val n = mergingCells.size
            val k = mergingCells.distinctBy { it.value }.size
            val newValue = vMax + n - k

            // Move the cells in gridState to trigger animation
            _gridState.value = currentState.map { cell ->
                if (mergingCells.any { it.id == cell.id }) {
                    cell.copy(x = x, y = y)
                } else {
                    cell
                }
            }

            // Set pending merge to signal UI to notify us when done
            _pendingMerge.value = MergeTransition(x, y, mergingCells, newValue)
        }
    }

    fun onMergeAnimationFinished() {
        val merge = _pendingMerge.value ?: return
        _pendingMerge.value = null

        val currentState = _gridState.value
        val stateAfterMerge = currentState.filter { cell ->
            merge.mergingCells.none { it.id == cell.id }
        } + HexagonCell("cell_${idCounter++}", merge.targetX, merge.targetY, merge.newValue)

        _gridState.value = stateAfterMerge
        
        // Playful score update
        _score.value += merge.newValue * (merge.mergingCells.size)
        if (_score.value > _bestScore.value) {
            _bestScore.value = _score.value
        }

        spawnFromQueue(stateAfterMerge)
    }

    private fun spawnFromQueue(currentState: List<HexagonCell>) {
        val currentPreviews = _previewState.value
        if (currentPreviews.isEmpty()) return

        // Find the first preview that can be spawned (its position is empty)
        val spawnableIndex = currentPreviews.indexOfFirst { p ->
            currentState.none { it.x == p.x && it.y == p.y }
        }

        val (newState, consumedPreviewsCount) = if (spawnableIndex != -1) {
            val previewToSpawn = currentPreviews[spawnableIndex]
            val spawnedState = currentState + HexagonCell(
                id = "cell_${idCounter++}",
                x = previewToSpawn.x,
                y = previewToSpawn.y,
                value = previewToSpawn.value
            )
            spawnedState to (spawnableIndex + 1)
        } else {
            // All 3 previews are blocked. Spawn a new random tile if possible.
            val occupied = currentState.map { it.x to it.y }.toSet()
            val emptyPositions = mutableListOf<Pair<Int, Int>>()
            for (y in 0 until rows) {
                for (x in 0 until columns) {
                    if (x to y !in occupied) emptyPositions.add(x to y)
                }
            }
            
            if (emptyPositions.isNotEmpty()) {
                val (rx, ry) = emptyPositions.random()
                val spawnPool = if (currentState.isEmpty()) listOf(1, 2) else currentState.map { it.value }.distinct()
                val spawnedState = currentState + HexagonCell(
                    id = "cell_${idCounter++}",
                    x = rx,
                    y = ry,
                    value = spawnPool.random()
                )
                spawnedState to currentPreviews.size
            } else {
                currentState to 0 // Grid is full
            }
        }

        // Calculate remaining previews and refill
        val remainingPreviews = currentPreviews.drop(consumedPreviewsCount).filter { p ->
            newState.none { it.x == p.x && it.y == p.y }
        }

        _gridState.value = newState
        _previewState.value = pickRandomPreviews(newState, remainingPreviews, 3)
    }

    private fun getNeighbors(x: Int, y: Int): List<Pair<Int, Int>> {
        val potentialNeighbors = if (x % 2 == 0) {
            listOf(
                x to y - 1, x to y + 1,
                x - 1 to y - 1, x - 1 to y,
                x + 1 to y - 1, x + 1 to y,
            )
        } else {
            listOf(
                x to y - 1, x to y + 1,
                x - 1 to y, x - 1 to y + 1,
                x + 1 to y, x + 1 to y + 1,
            )
        }

        return potentialNeighbors.filter { (nx, ny) ->
            nx in 0 until columns && ny in 0 until rows
        }
    }
}
