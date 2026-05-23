package com.pointlessgames.hexagone.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

internal class GameViewModel : ViewModel() {

    private val _gridState = MutableStateFlow<List<HexagonCell>>(emptyList())
    val gridState: StateFlow<List<HexagonCell>> = _gridState.asStateFlow()

    private val _previewState = MutableStateFlow<List<PreviewCell>>(emptyList())
    val previewState: StateFlow<List<PreviewCell>> = _previewState.asStateFlow()

    private val _pendingMerge = MutableStateFlow<MergeTransition?>(null)
    val pendingMerge: StateFlow<MergeTransition?> = _pendingMerge.asStateFlow()

    private val columns = 5
    private val rows = 4
    private var idCounter = 0

    init {
        generateInitialGrid()
        generateInitialPreview()
    }

    private fun generateInitialGrid() {
        val cells = mutableListOf<HexagonCell>()
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                if ((x + y) % 5 == 0) {
                    cells.add(HexagonCell("cell_${idCounter++}", x, y, Random.nextInt(1, 3)))
                }
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
        val occupied = (currentGrid.map { it.x to it.y } + existingPreviews.map { it.x to it.y }).toSet()
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                if (x to y !in occupied) {
                    emptyPositions.add(x to y)
                }
            }
        }

        val newPreviews = existingPreviews.toMutableList()
        val needed = count - newPreviews.size
        
        if (needed > 0 && emptyPositions.isNotEmpty()) {
            val selected = emptyPositions.shuffled().take(needed)
            selected.forEach { (x, y) ->
                newPreviews.add(PreviewCell("preview_${previewIdCounter++}", x, y, Random.nextInt(1, 3), newPreviews.size))
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
                val spawnedState = currentState + HexagonCell(
                    id = "cell_${idCounter++}",
                    x = rx,
                    y = ry,
                    value = Random.nextInt(1, 3)
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
