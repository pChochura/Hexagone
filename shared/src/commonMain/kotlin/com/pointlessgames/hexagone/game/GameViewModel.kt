package com.pointlessgames.hexagone.game

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.achievements.AchievementManager
import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource

internal class GameViewModel(
    private val settingsRepository: SettingsRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val achievementManager: AchievementManager,
) : ViewModel() {

    private val engine = GameEngine()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _hoveredMerge = MutableStateFlow<MergeTransition?>(null)
    val hoveredMerge: StateFlow<MergeTransition?> = _hoveredMerge.asStateFlow()

    private val _effects = MutableSharedFlow<GameEffect>(extraBufferCapacity = 64)
    val effects: SharedFlow<GameEffect> = _effects.asSharedFlow()

    private val stateDelegate = StateDelegate(
        uiState = _uiState,
        settingsRepository = settingsRepository,
        engine = engine,
        scope = viewModelScope,
        onCheckValidMoves = { checkValidMoves() }
    )

    private val effectDelegate = EffectDelegate(
        effects = _effects,
        scope = viewModelScope
    )

    private val achievementDelegate = AchievementDelegate(
        uiState = _uiState,
        achievementManager = achievementManager,
    )

    private val actionDelegate = ActionDelegate(
        uiState = _uiState,
        engine = engine,
        scope = viewModelScope,
        stateDelegate = stateDelegate,
        effectDelegate = effectDelegate,
        achievementDelegate = achievementDelegate,
        onSpawnRequested = { spawnFromQueue(_uiState.value.grid) },
        onCheckValidMoves = { checkValidMoves() },
        onUpdateLevel = { updateLevel() },
        onRecalculateHints = { recalculateHints() },
        onHoveredMergeChanged = { _hoveredMerge.value = it }
    )

    private val mergeDelegate = MergeDelegate(
        uiState = _uiState,
        engine = engine,
        scope = viewModelScope,
        stateDelegate = stateDelegate,
        effectDelegate = effectDelegate,
        achievementDelegate = achievementDelegate,
        onSpawnRequested = { decrementLifespan, skipSpawn -> 
            spawnFromQueue(_uiState.value.grid, decrementLifespan, skipSpawn) 
        }
    )

    private val debugDelegate = DebugDelegate(
        uiState = _uiState,
        engine = engine,
        onStateChanged = {
            recalculateHints()
            checkValidMoves()
            stateDelegate.persistState(stateDelegate.getCurrentGameState())
        },
        onSpawnRequested = {
            spawnFromQueue(_uiState.value.grid)
        },
    )


    init {
        viewModelScope.launch {
            val best = settingsRepository.getBestScore()
            val hintsEnabled = settingsRepository.getMergeHintsEnabled()
            val savedStateJson = settingsRepository.getGameState()

            if (savedStateJson != null) {
                try {
                    val savedState = Json.decodeFromString<GameState>(savedStateJson)
                    _uiState.update {
                        it.copy(
                            grid = savedState.grid,
                            preview = savedState.preview,
                            score = savedState.score,
                            level = savedState.level,
                            highestValue = savedState.highestValue,
                            combo = savedState.combo,
                            collectedPerks = savedState.collectedPerks,
                            maxCombo = savedState.maxCombo,
                            totalMerges = savedState.totalMerges,
                            onBoardPerks = savedState.onBoardPerks,
                            pendingLevelUps = savedState.pendingLevelUps,
                            perkSpawnCounter = savedState.perkSpawnCounter,
                            reachedComboTiers = savedState.reachedComboTiers,
                            perkOptions = savedState.perkOptions,
                            canReroll = savedState.canReroll,
                            bestScore = savedState.sessionBestScore,
                            mergeHintsEnabled = hintsEnabled,
                            isStuck = savedState.isStuck,
                            availableChoices = savedState.availableChoices,
                            perksUsedTracking = savedState.perksUsedTracking,
                            consecutiveUndos = savedState.consecutiveUndos,
                            comboTriggeredInSession = savedState.comboTriggeredInSession,
                            perkUsedInSession = savedState.perkUsedInSession,
                            undoUsedInSession = savedState.undoUsedInSession,
                            ghostPerkUsedInSession = savedState.ghostPerkUsedInSession,
                            seed = savedState.seed,
                            cellIdCounter = savedState.cellIdCounter,
                            previewIdCounter = savedState.previewIdCounter,
                            activePerk = savedState.activePerk,
                            selectedCellId = savedState.selectedCellId,
                        )
                    }
                    stateDelegate.setAbsoluteBestScore(maxOf(best, savedState.score))
                    updateLevel()
                    checkValidMoves()
                } catch (_: Exception) {
                    stateDelegate.setAbsoluteBestScore(best)
                    _uiState.update { it.copy(bestScore = best, mergeHintsEnabled = hintsEnabled) }
                    restartGame()
                }
            } else {
                stateDelegate.setAbsoluteBestScore(best)
                _uiState.update { it.copy(bestScore = best, mergeHintsEnabled = hintsEnabled) }
                restartGame()
            }
            recalculateHints()
        }

        viewModelScope.launch {
            achievementManager.unlockedAchievements.collect { achievement ->
                _effects.emit(GameEffect.AchievementUnlock(achievement))
            }
        }

        viewModelScope.launch {
            _uiState.collect { state ->
                achievementManager.updateSessionData(state)
            }
        }
    }

    fun addParticles(newParticles: List<Particle>) = effectDelegate.addParticles(newParticles)
    
    fun addScorePopup(
        gridX: Int, 
        gridY: Int, 
        score: Int, 
        color: Color, 
        labelRes: StringResource? = null
    ) = effectDelegate.addScorePopup(gridX, gridY, score, color, labelRes)
    
    fun addPerkPopup(gridX: Int, gridY: Int, perk: Perk) = effectDelegate.addPerkPopup(gridX, gridY, perk)

    fun onEmptySpaceClicked(x: Int, y: Int) = actionDelegate.onEmptySpaceClicked(x, y)
    
    fun onEmptySpaceTouchDown(x: Int, y: Int) = actionDelegate.onEmptySpaceTouchDown(x, y)
    
    fun onCellTouchDown(cell: HexagonCell) = actionDelegate.onCellTouchDown(cell)
    
    fun onCellTouchUp() { _hoveredMerge.value = null }
    
    fun onEmptySpaceTouchUp() { _hoveredMerge.value = null }
    
    fun onPreviewClicked(preview: PreviewCell) = actionDelegate.onPreviewClicked(preview)
    
    fun onCellClicked(cell: HexagonCell) = actionDelegate.onCellClicked(cell)
    
    fun onMergeAnimationFinished() = mergeDelegate.onMergeAnimationFinished()

    private fun updateLevel() {
        _uiState.update { state ->
            val random = kotlin.random.Random(state.seed)
            val lvl = engine.calculateLevel(state.score)
            val levelDifference = lvl - state.level
            if (levelDifference > 0) {
                val nextPerkOptions = state.perkOptions.ifEmpty { engine.pickWeightedPerks(3, random) }
                state.copy(
                    level = lvl,
                    levelProgress = engine.getLevelProgress(state.score, lvl),
                    highestValue = state.grid.maxOfOrNull { it.value } ?: 1,
                    perkOptions = nextPerkOptions,
                    pendingLevelUps = state.pendingLevelUps + levelDifference,
                    canReroll = if (state.perkOptions.isEmpty()) true else state.canReroll,
                    seed = random.nextLong()
                )
            } else {
                state.copy(
                    level = lvl,
                    levelProgress = engine.getLevelProgress(state.score, lvl),
                    highestValue = state.grid.maxOfOrNull { it.value } ?: 1,
                )
            }
        }
    }

    fun onUsePerkClicked(perk: Perk) {
        if (_uiState.value.activePerk == perk) {
            _uiState.update { it.copy(activePerk = null, selectedCellId = null) }
            recalculateHints()
            checkValidMoves()
            return
        }

        if (_uiState.value.isStuck && _uiState.value.stuckPerks.contains(perk)) {
            achievementDelegate.onStuckResolvedWithPerk()
        }

        when (perk) {
            Perk.ADVANCE_QUEUE -> {
                stateDelegate.saveState()
                _uiState.update {
                    it.consumePerk(Perk.ADVANCE_QUEUE)
                        .copy(isGameOver = false, activePerk = null, selectedCellId = null)
                }
                spawnFromQueue(_uiState.value.grid, decrementLifespan = false)
            }

            Perk.UNDO -> {
                if (stateDelegate.undoLastMove()) {
                    mergeDelegate.resetLastProcessed()
                    _uiState.update {
                        it.consumePerk(Perk.UNDO)
                            .copy(isGameOver = false, activePerk = null, selectedCellId = null)
                    }
                    achievementDelegate.onUndoUsed()
                    checkValidMoves()
                }
            }

            Perk.SKIP_SPAWN -> {
                _uiState.update {
                    it.copy(
                        isGameOver = false,
                        activePerk = Perk.SKIP_SPAWN,
                        selectedCellId = null,
                    )
                }
            }

            else -> {
                _uiState.update {
                    it.copy(
                        isGameOver = false,
                        activePerk = perk,
                        selectedCellId = null,
                    )
                }
            }
        }
        achievementDelegate.checkArchitectsDream(_uiState.value.grid, getPotentialMerges())
        recalculateHints()
        checkValidMoves()
    }

    private fun recalculateHints() {
        _uiState.update { state ->
            state.copy(
                mergeHints = if (state.mergeHintsEnabled) {
                    engine.findMergeHints(state.grid, state.preview, state.combo, state.activePerk)
                } else emptyList(),
            )
        }
    }

    fun onPerkSelected(perk: Perk) {
        _uiState.update {
            val random = kotlin.random.Random(it.seed)
            val remainingLevelUps = (it.pendingLevelUps - 1).coerceAtLeast(0)
            it.copy(
                collectedPerks = it.collectedPerks + perk,
                perkOptions = if (remainingLevelUps > 0) engine.pickWeightedPerks(3, random) else emptyList(),
                pendingLevelUps = remainingLevelUps,
                canReroll = true,
                seed = random.nextLong()
            )
        }
        achievementDelegate.checkPerkAchievements(perk, _uiState.value)
        achievementDelegate.onNonUndoAction()
        recalculateHints()
        checkValidMoves()
    }

    fun onRerollClicked() {
        val state = _uiState.value
        if (!state.canReroll || state.perkOptions.isEmpty()) return

        if (state.perkOptions.any { it.isLegendary }) {
            achievementDelegate.onRerollLegendary()
        }

        _uiState.update {
            val random = kotlin.random.Random(it.seed)
            it.copy(
                perkOptions = engine.pickWeightedPerks(3, random, excludeLegendary = true),
                canReroll = false,
                seed = random.nextLong()
            )
        }
        achievementDelegate.onRerollUsed()
        checkValidMoves()
    }

    fun onRestartClicked() {
        restartGame()
    }

    private fun restartGame() {
        stateDelegate.clearHistory()
        mergeDelegate.resetLastProcessed()
        val initialSeed = kotlin.random.Random.nextLong()
        val random = kotlin.random.Random(initialSeed)
        val (initialGrid, nextIdCounter) = engine.generateInitialGrid(random)
        val (initialPreviews, nextPreviewIdCounter) = engine.pickRandomPreviews(initialGrid, emptyList(), emptyList(), 3, random, 0)
        _uiState.value = GameUiState(
            grid = initialGrid,
            mergeHints = if (_uiState.value.mergeHintsEnabled) engine.findMergeHints(
                initialGrid,
                initialPreviews,
                0,
                null,
            ) else emptyList(),
            mergeHintsEnabled = _uiState.value.mergeHintsEnabled,
            preview = initialPreviews,
            bestScore = stateDelegate.absoluteBestScore,
            collectedPerks = emptyList(),
            onBoardPerks = emptyList(),
            perkSpawnCounter = 0,
            earnedRewardsThisTurn = emptyList(),
            seed = random.nextLong(),
            cellIdCounter = nextIdCounter,
            previewIdCounter = nextPreviewIdCounter
        )
        updateLevel()
        checkValidMoves()
    }

    private fun checkValidMoves() {
        val state = _uiState.value
        val isPossible = engine.isMovePossible(state.grid)
        val hasPerkOptions = state.perkOptions.isNotEmpty()

        val actionablePerks = state.collectedPerks.filter { perk ->
            engine.canPerkResolveStuck(
                perk = perk,
                grid = state.grid,
                previews = state.preview,
                previousState = stateDelegate.getLastHistoryState(),
            )
        }.toSet()

        _uiState.update {
            if (state.isDebugMode) {
                it.copy(isStuck = false, isGameOver = false, stuckPerks = emptySet())
            } else if (isPossible || hasPerkOptions) {
                it.copy(isStuck = false, isGameOver = false, stuckPerks = emptySet())
            } else if (actionablePerks.isNotEmpty()) {
                it.copy(isStuck = true, isGameOver = false, stuckPerks = actionablePerks)
            } else {
                it.copy(isStuck = false, stuckPerks = emptySet())
            }
        }

        if (!isPossible && hasPerkOptions.not() && actionablePerks.isEmpty() && !state.isDebugMode) {
            viewModelScope.launch {
                delay(1000)
                val finalResult = DetailedGameResult(
                    score = state.score,
                    maxCombo = state.maxCombo,
                    maxPiece = state.highestValue,
                    totalMerges = state.totalMerges,
                    level = state.level,
                    perksUsed = state.perksUsedTracking,
                    perksAvailable = state.collectedPerks,
                    region = settingsRepository.getPlayerRegion() ?: "Global",
                )

                val playerName = settingsRepository.getPlayerName()
                _uiState.update { it.copy(isGameOver = true, pendingResult = if (playerName == null) finalResult else null) }
                achievementDelegate.onGameFinished()
                settingsRepository.setGameState(null)

                if (playerName != null) {
                    val rankInfo = leaderboardRepository.submitResult(finalResult)
                    _uiState.update { it.copy(currentRank = rankInfo) }
                }
            }
        }
        achievementDelegate.checkArchitectsDream(_uiState.value.grid, getPotentialMerges())
        recalculateHints()
        stateDelegate.persistState(stateDelegate.getCurrentGameState())
    }

    private fun spawnFromQueue(
        currentState: List<HexagonCell>,
        decrementLifespan: Boolean = true,
        skipSpawn: Boolean = false,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            val gridWithoutTactical = engine.decrementTacticalFlags(currentState)
            val currentPerks = _uiState.value.onBoardPerks
            val random = kotlin.random.Random(_uiState.value.seed)
            val currentPreviewIdCounter = _uiState.value.previewIdCounter

            val (newState, newPreviewsResult, perksAfterSpawn) = if (skipSpawn) {
                Triple(gridWithoutTactical, _uiState.value.preview to currentPreviewIdCounter, currentPerks)
            } else {
                engine.spawnFromQueue(
                    gridWithoutTactical,
                    _uiState.value.preview,
                    currentPerks,
                    random,
                    currentPreviewIdCounter
                )
            }

            val (newPreviews, nextPreviewIdCounter) = newPreviewsResult

            val updatedPerks = if (decrementLifespan) {
                engine.updateOnBoardPerks(perksAfterSpawn)
            } else {
                perksAfterSpawn
            }

            if (updatedPerks.size < currentPerks.size) {
                achievementDelegate.onPerkMissed()
            }

            val (nextPerks, nextCounter) = engine.trySpawnPerkOnBoard(
                newState,
                newPreviews,
                updatedPerks,
                _uiState.value.perkSpawnCounter,
                random
            )

            val rewardsToEmit = _uiState.value.earnedRewardsThisTurn

            _uiState.update {
                it.copy(
                    grid = newState,
                    preview = newPreviews,
                    onBoardPerks = nextPerks,
                    perkSpawnCounter = nextCounter,
                    earnedRewardsThisTurn = emptyList(),
                    seed = random.nextLong(),
                    previewIdCounter = nextPreviewIdCounter
                )
            }

            rewardsToEmit.forEach { reward ->
                _effects.emit(reward)
            }

            achievementDelegate.onSpawnOccurred()
            updateLevel()
            checkValidMoves()
            _uiState.update { it.copy(isBusy = false) }
        }
    }

    fun getLevelProgress(): Float = _uiState.value.levelProgress

    fun getPotentialMerges() = actionDelegate.getPotentialMerges()

    fun onViewBoardToggled() {
        _uiState.update { it.copy(showGameOverBoard = !it.showGameOverBoard) }
    }

    fun setDebugSelectedValue(value: Int?) = debugDelegate.setDebugSelectedValue(value)
    fun toggleDebugMode() = debugDelegate.toggleDebugMode()
    fun toggleDebugAddAsGhost() = debugDelegate.toggleDebugAddAsGhost()
    fun onDebugCellClicked(x: Int, y: Int) = debugDelegate.onDebugCellClicked(x, y)
    fun addPerkManually(perk: Perk) = debugDelegate.addPerkManually(perk)

    fun getAchievementManager(): AchievementManager = achievementManager
}
