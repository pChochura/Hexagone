package com.pointlessgames.hexagone.game.logic

import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlin.math.pow
import kotlin.random.Random

class GameEngine(
    val columns: Int = 5,
    val rows: Int = 4
) {
    private var idCounter = 0
    private var previewIdCounter = 0

    fun generateInitialGrid(): List<HexagonCell> {
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
        return cells
    }

    fun pickRandomPreviews(
        currentGrid: List<HexagonCell>,
        existingPreviews: List<PreviewCell>,
        count: Int
    ): List<PreviewCell> {
        if (existingPreviews.isNotEmpty()) return existingPreviews

        val boardPool = if (currentGrid.isEmpty()) listOf(1, 2) else currentGrid.map { it.value }.distinct().sorted()
        val spawnPool = boardPool.take((boardPool.size * 0.7f).toInt().coerceAtLeast(2))

        val currentOccupied = currentGrid.map { it.x to it.y }.toSet()
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                if (x to y !in currentOccupied) emptyPositions.add(x to y)
            }
        }

        if (emptyPositions.isEmpty()) return emptyList()

        val spawnValue = spawnPool.random()

        // Pick a "central" empty tile that has at least 2 empty neighbors
        val candidates = emptyPositions.filter { pos ->
            val neighbors = getNeighbors(pos.first, pos.second)
            neighbors.count { it !in currentOccupied } >= 2
        }.shuffled()

        val groupPositions = if (candidates.isNotEmpty()) {
            val center = candidates.first()
            val neighbors = getNeighbors(center.first, center.second).filter { it !in currentOccupied }
            val groupSize = Random.nextInt(2, 4) // 2 or 3 tiles
            neighbors.take(groupSize)
        } else {
            listOf(emptyPositions.random())
        }

        return groupPositions.map { (x, y) ->
            PreviewCell("preview_${previewIdCounter++}", x, y, spawnValue, 0)
        }
    }

    fun getNeighbors(x: Int, y: Int): List<Pair<Int, Int>> {
        val potential = if (x % 2 == 0) {
            listOf(x to y - 1, x to y + 1, x - 1 to y - 1, x - 1 to y, x + 1 to y - 1, x + 1 to y)
        } else {
            listOf(x to y - 1, x to y + 1, x - 1 to y, x - 1 to y + 1, x + 1 to y, x + 1 to y + 1)
        }
        return potential.filter { (nx, ny) -> nx in 0 until columns && ny in 0 until rows }
    }

    fun calculateMerge(x: Int, y: Int, grid: List<HexagonCell>): MergeTransition? {
        val neighborCoords = getNeighbors(x, y)
        val neighborCells = grid.filter { cell ->
            neighborCoords.any { it.first == cell.x && it.second == cell.y }
        }

        val valuesToMove = neighborCells.groupBy { it.value }.filter { it.value.size > 1 }.keys

        if (valuesToMove.isNotEmpty()) {
            val mergingCells = neighborCells.filter { it.value in valuesToMove }
            val vMax = mergingCells.maxOf { it.value }
            val n = mergingCells.size
            val k = mergingCells.distinctBy { it.value }.size
            val newValue = vMax + n - k
            return MergeTransition(x, y, mergingCells, newValue)
        }
        return null
    }

    fun calculateFusion(x: Int, y: Int, grid: List<HexagonCell>): MergeTransition? {
        val neighborCoords = getNeighbors(x, y)
        val neighborCells = grid.filter { cell ->
            neighborCoords.any { it.first == cell.x && it.second == cell.y }
        }

        if (neighborCells.isNotEmpty()) {
            val vMax = neighborCells.maxOf { it.value }
            val n = neighborCells.size
            val newValue = vMax + n - 1
            return MergeTransition(x, y, neighborCells, newValue)
        }
        return null
    }

    fun isMovePossible(grid: List<HexagonCell>): Boolean {
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

    fun createCell(x: Int, y: Int, value: Int): HexagonCell {
        return HexagonCell("cell_${idCounter++}", x, y, value)
    }

    fun calculateLevel(score: Int): Int {
        var lvl = 1
        while (score >= 20 * (2.0.pow(lvl) - 1)) {
            lvl++
        }
        return lvl
    }

    fun getLevelProgress(score: Int, level: Int): Float {
        val currentLevelThreshold = 20 * (2.0.pow(level - 1) - 1).toFloat()
        val nextLevelThreshold = 20 * (2.0.pow(level) - 1).toFloat()
        return ((score - currentLevelThreshold) / (nextLevelThreshold - currentLevelThreshold)).coerceIn(0f, 1f)
    }

    fun spawnFromQueue(
        currentState: List<HexagonCell>,
        currentPreviews: List<PreviewCell>
    ): Pair<List<HexagonCell>, List<PreviewCell>> {
        if (currentPreviews.isEmpty()) return currentState to currentPreviews

        var newState = currentState
        currentPreviews.forEach { p ->
            if (newState.none { it.x == p.x && it.y == p.y }) {
                newState = newState + createCell(p.x, p.y, p.value)
            }
        }

        val nextPreviews = pickRandomPreviews(newState, emptyList(), 3)
        return newState to nextPreviews
    }
}
