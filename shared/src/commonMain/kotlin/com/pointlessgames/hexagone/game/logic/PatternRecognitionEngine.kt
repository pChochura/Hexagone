package com.pointlessgames.hexagone.game.logic

import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.PotentialMerge

internal data class PatternResult(val success: Boolean, val containsMimic: Boolean)

internal object PatternRecognitionEngine {

    fun checkRingOfFire(grid: List<HexagonCell>, engine: GameEngine): PatternResult {
        val occupied = grid.map { it.x to it.y }.toSet()
        for (y in 0 until engine.rows) {
            for (x in 0 until engine.columns) {
                if (x to y !in occupied) {
                    val neighbors = engine.getNeighbors(x, y)
                    if (neighbors.size == 6) {
                        val neighborCells = grid.filter { it.x to it.y in neighbors.toSet() }
                        if (neighborCells.size == 6) {
                            val normalValues = neighborCells.filter { !it.isMimic }.map { it.value }.distinct()
                            if (normalValues.size <= 1) {
                                return PatternResult(true, neighborCells.any { it.isMimic })
                            }
                        }
                    }
                }
            }
        }
        return PatternResult(false, false)
    }

    fun checkGreatWall(grid: List<HexagonCell>, engine: GameEngine): PatternResult {
        for (x in 0 until engine.columns) {
            val columnCells = grid.filter { it.x == x }
            if (columnCells.size == engine.rows) {
                val normalValues = columnCells.filter { !it.isMimic }.map { it.value }.distinct()
                if (normalValues.size <= 1) {
                    return PatternResult(true, columnCells.any { it.isMimic })
                }
            }
        }
        return PatternResult(false, false)
    }

    fun checkTwinPeaks(grid: List<HexagonCell>, engine: GameEngine): PatternResult {
        if (grid.isEmpty()) return PatternResult(false, false)
        val maxValue = grid.maxOf { it.value }
        val maxCells = grid.filter { it.value == maxValue || it.isMimic }
        if (maxCells.size < 2) return PatternResult(false, false)

        val corners = listOf(
            0 to 0,
            0 to engine.rows - 1,
            engine.columns - 1 to 0,
            engine.columns - 1 to engine.rows - 1
        )

        val occupiedCorners = maxCells.filter { it.x to it.y in corners.toSet() }
        if (occupiedCorners.size < 2) return PatternResult(false, false)

        // Check if any two are at opposite corners
        for (i in occupiedCorners.indices) {
            for (j in i + 1 until occupiedCorners.size) {
                val c1 = occupiedCorners[i]
                val c2 = occupiedCorners[j]
                if (c1.x != c2.x && c1.y != c2.y) {
                    return PatternResult(true, c1.isMimic || c2.isMimic)
                }
            }
        }
        return PatternResult(false, false)
    }

    fun checkThePrism(grid: List<HexagonCell>): PatternResult {
        val normalValues = grid.filter { !it.isMimic }.map { it.value }.distinct().sorted()
        val mimicsCount = grid.count { it.isMimic }
        
        if (normalValues.isEmpty()) {
            return PatternResult(mimicsCount >= 7, mimicsCount >= 1)
        }

        // Try every possible starting value for a sequence of 7
        val minPossible = (normalValues.first() - 6).coerceAtLeast(1)
        val maxPossible = normalValues.last()
        
        for (start in minPossible..maxPossible) {
            var usedMimics = 0
            var matched = 0
            var containsActualMimic = false
            for (v in start until start + 7) {
                if (normalValues.contains(v)) {
                    matched++
                } else if (usedMimics < mimicsCount) {
                    usedMimics++
                    matched++
                    containsActualMimic = true
                }
            }
            if (matched == 7) return PatternResult(true, containsActualMimic)
        }
        
        return PatternResult(false, false)
    }

    fun checkArchitectsDream(grid: List<HexagonCell>, potentialMerges: Map<Pair<Int, Int>, PotentialMerge>): Boolean {
        if (grid.size < 12) return false
        val allParticipatingIds = potentialMerges.values.flatMap { it.participatingIds }.toSet()
        return grid.all { it.id in allParticipatingIds }
    }

    fun checkQuadruplets(previews: List<com.pointlessgames.hexagone.game.model.PreviewCell>): Boolean {
        val normalValues = previews.filter { !it.isMimic }.groupBy { it.value }
        val mimicsCount = previews.count { it.isMimic }
        
        if (mimicsCount >= 4) return true
        
        return normalValues.any { it.value.size + mimicsCount >= 4 }
    }

    fun checkTheMedium(grid: List<HexagonCell>, previews: List<com.pointlessgames.hexagone.game.model.PreviewCell>, engine: GameEngine): PatternResult {
        // Find if any ghost is in a position where it's surrounded by 6 same-value solid tiles
        val ghostPositions = previews.map { it.x to it.y }
        for ((gx, gy) in ghostPositions) {
            val neighbors = engine.getNeighbors(gx, gy)
            if (neighbors.size == 6) {
                val neighborCells = grid.filter { it.x to it.y in neighbors.toSet() }
                if (neighborCells.size == 6) {
                    val normalValues = neighborCells.filter { !it.isMimic }.map { it.value }.distinct()
                    if (normalValues.size <= 1) {
                        return PatternResult(true, neighborCells.any { it.isMimic } || previews.find { it.x == gx && it.y == gy }?.isMimic == true)
                    }
                }
            }
        }
        return PatternResult(false, false)
    }

    fun checkMirrorImage(grid: List<HexagonCell>, engine: GameEngine): Boolean {
        val mimics = grid.filter { it.isMimic }
        if (mimics.size < 2) return false
        for (mimic in mimics) {
            val neighbors = engine.getNeighbors(mimic.x, mimic.y)
            if (mimics.any { it.id != mimic.id && it.x to it.y in neighbors.toSet() }) return true
        }
        return false
    }
}
