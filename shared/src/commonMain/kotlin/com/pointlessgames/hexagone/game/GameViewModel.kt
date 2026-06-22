package com.pointlessgames.hexagone.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.achievements.AchievementManager
import com.pointlessgames.hexagone.billing.BillingManager
import com.pointlessgames.hexagone.billing.BillingProduct
import com.pointlessgames.hexagone.billing.PurchaseResult.Error
import com.pointlessgames.hexagone.billing.PurchaseResult.Success
import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.data.MonetizationRepository
import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.game.logic.DailyChallengeProvider
import com.pointlessgames.hexagone.game.logic.DailyMissionUtils
import com.pointlessgames.hexagone.game.logic.GameEngine
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.model.DailyChallenge
import com.pointlessgames.hexagone.game.model.DailyChallengeProgress
import com.pointlessgames.hexagone.game.model.DetailedGameResult
import com.pointlessgames.hexagone.game.model.GameEffect
import com.pointlessgames.hexagone.game.model.GameState
import com.pointlessgames.hexagone.game.model.GameTip
import com.pointlessgames.hexagone.game.model.GameUiState
import com.pointlessgames.hexagone.game.model.HexDialogState
import com.pointlessgames.hexagone.game.model.HexDialogState.Confirmation
import com.pointlessgames.hexagone.game.model.HexDialogState.Info
import com.pointlessgames.hexagone.game.model.HexDialogState.PauseMenu
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.MissionRefreshState
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.TipId
import com.pointlessgames.hexagone.game.model.TipId.DAILY
import com.pointlessgames.hexagone.game.model.TipId.MERGE
import com.pointlessgames.hexagone.game.model.TipId.PERK
import com.pointlessgames.hexagone.game.model.TipId.POST_GAME
import com.pointlessgames.hexagone.game.model.TipTarget.GAME_OVER_BUTTONS
import com.pointlessgames.hexagone.game.model.TipTarget.GRID
import com.pointlessgames.hexagone.game.model.TipTarget.PERK_BAR
import com.pointlessgames.hexagone.game.model.TipTarget.SCORE_SECTION
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.daily_login_reward_message
import hexagone.shared.generated.resources.daily_login_reward_title
import hexagone.shared.generated.resources.onboarding_nickname_empty
import hexagone.shared.generated.resources.onboarding_nickname_error
import hexagone.shared.generated.resources.shop_buy_confirmation_message
import hexagone.shared.generated.resources.shop_buy_confirmation_title
import hexagone.shared.generated.resources.shop_buy_failure_message
import hexagone.shared.generated.resources.shop_buy_failure_title
import hexagone.shared.generated.resources.shop_buy_success_message
import hexagone.shared.generated.resources.shop_buy_success_title
import hexagone.shared.generated.resources.shop_common_bundle
import hexagone.shared.generated.resources.shop_legendary_bundle
import hexagone.shared.generated.resources.shop_rare_bundle
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

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

    private val inFlightActions = MutableStateFlow(0)

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
        onCheckValidMoves = { checkValidMoves() },
    )

    private val effectDelegate = EffectDelegate(
        effects = _effects,
        scope = viewModelScope,
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
        },
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
        onHoveredMergeChanged = { _hoveredMerge.value = it },
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
        },
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
                MERGE -> settingsRepository.setHasShownMergeTip(true)
                PERK -> settingsRepository.setHasShownPerkTip(true)
                POST_GAME -> settingsRepository.setHasShownPostGameTip(true)
                DAILY -> settingsRepository.setHasShownDailyChallengeTip(true)
            }
            _uiState.update { it.copy(activeTip = null) }
        }
    }

    private fun triggerTip(id: TipId) {
        val tipData = when (id) {
            MERGE -> Res.string.tip_merge_message to GRID
            PERK -> Res.string.tip_perk_message to PERK_BAR
            POST_GAME -> Res.string.tip_post_game_message to GAME_OVER_BUTTONS
            DAILY -> Res.string.tip_daily_message to SCORE_SECTION
        }
        _uiState.update {
            it.copy(activeTip = GameTip(id, tipData.first, tipData.second))
        }
    }


    init {
        viewModelScope.launch {
            settingsRepository.getSoundEnabledFlow().collect { soundEnabled ->
                _uiState.update { it.copy(isSoundEnabled = soundEnabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getHapticsEnabledFlow().collect { hapticsEnabled ->
                _uiState.update { it.copy(isHapticsEnabled = hapticsEnabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.getPlayerNameFlow().collect { name ->
                _uiState.update { it.copy(playerName = name) }
            }
        }
        viewModelScope.launch {
            val best = settingsRepository.getBestScore()
            val hintsEnabled = settingsRepository.getMergeHintsEnabled()
            val savedStateJson = settingsRepository.getGameState()

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val dateSeed = today.year * 10000L + (today.month.ordinal + 1) * 100L + today.day
            val yesterday = today.minus(1, DateTimeUnit.DAY)
            val yesterdaySeed =
                yesterday.year * 10000L + (yesterday.month.ordinal + 1) * 100L + yesterday.day

            val lastCompletedDate = settingsRepository.getLastCompletedChallengeDate()
            val completedDates =
                settingsRepository.getCompletedChallengeDates().mapNotNull { it.toLongOrNull() }
                    .toSet()
            var persistentCompletedMissionIds =
                settingsRepository.getPersistentCompletedMissionIds()
            val dailyMissionDate = settingsRepository.getDailyMissionDate()
            val dailyLoginDateSeed = settingsRepository.getDailyLoginDateSeed()
            var missionRefreshState: MissionRefreshState = MissionRefreshState.NONE

            var isDailyLoginClaimed = false
            var activeDialog: HexDialogState? = null
            var initialDiamonds = 0

            if (dateSeed > dailyLoginDateSeed) {
                isDailyLoginClaimed = true
                settingsRepository.setDailyLoginDateSeed(dateSeed)
                
                // Award 1 diamond directly via monetizationRepository
                inFlightActions.update { it + 1 }
                monetizationRepository.awardStreakRewards(
                    com.pointlessgames.hexagone.game.logic.StreakReward(diamonds = 1)
                )
                inFlightActions.update { it - 1 }
                
                // Set up the dialog to show
                activeDialog = Info(
                    title = Res.string.daily_login_reward_title,
                    message = Res.string.daily_login_reward_message,
                )
            }

            var currentDailyMissionDate = dailyMissionDate

            if (dailyMissionDate != 0L && dailyMissionDate != dateSeed) {
                if (dailyMissionDate == yesterdaySeed) {
                    if (completedDates.contains(dailyMissionDate)) {
                        missionRefreshState = MissionRefreshState.MISSIONS_COMPLETED_REFRESH(dailyMissionDate)
                        settingsRepository.setDailyMissionDate(dateSeed)
                        currentDailyMissionDate = dateSeed
                        settingsRepository.clearPersistentCompletedMissionIds()
                        persistentCompletedMissionIds = emptySet()
                    } else {
                        missionRefreshState = MissionRefreshState.CAN_KEEP(dailyMissionDate)
                    }
                } else {
                    missionRefreshState = MissionRefreshState.HARD_REFRESH(dailyMissionDate)
                    settingsRepository.setDailyMissionDate(dateSeed)
                    currentDailyMissionDate = dateSeed
                    settingsRepository.clearPersistentCompletedMissionIds()
                    persistentCompletedMissionIds = emptySet()
                }
            } else if (dailyMissionDate == 0L) {
                settingsRepository.setDailyMissionDate(dateSeed)
                currentDailyMissionDate = dateSeed
            }

            val effectiveMissionDate = if (missionRefreshState is MissionRefreshState.CAN_KEEP) {
                yesterday
            } else {
                today
            }

            var challengeStreak = DailyMissionUtils.calculateStreak(completedDates, effectiveMissionDate)

            val currentDailyChallenges =
                DailyChallengeProvider.getChallengesForDate(effectiveMissionDate, challengeStreak)

            if (savedStateJson != null) {
                try {
                    val savedState = Json.decodeFromString<GameState>(savedStateJson)
                    val isGameStarted = savedState.totalMerges > 0
                    val isSameDayChallenges =
                        savedState.dailyChallenges.map { it.challenge } == currentDailyChallenges

                    if (!isGameStarted && !isSameDayChallenges) {
                        stateDelegate.setAbsoluteBestScore(maxOf(best, savedState.score))
                        _uiState.update {
                            it.copy(
                                challengeStreak = challengeStreak,
                                isStreakCollectedToday = lastCompletedDate == dateSeed,
                                persistentCompletedMissionIds = persistentCompletedMissionIds,
                                dailyMissionDate = currentDailyMissionDate,
                                completedChallengeDates = completedDates,
                                mergeHintsEnabled = hintsEnabled,
                                diamonds = savedState.diamonds,
                                vouchers = savedState.vouchers,
                                missionRefreshState = missionRefreshState,
                                isDailyLoginClaimed = isDailyLoginClaimed,
                                activeDialog = activeDialog ?: it.activeDialog,
                            )
                        }
                        restartGame()
                    } else {
                        _uiState.update {
                            it.copy(
                                grid = savedState.grid,
                                preview = savedState.preview,
                                score = savedState.score,
                                diamonds = savedState.diamonds,
                                vouchers = savedState.vouchers,
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
                                dailyChallenges = if (savedState.dailyChallenges.isNotEmpty() && (missionRefreshState is MissionRefreshState.CAN_KEEP || isSameDayChallenges)) {
                                    savedState.dailyChallenges
                                } else {
                                    currentDailyChallenges.map { challenge ->
                                        DailyChallengeProgress(
                                            challenge,
                                        )
                                    }
                                },
                                completedChallengeDates = if (savedState.completedChallengeDates.isNotEmpty()) {
                                    savedState.completedChallengeDates
                                } else {
                                    completedDates
                                },
                                persistentCompletedMissionIds = persistentCompletedMissionIds,
                                dailyMissionDate = currentDailyMissionDate,
                                challengeStreak = challengeStreak,
                                isStreakCollectedToday = lastCompletedDate == dateSeed,
                                hasRevived = savedState.hasRevived,
                                missionRefreshState = missionRefreshState,
                                isDailyLoginClaimed = isDailyLoginClaimed,
                                activeDialog = activeDialog ?: it.activeDialog,
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
                            dailyChallenges = currentDailyChallenges.map { c ->
                                DailyChallengeProgress(c)
                            },
                            completedChallengeDates = completedDates,
                            challengeStreak = challengeStreak,
                            isStreakCollectedToday = lastCompletedDate == dateSeed,
                            missionRefreshState = missionRefreshState,
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
                        isStreakCollectedToday = lastCompletedDate == dateSeed,
                        persistentCompletedMissionIds = persistentCompletedMissionIds,
                        dailyMissionDate = currentDailyMissionDate,
                        missionRefreshState = missionRefreshState,
                        isDailyLoginClaimed = isDailyLoginClaimed,
                        activeDialog = activeDialog ?: it.activeDialog,
                    )
                }
                restartGame()
            }
            recalculateHints()

            if (!settingsRepository.getHasShownMergeTip() && _uiState.value.totalMerges == 0) {
                triggerTip(MERGE)
            } else if (!settingsRepository.getHasShownDailyChallengeTip()) {
                triggerTip(DAILY)
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
                combine(
                    billingManager.currencyBalances,
                    inFlightActions,
                ) { balances, inFlightCount ->
                    if (inFlightCount == 0) balances else null
                }.collect { balances ->
                    if (balances == null) return@collect

                    _uiState.update {
                        val vouchers = mapOf(
                            PerkCategory.COMMON to (balances["VCMN"] ?: it.vouchers[PerkCategory.COMMON] ?: 0),
                            PerkCategory.RARE to (balances["VRARE"] ?: it.vouchers[PerkCategory.RARE] ?: 0),
                            PerkCategory.LEGENDARY to (balances["VLGD"] ?: it.vouchers[PerkCategory.LEGENDARY] ?: 0),
                        )
                        it.copy(
                            diamonds = balances["diamonds"] ?: it.diamonds,
                            vouchers = vouchers,
                        )
                    }
                }
            }
            launch {
                billingManager.isInitializing.collect { loading ->
                    _uiState.update { it.copy(isShopLoading = loading) }
                }
            }
            launch {
                billingManager.purchaseEvents.collect { result ->
                    when (result) {
                        is Success -> {
                            _uiState.update {
                                it.copy(
                                    activeDialog = Info(
                                        title = Res.string.shop_buy_success_title,
                                        message = Res.string.shop_buy_success_message,
                                    ),
                                )
                            }
                        }

                        is Error -> {
                            _uiState.update {
                                it.copy(
                                    activeDialog = Info(
                                        title = Res.string.shop_buy_failure_title,
                                        message = Res.string.shop_buy_failure_message,
                                        isError = true,
                                    ),
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    fun onEmptySpaceClicked(x: Int, y: Int) = actionDelegate.onEmptySpaceClicked(x, y)

    fun onEmptySpaceTouchDown(x: Int, y: Int) = actionDelegate.onEmptySpaceTouchDown(x, y)

    fun onCellTouchDown(cell: HexagonCell) = actionDelegate.onCellTouchDown(cell)

    fun onCellTouchUp() {
        _hoveredMerge.value = null
    }

    fun onEmptySpaceTouchUp() {
        _hoveredMerge.value = null
    }

    fun onCellClicked(cell: HexagonCell) = actionDelegate.onCellClicked(cell)

    fun onMergeAnimationFinished() = mergeDelegate.onMergeAnimationFinished()

    private fun updateLevel() {
        _uiState.update { state ->
            val random = kotlin.random.Random(state.seed)
            val lvl = engine.calculateLevel(state.score)
            val levelDifference = lvl - state.level
            if (levelDifference > 0) {
                challengeDelegate.onLevelUp(lvl)
                val nextPerkOptions =
                    state.perkOptions.ifEmpty { engine.pickWeightedPerks(3, random) }
                state.copy(
                    level = lvl,
                    levelProgress = engine.getLevelProgress(state.score, lvl),
                    highestValue = state.grid.filter { !it.isMimic }.maxOfOrNull { it.value } ?: 1,
                    perkOptions = nextPerkOptions,
                    pendingLevelUps = state.pendingLevelUps + levelDifference,
                    canReroll = if (state.perkOptions.isEmpty()) true else state.canReroll,
                    seed = random.nextLong(),
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
                achievementDelegate.checkPerkAchievements(Perk.ADVANCE_QUEUE, _uiState.value)
                stateDelegate.saveState()
                _uiState.update {
                    it.consumePerk(Perk.ADVANCE_QUEUE)
                        .copy(isGameOver = false, activePerk = null, selectedCellId = null)
                }
                spawnFromQueue(_uiState.value.grid, decrementLifespan = false)
            }

            Perk.UNDO -> {
                achievementDelegate.checkPerkAchievements(Perk.UNDO, _uiState.value)
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
                triggerTip(PERK)
            }
        }
        if (_uiState.value.collectedPerks.size >= 10) {
            achievementDelegate.unlockDeepPockets()
        }
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
                seed = random.nextLong(),
            )
        }
        achievementDelegate.onRerollUsed()
        checkValidMoves()
    }

    fun onKeepMissionsClicked() {
        _uiState.update { it.copy(missionRefreshState = MissionRefreshState.NONE) }
    }

    fun onRefreshMissionsClicked() {
        viewModelScope.launch {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val dateSeed = today.year * 10000L + (today.month.ordinal + 1) * 100L + today.day

            settingsRepository.setDailyMissionDate(dateSeed)
            settingsRepository.clearPersistentCompletedMissionIds()

            val currentDailyChallenges = DailyChallengeProvider.getChallengesForDate(today, 0)

            _uiState.update {
                it.copy(
                    missionRefreshState = MissionRefreshState.NONE,
                    dailyChallenges = currentDailyChallenges.map { c -> DailyChallengeProgress(c) },
                    persistentCompletedMissionIds = emptySet(),
                    challengeStreak = 0,
                    isStreakCollectedToday = false,
                    dailyMissionDate = dateSeed,
                )
            }
        }
    }

    fun onAcknowledgeHardRefresh() {
        _uiState.update { it.copy(missionRefreshState = MissionRefreshState.NONE) }
    }

    fun onBackClicked() {
        val currentState = _uiState.value
        if (currentState.isGameOver || currentState.isPerksBankVisible || currentState.isNicknamePopupVisible) return
        if (currentState.activeDialog != null) return

        _uiState.update {
            it.copy(
                activeDialog = PauseMenu(
                    onResume = { onDismissDialog() },
                    onRestart = { onRestartClicked() },
                ),
            )
        }
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
        val (initialPreviews, nextPreviewIdCounter) = engine.pickRandomPreviews(
            initialGrid,
            emptyList(),
            emptyList(),
            random,
            0,
        )

        _uiState.update {
            it.copy(
                grid = initialGrid,
                mergeHints = if (it.mergeHintsEnabled) engine.findMergeHints(
                    initialGrid,
                    initialPreviews,
                    0,
                    null,
                ) else emptyList(),
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
                dailyChallenges = it.dailyChallenges.map { challengeProgress ->
                    if (challengeProgress.isCompleted) {
                        challengeProgress
                    } else {
                        challengeProgress.copy(progress = 0)
                    }
                },
                debugUsed = false,
                finalResult = null,
                isGameOver = false,
                showReviveOption = false,
                hasRevived = false,
                activePerk = null,
                selectedCellId = null,
                score = 0,
                combo = 0,
                comboMaintenanceTurns = 0,
                movesWithoutPerk = 0,
                levelProgress = 0f,
                pendingLevelUps = 0,
                perkOptions = emptyList(),
                maxCombo = 0,
                totalMerges = 0,
                showGameOverBoard = false,
                reachedComboTiers = emptySet(),
                availableChoices = 0,
                perksUsedTracking = emptyMap(),
                consecutiveUndos = 0,
                consecutiveMergesWithoutSpawn = 0,
                tacticalMergesCount = 0,
                comboTriggeredInSession = false,
                perkUsedInSession = false,
                undoUsedInSession = false,
                ghostPerkUsedInSession = false,
                barRaisedThisTurn = 0,
                tacticalGhostsThisTurn = 0,
                redemptionBaseline = null,
                consecutiveTacticalNoSpawn = 0,
                thawedIds = emptySet(),
            )
        }
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
                delay(1000.milliseconds)

                if (!state.hasRevived) {
                    _uiState.update { it.copy(showReviveOption = true) }
                    return@launch
                }

                val finalResult = DetailedGameResult(
                    score = state.score,
                    maxCombo = state.maxCombo,
                    maxPiece = state.highestValue,
                    totalMerges = state.totalMerges,
                    level = state.level,
                    perksUsed = state.perksUsedTracking,
                    perksAvailable = state.collectedPerks,
                    username = settingsRepository.getPlayerName() ?: "Unknown",
                    dailyChallenges = state.dailyChallenges,
                )

                _uiState.update {
                    it.copy(
                        isGameOver = true,
                        finalResult = finalResult,
                        currentRank = null,
                        bestScore = maxOf(it.bestScore, state.score),
                        sessionBestScore = maxOf(it.sessionBestScore, state.score),
                    )
                }
                effectDelegate.addGameOver()
                stateDelegate.persistBestScore(state.score)
                achievementDelegate.onGameFinished()
                settingsRepository.setGameState(null)

                if (!state.debugUsed) {
                    val rankInfo = leaderboardRepository.submitResult(finalResult)
                    _uiState.update { it.copy(currentRank = rankInfo) }
                }

                if (!settingsRepository.getHasShownPostGameTip()) {
                    triggerTip(POST_GAME)
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
                Triple(
                    gridWithoutTactical,
                    _uiState.value.preview to currentPreviewIdCounter,
                    currentPerks,
                )
            } else {
                engine.spawnFromQueue(
                    gridWithoutTactical,
                    _uiState.value.preview,
                    currentPerks,
                    random,
                    currentPreviewIdCounter,
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
                random,
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
                    thawedIds = thawedIds,
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
    fun setChallengeStreak(streak: Int) = debugDelegate.setChallengeStreak(streak)

    fun getAchievementManager(): AchievementManager = achievementManager

    fun onDismissDialog() {
        _uiState.update { it.copy(activeDialog = null) }
    }

    fun onBuyPerk(category: PerkCategory) {
        val cost = when (category) {
            PerkCategory.COMMON -> 50
            PerkCategory.RARE -> 150
            PerkCategory.LEGENDARY -> 500
        }
        val nameRes = when (category) {
            PerkCategory.COMMON -> Res.string.shop_common_bundle
            PerkCategory.RARE -> Res.string.shop_rare_bundle
            PerkCategory.LEGENDARY -> Res.string.shop_legendary_bundle
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeDialog = Confirmation(
                        title = Res.string.shop_buy_confirmation_title,
                        message = Res.string.shop_buy_confirmation_message,
                        formatArgs = listOf(cost, getString(nameRes)),
                        onConfirm = {
                            viewModelScope.launch {
                                inFlightActions.update { it + 1 }
                                _uiState.update { s ->
                                    val newVouchers = s.vouchers.toMutableMap()
                                    newVouchers[category] = (newVouchers[category] ?: 0) + 1
                                    s.copy(
                                        diamonds = s.diamonds - cost,
                                        vouchers = newVouchers,
                                        isShopProcessing = true,
                                    )
                                }
                                monetizationRepository.buyPerkVoucher(category)
                                _uiState.update { s -> s.copy(isShopProcessing = false) }
                                inFlightActions.update { it - 1 }
                            }
                        },
                    ),
                )
            }
        }
    }

    fun onUseVoucher(category: PerkCategory? = null) {
        _uiState.update { it.copy(isPerksBankVisible = true, perksBankCategory = category) }
    }

    fun onDismissVoucherSelection() {
        _uiState.update { it.copy(isPerksBankVisible = false) }
    }

    fun onDeclineRevive() {
        _uiState.update { it.copy(showReviveOption = false, hasRevived = true) }
        checkValidMoves()
    }

    fun onBuyAndRevive(category: PerkCategory) {
        onBuyPerk(category)
    }

    fun onPerkFromVoucherSelected(perk: Perk, category: PerkCategory) {
        viewModelScope.launch {
            inFlightActions.update { it + 1 }
            _uiState.update { s ->
                val newVouchers = s.vouchers.toMutableMap()
                val current = newVouchers[category] ?: 0
                if (current > 0) newVouchers[category] = current - 1
                s.copy(
                    vouchers = newVouchers,
                    isVoucherProcessing = true,
                )
            }
            if (monetizationRepository.usePerkVoucher(category)) {
                _uiState.update {
                    it.copy(
                        collectedPerks = it.collectedPerks + perk,
                        isPerksBankVisible = false,
                        isVoucherProcessing = false,
                        showReviveOption = false,
                        hasRevived = it.showReviveOption || it.hasRevived,
                        isGameOver = false,
                    )
                }
                checkValidMoves()
            } else {
                _uiState.update { it.copy(isVoucherProcessing = false) }
            }
            inFlightActions.update { it - 1 }
        }
    }

    fun onBuyPremiumProduct(product: BillingProduct) {
        viewModelScope.launch {
            inFlightActions.update { it + 1 }
            _uiState.update { it.copy(isShopProcessing = true) }
            billingManager.purchase(product)
            _uiState.update { it.copy(isShopProcessing = false) }
            inFlightActions.update { it - 1 }
        }
    }

    fun onShowNicknamePopup() {
        _uiState.update {
            it.copy(
                isNicknamePopupVisible = true,
                tempNickname = "",
                nicknameError = null,
            )
        }
    }

    fun onNicknameChanged(name: String) {
        _uiState.update { it.copy(tempNickname = name, nicknameError = null) }
    }

    fun onDismissNicknamePopup() {
        _uiState.update { it.copy(isNicknamePopupVisible = false) }
    }

    fun onConfirmNickname() {
        val name = _uiState.value.tempNickname.trim()
        if (name.isEmpty()) {
            viewModelScope.launch {
                _uiState.update { it.copy(nicknameError = getString(Res.string.onboarding_nickname_empty)) }
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isBusy = true) }
            try {
                leaderboardRepository.createProfile(name)
                _uiState.update { it.copy(isNicknamePopupVisible = false, isBusy = false) }

                // If game is over, re-trigger submission with the new nickname
                val finalResult = _uiState.value.finalResult
                if (_uiState.value.isGameOver && finalResult != null && !_uiState.value.debugUsed) {
                    val rankInfo = leaderboardRepository.submitResult(finalResult)
                    _uiState.update { it.copy(currentRank = rankInfo) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        nicknameError = e.message
                            ?: getString(Res.string.onboarding_nickname_error),
                        isBusy = false,
                    )
                }
            }
        }
    }

    private suspend fun handleChallengeComplete(challenge: DailyChallenge) {
        if (challenge.rewardScore > 0) {
            _uiState.update {
                val newScore = it.score + challenge.rewardScore
                it.copy(score = newScore, bestScore = maxOf(it.bestScore, newScore))
            }
            challengeDelegate.onScoreChanged(_uiState.value.score)
        }
        if (challenge.rewardPerk != null) {
            _uiState.update { it.copy(collectedPerks = it.collectedPerks + challenge.rewardPerk) }
        }

        val isFirstTimeToday = !_uiState.value.persistentCompletedMissionIds.contains(challenge.id)

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val dateSeed = today.year * 10000L + (today.month.number) * 100L + today.day

        var isDayCompleted = false
        var newStreakValue = 0

        // Add to persistent completed missions
        if (isFirstTimeToday) {
            settingsRepository.addPersistentCompletedMissionId(challenge.id)
            _uiState.update { it.copy(persistentCompletedMissionIds = it.persistentCompletedMissionIds + challenge.id) }
        }

        // Check if ALL daily challenges are now persistent-completed to update streak
        val allPersistentCompleted = _uiState.value.persistentCompletedMissionIds.size >= 3
        if (allPersistentCompleted && !_uiState.value.isStreakCollectedToday) {
            val missionDateSeed = settingsRepository.getDailyMissionDate()
            val lastCompletedDate = settingsRepository.getLastCompletedChallengeDate()

            // We complete for the day the missions were from
            if (lastCompletedDate != missionDateSeed) {
                val updatedCompletedDates = _uiState.value.completedChallengeDates + missionDateSeed
                val newStreak = DailyMissionUtils.calculateStreak(updatedCompletedDates, today)
                isDayCompleted = true
                newStreakValue = newStreak

                settingsRepository.setLastCompletedChallengeDate(missionDateSeed)
                settingsRepository.addCompletedChallengeDate(missionDateSeed.toString())

                val reward =
                    com.pointlessgames.hexagone.game.logic.StreakMilestones.getRewardForStreak(
                        newStreak,
                    )
                if (reward != null) {
                    inFlightActions.update { it + 1 }
                    monetizationRepository.awardStreakRewards(reward)
                    inFlightActions.update { it - 1 }
                }

                _uiState.update {
                    it.copy(
                        challengeStreak = newStreak,
                        completedChallengeDates = it.completedChallengeDates + missionDateSeed,
                        isStreakCollectedToday = missionDateSeed == dateSeed,
                    )
                }
            }
        }

        _effects.emit(
            GameEffect.DailyChallengeComplete(
                challenge,
                isFirstTimeToday,
                isDayCompleted,
                newStreakValue,
            ),
        )
    }


}
