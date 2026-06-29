package com.pointlessgames.hexagone.game

import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.logic.ScoreResult
import com.pointlessgames.hexagone.game.logic.Scoring
import com.pointlessgames.hexagone.game.model.GameItem
import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PotentialMerge
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ActionDelegate(
    private val uiState: MutableStateFlow<GameUiState>,
    private val engine: GameEngine,
    private val stateDelegate: StateDelegate,
    private val effectDelegate: EffectDelegate,
    private val achievementDelegate: AchievementDelegate,
    private val challengeDelegate: ChallengeDelegate,
    private val onSpawnRequested: () -> Unit,
    private val onCheckValidMoves: () -> Unit,
    private val onUpdateLevel: () -> Unit,
    private val onHoveredMergeChanged: (MergeTransition?) -> Unit,
) {
    
    fun onEmptySpaceTouchDown(x: Int, y: Int) {
        val state = uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        val mergeResult = state.activePerk?.onEmptySpaceTouchDown(x, y, state, engine) ?: run {
            val (m, _) = engine.calculateMerge(x, y, state.grid, 0)
            m?.copy(resultId = "preview_merge")
        }
        val finalResult = mergeResult?.copy(baseScore = calculatePotentialScore(mergeResult, state))
        onHoveredMergeChanged(finalResult)
    }

    fun onCellTouchDown(cell: HexagonCell) {
        val state = uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        val mergeResult = state.activePerk?.onCellTouchDown(cell, state, engine)
        val finalResult = mergeResult?.copy(baseScore = calculatePotentialScore(mergeResult, state))
        onHoveredMergeChanged(finalResult)
    }

    fun onEmptySpaceClicked(x: Int, y: Int) {
        val state = uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        onHoveredMergeChanged(null)
        achievementDelegate.onNonUndoAction()

        val perk = state.activePerk
        val actionResult = perk?.onEmptySpaceClicked(x, y, state, engine) ?: run {
            if (state.grid.any { it.x == x && it.y == y }) return
            val (merge, nextIdCounter) = engine.calculateMerge(x, y, state.grid, state.cellIdCounter)
            if (merge != null) com.pointlessgames.hexagone.game.model.PerkActionResult.TriggerMerge(merge) else com.pointlessgames.hexagone.game.model.PerkActionResult.None
        }
        processPerkActionResult(actionResult, perk)
    }

    fun onCellClicked(cell: HexagonCell) {
        val state = uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        onHoveredMergeChanged(null)
        if (state.activePerk != null) {
            challengeDelegate.onPerkUsed(state.activePerk)
        }

        val perk = state.activePerk
        val actionResult = perk?.onCellClicked(cell, state, engine) ?: com.pointlessgames.hexagone.game.model.PerkActionResult.None
        processPerkActionResult(actionResult, perk)
    }

    fun onPreviewClicked(preview: PreviewCell) {
        val state = uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        if (state.activePerk != null) {
            challengeDelegate.onPerkUsed(state.activePerk)
        }

        val perk = state.activePerk
        val actionResult = perk?.onPreviewClicked(preview, state, engine) ?: com.pointlessgames.hexagone.game.model.PerkActionResult.None
        processPerkActionResult(actionResult, perk)
    }

    private fun processPerkActionResult(result: com.pointlessgames.hexagone.game.model.PerkActionResult, perk: Perk?) {
        when (result) {
            is com.pointlessgames.hexagone.game.model.PerkActionResult.None -> return
            is com.pointlessgames.hexagone.game.model.PerkActionResult.SelectCell -> {
                uiState.update { it.copy(selectedCellId = result.cellId) }
            }
            is com.pointlessgames.hexagone.game.model.PerkActionResult.MoveTile -> moveTile(result.sourceId, result.targetX, result.targetY)
            is com.pointlessgames.hexagone.game.model.PerkActionResult.DuplicateTile -> duplicateTile(result.sourceId, result.targetX, result.targetY)
            is com.pointlessgames.hexagone.game.model.PerkActionResult.SwapTiles -> {
                stateDelegate.saveState()
                swapTiles(result.id1, result.id2)
            }
            is com.pointlessgames.hexagone.game.model.PerkActionResult.TriggerMerge -> executeTriggerMerge(result.merge, perk)
            is com.pointlessgames.hexagone.game.model.PerkActionResult.RemoveTile -> executeRemoveTile(result.targetId, perk)
            is com.pointlessgames.hexagone.game.model.PerkActionResult.IncrementTile -> executeIncrementTile(result.targetId, perk)
            is com.pointlessgames.hexagone.game.model.PerkActionResult.MimicTile -> executeMimicTile(result.targetId, perk)
            is com.pointlessgames.hexagone.game.model.PerkActionResult.FreezeTile -> executeFreezeTile(result.targetId, perk)
        }
    }

    private fun executeTriggerMerge(merge: MergeTransition, perk: Perk?) {
        val state = uiState.value
        if (perk != null) achievementDelegate.checkPerkAchievements(perk, state)
        challengeDelegate.onMovePerformed()
        stateDelegate.saveState()
        uiState.update { currentState ->
            val firstStep = merge.steps.firstOrNull()
            val isPathMerge = merge.resultId.contains("path_merge")
            val nextComboValue = Scoring.getNextStepCombo(currentState.combo, 0, isPathMerge)
            val stepScore = if (firstStep != null) Scoring.getStepScore(firstStep.baseScore, nextComboValue) else 0

            val stateAfterMergeStart = currentState.copy(
                grid = currentState.grid.map { cell ->
                    if (firstStep?.mergingCells?.any { it.id == cell.id } == true) cell.copy(
                        x = merge.targetX,
                        y = merge.targetY,
                    ) else cell
                },
                preview = currentState.preview.filterNot { it.x == merge.targetX && it.y == merge.targetY },
                pendingMerge = merge.copy(
                    isPerkAssisted = perk != null,
                    startingCombo = currentState.combo
                ),
                activeMergeStepIndex = 0,
                pendingMergeScore = stepScore,
                combo = nextComboValue,
                isBusy = true,
                cellIdCounter = state.cellIdCounter + 1
            )
            if (perk != null) {
                stateAfterMergeStart.consumePerk(perk)
            } else {
                stateAfterMergeStart
            }
        }
    }

    private fun executeRemoveTile(targetId: String, perk: Perk?) {
        val state = uiState.value
        stateDelegate.saveState()
        var result: ScoreResult? = null
        var targetPos: Pair<Int, Int>? = null
        var mergeReport: MergeTransition? = null

        uiState.update { currentState ->
            val itemToRemove = getGameItem(targetId) ?: return@update currentState
            targetPos = itemToRemove.x to itemToRemove.y

            val merge = MergeTransition(
                targetX = itemToRemove.x,
                targetY = itemToRemove.y,
                steps = emptyList(),
                finalValue = 0,
                totalCells = 1,
                uniqueGroups = 0,
                baseScore = itemToRemove.value * 10,
                resultId = "preview_remove",
                isRemoval = true,
                participatingIds = setOf(itemToRemove.id),
            )
            mergeReport = merge
            val scoreResult = Scoring.calculateFinalScore(
                merge = merge,
                grid = currentState.grid,
                preview = currentState.preview,
                initialCombo = currentState.combo,
                activePerk = currentState.activePerk,
                redemptionBaseline = stateDelegate.redemptionBaseline,
            )
            result = scoreResult

            val nextScore = currentState.score + scoreResult.totalScore
            val nextGrid = currentState.grid.filter { it.id != targetId }
            val nextPreview = currentState.preview.filter { it.id != targetId }
            val nextState = currentState.copy(
                grid = nextGrid,
                preview = nextPreview,
                score = nextScore,
                bestScore = maxOf(currentState.bestScore, nextScore),
            )
            if (perk != null) nextState.consumePerk(perk).copy(activePerk = null, selectedCellId = null) else nextState
        }

        result?.let { scoreResult ->
            targetPos?.let { (tx, ty) ->
                stateDelegate.persistBestScore(uiState.value.score)
                stateDelegate.setRedemptionBaseline(null)

                effectDelegate.handlePopups(
                    targetX = tx,
                    targetY = ty,
                    totalScore = scoreResult.totalScore,
                    isRedemption = scoreResult.redemptionBonus > 0,
                    isBarRaised = scoreResult.barRaisedBonus > 0,
                    isSacrifice = scoreResult.sacrificeBonus > 0,
                    isTactical = scoreResult.isTactical,
                    isExecution = scoreResult.isExecution
                )
                effectDelegate.addTileRemoved(tx, ty)
                achievementDelegate.onMergeDetails(scoreResult.isTactical, scoreResult.barRaisedBonus > 0, scoreResult.sacrificeBonus > 0)
                mergeReport?.let { challengeDelegate.onMerge(it, scoreResult) }
            }
        }

        achievementDelegate.checkCleanse(state.grid, uiState.value.grid)
        achievementDelegate.checkScoreAchievements(uiState.value.score)
        if (perk != null) achievementDelegate.checkPerkAchievements(perk, uiState.value, isTargetGhost = false)
        achievementDelegate.onNonUndoAction()
        challengeDelegate.onScoreChanged(uiState.value.score)
        finalizeAction()
    }

    private fun executeIncrementTile(targetId: String, perk: Perk?) {
        val state = uiState.value
        stateDelegate.saveState()
        uiState.update { currentState ->
            val itemToUpdate = getGameItem(targetId) ?: return@update currentState
            val nextGrid = currentState.grid.map {
                if (it.id == targetId) it.copy(value = it.value + 1) else it
            }
            val nextPreview = currentState.preview.map {
                if (it.id == targetId) it.copy(value = it.value + 1) else it
            }
            val nextState = currentState.copy(grid = nextGrid, preview = nextPreview)
            if (perk != null) nextState.consumePerk(perk).copy(activePerk = null, selectedCellId = null) else nextState
        }
        if (perk != null) achievementDelegate.checkPerkAchievements(perk, uiState.value, isTargetGhost = false)
        finalizeAction()
    }

    private fun executeMimicTile(targetId: String, perk: Perk?) {
        val state = uiState.value
        stateDelegate.saveState()
        uiState.update { currentState ->
            val itemToUpdate = getGameItem(targetId) ?: return@update currentState
            val nextGrid = currentState.grid.map {
                if (it.id == targetId) it.copy(isMimic = true) else it
            }
            val nextPreview = currentState.preview.map {
                if (it.id == targetId) it.copy(isMimic = true) else it
            }
            val nextState = currentState.copy(grid = nextGrid, preview = nextPreview)
            if (perk != null) nextState.consumePerk(perk).copy(activePerk = null, selectedCellId = null) else nextState
        }
        if (perk != null) achievementDelegate.checkPerkAchievements(perk, uiState.value, isTargetGhost = false)
        finalizeAction()
    }

    private fun executeFreezeTile(targetId: String, perk: Perk?) {
        val state = uiState.value
        stateDelegate.saveState()
        uiState.update { currentState ->
            val itemToUpdate = getGameItem(targetId) ?: return@update currentState
            val nextGrid = currentState.grid.map {
                if (it.id == targetId) it.copy(isFrozen = true) else it
            }
            val nextPreview = currentState.preview
            val nextState = currentState.copy(grid = nextGrid, preview = nextPreview)
            if (perk != null) nextState.consumePerk(perk).copy(activePerk = null, selectedCellId = null) else nextState
        }
        if (perk != null) achievementDelegate.checkPerkAchievements(perk, uiState.value, isTargetGhost = false)
        finalizeAction()
    }

    private fun moveTile(selectedId: String, x: Int, y: Int) {
        val isTargetGhost = uiState.value.preview.any { it.id == selectedId }
        stateDelegate.saveState()
        var isGhostMarkedTactical = false
        uiState.update { currentState ->
            val cellToMove = currentState.grid.find { it.id == selectedId }
            val previewToMove = currentState.preview.find { it.id == selectedId }
            if (cellToMove == null && previewToMove == null) return@update currentState

            val collectedPerk = currentState.onBoardPerks.find { it.x == x && it.y == y }?.perk
            if (collectedPerk != null) {
                effectDelegate.addPerkPopup(x, y, collectedPerk)
                achievementDelegate.onPerkCollectedFromBoard()
            }

            val updatedGrid = if (cellToMove != null) {
                currentState.grid.map {
                    if (it.id == selectedId) it.copy(x = x, y = y, isTactical = true) else it
                }
            } else {
                if (previewToMove != null) {
                    isGhostMarkedTactical = true
                }
                currentState.grid
            }

            if (previewToMove != null && collectedPerk != null) {
                achievementDelegate.onPossession()
            }

            val updatedPreview = if (previewToMove != null) {
                currentState.preview.map {
                    if (it.id == selectedId) it.copy(x = x, y = y, isTactical = true) else it
                }.filterNot { it.id != selectedId && it.x == x && it.y == y }
            } else {
                currentState.preview.filterNot { it.x == x && it.y == y }
            }

            currentState.copy(
                grid = updatedGrid,
                preview = updatedPreview,
                collectedPerks = currentState.collectedPerks + listOfNotNull(collectedPerk),
                onBoardPerks = currentState.onBoardPerks.filterNot { it.x == x && it.y == y },
            ).consumePerk(Perk.MOVE_TILE).copy(activePerk = null, selectedCellId = null)
        }
        if (isGhostMarkedTactical) {
            achievementDelegate.onGhostMarkedTactical()
        }
        achievementDelegate.checkPerkAchievements(Perk.MOVE_TILE, uiState.value, isTargetGhost = isTargetGhost)
        stateDelegate.setRedemptionBaseline(null)
        finalizeAction()
    }

    private fun duplicateTile(selectedId: String, x: Int, y: Int) {
        val isTargetGhost = uiState.value.preview.any { it.id == selectedId }
        stateDelegate.saveState()
        var isGhostMarkedTactical = false
        uiState.update { currentState ->
            val cellToCopy = currentState.grid.find { it.id == selectedId }
            val previewToCopy = currentState.preview.find { it.id == selectedId }
            val value = cellToCopy?.value ?: previewToCopy?.value ?: return@update currentState
            val isMimic = cellToCopy?.isMimic ?: previewToCopy?.isMimic ?: false

            val collectedPerk = currentState.onBoardPerks.find { it.x == x && it.y == y }?.perk
            if (collectedPerk != null) {
                effectDelegate.addPerkPopup(x, y, collectedPerk)
                achievementDelegate.onPerkCollectedFromBoard()
            }

            var nextCellIdCounter = currentState.cellIdCounter
            var nextPreviewIdCounter = currentState.previewIdCounter

            val updatedGrid = if (cellToCopy != null) {
                currentState.grid + engine.createCell(x, y, value, id = "cell_${nextCellIdCounter++}", isTactical = true, isMimic = isMimic)
            } else {
                currentState.grid
            }

            val updatedPreview = if (previewToCopy != null) {
                isGhostMarkedTactical = true
                currentState.preview.filterNot { it.x == x && it.y == y } + engine.createPreviewCell(
                    x,
                    y,
                    value,
                    id = "preview_${nextPreviewIdCounter++}",
                    isTactical = true,
                    isMimic = isMimic
                )
            } else {
                currentState.preview.filterNot { it.x == x && it.y == y }
            }

            val potentialMerges = getPotentialMerges()
            if (potentialMerges.values.any { it.participatingIds.contains(selectedId) }) {
                achievementDelegate.onDoubleVisionOccurred()
            }

            currentState.copy(
                grid = updatedGrid,
                preview = updatedPreview,
                collectedPerks = currentState.collectedPerks + listOfNotNull(collectedPerk),
                onBoardPerks = currentState.onBoardPerks.filterNot { it.x == x && it.y == y },
                cellIdCounter = nextCellIdCounter,
                previewIdCounter = nextPreviewIdCounter,
            ).consumePerk(Perk.DUPLICATE_TILE).copy(activePerk = null, selectedCellId = null)
        }
        if (isGhostMarkedTactical) {
            achievementDelegate.onGhostMarkedTactical()
        }
        achievementDelegate.checkPerkAchievements(Perk.DUPLICATE_TILE, uiState.value, isTargetGhost = isTargetGhost)
        stateDelegate.setRedemptionBaseline(null)
        finalizeAction()
    }

    private fun swapTiles(id1: String, id2: String) {
        val isTargetGhost = uiState.value.preview.any { it.id == id1 || it.id == id2 }
        var ghostsMarkedTactical = 0
        uiState.update { currentState ->
            val item1 = currentState.grid.find { it.id == id1 }
                ?: currentState.preview.find { it.id == id1 }
            val item2 = currentState.grid.find { it.id == id2 }
                ?: currentState.preview.find { it.id == id2 }

            if (item1 == null || item2 == null) return@update currentState

            val x1 = if (item1 is HexagonCell) item1.x else (item1 as PreviewCell).x
            val y1 = if (item1 is HexagonCell) item1.y else (item1 as PreviewCell).y
            val x2 = if (item2 is HexagonCell) item2.x else (item2 as PreviewCell).x
            val y2 = if (item2 is HexagonCell) item2.y else (item2 as PreviewCell).y

            val updatedGrid = currentState.grid.map { cell ->
                when (cell.id) {
                    id1 -> cell.copy(x = x2, y = y2, isTactical = true)
                    id2 -> cell.copy(x = x1, y = y1, isTactical = true)
                    else -> cell
                }
            }
            if (id1.startsWith("preview")) {
                ghostsMarkedTactical++
            }
            if (id2.startsWith("preview")) {
                ghostsMarkedTactical++
            }
            val updatedPreview = currentState.preview.map { preview ->
                when (preview.id) {
                    id1 -> preview.copy(x = x2, y = y2, isTactical = true)
                    id2 -> preview.copy(x = x1, y = y1, isTactical = true)
                    else -> preview
                }
            }

            val collectedPerkAt1 = currentState.onBoardPerks.find { it.x == x1 && it.y == y1 }?.perk
            val collectedPerkAt2 = currentState.onBoardPerks.find { it.x == x2 && it.y == y2 }?.perk

            if (collectedPerkAt1 != null) {
                effectDelegate.addPerkPopup(x1, y1, collectedPerkAt1)
                achievementDelegate.onPerkCollectedFromBoard()
            }
            if (collectedPerkAt2 != null) {
                effectDelegate.addPerkPopup(x2, y2, collectedPerkAt2)
                achievementDelegate.onPerkCollectedFromBoard()
            }

            currentState.copy(
                grid = updatedGrid,
                preview = updatedPreview,
                collectedPerks = currentState.collectedPerks + listOfNotNull(
                    collectedPerkAt1,
                    collectedPerkAt2,
                ),
                onBoardPerks = currentState.onBoardPerks.filterNot { (it.x == x1 && it.y == y1) || (it.x == x2 && it.y == y2) },
            ).consumePerk(Perk.SWAP_TILES).copy(activePerk = null, selectedCellId = null)
        }
        repeat(ghostsMarkedTactical) {
            achievementDelegate.onGhostMarkedTactical()
        }
        achievementDelegate.checkPerkAchievements(Perk.SWAP_TILES, uiState.value, isTargetGhost = isTargetGhost)
        stateDelegate.setRedemptionBaseline(null)
        finalizeAction()
    }

    private fun finalizeAction() {
        challengeDelegate.onMovePerformed()
        challengeDelegate.checkBoardState(engine)
        achievementDelegate.onNonUndoAction()
        achievementDelegate.checkPatternAchievements(uiState.value.grid, uiState.value.preview, engine)
        onUpdateLevel()
        if (uiState.value.preview.isEmpty()) {
            onSpawnRequested()
        } else {
            onCheckValidMoves()
        }
    }

    fun getPotentialMerges(): Map<Pair<Int, Int>, PotentialMerge> {
        val state = uiState.value
        val grid = state.grid
        val perk = state.activePerk
        val result = mutableMapOf<Pair<Int, Int>, PotentialMerge>()

        for (x in 0 until engine.columns) {
            for (y in 0 until engine.rows) {
                if (grid.none { it.x == x && it.y == y }) {
                    val (merge, _) = when (perk) {
                        Perk.FUSION -> engine.calculateFusion(x, y, grid, 0)
                        Perk.PATH_MERGE -> engine.calculatePathMerge(x, y, grid, 0)
                        Perk.CHAIN_MERGE -> engine.simulateChainMerge(x, y, grid, state.combo) to 0
                        else -> engine.calculateMerge(x, y, grid, 0)
                    }

                    if (merge != null) {
                        result[x to y] = PotentialMerge(
                            targetX = x,
                            targetY = y,
                            finalValue = merge.finalValue,
                            baseScore = calculatePotentialScore(merge, state),
                            participatingIds = merge.steps.flatMap { it.mergingCells }.map { it.id }
                                .toSet(),
                        )
                    }
                }
            }
        }
        return result
    }

    fun calculatePotentialScore(merge: MergeTransition, state: GameUiState): Int {
        return Scoring.calculateFinalScore(
            merge = merge,
            grid = state.grid,
            preview = state.preview,
            initialCombo = state.combo,
            activePerk = state.activePerk,
            redemptionBaseline = stateDelegate.redemptionBaseline,
        ).totalScore
    }

    private fun getGameItem(id: String): GameItem? {
        val state = uiState.value
        state.grid.find { it.id == id }?.let { return GameItem(it.id, it.x, it.y, it.value, false, it.isMimic) }
        state.preview.find { it.id == id }
            ?.let { return GameItem(it.id, it.x, it.y, it.value, true, it.isMimic) }
        return null
    }
}
