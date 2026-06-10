package com.pointlessgames.hexagone.game

import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.game.model.GameState
import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.logic.GameEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class StateDelegate(
    private val uiState: MutableStateFlow<GameUiState>,
    private val settingsRepository: SettingsRepository,
    private val engine: GameEngine,
    private val scope: CoroutineScope,
    private val onCheckValidMoves: () -> Unit,
) {
    var absoluteBestScore = 0
        private set
    
    private val stateHistory = mutableListOf<GameState>()
    
    val redemptionBaseline: Int?
        get() = uiState.value.redemptionBaseline

    fun setAbsoluteBestScore(score: Int) {
        absoluteBestScore = score
    }
    
    fun setRedemptionBaseline(score: Int?) {
        uiState.update { it.copy(redemptionBaseline = score) }
    }

    fun persistBestScore(score: Int) {
        if (score > absoluteBestScore) {
            absoluteBestScore = score
            scope.launch {
                settingsRepository.setBestScore(score)
            }
        }
    }

    fun saveState() {
        val currentState = getCurrentGameState()
        val choices = engine.countPossibleMoves(currentState.grid) +
                currentState.collectedPerks.count { perk ->
                    engine.canPerkResolveStuck(
                        perk = perk,
                        grid = currentState.grid,
                        previews = currentState.preview,
                        previousState = stateHistory.lastOrNull(),
                    )
                } +
                (if (currentState.perkOptions.isNotEmpty()) 1 else 0)

        val stateToSave = currentState.copy(availableChoices = choices)

        stateHistory.add(stateToSave)
        if (stateHistory.size > 10) stateHistory.removeAt(0)
        persistState(stateToSave)
    }

    fun getCurrentGameState(): GameState {
        val state = uiState.value
        return GameState(
            grid = state.grid,
            preview = state.preview,
            score = state.score,
            level = state.level,
            highestValue = state.highestValue,
            combo = state.combo,
            collectedPerks = state.collectedPerks,
            maxCombo = state.maxCombo,
            totalMerges = state.totalMerges,
            onBoardPerks = state.onBoardPerks,
            pendingLevelUps = state.pendingLevelUps,
            perkSpawnCounter = state.perkSpawnCounter,
            reachedComboTiers = state.reachedComboTiers,
            perkOptions = state.perkOptions,
            canReroll = state.canReroll,
            sessionBestScore = state.sessionBestScore,
            isStuck = state.isStuck,
            availableChoices = state.availableChoices,
            perksUsedTracking = state.perksUsedTracking,
            consecutiveUndos = state.consecutiveUndos,
            consecutiveMergesWithoutSpawn = state.consecutiveMergesWithoutSpawn,
            tacticalMergesCount = state.tacticalMergesCount,
            comboTriggeredInSession = state.comboTriggeredInSession,
            perkUsedInSession = state.perkUsedInSession,
            undoUsedInSession = state.undoUsedInSession,
            ghostPerkUsedInSession = state.ghostPerkUsedInSession,
            redemptionBaseline = state.redemptionBaseline,
            seed = state.seed,
            cellIdCounter = state.cellIdCounter,
            previewIdCounter = state.previewIdCounter,
            activePerk = state.activePerk,
            selectedCellId = state.selectedCellId,
            dailyChallenges = state.dailyChallenges,
        )
    }

    fun persistState(state: GameState) {
        scope.launch {
            settingsRepository.setGameState(Json.encodeToString(state))
        }
    }

    fun undoLastMove(): Boolean {
        if (stateHistory.isEmpty()) return false
        val currentState = uiState.value
        val previousState = stateHistory.removeAt(stateHistory.size - 1)

        val undoneMoveScore = currentState.score - previousState.score

        // When undoing, we must ensure that all remaining states in history 
        // also reflect that one UNDO perk has been consumed.
        // Otherwise, restoring them later would bring back the original UNDO count, 
        // leading to infinite undos.
        stateHistory.forEachIndexed { index, state ->
            val undos = state.collectedPerks.toMutableList()
            val undoIndex = undos.indexOf(Perk.UNDO)
            if (undoIndex != -1) {
                undos.removeAt(undoIndex)
                stateHistory[index] = state.copy(collectedPerks = undos)
            }
        }

        uiState.update { state ->
            state.copy(
                grid = previousState.grid,
                preview = previousState.preview,
                score = previousState.score,
                level = previousState.level,
                highestValue = previousState.highestValue,
                combo = previousState.combo,
                collectedPerks = previousState.collectedPerks,
                maxCombo = maxOf(currentState.maxCombo, previousState.maxCombo),
                totalMerges = previousState.totalMerges,
                onBoardPerks = previousState.onBoardPerks,
                reachedComboTiers = previousState.reachedComboTiers,
                pendingLevelUps = previousState.pendingLevelUps,
                perkOptions = previousState.perkOptions,
                canReroll = previousState.canReroll,
                perkSpawnCounter = previousState.perkSpawnCounter,
                perksUsedTracking = currentState.perksUsedTracking,
                consecutiveUndos = currentState.consecutiveUndos,
                consecutiveMergesWithoutSpawn = previousState.consecutiveMergesWithoutSpawn,
                tacticalMergesCount = previousState.tacticalMergesCount,
                comboTriggeredInSession = currentState.comboTriggeredInSession || previousState.comboTriggeredInSession,
                perkUsedInSession = currentState.perkUsedInSession || previousState.perkUsedInSession,
                undoUsedInSession = currentState.undoUsedInSession || previousState.undoUsedInSession,
                ghostPerkUsedInSession = currentState.ghostPerkUsedInSession || previousState.ghostPerkUsedInSession,
                redemptionBaseline = undoneMoveScore,
                seed = previousState.seed,
                cellIdCounter = previousState.cellIdCounter,
                previewIdCounter = previousState.previewIdCounter,
                earnedRewardsThisTurn = emptyList(),
                pendingMerge = null,
                activePerk = null,
                selectedCellId = null,
                isBusy = false,
                dailyChallenges = previousState.dailyChallenges,
            )
        }

        onCheckValidMoves()
        return true
    }
    
    fun clearHistory() {
        stateHistory.clear()
    }
    
    fun getLastHistoryState() = stateHistory.lastOrNull()
}
