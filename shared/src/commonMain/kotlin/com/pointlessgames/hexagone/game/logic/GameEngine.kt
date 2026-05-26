package com.pointlessgames.hexagone.game.logic

import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeHint
import com.pointlessgames.hexagone.game.model.MergeStep
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.OnBoardPerk
import com.pointlessgames.hexagone.game.model.Perk
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

    fun calculateBaseScore(cells: List<HexagonCell>): Int {
        val n = cells.size
        return cells.sumOf { it.value } + n * n - n
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

        // A merge happens if any group has at least 2 cells total
        val valuesToMerge = groups.filter { it.value.size >= 2 }.keys.sortedDescending()

        if (valuesToMerge.isNotEmpty()) {
            val steps = mutableListOf<MergeStep>()
            var currentCenterValue = 0
            var totalCells = 0

            valuesToMerge.forEachIndexed { index, value ->
                val groupCells = groups[value]!!
                val mergingNeighbors = groupCells.filter { it.id != centerCell?.id }

                if (index == 0) {
                    currentCenterValue = value + groupCells.size - 1
                    steps.add(MergeStep(mergingNeighbors, currentCenterValue))
                    totalCells += groupCells.size
                } else {
                    // Merging next group into existing center
                    val n = groupCells.size + 1 // group + current center
                    currentCenterValue = maxOf(currentCenterValue, value) + n - 2
                    steps.add(MergeStep(groupCells, currentCenterValue))
                    totalCells += groupCells.size
                }
            }

            val mergingCells = neighborCells.filter { it.value in valuesToMerge } + listOfNotNull(centerCell).filter { it.value in valuesToMerge }
            val baseScore = calculateBaseScore(mergingCells)

            return MergeTransition(
                targetX = x,
                targetY = y,
                steps = steps,
                finalValue = currentCenterValue,
                totalCells = totalCells,
                uniqueGroups = valuesToMerge.size,
                baseScore = baseScore,
                resultId = "cell_${idCounter++}"
            )
        }
        return null
    }

    fun calculateFusion(x: Int, y: Int, grid: List<HexagonCell>): MergeTransition? {
        val neighborCoords = getNeighbors(x, y)
        val neighborCells = grid.filter { cell ->
            neighborCoords.any { it.first == cell.x && it.second == cell.y }
        }

        val centerCell = grid.find { it.x == x && it.y == y }
        val allCells = neighborCells + listOfNotNull(centerCell)

        if (allCells.isNotEmpty()) {
            val vMax = allCells.maxOf { it.value }
            val n = allCells.size
            val k = allCells.distinctBy { it.value }.size
            val newValue = vMax + n - 1
            val baseScore = calculateBaseScore(allCells)

            return MergeTransition(
                targetX = x,
                targetY = y,
                steps = listOf(MergeStep(neighborCells, newValue)),
                finalValue = newValue,
                totalCells = n,
                uniqueGroups = k,
                baseScore = baseScore,
                resultId = "cell_${idCounter++}"
            )
        }
        return null
    }

    fun findMergeHints(
        grid: List<HexagonCell>,
        previews: List<PreviewCell>,
        currentCombo: Int,
        activePerk: Perk? = null
    ): List<MergeHint> {
        val occupied = grid.map { it.x to it.y }.toSet()
        val potentials = mutableListOf<Triple<Int, Int, Double>>() // x, y, evaluation

        for (y in 0 until rows) {
            for (x in 0 until columns) {
                if (x to y !in occupied) {
                    val merge = if (activePerk == Perk.FUSION) {
                        calculateFusion(x, y, grid)
                    } else {
                        calculateMerge(x, y, grid)
                    }

                    if (merge != null) {
                        // 1. Immediate Score
                        val comboMultiplier = currentCombo + 1
                        var immediateScore = merge.baseScore * comboMultiplier
                        var totalUniqueGroups = merge.uniqueGroups
                        var finalValue = merge.finalValue

                        // 2. Simulate Chain Merge if active
                        if (activePerk == Perk.CHAIN_MERGE) {
                            var currentGrid = grid.filter { cell ->
                                merge.steps.none { step -> step.mergingCells.any { it.id == cell.id } } &&
                                        (cell.x != x || cell.y != y)
                            } + createCell(x, y, merge.finalValue)
                            
                            var chainCount = 0
                            while (chainCount < 10) { // Safety break
                                val chain = calculateMerge(x, y, currentGrid) ?: break
                                immediateScore += chain.baseScore * (comboMultiplier + chainCount + 1)
                                totalUniqueGroups += chain.uniqueGroups
                                finalValue = chain.finalValue
                                currentGrid = currentGrid.filter { cell ->
                                    chain.steps.none { step -> step.mergingCells.any { it.id == cell.id } } &&
                                            (cell.x != x || cell.y != y)
                                } + createCell(x, y, chain.finalValue)
                                chainCount++
                            }
                        }

                        // 3. Grid after merge (for future potential)
                        val gridAfterMerge = grid.filter { cell ->
                            merge.steps.none { step -> step.mergingCells.any { it.id == cell.id } } &&
                                    (cell.x != x || cell.y != y)
                        } + createCell(x, y, finalValue)

                        // 4. Future Potential (Queue landing)
                        var futureScore = 0
                        val gridAfterQueue = gridAfterMerge.toMutableList()
                        previews.forEach { p ->
                            if (gridAfterQueue.none { it.x == p.x && it.y == p.y }) {
                                gridAfterQueue.add(createCell(p.x, p.y, p.value))
                            }
                        }

                        previews.forEach { p ->
                            val pMerge = calculateMerge(p.x, p.y, gridAfterQueue)
                            if (pMerge != null) {
                                futureScore += pMerge.baseScore * (currentCombo + totalUniqueGroups)
                            }
                        }

                        val totalEval = immediateScore.toDouble() +
                                futureScore.toDouble() * 0.7 +
                                (totalUniqueGroups - 1) * 100.0

                        potentials.add(Triple(x, y, totalEval))
                    }
                }
            }
        }

        if (potentials.isEmpty()) return emptyList()

        val minEval = potentials.minOf { it.third }
        val maxEval = potentials.maxOf { it.third }
        val range = (maxEval - minEval).coerceAtLeast(1.0)

        return potentials.map { (x, y, eval) ->
            val weight = ((eval - minEval) / range).toFloat()
            MergeHint(x, y, weight)
        }
    }

    fun pickWeightedPerks(count: Int): List<Perk> {
        val pool = Perk.entries.toMutableList()
        val result = mutableListOf<Perk>()
        
        repeat(count) {
            if (pool.isEmpty()) return@repeat
            val totalWeight = pool.sumOf { it.baseWeight }
            var random = Random.nextInt(totalWeight)
            
            for (perk in pool) {
                random -= perk.baseWeight
                if (random < 0) {
                    result.add(perk)
                    pool.remove(perk)
                    break
                }
            }
        }
        
        return result
    }

    fun getPerkDropRate(perk: Perk): Int {
        val totalWeight = Perk.entries.sumOf { it.baseWeight }
        return (perk.baseWeight.toFloat() / totalWeight * 100).toInt()
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

    fun createCell(x: Int, y: Int, value: Int, id: String? = null): HexagonCell {
        return HexagonCell(id ?: "cell_${idCounter++}", x, y, value)
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


    fun trySpawnPerkOnBoard(
        grid: List<HexagonCell>,
        existingPerks: List<OnBoardPerk>
    ): List<OnBoardPerk> {
        // 10% chance to spawn a perk on an empty space
        if (Random.nextFloat() > 0.1f) return existingPerks
        
        val occupied = grid.map { it.x to it.y }.toSet()
        val perkPositions = existingPerks.map { it.x to it.y }.toSet()
        
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                if (x to y !in occupied && x to y !in perkPositions) {
                    emptyPositions.add(x to y)
                }
            }
        }
        
        if (emptyPositions.isEmpty()) return existingPerks
        
        val pos = emptyPositions.random()
        val perk = pickWeightedPerks(1).first()
        
        return existingPerks + OnBoardPerk(pos.first, pos.second, perk, 3)
    }

    fun updateOnBoardPerks(perks: List<OnBoardPerk>): List<OnBoardPerk> {
        return perks.map { it.copy(lifespan = it.lifespan - 1) }.filter { it.lifespan > 0 }
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
