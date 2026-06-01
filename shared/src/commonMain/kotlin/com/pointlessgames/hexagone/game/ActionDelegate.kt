package com.pointlessgames.hexagone.game

import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.logic.Scoring
import com.pointlessgames.hexagone.game.model.GameItem
import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PotentialMerge
import com.pointlessgames.hexagone.game.model.PreviewCell
import com.pointlessgames.hexagone.ui.theme.Colors
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.label_bar_raised
import hexagone.shared.generated.resources.label_cleanup
import hexagone.shared.generated.resources.label_janitor_plus
import hexagone.shared.generated.resources.label_sacrifice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ActionDelegate(
    private val uiState: MutableStateFlow<GameUiState>,
    private val engine: GameEngine,
    private val scope: CoroutineScope,
    private val stateDelegate: StateDelegate,
    private val effectDelegate: EffectDelegate,
    private val onSpawnRequested: () -> Unit,
    private val onCheckValidMoves: () -> Unit,
    private val onUpdateLevel: () -> Unit,
    private val onRecalculateHints: () -> Unit,
    private val onHoveredMergeChanged: (MergeTransition?) -> Unit,
) {
    fun onEmptySpaceClicked(x: Int, y: Int) {
        val state = uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        onHoveredMergeChanged(null)

        val perk = state.activePerk
        val selectedId = state.selectedCellId
        val previewAtPos = state.preview.find { it.x == x && it.y == y }

        if (perk == Perk.PATH_MERGE) return
        val isTileOnlyPerk =
            perk == Perk.REMOVE_TILE || perk == Perk.INCREMENT_TILE || perk == Perk.SWAP_TILES
        if (isTileOnlyPerk && previewAtPos == null) return

        if (perk != null && perk != Perk.FUSION && perk != Perk.CHAIN_MERGE && perk != Perk.SKIP_SPAWN && previewAtPos != null) {
            if (selectedId == previewAtPos.id) {
                uiState.update { it.copy(selectedCellId = null) }
                return
            }
            if (selectedId == null || perk == Perk.REMOVE_TILE || (perk == Perk.SWAP_TILES && selectedId != previewAtPos.id)) {
                onPreviewClicked(previewAtPos)
                return
            }
        }

        if (perk == Perk.MOVE_TILE && selectedId != null) {
            moveTile(selectedId, x, y)
            return
        }

        if (perk == Perk.DUPLICATE_TILE && selectedId != null) {
            duplicateTile(selectedId, x, y)
            return
        }

        if (state.grid.any { it.x == x && it.y == y }) return

        val merge = if (perk == Perk.FUSION) {
            engine.calculateFusion(x, y, state.grid)
        } else {
            engine.calculateMerge(x, y, state.grid)
        }

        if (merge != null) {
            stateDelegate.saveState()
            uiState.update { currentState ->
                val firstStep = merge.steps.first()
                val isPathMerge = merge.resultId == "preview_path_merge"
                val nextComboValue = Scoring.getNextStepCombo(currentState.combo, 0, isPathMerge)
                val stepScore = Scoring.getStepScore(firstStep.baseScore, nextComboValue)

                val stateAfterMergeStart = currentState.copy(
                    grid = currentState.grid.map { cell ->
                        if (firstStep.mergingCells.any { it.id == cell.id }) cell.copy(
                            x = x,
                            y = y,
                        ) else cell
                    },
                    preview = currentState.preview.filterNot { it.x == x && it.y == y },
                    pendingMerge = merge,
                    activeMergeStepIndex = 0,
                    pendingMergeScore = stepScore,
                    combo = nextComboValue,
                    isBusy = true,
                )
                if (perk == Perk.FUSION) {
                    stateAfterMergeStart.consumePerk(Perk.FUSION)
                } else {
                    stateAfterMergeStart
                }
            }
        } else {
            if (perk == Perk.CHAIN_MERGE) {
                stateDelegate.saveState()
                uiState.update { it.consumePerk(Perk.CHAIN_MERGE).copy(activePerk = null) }
                finalizeAction()
            }
        }
    }

    fun onEmptySpaceTouchDown(x: Int, y: Int) {
        val state = uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        val perk = state.activePerk
        val selectedId = state.selectedCellId
        val ghostAtPos = state.preview.find { it.x == x && it.y == y }

        if (perk != null && selectedId != null && ghostAtPos?.id == selectedId) return

        if (perk == Perk.PATH_MERGE) return
        val isTileOnlyPerk =
            perk == Perk.REMOVE_TILE || perk == Perk.INCREMENT_TILE || perk == Perk.SWAP_TILES
        if (isTileOnlyPerk && ghostAtPos == null) return

        val merge = when (perk) {
            Perk.REMOVE_TILE -> {
                if (ghostAtPos != null) {
                    val merge = MergeTransition(
                        targetX = x,
                        targetY = y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = ghostAtPos.value * 10,
                        resultId = "preview_remove_queue",
                        isRemoval = true,
                        participatingIds = setOf(ghostAtPos.id),
                    )
                    merge.copy(baseScore = calculatePotentialScore(merge, state))
                } else null
            }

            Perk.INCREMENT_TILE -> {
                if (ghostAtPos != null) {
                    val nextValue = ghostAtPos.value + 1
                    val merge = MergeTransition(
                        targetX = x,
                        targetY = y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = 0,
                        resultId = "preview_increment_queue",
                        participatingIds = setOf(ghostAtPos.id),
                        previewValues = mapOf(ghostAtPos.id to nextValue),
                    )
                    merge.copy(baseScore = calculatePotentialScore(merge, state))
                } else null
            }

            Perk.PATH_MERGE -> {
                val merge = engine.calculatePathMerge(x, y, state.grid)
                merge?.let {
                    it.copy(
                        resultId = "preview_path_merge",
                        forceSolidIds = setOf("preview_path_merge"),
                        previewValues = ghostAtPos?.let { mapOf(it.id to merge.finalValue) }
                            ?: emptyMap(),
                        baseScore = calculatePotentialScore(it, state)
                    )
                }
            }

            Perk.MOVE_TILE, Perk.DUPLICATE_TILE -> {
                if (selectedId != null && (ghostAtPos == null || selectedId != ghostAtPos.id)) {
                    val source = getGameItem(selectedId)
                    if (source != null) {
                        val resultId =
                            if (perk == Perk.MOVE_TILE) "preview_move" else "preview_duplicate"
                        val swaps =
                            if (perk == Perk.MOVE_TILE) mapOf(selectedId to (x to y)) else null
                        val isSourceSolid = !source.isGhost
                        val forceSolidIds = if (isSourceSolid) setOf(resultId) else emptySet()
                        val forceGhostAtSource =
                            if (perk == Perk.MOVE_TILE && source.isGhost) setOf(selectedId) else emptySet()

                        MergeTransition(
                            targetX = x,
                            targetY = y,
                            steps = emptyList(),
                            finalValue = source.value,
                            totalCells = 1,
                            uniqueGroups = 0,
                            baseScore = 0,
                            resultId = resultId,
                            participatingIds = setOf(selectedId) + listOfNotNull(ghostAtPos?.id),
                            previewSwaps = swaps,
                            forceSolidIds = forceSolidIds + forceGhostAtSource,
                        )
                    } else null
                } else null
            }

            Perk.SWAP_TILES -> {
                if (selectedId != null && ghostAtPos != null && selectedId != ghostAtPos.id) {
                    val source = getGameItem(selectedId)
                    val target = ghostAtPos
                    if (source != null) {
                        val swaps = mapOf(
                            source.id to (target.x to target.y),
                            target.id to (source.x to source.y),
                        )

                        MergeTransition(
                            targetX = x,
                            targetY = y,
                            steps = emptyList(),
                            finalValue = 0,
                            totalCells = 1,
                            uniqueGroups = 0,
                            baseScore = 0,
                            resultId = "preview_swap",
                            previewSwaps = swaps,
                            participatingIds = setOf(source.id, target.id),
                        )
                    } else null
                } else null
            }

            Perk.FUSION -> {
                val merge = engine.calculateFusion(x, y, state.grid)
                merge?.let { it.copy(baseScore = calculatePotentialScore(it, state), resultId = "preview_fusion") }
            }
            null, Perk.CHAIN_MERGE, Perk.SKIP_SPAWN -> {
                val merge = if (perk == Perk.CHAIN_MERGE) {
                    engine.simulateChainMerge(x, y, state.grid, state.combo)
                } else {
                    engine.calculateMerge(x, y, state.grid)
                }
                merge?.let { it.copy(baseScore = calculatePotentialScore(it, state), resultId = "preview_merge") }
            }

            else -> null
        }
        onHoveredMergeChanged(merge)
    }

    fun onCellTouchDown(cell: HexagonCell) {
        val state = uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        val perk = state.activePerk
        val selectedId = state.selectedCellId

        if (perk != null && selectedId == cell.id) return

        val merge = when (perk) {
            Perk.PATH_MERGE -> {
                val m = engine.calculatePathMerge(cell.x, cell.y, state.grid)
                m?.let {
                    it.copy(
                        resultId = "preview_path_merge",
                        forceSolidIds = setOf("preview_path_merge"),
                        previewValues = mapOf(cell.id to m.finalValue),
                        baseScore = calculatePotentialScore(it, state)
                    )
                }
            }

            Perk.REMOVE_TILE -> {
                val merge = MergeTransition(
                    targetX = cell.x,
                    targetY = cell.y,
                    steps = emptyList(),
                    finalValue = 0,
                    totalCells = 1,
                    uniqueGroups = 0,
                    baseScore = cell.value * 10,
                    resultId = "preview_remove",
                    isRemoval = true,
                    participatingIds = setOf(cell.id),
                )
                merge.copy(baseScore = calculatePotentialScore(merge, state))
            }

            Perk.INCREMENT_TILE -> {
                val nextValue = cell.value + 1
                val merge = MergeTransition(
                    targetX = cell.x,
                    targetY = cell.y,
                    steps = emptyList(),
                    finalValue = 0,
                    totalCells = 1,
                    uniqueGroups = 0,
                    baseScore = 0,
                    resultId = "preview_increment",
                    participatingIds = setOf(cell.id),
                    previewValues = mapOf(cell.id to nextValue),
                )
                merge.copy(baseScore = calculatePotentialScore(merge, state))
            }

            Perk.SWAP_TILES -> {
                if (selectedId != null && selectedId != cell.id) {
                    val source = getGameItem(selectedId)
                    val target = cell
                    if (source != null) {
                        val swaps = mapOf(
                            source.id to (target.x to target.y),
                            target.id to (source.x to source.y),
                        )

                        MergeTransition(
                            targetX = cell.x,
                            targetY = cell.y,
                            steps = emptyList(),
                            finalValue = 0,
                            totalCells = 1,
                            uniqueGroups = 0,
                            baseScore = 0,
                            resultId = "preview_swap",
                            previewSwaps = swaps,
                            participatingIds = setOf(source.id, target.id),
                        )
                    } else null
                } else null
            }

            null -> null
            else -> null
        }
        onHoveredMergeChanged(merge)
    }

    fun onPreviewClicked(preview: PreviewCell) {
        val state = uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        when (val perk = state.activePerk) {
            Perk.MOVE_TILE -> {
                val selectedId = state.selectedCellId
                if (selectedId != null && selectedId != preview.id) {
                    moveTile(selectedId, preview.x, preview.y)
                } else {
                    uiState.update { it.copy(selectedCellId = preview.id) }
                }
            }

            Perk.DUPLICATE_TILE -> {
                uiState.update { it.copy(selectedCellId = if (it.selectedCellId == preview.id) null else preview.id) }
            }

            Perk.REMOVE_TILE -> {
                stateDelegate.saveState()
                uiState.update { currentState ->
                    val previewToTarget = currentState.preview.find { it.id == preview.id }
                        ?: return@update currentState
                    val merge = MergeTransition(
                        targetX = previewToTarget.x,
                        targetY = previewToTarget.y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = previewToTarget.value * 10,
                        resultId = "preview_remove_queue",
                        isRemoval = true,
                        participatingIds = setOf(previewToTarget.id),
                    )
                    val scoreResult = Scoring.calculateFinalScore(
                        merge = merge,
                        grid = currentState.grid,
                        preview = currentState.preview,
                        initialCombo = currentState.combo,
                        activePerk = currentState.activePerk,
                        lastMoveScore = stateDelegate.lastMoveScore
                    )

                    val labelRes =
                        if (scoreResult.barRaisedBonus > 0) Res.string.label_bar_raised else Res.string.label_cleanup
                    val nextScore = currentState.score + scoreResult.totalScore

                    stateDelegate.persistBestScore(nextScore)
                    val popupColor =
                        if (scoreResult.barRaisedBonus > 0) Colors().skyBlue else Colors().greyBlue
                    effectDelegate.addScorePopup(
                        previewToTarget.x,
                        previewToTarget.y,
                        scoreResult.totalScore,
                        popupColor,
                        labelRes
                    )

                    currentState.copy(
                        preview = currentState.preview.filter { it.id != previewToTarget.id },
                        score = nextScore,
                    ).consumePerk(Perk.REMOVE_TILE).copy(activePerk = null, selectedCellId = null)
                }
                finalizeAction()
            }

            Perk.SWAP_TILES -> {
                val selectedId = state.selectedCellId
                if (selectedId == null) {
                    uiState.update { it.copy(selectedCellId = preview.id) }
                } else if (selectedId != preview.id) {
                    swapTiles(selectedId, preview.id)
                }
            }

            Perk.INCREMENT_TILE -> {
                stateDelegate.saveState()
                uiState.update { currentState ->
                    val previewToUpdate = currentState.preview.find { it.id == preview.id }
                        ?: return@update currentState
                    val merge = MergeTransition(
                        targetX = previewToUpdate.x,
                        targetY = previewToUpdate.y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = 0,
                        resultId = "preview_increment_queue",
                        participatingIds = setOf(previewToUpdate.id),
                        previewValues = mapOf(previewToUpdate.id to previewToUpdate.value + 1),
                    )
                    val scoreResult = Scoring.calculateFinalScore(
                        merge = merge,
                        grid = currentState.grid,
                        preview = currentState.preview,
                        initialCombo = currentState.combo,
                        activePerk = currentState.activePerk,
                        lastMoveScore = stateDelegate.lastMoveScore
                    )

                    val nextPreview = currentState.preview.map {
                        if (it.id == previewToUpdate.id) it.copy(
                            value = it.value + 1,
                            isTactical = true,
                        ) else it
                    }

                    if (scoreResult.totalScore > 0) {
                        val nextScore = currentState.score + scoreResult.totalScore
                        stateDelegate.persistBestScore(nextScore)
                        effectDelegate.addScorePopup(
                            previewToUpdate.x,
                            previewToUpdate.y,
                            scoreResult.totalScore,
                            Colors().skyBlue,
                            Res.string.label_bar_raised,
                        )
                        currentState.copy(
                            preview = nextPreview,
                            score = nextScore,
                        ).consumePerk(Perk.INCREMENT_TILE)
                            .copy(activePerk = null, selectedCellId = null)
                    } else {
                        currentState.copy(preview = nextPreview).consumePerk(Perk.INCREMENT_TILE)
                            .copy(activePerk = null, selectedCellId = null)
                    }
                }
                finalizeAction()
            }

            else -> {}
        }
    }

    fun onCellClicked(cell: HexagonCell) {
        val state = uiState.value
        if (state.pendingMerge != null || state.isBusy || state.isGameOver || (state.isStuck && state.activePerk == null) || state.perkOptions.isNotEmpty()) return

        when (val perk = state.activePerk) {
            Perk.MOVE_TILE -> {
                uiState.update { it.copy(selectedCellId = if (it.selectedCellId == cell.id) null else cell.id) }
            }

            Perk.DUPLICATE_TILE -> {
                uiState.update { it.copy(selectedCellId = if (it.selectedCellId == cell.id) null else cell.id) }
            }

            Perk.INCREMENT_TILE -> {
                stateDelegate.saveState()
                uiState.update { currentState ->
                    val cellToUpdate =
                        currentState.grid.find { it.id == cell.id } ?: return@update currentState
                    val merge = MergeTransition(
                        targetX = cellToUpdate.x,
                        targetY = cellToUpdate.y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = 0,
                        resultId = "preview_increment",
                        participatingIds = setOf(cellToUpdate.id),
                        previewValues = mapOf(cellToUpdate.id to cellToUpdate.value + 1),
                    )
                    val scoreResult = Scoring.calculateFinalScore(
                        merge = merge,
                        grid = currentState.grid,
                        preview = currentState.preview,
                        initialCombo = currentState.combo,
                        activePerk = currentState.activePerk,
                        lastMoveScore = stateDelegate.lastMoveScore
                    )

                    val nextGrid = currentState.grid.map {
                        if (it.id == cellToUpdate.id) it.copy(
                            value = it.value + 1,
                            isTactical = true,
                        ) else it
                    }

                    if (scoreResult.totalScore > 0) {
                        val nextScore = currentState.score + scoreResult.totalScore
                        stateDelegate.persistBestScore(nextScore)
                        effectDelegate.addScorePopup(
                            cellToUpdate.x,
                            cellToUpdate.y,
                            scoreResult.totalScore,
                            Colors().skyBlue,
                            Res.string.label_bar_raised,
                        )
                        currentState.copy(
                            grid = nextGrid,
                            score = nextScore,
                        ).consumePerk(Perk.INCREMENT_TILE)
                            .copy(activePerk = null, selectedCellId = null)
                    } else {
                        currentState.copy(grid = nextGrid).consumePerk(Perk.INCREMENT_TILE)
                            .copy(activePerk = null, selectedCellId = null)
                    }
                }
                finalizeAction()
            }

            Perk.PATH_MERGE -> {
                val merge = engine.calculatePathMerge(cell.x, cell.y, state.grid)
                if (merge != null) {
                    stateDelegate.saveState()
                    uiState.update { currentState ->
                        val firstStep = merge.steps.first()
                        val nextComboValue = Scoring.getNextStepCombo(currentState.combo, 0, true)
                        val stepScore = Scoring.getStepScore(firstStep.baseScore, nextComboValue)

                        currentState.copy(
                            grid = currentState.grid.map { c ->
                                if (firstStep.mergingCells.any { it.id == c.id }) c.copy(
                                    x = cell.x,
                                    y = cell.y,
                                ) else c
                            },
                            pendingMerge = merge.copy(resultId = "preview_path_merge"),
                            activeMergeStepIndex = 0,
                            pendingMergeScore = stepScore,
                            combo = nextComboValue,
                            isBusy = true,
                        )
                    }
                }
            }

            Perk.REMOVE_TILE -> {
                stateDelegate.saveState()
                uiState.update { currentState ->
                    val cellToRemove =
                        currentState.grid.find { it.id == cell.id } ?: return@update currentState
                    val merge = MergeTransition(
                        targetX = cellToRemove.x,
                        targetY = cellToRemove.y,
                        steps = emptyList(),
                        finalValue = 0,
                        totalCells = 1,
                        uniqueGroups = 0,
                        baseScore = cellToRemove.value * 10,
                        resultId = "preview_remove",
                        isRemoval = true,
                        participatingIds = setOf(cellToRemove.id),
                    )
                    val scoreResult = Scoring.calculateFinalScore(
                        merge = merge,
                        grid = currentState.grid,
                        preview = currentState.preview,
                        initialCombo = currentState.combo,
                        activePerk = currentState.activePerk,
                        lastMoveScore = stateDelegate.lastMoveScore
                    )

                    val labelRes = when {
                        scoreResult.barRaisedBonus > 0 && scoreResult.sacrificeBonus > 0 -> Res.string.label_janitor_plus
                        scoreResult.barRaisedBonus > 0 -> Res.string.label_bar_raised
                        scoreResult.sacrificeBonus > 0 -> Res.string.label_sacrifice
                        else -> Res.string.label_cleanup
                    }

                    val nextScore = currentState.score + scoreResult.totalScore
                    stateDelegate.persistBestScore(nextScore)
                    val popupColor = when {
                        scoreResult.barRaisedBonus > 0 -> Colors().skyBlue
                        scoreResult.sacrificeBonus > 0 -> Colors().pink
                        else -> Colors().greyBlue
                    }
                    effectDelegate.addScorePopup(
                        cellToRemove.x,
                        cellToRemove.y,
                        scoreResult.totalScore,
                        popupColor,
                        labelRes,
                    )

                    currentState.copy(
                        grid = currentState.grid.filter { it.id != cellToRemove.id },
                        score = nextScore,
                    ).consumePerk(Perk.REMOVE_TILE).copy(activePerk = null, selectedCellId = null)
                }
                finalizeAction()
            }

            Perk.SWAP_TILES -> {
                val selectedId = state.selectedCellId
                if (selectedId == null) {
                    uiState.update { it.copy(selectedCellId = cell.id) }
                } else if (selectedId == cell.id) {
                    uiState.update { it.copy(selectedCellId = null) }
                } else {
                    stateDelegate.saveState()
                    swapTiles(selectedId, cell.id)
                }
            }

            else -> {}
        }
    }

    private fun moveTile(selectedId: String, x: Int, y: Int) {
        stateDelegate.saveState()
        uiState.update { currentState ->
            val cellToMove = currentState.grid.find { it.id == selectedId }
            val previewToMove = currentState.preview.find { it.id == selectedId }
            if (cellToMove == null && previewToMove == null) return@update currentState

            val collectedPerk = currentState.onBoardPerks.find { it.x == x && it.y == y }?.perk
            if (collectedPerk != null) {
                effectDelegate.addPerkPopup(x, y, collectedPerk)
            }

            val updatedGrid = if (cellToMove != null) {
                currentState.grid.map {
                    if (it.id == selectedId) it.copy(x = x, y = y, isTactical = true) else it
                }
            } else {
                currentState.grid
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
        finalizeAction()
    }

    private fun duplicateTile(selectedId: String, x: Int, y: Int) {
        stateDelegate.saveState()
        uiState.update { currentState ->
            val cellToCopy = currentState.grid.find { it.id == selectedId }
            val previewToCopy = currentState.preview.find { it.id == selectedId }
            val value = cellToCopy?.value ?: previewToCopy?.value ?: return@update currentState

            val collectedPerk = currentState.onBoardPerks.find { it.x == x && it.y == y }?.perk
            if (collectedPerk != null) {
                effectDelegate.addPerkPopup(x, y, collectedPerk)
            }

            val updatedGrid = if (cellToCopy != null) {
                currentState.grid + engine.createCell(x, y, value, isTactical = true)
            } else {
                currentState.grid
            }

            val updatedPreview = if (previewToCopy != null) {
                currentState.preview.filterNot { it.x == x && it.y == y } + engine.createPreviewCell(x, y, value, isTactical = true)
            } else {
                currentState.preview.filterNot { it.x == x && it.y == y }
            }

            currentState.copy(
                grid = updatedGrid,
                preview = updatedPreview,
                collectedPerks = currentState.collectedPerks + listOfNotNull(collectedPerk),
                onBoardPerks = currentState.onBoardPerks.filterNot { it.x == x && it.y == y },
            ).consumePerk(Perk.DUPLICATE_TILE).copy(activePerk = null, selectedCellId = null)
        }
        finalizeAction()
    }

    private fun swapTiles(id1: String, id2: String) {
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
            }
            if (collectedPerkAt2 != null) {
                effectDelegate.addPerkPopup(x2, y2, collectedPerkAt2)
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
        finalizeAction()
    }

    private fun finalizeAction() {
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
                    val merge = when (perk) {
                        Perk.FUSION -> engine.calculateFusion(x, y, grid)
                        Perk.PATH_MERGE -> engine.calculatePathMerge(x, y, grid)
                        Perk.CHAIN_MERGE -> engine.simulateChainMerge(x, y, grid, state.combo)
                        else -> engine.calculateMerge(x, y, grid)
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
            lastMoveScore = stateDelegate.lastMoveScore
        ).totalScore
    }

    private fun getGameItem(id: String): GameItem? {
        val state = uiState.value
        state.grid.find { it.id == id }?.let { return GameItem(it.id, it.x, it.y, it.value, false) }
        state.preview.find { it.id == id }
            ?.let { return GameItem(it.id, it.x, it.y, it.value, true) }
        return null
    }
}
