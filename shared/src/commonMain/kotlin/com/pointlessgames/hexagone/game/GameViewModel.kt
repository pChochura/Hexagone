package com.pointlessgames.hexagone.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.achievements.AchievementManager
import com.pointlessgames.hexagone.billing.BillingManager
import com.pointlessgames.hexagone.billing.BillingProduct
import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.data.MonetizationRepository
import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.game.logic.DailyChallengeProvider
import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.model.DailyChallenge
import com.pointlessgames.hexagone.game.model.DailyChallengeProgress
import com.pointlessgames.hexagone.game.model.DetailedGameResult
import com.pointlessgames.hexagone.game.model.GameEffect
import com.pointlessgames.hexagone.game.model.GameState
import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Perk
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.tip_daily_message
import hexagone.shared.generated.resources.tip_merge_message
import hexagone.shared.generated.resources.tip_perk_message
import hexagone.shared.generated.resources.tip_post_game_message
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class GameViewModel(
    private val settingsRepository: SettingsRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val monetizationRepository: MonetizationRepository,
    private val achievementManager: AchievementManager,
    private val billingManager: BillingManager,
) : ViewModel() {

    private val engine = GameEngine()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _storeProducts = MutableStateFlow<List<BillingProduct>>(emptyList())
    val storeProducts: StateFlow<List<BillingProduct>> = _storeProducts.asStateFlow()

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

    private val challengeDelegate = ChallengeDelegate(
        uiState = _uiState,
        onChallengeComplete = { challenge ->
            viewModelScope.launch {
                handleChallengeComplete(challenge)
            }
        }
    )

    private val actionDelegate = ActionDelegate(
        uiState = _uiState,
        engine = engine,
        stateDelegate = stateDelegate,
        effectDelegate = effectDelegate,
        achievementDelegate = achievementDelegate,
        challengeDelegate = challengeDelegate,
        onSpawnRequested = { spawnFromQueue(_uiState.value.grid) },
        onCheckValidMoves = { checkValidMoves() },
        onUpdateLevel = { updateLevel() },
        onHoveredMergeChanged = { _hoveredMerge.value = it }
    )

    private val mergeDelegate = MergeDelegate(
        uiState = _uiState,
        engine = engine,
        scope = viewModelScope,
        stateDelegate = stateDelegate,
        effectDelegate = effectDelegate,
        achievementDelegate = achievementDelegate,
        challengeDelegate = challengeDelegate,
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

    fun onDismissTip() {
        val currentTip = _uiState.value.activeTip ?: return
        viewModelScope.launch {
            when (currentTip.id) {
                com.pointlessgames.hexagone.game.model.TipId.MERGE -> settingsRepository.setHasShownMergeTip(true)
                com.pointlessgames.hexagone.game.model.TipId.PERK -> settingsRepository.setHasShownPerkTip(true)
                com.pointlessgames.hexagone.game.model.TipId.POST_GAME -> settingsRepository.setHasShownPostGameTip(true)
                com.pointlessgames.hexagone.game.model.TipId.DAILY -> settingsRepository.setHasShownDailyChallengeTip(true)
            }
            _uiState.update { it.copy(activeTip = null) }
        }
    }

    private fun triggerTip(id: com.pointlessgames.hexagone.game.model.TipId) {
        val tipData = when (id) {
            com.pointlessgames.hexagone.game.model.TipId.MERGE -> 
                Res.string.tip_merge_message to com.pointlessgames.hexagone.game.model.TipTarget.GRID
            com.pointlessgames.hexagone.game.model.TipId.PERK -> 
                Res.string.tip_perk_message to com.pointlessgames.hexagone.game.model.TipTarget.PERK_BAR
            com.pointlessgames.hexagone.game.model.TipId.POST_GAME -> 
                Res.string.tip_post_game_message to com.pointlessgames.hexagone.game.model.TipTarget.GAME_OVER_BUTTONS
            com.pointlessgames.hexagone.game.model.TipId.DAILY -> 
                Res.string.tip_daily_message to com.pointlessgames.hexagone.game.model.TipTarget.SCORE_SECTION
        }
        _uiState.update { it.copy(activeTip = com.pointlessgames.hexagone.game.model.GameTip(id, tipData.first, tipData.second)) }
    }


    init {
        viewModelScope.launch {
            val best = settingsRepository.getBestScore()
            val hintsEnabled = settingsRepository.getMergeHintsEnabled()
            val savedStateJson = settingsRepository.getGameState()

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val dateSeed = today.year * 10000L + (today.month.ordinal + 1) * 100L + today.day
            val yesterday = today.minus(1, DateTimeUnit.DAY)
            val yesterdaySeed = yesterday.year * 10000L + (yesterday.month.ordinal + 1) * 100L + yesterday.day

            val lastCompletedDate = settingsRepository.getLastCompletedChallengeDate()
            var challengeStreak = settingsRepository.getChallengeStreak()
            val completedDates = settingsRepository.getCompletedChallengeDates().mapNotNull { it.toLongOrNull() }.toSet()

            if (lastCompletedDate != 0L && lastCompletedDate != dateSeed && lastCompletedDate != yesterdaySeed) {
                challengeStreak = 0
                settingsRepository.setChallengeStreak(0)
            }

            val currentDailyChallenges = DailyChallengeProvider.getChallengesForDate(today, challengeStreak)

            if (savedStateJson != null) {
                try {
                    val savedState = Json.decodeFromString<GameState>(savedStateJson)
                    val isGameStarted = savedState.totalMerges > 0
                    val isSameDayChallenges = savedState.dailyChallenges.map { it.challenge } == currentDailyChallenges

                    if (!isGameStarted && !isSameDayChallenges) {
                        stateDelegate.setAbsoluteBestScore(maxOf(best, savedState.score))
                        _uiState.update {
                            it.copy(
                                challengeStreak = challengeStreak,
                                isStreakCollectedToday = lastCompletedDate == dateSeed,
                                completedChallengeDates = completedDates,
                                mergeHintsEnabled = hintsEnabled,
                            )
                        }
                        restartGame()
                    } else {
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
                                bestScore = maxOf(best, savedState.score),
                                sessionBestScore = savedState.sessionBestScore,
                                mergeHintsEnabled = hintsEnabled,
                                isStuck = savedState.isStuck,
                                availableChoices = savedState.availableChoices,
                                perksUsedTracking = savedState.perksUsedTracking,
                                consecutiveUndos = savedState.consecutiveUndos,
                                comboTriggeredInSession = savedState.comboTriggeredInSession,
                                perkUsedInSession = savedState.perkUsedInSession,
                                undoUsedInSession = savedState.undoUsedInSession,
                                ghostPerkUsedInSession = savedState.ghostPerkUsedInSession,
                                debugUsed = savedState.debugUsed,
                                seed = savedState.seed,
                                cellIdCounter = savedState.cellIdCounter,
                                previewIdCounter = savedState.previewIdCounter,
                                activePerk = savedState.activePerk,
                                selectedCellId = savedState.selectedCellId,
                                dailyChallenges = if (savedState.dailyChallenges.isNotEmpty()) {
                                    savedState.dailyChallenges
                                } else {
                                    currentDailyChallenges.map { challenge -> DailyChallengeProgress(challenge) }
                                },
                                completedChallengeDates = if (savedState.completedChallengeDates.isNotEmpty()) {
                                    savedState.completedChallengeDates
                                } else {
                                    completedDates
                                },
                                challengeStreak = challengeStreak,
                                isStreakCollectedToday = lastCompletedDate == dateSeed
                            )
                        }
                        stateDelegate.setAbsoluteBestScore(maxOf(best, savedState.score))
                        updateLevel()
                        checkValidMoves()
                    }
                } catch (_: Exception) {
                    stateDelegate.setAbsoluteBestScore(best)
                    _uiState.update { 
                        it.copy(
                            bestScore = best, 
                            sessionBestScore = best,
                            mergeHintsEnabled = hintsEnabled,
                            dailyChallenges = currentDailyChallenges.map { c -> DailyChallengeProgress(c) },
                            completedChallengeDates = completedDates,
                            challengeStreak = challengeStreak,
                            isStreakCollectedToday = lastCompletedDate == dateSeed
                        ) 
                    }
                    restartGame()
                }
            } else {
                stateDelegate.setAbsoluteBestScore(best)
                _uiState.update { 
                    it.copy(
                        bestScore = best, 
                        sessionBestScore = best,
                        mergeHintsEnabled = hintsEnabled,
                        dailyChallenges = currentDailyChallenges.map { c -> DailyChallengeProgress(c) },
                        completedChallengeDates = completedDates,
                        challengeStreak = challengeStreak,
                        isStreakCollectedToday = lastCompletedDate == dateSeed
                    ) 
                }
                restartGame()
            }
            recalculateHints()

            if (!settingsRepository.getHasShownMergeTip() && _uiState.value.totalMerges == 0) {
                triggerTip(com.pointlessgames.hexagone.game.model.TipId.MERGE)
            } else if (!settingsRepository.getHasShownDailyChallengeTip()) {
                triggerTip(com.pointlessgames.hexagone.game.model.TipId.DAILY)
            }
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

        viewModelScope.launch {
            billingManager.initialize()
            launch {
                billingManager.products.collect { products ->
                    _storeProducts.value = products
                }
            }
            launch {
                billingManager.currencyBalances.collect { balances ->
                    val vouchers = mapOf(
                        PerkCategory.COMMON to (balances["VCMN"] ?: 0),
                        PerkCategory.RARE to (balances["VRARE"] ?: 0),
                        PerkCategory.LEGENDARY to (balances["VLGD"] ?: 0)
                    )
                    _uiState.update { 
                        it.copy(
                            diamonds = balances["diamonds"] ?: 0,
                            vouchers = vouchers
                        ) 
                    }
                }
            }
            launch {
                billingManager.isInitializing.collect { loading ->
                    _uiState.update { it.copy(isShopLoading = loading) }
                }
            }
        }
    }

    fun onEmptySpaceClicked(x: Int, y: Int) = actionDelegate.onEmptySpaceClicked(x, y)
    
    fun onEmptySpaceTouchDown(x: Int, y: Int) = actionDelegate.onEmptySpaceTouchDown(x, y)
    
    fun onCellTouchDown(cell: HexagonCell) = actionDelegate.onCellTouchDown(cell)
    
    fun onCellTouchUp() { _hoveredMerge.value = null }
    
    fun onEmptySpaceTouchUp() { _hoveredMerge.value = null }
    
    fun onCellClicked(cell: HexagonCell) = actionDelegate.onCellClicked(cell)
    
    fun onMergeAnimationFinished() = mergeDelegate.onMergeAnimationFinished()

    private fun updateLevel() {
        _uiState.update { state ->
            val random = kotlin.random.Random(state.seed)
            val lvl = engine.calculateLevel(state.score)
            val levelDifference = lvl - state.level
            if (levelDifference > 0) {
                challengeDelegate.onLevelUp(lvl)
                val nextPerkOptions = state.perkOptions.ifEmpty { engine.pickWeightedPerks(3, random) }
                state.copy(
                    level = lvl,
                    levelProgress = engine.getLevelProgress(state.score, lvl),
                    highestValue = state.grid.filter { !it.isMimic }.maxOfOrNull { it.value } ?: 1,
                    perkOptions = nextPerkOptions,
                    pendingLevelUps = state.pendingLevelUps + levelDifference,
                    canReroll = if (state.perkOptions.isEmpty()) true else state.canReroll,
                    seed = random.nextLong()
                )
            } else {
                state.copy(
                    level = lvl,
                    levelProgress = engine.getLevelProgress(state.score, lvl),
                    highestValue = state.grid.filter { !it.isMimic }.maxOfOrNull { it.value } ?: 1,
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
                perkOptions = if (remainingLevelUps > 0) engine.pickWeightedPerks(
                    3,
                    random,
                ) else emptyList(),
                pendingLevelUps = remainingLevelUps,
                canReroll = true,
                seed = random.nextLong(),
            )
        }
        viewModelScope.launch {
            if (!settingsRepository.getHasShownPerkTip()) {
                triggerTip(com.pointlessgames.hexagone.game.model.TipId.PERK)
            }
        }
        achievementDelegate.checkPerkAchievements(perk, _uiState.value)
        achievementDelegate.onNonUndoAction()
        recalculateHints()
        checkValidMoves()
    }

    fun onRerollClicked() {
        val state = _uiState.value
        if (!state.canReroll || state.perkOptions.isEmpty()) return

        challengeDelegate.onReroll(state.perkOptions)

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
        
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val currentDailyChallenges = DailyChallengeProvider.getChallengesForDate(today, _uiState.value.challengeStreak)

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
            sessionBestScore = stateDelegate.absoluteBestScore,
            collectedPerks = emptyList(),
            onBoardPerks = emptyList(),
            perkSpawnCounter = 0,
            earnedRewardsThisTurn = emptyList(),
            seed = random.nextLong(),
            cellIdCounter = nextIdCounter,
            previewIdCounter = nextPreviewIdCounter,
            dailyChallenges = currentDailyChallenges.map { DailyChallengeProgress(it) },
            completedChallengeDates = _uiState.value.completedChallengeDates,
            challengeStreak = _uiState.value.challengeStreak,
            isStreakCollectedToday = _uiState.value.isStreakCollectedToday,
            debugUsed = false,
            finalResult = null
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
                    dailyChallenges = state.dailyChallenges,
                    debugUsed = state.debugUsed,
                )

                val playerName = settingsRepository.getPlayerName()
                _uiState.update { 
                    it.copy(
                        isGameOver = true, 
                        pendingResult = if (playerName == null && !state.debugUsed) finalResult else null,
                        finalResult = finalResult
                    ) 
                }
                achievementDelegate.onGameFinished()
                settingsRepository.setGameState(null)

                if (playerName != null && !state.debugUsed) {
                    val rankInfo = leaderboardRepository.submitResult(finalResult)
                    _uiState.update { it.copy(currentRank = rankInfo) }
                }

                if (!settingsRepository.getHasShownPostGameTip()) {
                    triggerTip(com.pointlessgames.hexagone.game.model.TipId.POST_GAME)
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

            val thawedIds = currentState.filter { it.isFrozen }.map { it.id }.toSet()
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
                    previewIdCounter = nextPreviewIdCounter,
                    thawedIds = thawedIds
                )
            }

            rewardsToEmit.forEach { reward ->
                _effects.emit(reward)
            }

            challengeDelegate.onSpawnOccurred(_uiState.value.combo)
            challengeDelegate.checkBoardState(engine)
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

    fun onShopClicked() {
        _uiState.update { it.copy(isShopVisible = true) }
    }

    fun onDismissShop() {
        _uiState.update { it.copy(isShopVisible = false) }
    }

    fun onBuyPerk(category: PerkCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(isShopProcessing = true) }
            monetizationRepository.buyPerkVoucher(category)
            _uiState.update { it.copy(isShopProcessing = false) }
        }
    }

    fun onUseVoucher(category: PerkCategory) {
        _uiState.update { it.copy(activeVoucherSelection = category) }
    }

    fun onDismissVoucherSelection() {
        _uiState.update { it.copy(activeVoucherSelection = null) }
    }

    fun onPerkFromVoucherSelected(perk: Perk, category: PerkCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(isVoucherProcessing = true) }
            if (monetizationRepository.usePerkVoucher(category)) {
                _uiState.update { 
                    it.copy(
                        collectedPerks = it.collectedPerks + perk,
                        activeVoucherSelection = null,
                        isVoucherProcessing = false
                    ) 
                }
            } else {
                _uiState.update { it.copy(isVoucherProcessing = false) }
            }
        }
    }

    fun onBuyPremiumProduct(product: BillingProduct) {
        viewModelScope.launch {
            _uiState.update { it.copy(isShopProcessing = true) }
            billingManager.purchase(product)
            _uiState.update { it.copy(isShopProcessing = false) }
        }
    }

    fun onReviveWithPerk(category: PerkCategory) {
        onUseVoucher(category)
    }

    private suspend fun handleChallengeComplete(challenge: DailyChallenge) {
        if (challenge.rewardScore > 0) {
            _uiState.update { 
                val newScore = it.score + challenge.rewardScore
                it.copy(score = newScore, bestScore = maxOf(it.bestScore, newScore)) 
            }
        }
        if (challenge.rewardPerk != null) {
            _uiState.update { it.copy(collectedPerks = it.collectedPerks + challenge.rewardPerk) }
        }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val dateSeed = today.year * 10000L + (today.month.ordinal + 1) * 100L + today.day

        // Check if ALL daily challenges are now completed in the CURRENT GAME to update streak
        val allCompleted = _uiState.value.dailyChallenges.all { it.isCompleted }
        if (allCompleted && !_uiState.value.isStreakCollectedToday) {
            val lastDate = settingsRepository.getLastCompletedChallengeDate()
            if (lastDate != dateSeed) {
                val currentStreak = settingsRepository.getChallengeStreak()
                val yesterday = today.minus(1, DateTimeUnit.DAY)
                val yesterdaySeed = yesterday.year * 10000L + (yesterday.month.ordinal + 1) * 100L + yesterday.day

                val newStreak = if (lastDate == yesterdaySeed) currentStreak + 1 else 1

                settingsRepository.setLastCompletedChallengeDate(dateSeed)
                settingsRepository.setChallengeStreak(newStreak)
                settingsRepository.addCompletedChallengeDate(dateSeed.toString())

                val reward = com.pointlessgames.hexagone.game.logic.StreakMilestones.getRewardForStreak(newStreak)
                if (reward != null) {
                    monetizationRepository.awardStreakRewards(reward)
                }

                _uiState.update { 
                    it.copy(
                        challengeStreak = newStreak,
                        completedChallengeDates = it.completedChallengeDates + dateSeed,
                        isStreakCollectedToday = true,
                    )
                }
            }
        }

        _effects.emit(GameEffect.DailyChallengeComplete(challenge))
    }
}
