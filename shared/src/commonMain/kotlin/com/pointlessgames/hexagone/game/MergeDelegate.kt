package com.pointlessgames.hexagone.game

import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.logic.Scoring
import com.pointlessgames.hexagone.game.model.ComboTier
import com.pointlessgames.hexagone.game.model.GameEffect
import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeStep
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class MergeDelegate(
    private val uiState: MutableStateFlow<GameUiState>,
    private val engine: GameEngine,
    private val scope: CoroutineScope,
    private val stateDelegate: StateDelegate,
    private val effectDelegate: EffectDelegate,
    private val achievementDelegate: AchievementDelegate,
    private val challengeDelegate: ChallengeDelegate,
    private val onSpawnRequested: (decrementLifespan: Boolean, skipSpawn: Boolean) -> Unit,
) {
    private var lastProcessedMergeId: String? = null
    private var lastProcessedStepIndex: Int = -1

    fun resetLastProcessed() {
        lastProcessedMergeId = null
        lastProcessedStepIndex = -1
    }

    fun onMergeAnimationFinished() {
        val state = uiState.value
        val merge = state.pendingMerge
        if (merge == null) {
            resetLastProcessed()
            return
        }
        val stepIndex = state.activeMergeStepIndex

        if (lastProcessedMergeId == merge.resultId && lastProcessedStepIndex == stepIndex) return
        lastProcessedMergeId = merge.resultId
        lastProcessedStepIndex = stepIndex

        val currentStep = merge.steps[stepIndex]

        scope.launch {
            uiState.update { it.copy(isBusy = true) }

            val currentState = uiState.value
            val stateAfterStep = calculateStateAfterStep(currentState, merge, currentStep)

            if (stepIndex >= merge.steps.lastIndex) {
                processFinalStep(merge, currentState, stateAfterStep)
            } else {
                processNextStep(merge, stepIndex, stateAfterStep)
            }
        }
    }

    private fun calculateStateAfterStep(
        currentState: GameUiState,
        merge: MergeTransition,
        currentStep: MergeStep,
    ): List<HexagonCell> {
        return currentState.grid.filter { cell ->
            currentStep.mergingCells.none { it.id == cell.id } && (cell.x != merge.targetX || cell.y != merge.targetY)
        } + engine.createCell(
            merge.targetX,
            merge.targetY,
            currentStep.resultValue,
            merge.resultId,
        )
    }

    private suspend fun processFinalStep(
        merge: MergeTransition,
        currentState: GameUiState,
        stateAfterStep: List<HexagonCell>,
    ) {
        val barRaisedBonus = Scoring.calculateBarRaisedBonus(
            currentState.grid,
            currentState.preview,
            stateAfterStep,
            currentState.preview.filter { it.x != merge.targetX || it.y != merge.targetY },
        )

        val scoreResult = Scoring.calculateFinalScore(
            merge,
            currentState.grid,
            currentState.preview,
            merge.startingCombo,
            currentState.activePerk,
            stateDelegate.redemptionBaseline
        ).let {
            // Use the actual barRaisedBonus we calculated with the new grid
            val multiplier = (it.finalCombo + 1).coerceAtMost(Scoring.MAX_COMBO_MULTIPLIER)
            
            // Use a dampened multiplier consistent with Scoring logic
            val dampenedMultiplier = 1 + (multiplier - 1) / 3
            val updatedBarRaised = barRaisedBonus * dampenedMultiplier
            val updatedSacrifice = it.sacrificeBonus * dampenedMultiplier
            
            // Recalculate total score with the actual bonuses
            val scoreBeforeRedemption = it.stepScore + updatedBarRaised + updatedSacrifice
            val redemptionBonus = Scoring.calculateRedemptionBonus(scoreBeforeRedemption, stateDelegate.redemptionBaseline)
            
            it.copy(
                totalScore = scoreBeforeRedemption + redemptionBonus,
                bonusScore = updatedBarRaised + updatedSacrifice + redemptionBonus,
                barRaisedBonus = barRaisedBonus,
                redemptionBonus = redemptionBonus
            )
        }

        stateDelegate.setRedemptionBaseline(null)
        val finalCombo = scoreResult.finalCombo
        val redemptionBonus = scoreResult.redemptionBonus > 0
        val isBarRaised = barRaisedBonus > 0
        val isSacrifice = scoreResult.sacrificeBonus > 0

        if (redemptionBonus) {
            achievementDelegate.onRedemptionOccurred()
        }

        effectDelegate.handlePopups(
            merge.targetX,
            merge.targetY,
            scoreResult.totalScore,
            redemptionBonus,
            isBarRaised,
            isSacrifice,
            scoreResult.isTactical,
            scoreResult.isExecution
        )
        val mergeIntensity = (0.4f + (finalCombo * 0.1f) + (merge.totalCells - 2) * 0.2f).coerceIn(0.2f, 2f)
        effectDelegate.addMergeParticles(merge.targetX, merge.targetY, merge.finalValue, intensity = mergeIntensity, combo = finalCombo)

        val collectedOnBoard =
            currentState.onBoardPerks.find { it.x == merge.targetX && it.y == merge.targetY }?.perk

        if (collectedOnBoard != null) {
            effectDelegate.addPerkPopup(merge.targetX, merge.targetY, collectedOnBoard)
            effectDelegate.addMergeParticles(merge.targetX, merge.targetY, collectedOnBoard.ordinal, isPerk = true)
            achievementDelegate.onPerkCollectedFromBoard()
        }

        val remainingOnBoard =
            currentState.onBoardPerks.filterNot { it.x == merge.targetX && it.y == merge.targetY }

        val finalScore = currentState.score + scoreResult.totalScore
        stateDelegate.persistBestScore(finalScore)

        val random = kotlin.random.Random(currentState.seed)
        val tierRewards = handleTierRewards(finalCombo, currentState, random)
        val newlyEarnedPerks = tierRewards.map { it.second }
        val newReachedTiers = tierRewards.map { it.first }.toSet()
        val newRewardEffects = tierRewards.map { GameEffect.TierReward(it.first, it.second) }

        val isZenithReached = (finalCombo + 1) >= ComboTier.ZENITH.threshold &&
                !currentState.reachedComboTiers.contains(ComboTier.ZENITH)

        val finalGrid = if (isZenithReached) {
            stateAfterStep.map { it.copy(value = it.value + 1) }
        } else stateAfterStep

        val finalPreview = if (isZenithReached) {
            currentState.preview.map { it.copy(value = it.value + 1) }
        } else currentState.preview

        uiState.update {
            it.copy(
                score = finalScore,
                bestScore = maxOf(it.bestScore, finalScore),
                levelProgress = engine.getLevelProgress(finalScore, it.level),
                combo = finalCombo,
                grid = finalGrid,
                preview = finalPreview,
                collectedPerks = it.collectedPerks + listOfNotNull(collectedOnBoard) + newlyEarnedPerks,
                onBoardPerks = remainingOnBoard,
                reachedComboTiers = if (finalCombo == 0) emptySet() else it.reachedComboTiers + newReachedTiers,
                earnedRewardsThisTurn = it.earnedRewardsThisTurn + newRewardEffects,
                mergeHints = if (it.mergeHintsEnabled) engine.findMergeHints(
                    finalGrid,
                    finalPreview,
                    finalCombo,
                    it.activePerk,
                ) else emptyList(),
                pendingMerge = null,
                activeMergeStepIndex = 0,
                pendingMergeScore = 0,
                isBusy = true,
                maxCombo = maxOf(it.maxCombo, finalCombo),
                totalMerges = it.totalMerges + 1,
                seed = random.nextLong()
            )
        }

        achievementDelegate.checkMergeAchievements(merge, engine, scoreResult.totalScore)
        achievementDelegate.checkComboAchievements(finalCombo)
        achievementDelegate.checkComboBroken(currentState.combo, finalCombo)
        
        if (currentState.combo > 0 && finalCombo == 0) {
            effectDelegate.addComboBroken()
        }
        achievementDelegate.checkScoreAchievements(finalScore)
        achievementDelegate.onMergesIncremented(merge.uniqueGroups)
        achievementDelegate.onMergeDetails(merge.isTactical, isBarRaised, isSacrifice)
        achievementDelegate.onNonUndoAction()
        challengeDelegate.onMerge(merge, scoreResult)
        challengeDelegate.onScoreChanged(finalScore)
        challengeDelegate.onCombo(finalCombo)
        challengeDelegate.checkBoardState(engine)

        handleChainMerge(merge, finalCombo)
    }

    private fun handleTierRewards(
        finalCombo: Int,
        currentState: GameUiState,
        random: kotlin.random.Random
    ): List<Pair<ComboTier, Perk>> {
        val rewards = mutableListOf<Pair<ComboTier, Perk>>()
        ComboTier.entries.forEach { tier ->
            if (finalCombo + 1 >= tier.threshold && !currentState.reachedComboTiers.contains(tier)) {
                val reward = when (tier) {
                    ComboTier.SURGE -> Perk.entries.filter { it.baseWeight in 50..80 }.random(random)
                    ComboTier.OVERDRIVE -> Perk.entries.filter { it.isLegendary }.random(random)
                    ComboTier.ZENITH -> Perk.entries.filter { it.isLegendary }.random(random)
                }
                rewards.add(tier to reward)
            }
        }
        return rewards
    }

    private suspend fun handleChainMerge(merge: MergeTransition, finalCombo: Int) {
        val stateBeforeChain = uiState.value
        val (chainMerge, nextIdCounter) = if (stateBeforeChain.activePerk == Perk.CHAIN_MERGE) {
            engine.calculateMerge(merge.targetX, merge.targetY, stateBeforeChain.grid, stateBeforeChain.cellIdCounter)
        } else (null to stateBeforeChain.cellIdCounter)

        if (chainMerge != null) {
            delay(150)
            val nextCombo = finalCombo + 1
            val chainScore = Scoring.getStepScore(chainMerge.steps.first().baseScore, nextCombo)

            uiState.update {
                it.copy(
                    combo = nextCombo,
                    grid = it.grid.map { cell ->
                        if (chainMerge.steps.first().mergingCells.any { it.id == cell.id }) cell.copy(
                            x = merge.targetX,
                            y = merge.targetY,
                        ) else cell
                    },
                    pendingMerge = chainMerge.copy(
                        resultId = chainMerge.resultId + "_chain",
                        isPerkAssisted = true,
                        startingCombo = finalCombo
                    ),
                    activeMergeStepIndex = 0,
                    pendingMergeScore = chainScore,
                    cellIdCounter = nextIdCounter
                )
            }
        } else {
            val activePerkBeforeClear = uiState.value.activePerk
            val isChainStep = merge.resultId.endsWith("_chain")
            val oldCombo = uiState.value.combo
            uiState.update { state ->
                val activePerk = state.activePerk
                val shouldConsume = activePerk == Perk.CHAIN_MERGE || 
                                    activePerk == Perk.PATH_MERGE || 
                                    activePerk == Perk.SKIP_SPAWN
                val nextState = if (shouldConsume) state.consumePerk(activePerk) else state
                
                // Only force a reset if we are using CHAIN_MERGE, no chain happened, 
                // AND the merge wouldn't have naturally maintained the combo (uniqueGroups <= 1)
                val isNaturallyMaintained = merge.uniqueGroups > 1 || merge.resultId.contains("path_merge")
                val nextCombo = if (activePerk == Perk.CHAIN_MERGE && !isChainStep && !isNaturallyMaintained) 0 else state.combo

                nextState.copy(activePerk = null, combo = nextCombo)
            }

            if (activePerkBeforeClear == Perk.CHAIN_MERGE && !isChainStep) {
                achievementDelegate.checkComboBroken(oldCombo, 0)
                if (oldCombo > 0) {
                    effectDelegate.addComboBroken()
                }
            }

            if (activePerkBeforeClear == Perk.SKIP_SPAWN) {
                onSpawnRequested(false, true)
            } else {
                onSpawnRequested(true, false)
            }
        }
    }

    private suspend fun processNextStep(
        merge: MergeTransition,
        stepIndex: Int,
        stateAfterStep: List<HexagonCell>,
    ) {
        delay(100)
        val nextStep = merge.steps[stepIndex + 1]

        uiState.update { state ->
            val random = kotlin.random.Random(state.seed)
            val isPathMerge = merge.resultId.contains("path_merge")
            val nextComboValue = Scoring.getNextStepCombo(state.combo, stepIndex + 1, isPathMerge)
            val stepScore = Scoring.getStepScore(nextStep.baseScore, nextComboValue)

            val tierRewards = handleTierRewards(nextComboValue, state, random)
            val newReachedTiers = tierRewards.map { it.first }.toSet()
            val newlyEarnedPerks = tierRewards.map { it.second }
            val newRewardEffects = tierRewards.map { GameEffect.TierReward(it.first, it.second) }

            state.copy(
                combo = nextComboValue,
                reachedComboTiers = state.reachedComboTiers + newReachedTiers,
                earnedRewardsThisTurn = state.earnedRewardsThisTurn + newRewardEffects,
                collectedPerks = state.collectedPerks + newlyEarnedPerks,
                grid = stateAfterStep.map { cell ->
                    if (nextStep.mergingCells.any { it.id == cell.id }) cell.copy(
                        x = merge.targetX,
                        y = merge.targetY,
                    ) else cell
                },
                activeMergeStepIndex = stepIndex + 1,
                pendingMergeScore = state.pendingMergeScore + stepScore,
                seed = random.nextLong()
            )
        }
    }
}
