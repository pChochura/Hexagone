package com.pointlessgames.hexagone.game.logic

import com.pointlessgames.hexagone.game.model.GameState
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeHint
import com.pointlessgames.hexagone.game.model.MergeStep
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.OnBoardPerk
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlin.math.pow
import kotlin.random.Random

internal class GameEngine(
    val columns: Int = 5,
    val rows: Int = 4
) {
    fun generateInitialGrid(random: Random): Pair<List<HexagonCell>, Int> {
        var idCounter = 0
        val cells = mutableListOf<HexagonCell>()
        val startX = random.nextInt(columns)
        val startY = random.nextInt(rows)
        val neighbors = getNeighbors(startX, startY)

        if (neighbors.isNotEmpty()) {
            val (nx, ny) = neighbors.random(random)
            val startValue = random.nextInt(1, 3)
            cells.add(HexagonCell("cell_${idCounter++}", startX, startY, startValue))
            cells.add(HexagonCell("cell_${idCounter++}", nx, ny, startValue))
        }

        val count = random.nextInt(2, 4)
        repeat(count) {
            val occupied = cells.map { it.x to it.y }.toSet()
            val empty = mutableListOf<Pair<Int, Int>>()
            for (y in 0 until rows) {
                for (x in 0 until columns) {
                    if (x to y !in occupied) empty.add(x to y)
                }
            }
            if (empty.isNotEmpty()) {
                val (rx, ry) = empty.random(random)
                cells.add(HexagonCell("cell_${idCounter++}", rx, ry, random.nextInt(1, 3)))
            }
        }
        return cells to idCounter
    }

    fun pickRandomPreviews(
        currentGrid: List<HexagonCell>,
        existingPreviews: List<PreviewCell>,
        existingPerks: List<OnBoardPerk>,
        count: Int,
        random: Random,
        initialPreviewIdCounter: Int
    ): Pair<List<PreviewCell>, Int> {
        if (existingPreviews.isNotEmpty()) return existingPreviews to initialPreviewIdCounter

        var previewIdCounter = initialPreviewIdCounter
        val boardPool = if (currentGrid.isEmpty()) listOf(1, 2) else currentGrid.map { it.value }.distinct().sorted()
        val spawnPool = boardPool.take((boardPool.size * 0.7f).toInt().coerceAtLeast(2))

        val currentOccupied = currentGrid.map { it.x to it.y }.toSet()
        val perkPositions = existingPerks.map { it.x to it.y }.toSet()
        
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                if (x to y !in currentOccupied) emptyPositions.add(x to y)
            }
        }

        if (emptyPositions.isEmpty()) return emptyList<PreviewCell>() to previewIdCounter

        val spawnValue = spawnPool.random(random)

        // Prioritize positions that are both empty AND don't have a perk
        val bestPositions = emptyPositions.filter { it !in perkPositions }
        val targetPool = if (bestPositions.isNotEmpty()) bestPositions else emptyPositions

        // Pick a "central" empty tile that has at least 2 empty neighbors
        val candidates = targetPool.filter { pos ->
            val neighbors = getNeighbors(pos.first, pos.second)
            neighbors.count { it !in currentOccupied } >= 2
        }.shuffled(random)

        val groupPositions = if (candidates.isNotEmpty()) {
            val center = candidates.first()
            val neighbors = getNeighbors(center.first, center.second).filter { it !in currentOccupied }
            val groupSize = random.nextInt(2, 4) // 2 or 3 tiles
            neighbors.take(groupSize)
        } else {
            listOf(targetPool.random(random))
        }

        val previews = groupPositions.map { (x, y) ->
            PreviewCell("preview_${previewIdCounter++}", x, y, spawnValue, 0)
        }
        return previews to previewIdCounter
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

    fun calculateMerge(x: Int, y: Int, grid: List<HexagonCell>, idCounter: Int): Pair<MergeTransition?, Int> {
        var currentIdCounter = idCounter
        val neighborCoords = getNeighbors(x, y)
        val neighborCells = grid.filter { cell ->
            !cell.isFrozen && neighborCoords.any { it.first == cell.x && it.second == cell.y }
        }

        val centerCell = grid.find { it.x == x && it.y == y }?.takeIf { !it.isFrozen }

        // Group neighbors by value
        val groups = neighborCells.groupBy { it.value }.toMutableMap()

        // If there's a center cell, add it to the corresponding group
        centerCell?.let { cell ->
            val group = groups[cell.value] ?: emptyList()
            groups[cell.value] = group + cell
        }

        // A merge happens if any group has at least 2 cells total
        val valuesToMerge = groups.filter { it.value.size >= 2 }.keys.sortedDescending()

        if (valuesToMerge.isNotEmpty()) {
            val steps = mutableListOf<MergeStep>()
            var currentCenterValue = 0
            var totalCells = 0

            valuesToMerge.forEachIndexed { index, value ->
                val groupCells = groups[value]!!
                val mergingNeighbors =
                    groupCells.filter { it.id != (centerCell?.id ?: "placed_temp") }

                if (index == 0) {
                    currentCenterValue = value + groupCells.size - 1
                    steps.add(
                        MergeStep(
                            mergingNeighbors,
                            currentCenterValue,
                            calculateBaseScore(groupCells)
                        )
                    )
                    totalCells += groupCells.size
                } else {
                    // Merging next group into existing center
                    val prevCenterValue = currentCenterValue
                    val n = groupCells.size + 1 // group + current center
                    currentCenterValue = maxOf(currentCenterValue, value) + n - 2
                    steps.add(
                        MergeStep(
                            groupCells,
                            currentCenterValue,
                            calculateBaseScore(groupCells + createCell(x, y, prevCenterValue, id = "temp"))
                        )
                    )
                    totalCells += groupCells.size
                }
            }

            val mergingCells = neighborCells.filter { it.value in valuesToMerge } +
                    listOfNotNull(centerCell).filter { it.value in valuesToMerge }

            var baseScore = calculateBaseScore(mergingCells)
            if (mergingCells.any { it.isTactical }) {
                baseScore = (baseScore * 1.5).toInt()
            }

            val transition = MergeTransition(
                targetX = x,
                targetY = y,
                steps = steps,
                finalValue = currentCenterValue,
                totalCells = totalCells,
                uniqueGroups = valuesToMerge.size,
                baseScore = baseScore,
                resultId = "cell_${currentIdCounter++}",
                isTactical = mergingCells.any { it.isTactical },
                participatingIds = mergingCells.map { it.id }.toSet()
            )
            return transition to currentIdCounter
        }
        return null to currentIdCounter
    }

    fun calculatePathMerge(x: Int, y: Int, value: Int, grid: List<HexagonCell>, idCounter: Int): Pair<MergeTransition?, Int> {
        var currentIdCounter = idCounter
        val targetCellInGrid = grid.find { it.x == x && it.y == y }?.takeIf { !it.isFrozen }
        val targetValue = value

        val connectedCells = mutableSetOf<HexagonCell>()
        val queue = mutableListOf(targetCellInGrid ?: createCell(x, y, targetValue, id = "temp"))

        while (queue.isNotEmpty()) {
            val current = queue.removeAt(0)
            if (connectedCells.any { it.id == current.id }) continue
            connectedCells.add(current)

            val neighborCoords = getNeighbors(current.x, current.y)
            grid.filter { cell ->
                !cell.isFrozen &&
                        cell.value == targetValue &&
                        neighborCoords.any { it.first == cell.x && it.second == cell.y }
            }.forEach { queue.add(it) }
        }

        if (connectedCells.size < 2) return null to currentIdCounter

        val mergingNeighbors = connectedCells.filter { it.id != (targetCellInGrid?.id ?: "") }
        val finalValue = targetValue + connectedCells.size - 1
        
        val steps = mutableListOf<MergeStep>()
        var currentCenterValue = targetValue
        mergingNeighbors.forEach { neighbor ->
            val stepValue = currentCenterValue + 1
            steps.add(
                MergeStep(
                    mergingCells = listOf(neighbor),
                    resultValue = stepValue,
                    baseScore = calculateBaseScore(listOf(neighbor, createCell(x, y, currentCenterValue, id = "temp")))
                )
            )
            currentCenterValue = stepValue
        }

        var baseScore = calculateBaseScore(connectedCells.toList())
        if (connectedCells.any { it.isTactical }) {
            baseScore = (baseScore * 1.5).toInt()
        }

        val transition = MergeTransition(
            targetX = x,
            targetY = y,
            steps = steps,
            finalValue = finalValue,
            totalCells = connectedCells.size,
            uniqueGroups = mergingNeighbors.size,
            baseScore = baseScore,
            resultId = "cell_path_merge_${currentIdCounter++}",
            isTactical = connectedCells.any { it.isTactical },
            participatingIds = connectedCells.map { it.id }.toSet()
        )
        return transition to currentIdCounter
    }

    fun calculatePathMerge(x: Int, y: Int, grid: List<HexagonCell>, idCounter: Int): Pair<MergeTransition?, Int> {
        val targetCell = grid.find { it.x == x && it.y == y } ?: return null to idCounter
        return calculatePathMerge(x, y, targetCell.value, grid, idCounter)
    }

    fun calculateFusion(x: Int, y: Int, grid: List<HexagonCell>, idCounter: Int): Pair<MergeTransition?, Int> {
        var currentIdCounter = idCounter
        val neighborCoords = getNeighbors(x, y)
        val neighborCells = grid.filter { cell ->
            !cell.isFrozen && neighborCoords.any { it.first == cell.x && it.second == cell.y }
        }

        val centerCell = grid.find { it.x == x && it.y == y }?.takeIf { !it.isFrozen }
        val allCells = neighborCells + listOfNotNull(centerCell)

        // Fusion requires at least two neighbors to trigger
        if (neighborCells.size >= 2) {
            val groups = allCells.groupBy { it.value }
            val sortedValues = groups.keys.sortedDescending()

            val steps = mutableListOf<MergeStep>()
            var currentCenterValue = 0
            var totalCells = 0

            sortedValues.forEachIndexed { index, value ->
                val groupCells = groups[value]!!
                val mergingNeighbors =
                    groupCells.filter { it.id != (centerCell?.id ?: "placed_temp") }

                if (index == 0) {
                    currentCenterValue = value + groupCells.size - 1
                    if (mergingNeighbors.isNotEmpty()) {
                        steps.add(
                            MergeStep(
                                mergingNeighbors,
                                currentCenterValue,
                                calculateBaseScore(groupCells)
                            )
                        )
                    }
                    totalCells += groupCells.size
                } else {
                    val prevValue = currentCenterValue
                    val n = groupCells.size + 1
                    currentCenterValue = maxOf(currentCenterValue, value) + n - 1
                    steps.add(
                        MergeStep(
                            groupCells,
                            currentCenterValue,
                            calculateBaseScore(groupCells + createCell(x, y, prevValue, id = "temp"))
                        )
                    )
                    totalCells += groupCells.size
                }
            }

            var baseScore = calculateBaseScore(allCells)
            if (allCells.any { it.isTactical }) {
                baseScore = (baseScore * 1.5).toInt()
            }

            val transition = MergeTransition(
                targetX = x,
                targetY = y,
                steps = steps.ifEmpty { listOf(MergeStep(emptyList(), currentCenterValue)) },
                finalValue = currentCenterValue,
                totalCells = allCells.size,
                uniqueGroups = sortedValues.size,
                baseScore = baseScore,
                resultId = "cell_${currentIdCounter++}",
                isTactical = allCells.any { it.isTactical },
                participatingIds = allCells.map { it.id }.toSet()
            )
            return transition to currentIdCounter
        }
        return null to currentIdCounter
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
                        calculateFusion(x, y, grid, 0).first
                    } else {
                        calculateMerge(x, y, grid, 0).first
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
                            } + createCell(x, y, merge.finalValue, id = "temp")
                            
                            var chainCount = 0
                            while (chainCount < 10) { // Safety break
                                val chain = calculateMerge(x, y, currentGrid, 0).first ?: break
                                immediateScore += chain.baseScore * (comboMultiplier + chainCount + 1)
                                totalUniqueGroups += chain.uniqueGroups
                                finalValue = chain.finalValue
                                currentGrid = currentGrid.filter { cell ->
                                    chain.steps.none { step -> step.mergingCells.any { it.id == cell.id } } &&
                                            (cell.x != x || cell.y != y)
                                } + createCell(x, y, chain.finalValue, id = "temp")
                                chainCount++
                            }
                        }

                        // 3. Grid after merge (for future potential)
                        val gridAfterMerge = grid.filter { cell ->
                            merge.steps.none { step -> step.mergingCells.any { it.id == cell.id } } &&
                                    (cell.x != x || cell.y != y)
                        } + createCell(x, y, finalValue, id = "temp")

                        // 4. Future Potential (Queue landing)
                        var futureScore = 0
                        val gridAfterQueue = gridAfterMerge.toMutableList()
                        previews.forEach { p ->
                            if (gridAfterQueue.none { it.x == p.x && it.y == p.y }) {
                                gridAfterQueue.add(createCell(p.x, p.y, p.value, id = "temp"))
                            }
                        }

                        previews.forEach { p ->
                            val pMerge = calculateMerge(p.x, p.y, gridAfterQueue, 0).first
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

    fun simulateChainMerge(x: Int, y: Int, grid: List<HexagonCell>, combo: Int): MergeTransition? {
        var merge = calculateMerge(x, y, grid, 0).first ?: return null
        var totalScore = merge.baseScore * (combo + 1)
        var finalValue = merge.finalValue
        var currentGrid = grid.filter { cell ->
            merge.steps.none { step -> step.mergingCells.any { it.id == cell.id } } &&
                    (cell.x != x || cell.y != y)
        } + createCell(x, y, merge.finalValue, id = "temp")

        val allMergingIds = merge.steps.flatMap { it.mergingCells }.map { it.id }.toMutableSet()
        var chainCount = 1
        while (chainCount < 10) {
            val chain = calculateMerge(x, y, currentGrid, 0).first ?: break
            totalScore += chain.baseScore * (combo + chainCount + 1)
            finalValue = chain.finalValue
            allMergingIds.addAll(chain.steps.flatMap { it.mergingCells }.map { it.id })
            currentGrid = currentGrid.filter { cell ->
                chain.steps.none { step -> step.mergingCells.any { it.id == cell.id } } &&
                        (cell.x != x || cell.y != y)
            } + createCell(x, y, chain.finalValue, id = "temp")
            chainCount++
        }

        return merge.copy(
            finalValue = finalValue,
            baseScore = totalScore,
            uniqueGroups = chainCount, 
            resultId = "preview_chain",
            participatingIds = allMergingIds
        )
    }

    fun pickWeightedPerks(count: Int, random: Random, excludeLegendary: Boolean = false): List<Perk> {
        val pool = Perk.entries.filter { !excludeLegendary || !it.isLegendary }.toMutableList()
        val result = mutableListOf<Perk>()
        
        repeat(count) {
            if (pool.isEmpty()) return@repeat
            val totalWeight = pool.sumOf { it.baseWeight }
            var r = random.nextInt(totalWeight)
            
            for (perk in pool) {
                r -= perk.baseWeight
                if (r < 0) {
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
        return countPossibleMoves(grid) > 0
    }

    fun countPossibleMoves(grid: List<HexagonCell>): Int {
        val occupied = grid.map { it.x to it.y }.toSet()
        var possibleMoves = 0
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                if (x to y !in occupied) {
                    val neighbors = getNeighbors(x, y)
                    val neighborCells = grid.filter { cell -> !cell.isFrozen && neighbors.any { it.first == cell.x && it.second == cell.y } }
                    if (neighborCells.groupBy { it.value }.any { it.value.size >= 2 }) {
                        possibleMoves++
                    }
                }
            }
        }
        return possibleMoves
    }

    fun createCell(x: Int, y: Int, value: Int, id: String? = null, isTactical: Boolean = false): HexagonCell {
        return HexagonCell(id ?: "cell_temp", x, y, value, isTactical)
    }

    fun createPreviewCell(x: Int, y: Int, value: Int, id: String? = null, isTactical: Boolean = false): PreviewCell {
        return PreviewCell(id ?: "preview_temp", x, y, value, 0, isTactical)
    }

    fun syncCounters(grid: List<HexagonCell>, previews: List<PreviewCell>) {
        // No longer needed as we use state counters
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
        previews: List<PreviewCell>,
        existingPerks: List<OnBoardPerk>,
        perkSpawnCounter: Int,
        random: Random
    ): Pair<List<OnBoardPerk>, Int> {
        // Increment counter for this turn
        val newCounter = perkSpawnCounter + 1

        // 1. Rule: Only one perk on the board at a time
        if (existingPerks.isNotEmpty()) return existingPerks to newCounter

        // 2. Rule: Pity/Fairness system
        // Minimum 8 turns, Guaranteed at 15 turns
        val minTurns = 8
        val maxTurns = 15
        
        if (newCounter < minTurns) return existingPerks to newCounter
        
        val spawnChance = (newCounter - minTurns).toFloat() / (maxTurns - minTurns)
        if (random.nextFloat() > spawnChance) return existingPerks to newCounter
        
        // 3. Rule: Don't spawn where a ghost/preview will land
        val occupied = grid.map { it.x to it.y }.toSet()
        val previewPositions = previews.map { it.x to it.y }.toSet()
        
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (y in 0 until rows) {
            for (x in 0 until columns) {
                if (x to y !in occupied && x to y !in previewPositions) {
                    emptyPositions.add(x to y)
                }
            }
        }
        
        if (emptyPositions.isEmpty()) return existingPerks to newCounter
        
        val pos = emptyPositions.random(random)
        val perk = pickWeightedPerks(1, random, excludeLegendary = false).filter { it != Perk.PATH_MERGE }.firstOrNull() ?: return existingPerks to newCounter
        
        val lifespan = when {
            perk.baseWeight <= 20 -> 1 // Legendary: must be collected immediately
            perk.baseWeight <= 50 -> 2 // Rare: short window
            else -> 3 // Common: standard window
        }
        
        // Reset counter on successful spawn
        return (existingPerks + OnBoardPerk(pos.first, pos.second, perk, lifespan)) to 0
    }

    fun updateOnBoardPerks(perks: List<OnBoardPerk>): List<OnBoardPerk> {
        return perks.map { it.copy(lifespan = it.lifespan - 1) }.filter { it.lifespan > 0 }
    }

    fun spawnFromQueue(
        currentState: List<HexagonCell>,
        currentPreviews: List<PreviewCell>,
        currentPerks: List<OnBoardPerk>,
        random: Random,
        initialPreviewIdCounter: Int
    ): Triple<List<HexagonCell>, Pair<List<PreviewCell>, Int>, List<OnBoardPerk>> {
        var newState = currentState
        var nextPerks = currentPerks
        currentPreviews.forEach { p ->
            if (newState.none { it.x == p.x && it.y == p.y }) {
                newState = newState + createCell(p.x, p.y, p.value, id = p.id, isTactical = p.isTactical)
                // Delete perk if a ghost lands on it (don't collect)
                nextPerks = nextPerks.filterNot { it.x == p.x && it.y == p.y }
            }
        }

        val nextPreviews = pickRandomPreviews(newState, emptyList(), nextPerks, 3, random, initialPreviewIdCounter)
        return Triple(newState, nextPreviews, nextPerks)
    }

    fun decrementTacticalFlags(grid: List<HexagonCell>): List<HexagonCell> {
        return grid.map { it.copy(isTactical = false, isFrozen = false) }
    }

    fun canPerkResolveStuck(
        perk: Perk,
        grid: List<HexagonCell>,
        previews: List<PreviewCell>,
        previousState: GameState?
    ): Boolean {
        return when (perk) {
            Perk.UNDO -> previousState != null && !previousState.isStuck && previousState.availableChoices > 1
            Perk.REMOVE_TILE -> grid.isNotEmpty()
            Perk.MOVE_TILE -> {
                val occupied = grid.map { it.x to it.y }.toSet()
                if (occupied.size >= columns * rows) return false
                val distinctValues = grid.map { it.value }.distinct()
                for (y in 0 until rows) {
                    for (x in 0 until columns) {
                        if (x to y !in occupied) {
                            for (v in distinctValues) {
                                if (checkMergeAt(x, y, grid + createCell(x, y, v, id = "temp"))) return true
                            }
                        }
                    }
                }
                false
            }
            Perk.SWAP_TILES -> {
                // Swapping two solid tiles
                for (i in grid.indices) {
                    for (j in i + 1 until grid.size) {
                        val c1 = grid[i]
                        val c2 = grid[j]
                        val tempGrid = grid.map {
                            when (it.id) {
                                c1.id -> it.copy(value = c2.value)
                                c2.id -> it.copy(value = c1.value)
                                else -> it
                            }
                        }
                        if (checkMergeAt(c1.x, c1.y, tempGrid) || checkMergeAt(c2.x, c2.y, tempGrid)) return true
                    }
                }
                // Swapping a solid tile with a ghost
                for (cell in grid) {
                    for (preview in previews) {
                        val tempGrid = grid.map {
                            if (it.id == cell.id) it.copy(value = preview.value) else it
                        }
                        if (checkMergeAt(cell.x, cell.y, tempGrid)) return true
                        
                        // Also check if the ghost's new position (cell.x, cell.y) would create a merge
                        // but ghosts don't merge on their own, they only merge when they land.
                        // However, if we swap, the ghost stays a ghost at the new position.
                        // Wait, canPerkResolveStuck should check if the perk allows a merge to happen NOW.
                        // Swapping a ghost with a solid tile changes the value of the solid tile at its position.
                        // That's what the code above checks.
                    }
                }
                false
            }
            Perk.INCREMENT_TILE -> {
                grid.any { cell ->
                    checkMergeAt(cell.x, cell.y, grid.map { if (it.id == cell.id) it.copy(value = it.value + 1) else it })
                }
            }
            Perk.DUPLICATE_TILE -> {
                val occupied = grid.map { it.x to it.y }.toSet()
                if (occupied.size >= columns * rows) return false
                val distinctValues = (grid.map { it.value } + previews.map { it.value }).distinct()
                for (y in 0 until rows) {
                    for (x in 0 until columns) {
                        if (x to y !in occupied) {
                            for (v in distinctValues) {
                                if (checkMergeAt(x, y, grid + createCell(x, y, v, id = "temp"))) return true
                            }
                        }
                    }
                }
                false
            }
            Perk.FUSION -> {
                grid.any { cell ->
                    !cell.isFrozen &&
                            getNeighbors(cell.x, cell.y).count { n ->
                                grid.any { it.x == n.first && it.y == n.second && !it.isFrozen }
                            } >= 2
                }
            }
            Perk.PATH_MERGE -> {
                grid.any { cell ->
                    !cell.isFrozen &&
                            getNeighbors(cell.x, cell.y).any { n ->
                                grid.any {
                                    it.value == cell.value &&
                                            !it.isFrozen &&
                                            it.x == n.first && it.y == n.second
                                }
                            }
                }
            }
            Perk.ADVANCE_QUEUE -> {
                previews.any { p ->
                    val neighbors = getNeighbors(p.x, p.y)
                    grid.count { n -> !n.isFrozen && n.value == p.value && neighbors.any { it.first == n.x && it.second == n.y } } >= 1
                }
            }
            else -> false
        }
    }

    private fun checkMergeAt(x: Int, y: Int, grid: List<HexagonCell>): Boolean {
        val neighborCoords = getNeighbors(x, y)
        val neighborCells = grid.filter { cell ->
            !cell.isFrozen && neighborCoords.any { it.first == cell.x && it.second == cell.y }
        }
        val centerCell = grid.find { it.x == x && it.y == y }?.takeIf { !it.isFrozen }
        val groups = neighborCells.groupBy { it.value }.toMutableMap()
        centerCell?.let { center ->
            val group = groups[center.value] ?: emptyList()
            groups[center.value] = group + center
        }
        return groups.any { it.value.size >= 2 }
    }
}
