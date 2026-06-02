package com.pointlessgames.hexagone.game.logic

import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.PotentialMerge

internal object PatternRecognitionEngine {

    fun checkRingOfFire(grid: List<HexagonCell>, engine: GameEngine): Boolean {
        val occupied = grid.map { it.x to it.y }.toSet()
        for (y in 0 until engine.rows) {
            for (x in 0 until engine.columns) {
                if (x to y !in occupied) {
                    val neighbors = engine.getNeighbors(x, y)
                    if (neighbors.size == 6) {
                        val neighborCells = grid.filter { it.x to it.y in neighbors.toSet() }
                        if (neighborCells.size == 6 && neighborCells.map { it.value }.distinct().size == 1) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun checkGreatWall(grid: List<HexagonCell>, engine: GameEngine): Boolean {
        for (x in 0 until engine.columns) {
            val columnCells = grid.filter { it.x == x }
            if (columnCells.size == engine.rows && columnCells.map { it.value }.distinct().size == 1) {
                return true
            }
        }
        return false
    }

    fun checkTwinPeaks(grid: List<HexagonCell>, engine: GameEngine): Boolean {
        if (grid.isEmpty()) return false
        val maxValue = grid.maxOf { it.value }
        val maxCells = grid.filter { it.value == maxValue }
        if (maxCells.size < 2) return false

        val corners = listOf(
            0 to 0,
            0 to engine.rows - 1,
            engine.columns - 1 to 0,
            engine.columns - 1 to engine.rows - 1
        )

        val occupiedCorners = maxCells.filter { it.x to it.y in corners.toSet() }
        if (occupiedCorners.size < 2) return false

        // Check if any two are at opposite corners
        for (i in occupiedCorners.indices) {
            for (j in i + 1 until occupiedCorners.size) {
                val c1 = occupiedCorners[i]
                val c2 = occupiedCorners[j]
                if (c1.x != c2.x && c1.y != c2.y) return true
            }
        }
        return false
    }

    fun checkThePrism(grid: List<HexagonCell>): Boolean {
        val values = grid.map { it.value }.distinct().sorted()
        if (values.size < 7) return false
        
        var consecutiveCount = 1
        for (i in 0 until values.size - 1) {
            if (values[i + 1] == values[i] + 1) {
                consecutiveCount++
                if (consecutiveCount >= 7) return true
            } else {
                consecutiveCount = 1
            }
        }
        return false
    }

    fun checkArchitectsDream(grid: List<HexagonCell>, potentialMerges: Map<Pair<Int, Int>, PotentialMerge>): Boolean {
        if (grid.size < 12) return false
        val allParticipatingIds = potentialMerges.values.flatMap { it.participatingIds }.toSet()
        return grid.all { it.id in allParticipatingIds }
    }

    fun checkQuadruplets(previews: List<com.pointlessgames.hexagone.game.model.PreviewCell>): Boolean {
        return previews.groupBy { it.value }.any { it.value.size >= 4 }
    }

    fun checkTheMedium(grid: List<HexagonCell>, previews: List<com.pointlessgames.hexagone.game.model.PreviewCell>, engine: GameEngine): Boolean {
        // Find if any ghost is in a position where it's surrounded by 6 same-value solid tiles
        val ghostPositions = previews.map { it.x to it.y }
        for ((gx, gy) in ghostPositions) {
            val neighbors = engine.getNeighbors(gx, gy)
            if (neighbors.size == 6) {
                val neighborCells = grid.filter { it.x to it.y in neighbors.toSet() }
                if (neighborCells.size == 6 && neighborCells.map { it.value }.distinct().size == 1) {
                    return true
                }
            }
        }
        return false
    }
}
