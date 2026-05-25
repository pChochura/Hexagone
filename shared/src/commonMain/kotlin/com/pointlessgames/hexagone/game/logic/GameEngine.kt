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
        
        val centerCell = grid.find { it.x == x && it.y == y }
        
        // Group neighbors by value
        val groups = neighborCells.groupBy { it.value }.toMutableMap()
        
        // If there's a center cell, add it to the corresponding group
        centerCell?.let { center ->
            val group = groups[center.value] ?: emptyList()
            groups[center.value] = group + center
        }

        // A merge happens if any group has at least 2 cells (excluding center-only case)
        // Actually, if there is a center cell, even 1 neighbor matching it should merge.
        // So total size >= 2
        val valuesToMerge = groups.filter { it.value.size >= 2 }.keys

        if (valuesToMerge.isNotEmpty()) {
            val mergingCells = neighborCells.filter { it.value in valuesToMerge }
            // If center cell is part of the merge, it's already in the grid but will be "replaced"
            // Wait, MergeTransition.mergingCells are the ones that ANIMATE to the center.
            // If the center cell is already there, it doesn't need to animate.
            val vMax = (mergingCells + listOfNotNull(centerCell).filter { it.value in valuesToMerge }).maxOf { it.value }
            val n = mergingCells.size + (if (centerCell != null && centerCell.value in valuesToMerge) 1 else 0)
            val k = (mergingCells + listOfNotNull(centerCell).filter { it.value in valuesToMerge }).distinctBy { it.value }.size
            val newValue = vMax + n - k
            return MergeTransition(x, y, mergingCells, newValue, n)
        }
        return null
    }

    fun calculateFusion(x: Int, y: Int, grid: List<HexagonCell>): MergeTransition? {
        val neighborCoords = getNeighbors(x, y)
        val neighborCells = grid.filter { cell ->
            neighborCoords.any { it.first == cell.x && it.second == cell.y }
        }
        
        val centerCell = grid.find { it.x == x && it.y == y }

        if (neighborCells.isNotEmpty() || centerCell != null) {
            val allCells = neighborCells + listOfNotNull(centerCell)
            if (allCells.isEmpty()) return null
            
            val vMax = allCells.maxOf { it.value }
            val n = allCells.size
            val newValue = vMax + n - 1
            return MergeTransition(x, y, neighborCells, newValue, n)
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
        // Quadratic threshold: 50 * (level-1)^2
        // Level 1: 0
        // Level 2: 50
        // Level 3: 200
        // Level 4: 450
        // Level 5: 800
        while (score >= 50 * lvl.toDouble().pow(2)) {
            lvl++
        }
        return lvl
    }

    fun getLevelProgress(score: Int, level: Int): Float {
        val currentLevelThreshold = 50 * (level - 1).toDouble().pow(2).toFloat()
        val nextLevelThreshold = 50 * level.toDouble().pow(2).toFloat()
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
