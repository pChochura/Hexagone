package com.pointlessgames.hexagone.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.model.TipTarget
import com.pointlessgames.hexagone.game.ui.components.AchievementNotification
import com.pointlessgames.hexagone.game.ui.components.DebugOverlay
import com.pointlessgames.hexagone.game.ui.components.GameGridOverlay
import com.pointlessgames.hexagone.game.ui.components.GameOverlays
import com.pointlessgames.hexagone.game.ui.components.PerkBar
import com.pointlessgames.hexagone.game.ui.components.ScoreSection
import com.pointlessgames.hexagone.game.ui.components.SettingsDialog
import com.pointlessgames.hexagone.game.ui.components.TipOverlay
import com.pointlessgames.hexagone.game.ui.components.VoucherSelectionDialog
import com.pointlessgames.hexagone.game.ui.components.trackTipTarget
import com.pointlessgames.hexagone.leaderboard.LeaderboardViewModel
import com.pointlessgames.hexagone.ui.theme.IsSmallDevice
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.utils.BackHandler
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.no_moves_left_warning
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
) {
    val navigator = com.pointlessgames.hexagone.LocalNavigator.current
    // Fine-grained state collection for reactivity and optimization.
    // We collect individual fields into Compose State objects.
    // Reading from these State objects in providers ensures children and snapshotFlows are reactive.
    val uiState = viewModel.uiState
    val isGameOverState =
        remember(uiState) { uiState.map { it.isGameOver }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.isGameOver,
        )
    val showGameOverBoardState = remember(uiState) {
        uiState.map { it.showGameOverBoard }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.showGameOverBoard)
    val isStuckState =
        remember(uiState) { uiState.map { it.isStuck }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.isStuck,
        )
    val gridState =
        remember(uiState) { uiState.map { it.grid }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.grid,
        )
    val activePerkState =
        remember(uiState) { uiState.map { it.activePerk }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.activePerk,
        )
    val isDebugModeState =
        remember(uiState) { uiState.map { it.isDebugMode }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.isDebugMode,
        )
    val pendingResultState = remember(uiState) {
        uiState.map { it.pendingResult }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.pendingResult)
    val activeTipState =
        remember(uiState) { uiState.map { it.activeTip }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.activeTip,
        )
    val isVoucherProcessingState =
        remember(uiState) {
            uiState.map { it.isVoucherProcessing }.distinctUntilChanged()
        }.collectAsState(
            viewModel.uiState.value.isVoucherProcessing,
        )

    // Helper delegates for GameScreen's own logic.
    // Accessing these 'by' variables will trigger recomposition of GameScreen.
    val isGameOver by isGameOverState
    val showGameOverBoard by showGameOverBoardState
    val isStuck by isStuckState
    val activePerk by activePerkState
    val isDebugMode by isDebugModeState
    val pendingResult by pendingResultState
    val activeTip by activeTipState
    val isVoucherProcessing by isVoucherProcessingState
    val activeDialogState =
        remember(uiState) { uiState.map { it.activeDialog }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.activeDialog,
        )
    val activeDialog by activeDialogState
    val activeVoucherSelectionState =
        remember(uiState) {
            uiState.map { it.activeVoucherSelection }.distinctUntilChanged()
        }.collectAsState(
            viewModel.uiState.value.activeVoucherSelection,
        )
    val activeVoucherSelection by activeVoucherSelectionState

    // Other states primarily used by providers passed to children.
    // GameScreen won't recompose when these change unless it reads them directly.
    val scoreState =
        remember(uiState) { uiState.map { it.score }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.score,
        )
    val bestScoreState =
        remember(uiState) { uiState.map { it.bestScore }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.bestScore,
        )
    val comboState =
        remember(uiState) { uiState.map { it.combo }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.combo,
        )
    val levelState =
        remember(uiState) { uiState.map { it.level }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.level,
        )
    val highestValueState =
        remember(uiState) { uiState.map { it.highestValue }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.highestValue,
        )
    val dailyChallengesState = remember(uiState) {
        uiState.map { it.dailyChallenges }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.dailyChallenges)
    val collectedPerksState = remember(uiState) {
        uiState.map { it.collectedPerks }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.collectedPerks)
    val sessionBestScoreState = remember(uiState) {
        uiState.map { it.sessionBestScore }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.sessionBestScore)
    val maxComboState =
        remember(uiState) { uiState.map { it.maxCombo }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.maxCombo,
        )
    val totalMergesState =
        remember(uiState) { uiState.map { it.totalMerges }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.totalMerges,
        )
    val perkOptionsState =
        remember(uiState) { uiState.map { it.perkOptions }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.perkOptions,
        )
    val pendingLevelUpsState = remember(uiState) {
        uiState.map { it.pendingLevelUps }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.pendingLevelUps)
    val canRerollState =
        remember(uiState) { uiState.map { it.canReroll }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.canReroll,
        )
    val currentRankState =
        remember(uiState) { uiState.map { it.currentRank }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.currentRank,
        )
    val finalResultState =
        remember(uiState) { uiState.map { it.finalResult }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.finalResult,
        )
    val debugSelectedValueState = remember(uiState) {
        uiState.map { it.debugSelectedValue }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.debugSelectedValue)
    val debugAddAsGhostState = remember(uiState) {
        uiState.map { it.debugAddAsGhost }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.debugAddAsGhost)
    val challengeStreakState = remember(uiState) {
        uiState.map { it.challengeStreak }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.challengeStreak)
    val debugUsedState =
        remember(uiState) { uiState.map { it.debugUsed }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.debugUsed,
        )
    val stuckPerksState =
        remember(uiState) { uiState.map { it.stuckPerks }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.stuckPerks,
        )
    val mergeHintsState =
        remember(uiState) { uiState.map { it.mergeHints }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.mergeHints,
        )
    val previewState =
        remember(uiState) { uiState.map { it.preview }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.preview,
        )
    val onBoardPerksState =
        remember(uiState) { uiState.map { it.onBoardPerks }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.onBoardPerks,
        )
    val pendingMergeState =
        remember(uiState) { uiState.map { it.pendingMerge }.distinctUntilChanged() }.collectAsState(
            viewModel.uiState.value.pendingMerge,
        )
    val activeMergeStepIndexState = remember(uiState) {
        uiState.map { it.activeMergeStepIndex }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.activeMergeStepIndex)
    val pendingMergeScoreState = remember(uiState) {
        uiState.map { it.pendingMergeScore }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.pendingMergeScore)
    val selectedCellIdState = remember(uiState) {
        uiState.map { it.selectedCellId }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.selectedCellId)
    val storeProductsState = viewModel.storeProducts.collectAsState()

    var showSettings by remember { mutableStateOf(false) }
    var initiallySelectedAchievement by remember {
        mutableStateOf<com.pointlessgames.hexagone.achievements.GameAchievement?>(
            null,
        )
    }
    val leaderboardViewModel: LeaderboardViewModel = koinViewModel()

    val targetRects = remember { mutableStateMapOf<TipTarget, Rect>() }

    val tierRewardQueue =
        remember { mutableStateListOf<Pair<com.pointlessgames.hexagone.game.model.ComboTier, com.pointlessgames.hexagone.game.model.Perk>>() }
    val challengeRewardQueue =
        remember { mutableStateListOf<com.pointlessgames.hexagone.game.model.DailyChallenge>() }

    val activeTierReward = tierRewardQueue.firstOrNull()
    val activeChallengeReward = challengeRewardQueue.firstOrNull()

    val achievementQueue =
        remember { mutableStateListOf<com.pointlessgames.hexagone.achievements.GameAchievement>() }
    val activeAchievement = achievementQueue.firstOrNull()

    val onEmptySpaceClick = remember(viewModel) { viewModel::onEmptySpaceClicked }
    val onEmptySpaceTouchDown = remember(viewModel) { viewModel::onEmptySpaceTouchDown }
    val onEmptySpaceTouchUp = remember(viewModel) { viewModel::onEmptySpaceTouchUp }
    val onCellTouchDown = remember(viewModel) { viewModel::onCellTouchDown }
    val onCellTouchUp = remember(viewModel) { viewModel::onCellTouchUp }
    val onCellClick = remember(viewModel) { viewModel::onCellClicked }
    val onMergeAnimationFinished = remember(viewModel) { viewModel::onMergeAnimationFinished }
    val onPerkClick = remember(viewModel) { viewModel::onUsePerkClicked }
    val onShopClick = remember(viewModel) { { navigator.navigateTo(com.pointlessgames.hexagone.Route.Shop) } }
    val onPerkSelected = remember(viewModel) { viewModel::onPerkSelected }
    val onRestart = remember(viewModel) { viewModel::onRestartClicked }
    val onViewBoardToggle = remember(viewModel) { viewModel::onViewBoardToggled }
    val onDebugToggle = remember(viewModel) { viewModel::toggleDebugMode }
    val onDebugCellClick = remember(viewModel) { viewModel::onDebugCellClicked }

    BackHandler(enabled = showSettings) {
        showSettings = false
    }

    BackHandler(enabled = activeVoucherSelection != null) {
        viewModel.onDismissVoucherSelection()
    }

    BackHandler(enabled = activeDialog != null) {
        viewModel.onDismissDialog()
    }

    BackHandler(enabled = isGameOver && activeTierReward == null && activeChallengeReward == null) {
        if (showGameOverBoard) {
            onViewBoardToggle()
        } else {
            onRestart()
        }
    }

    val gridAlphaState = animateFloatAsState(
        targetValue = if (isGameOver && !showGameOverBoard) 0.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "grid_alpha",
    )

    val gridAlphaProvider = remember { { gridAlphaState.value } }

    val infiniteTransition = rememberInfiniteTransition(label = "stuck_pulse")
    val stuckBounceState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "stuck_bounce",
    )
    val stuckBounceProvider = remember { { stuckBounceState.value } }

    // Stable Providers for UI components - reading from collected State.
    // These lambdas are stable, and reading .value from State ensures reactivity.
    val scoreProvider = remember { { scoreState.value } }
    val bestScoreProvider = remember { { bestScoreState.value } }
    val comboProvider = remember { { comboState.value } }
    val levelProvider = remember { { levelState.value } }
    val highestValueProvider = remember { { highestValueState.value } }
    val activePerkProvider = remember { { activePerkState.value } }
    val isDailyChallengeCompletedProvider =
        remember { { dailyChallengesState.value.all { it.isCompleted } } }
    val collectedPerksProvider = remember { { collectedPerksState.value } }
    val isStuckProvider = remember { { isStuckState.value } }
    val stuckPerksProvider = remember { { stuckPerksState.value } }
    val isGameOverProvider = remember { { isGameOverState.value } }
    val showGameOverBoardProvider = remember { { showGameOverBoardState.value } }
    val sessionBestScoreProvider = remember { { sessionBestScoreState.value } }
    val maxComboProvider = remember { { maxComboState.value } }
    val totalMergesProvider = remember { { totalMergesState.value } }
    val perkOptionsProvider = remember { { perkOptionsState.value } }
    val pendingLevelUpsProvider = remember { { pendingLevelUpsState.value } }
    val canRerollProvider = remember { { canRerollState.value } }
    val currentRankProvider = remember { { currentRankState.value } }
    val finalResultProvider = remember { { finalResultState.value } }
    val isDebugModeProvider = remember { { isDebugModeState.value } }
    val debugSelectedValueProvider = remember { { debugSelectedValueState.value } }
    val debugAddAsGhostProvider = remember { { debugAddAsGhostState.value } }
    val challengeStreakProvider = remember { { challengeStreakState.value } }
    val isStreakCollectedTodayState = remember(uiState) {
        uiState.map { it.isStreakCollectedToday }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.isStreakCollectedToday)
    val debugUsedProvider = remember { { debugUsedState.value } }

    // Stable Providers for GameGridOverlay
    val mergeHintsProvider = remember { { mergeHintsState.value } }
    val onBoardPerksProvider = remember { { onBoardPerksState.value } }
    val potentialMergesProvider = remember(viewModel) {
        derivedStateOf {
            gridState.value
            activePerkState.value
            viewModel.getPotentialMerges()
        }
    }
    val pendingMergeProvider = remember { { pendingMergeState.value } }
    val activeMergeStepIndexProvider = remember { { activeMergeStepIndexState.value } }
    val pendingMergeScoreProvider = remember { { pendingMergeScoreState.value } }
    val selectedCellIdProvider = remember { { selectedCellIdState.value } }

    LaunchedEffect(pendingResult) {
        if (pendingResult != null) {
            leaderboardViewModel.setPendingResult(pendingResult)
            navigator.navigateTo(com.pointlessgames.hexagone.Route.Leaderboard)
        }
    }

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is com.pointlessgames.hexagone.game.model.GameEffect.TierReward -> {
                    tierRewardQueue.add(effect.tier to effect.perk)
                }

                is com.pointlessgames.hexagone.game.model.GameEffect.AchievementUnlock -> {
                    achievementQueue.add(effect.achievement)
                }

                is com.pointlessgames.hexagone.game.model.GameEffect.DailyChallengeComplete -> {
                    challengeRewardQueue.add(effect.challenge)
                }

                else -> {}
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { clip = false }
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background),
                ),
            ),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize().safeDrawingPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Left Column: Score Section
                    if (!isDebugModeProvider()) {
                        Box(
                            modifier = Modifier
                                .width(IntrinsicSize.Min)
                                .fillMaxHeight()
                                .padding(MaterialTheme.spacing.medium.scaled),
                            contentAlignment = Alignment.TopCenter,
                        ) {
                            ScoreSection(
                                scoreProvider = scoreProvider,
                                bestScoreProvider = bestScoreProvider,
                                comboProvider = comboProvider,
                                levelProvider = levelProvider,
                                progressProvider = { viewModel.getLevelProgress() },
                                highestValueProvider = highestValueProvider,
                                activePerkProvider = activePerkProvider,
                                isVertical = true,
                                onLevelClick = if (com.pointlessgames.hexagone.utils.isDebug) onDebugToggle else ({}),
                                onLeaderboardClick = { navigator.navigateTo(com.pointlessgames.hexagone.Route.Leaderboard) },
                                onAchievementsClick = { navigator.navigateTo(com.pointlessgames.hexagone.Route.Achievements()) },
                                onSettingsClick = { showSettings = true },
                                onDailyChallengeClick = { navigator.navigateTo(com.pointlessgames.hexagone.Route.DailyMissions) },
                                isDailyChallengeCompletedProvider = isDailyChallengeCompletedProvider,
                                modifier = Modifier.trackTipTarget(TipTarget.SCORE_SECTION) { target, rect ->
                                    targetRects[target] = rect
                                },
                            )
                        }
                    }

                    // Center: Grid
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(MaterialTheme.spacing.medium.scaled),
                        contentAlignment = Alignment.Center,
                    ) {
                        GameGridOverlay(
                            gridState = gridState.value,
                            onBoardPerksProvider = onBoardPerksProvider,
                            mergeHintsProvider = mergeHintsProvider,
                            previewState = previewState.value,
                            pendingMergeProvider = pendingMergeProvider,
                            hoveredMergeState = viewModel.hoveredMerge,
                            potentialMergesProvider = { potentialMergesProvider.value },
                            activePerkProvider = activePerkProvider,
                            selectedCellIdProvider = selectedCellIdProvider,
                            activeMergeStepIndexProvider = activeMergeStepIndexProvider,
                            comboProvider = comboProvider,
                            effects = viewModel.effects,
                            onEmptySpaceClick = if (isDebugMode) onDebugCellClick else onEmptySpaceClick,
                            onEmptySpaceTouchDown = if (isDebugMode) { _, _ -> } else onEmptySpaceTouchDown,
                            onEmptySpaceTouchUp = onEmptySpaceTouchUp,
                            onCellTouchDown = if (isDebugMode) { _ -> } else onCellTouchDown,
                            onCellTouchUp = onCellTouchUp,
                            onCellClick = if (isDebugMode) { cell ->
                                onDebugCellClick(
                                    cell.x,
                                    cell.y,
                                )
                            } else onCellClick,
                            onMergeAnimationFinished = onMergeAnimationFinished,
                            modifier = Modifier
                                .fillMaxSize()
                                .trackTipTarget(TipTarget.GRID) { target, rect ->
                                    targetRects[target] = rect
                                },
                        )
                    }

                    // Placeholder for Perk Bar (to keep layout consistent)
                    if (!isDebugModeProvider()) {
                        Spacer(Modifier.width(100.dp.scaled))
                    }
                }
            } else {
                // Portrait (Existing Layout)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = gridAlphaProvider()
                            clip = false
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer { clip = false }
                            .padding(
                                horizontal = MaterialTheme.spacing.large.scaled,
                                vertical = MaterialTheme.spacing.small.scaled,
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (!isDebugModeProvider()) {
                            ScoreSection(
                                scoreProvider = scoreProvider,
                                bestScoreProvider = bestScoreProvider,
                                comboProvider = comboProvider,
                                levelProvider = levelProvider,
                                progressProvider = { viewModel.getLevelProgress() },
                                highestValueProvider = highestValueProvider,
                                activePerkProvider = activePerkProvider,
                                onLevelClick = if (com.pointlessgames.hexagone.utils.isDebug) onDebugToggle else ({}),
                                onLeaderboardClick = { navigator.navigateTo(com.pointlessgames.hexagone.Route.Leaderboard) },
                                onAchievementsClick = { navigator.navigateTo(com.pointlessgames.hexagone.Route.Achievements()) },
                                onSettingsClick = { showSettings = true },
                                onDailyChallengeClick = { navigator.navigateTo(com.pointlessgames.hexagone.Route.DailyMissions) },
                                isDailyChallengeCompletedProvider = isDailyChallengeCompletedProvider,
                                modifier = Modifier.trackTipTarget(TipTarget.SCORE_SECTION) { target, rect ->
                                    targetRects[target] = rect
                                },
                            )

                            Spacer(Modifier.weight(if (IsSmallDevice) 0.05f else 0.1f))
                        }

                        GameGridOverlay(
                            gridState = gridState.value,
                            onBoardPerksProvider = onBoardPerksProvider,
                            mergeHintsProvider = mergeHintsProvider,
                            previewState = previewState.value,
                            pendingMergeProvider = pendingMergeProvider,
                            hoveredMergeState = viewModel.hoveredMerge,
                            potentialMergesProvider = { potentialMergesProvider.value },
                            activePerkProvider = activePerkProvider,
                            selectedCellIdProvider = selectedCellIdProvider,
                            activeMergeStepIndexProvider = activeMergeStepIndexProvider,
                            comboProvider = comboProvider,
                            effects = viewModel.effects,
                            onEmptySpaceClick = if (isDebugMode) onDebugCellClick else onEmptySpaceClick,
                            onEmptySpaceTouchDown = if (isDebugMode) { _, _ -> } else onEmptySpaceTouchDown,
                            onEmptySpaceTouchUp = onEmptySpaceTouchUp,
                            onCellTouchDown = if (isDebugMode) { _ -> } else onCellTouchDown,
                            onCellTouchUp = onCellTouchUp,
                            onCellClick = if (isDebugMode) { cell ->
                                onDebugCellClick(
                                    cell.x,
                                    cell.y,
                                )
                            } else onCellClick,
                            onMergeAnimationFinished = onMergeAnimationFinished,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                                .trackTipTarget(TipTarget.GRID) { target, rect ->
                                    targetRects[target] = rect
                                },
                        )

                        if (!isDebugModeProvider()) {
                            Spacer(Modifier.weight(if (IsSmallDevice) 0.05f else 0.1f))
                        }
                    }

                    if (!isDebugModeProvider()) {
                        // Placeholder to keep space for PerkBar
                        Spacer(Modifier.height(MaterialTheme.spacing.immense.scaled))
                    } else {
                        DebugOverlay(
                            isVisible = true,
                            selectedValue = debugSelectedValueProvider(),
                            isGhostMode = debugAddAsGhostProvider(),
                            currentStreak = challengeStreakProvider(),
                            onValueSelected = viewModel::setDebugSelectedValue,
                            onGhostModeToggled = viewModel::toggleDebugAddAsGhost,
                            onStreakChanged = viewModel::setChallengeStreak,
                            onPerkClick = viewModel::addPerkManually,
                            onClose = onDebugToggle,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        GameOverlays(
            isGameOverProvider = isGameOverProvider,
            scoreProvider = scoreProvider,
            bestScoreProvider = bestScoreProvider,
            sessionBestScoreProvider = sessionBestScoreProvider,
            levelProvider = levelProvider,
            maxComboProvider = maxComboProvider,
            totalMergesProvider = totalMergesProvider,
            highestValueProvider = highestValueProvider,
            showBoardProvider = showGameOverBoardProvider,
            perkOptionsProvider = perkOptionsProvider,
            pendingLevelUpsProvider = pendingLevelUpsProvider,
            canRerollProvider = canRerollProvider,
            onPerkSelected = onPerkSelected,
            onRerollClicked = viewModel::onRerollClicked,
            onRestart = onRestart,
            onViewBoardToggle = onViewBoardToggle,
            onShare = { /* TODO: Implement snapshot and share */ },
            onLeaderboard = { navigator.navigateTo(com.pointlessgames.hexagone.Route.Leaderboard) },
            activeTierReward = activeTierReward,
            onTierRewardFinished = { if (tierRewardQueue.isNotEmpty()) tierRewardQueue.removeAt(0) },
            activeChallengeReward = activeChallengeReward,
            onChallengeRewardFinished = {
                if (challengeRewardQueue.isNotEmpty())
                    challengeRewardQueue.removeAt(0)
            },
            rankingInfoProvider = currentRankProvider,
            debugUsedProvider = debugUsedProvider,
            finalResultProvider = finalResultProvider,
            modifier = Modifier.trackTipTarget(TipTarget.GAME_OVER_BUTTONS) { target, rect ->
                targetRects[target] = rect
            },
        )

        // Persistent Perk Bar (Above Game Over Dim)
        if (!isDebugModeProvider()) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isLandscape = maxWidth > maxHeight
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.then(if (isLandscape) Modifier.fillMaxHeight() else Modifier.fillMaxWidth()),
                    ) {
                        if (!isLandscape && isStuckProvider() && activePerkProvider() == null) {
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(0, stuckBounceProvider().dp.roundToPx()) }
                                    .offset(y = -MaterialTheme.spacing.semiMedium)
                                    .shadow(
                                        elevation = MaterialTheme.spacing.medium,
                                        shape = RoundedCornerShape(MaterialTheme.cornerRadius.small),
                                    )
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(MaterialTheme.cornerRadius.small),
                                    )
                                    .border(
                                        MaterialTheme.spacing.tiny,
                                        Color.White.copy(alpha = 0.5f),
                                        RoundedCornerShape(MaterialTheme.cornerRadius.small),
                                    )
                                    .padding(
                                        horizontal = MaterialTheme.spacing.medium,
                                        vertical = MaterialTheme.spacing.semiSmall,
                                    ),
                            ) {
                                Text(
                                    text = stringResource(Res.string.no_moves_left_warning),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp.scaled,
                                    letterSpacing = 1.sp.scaled,
                                )
                            }
                            Spacer(Modifier.height(MaterialTheme.spacing.medium.scaled))
                        }

                        PerkBar(
                            collectedPerksProvider = collectedPerksProvider,
                            activePerkProvider = activePerkProvider,
                            isStuckProvider = isStuckProvider,
                            stuckPerksProvider = stuckPerksProvider,
                            onPerkClick = onPerkClick,
                            onShopClick = onShopClick,
                            isVertical = isLandscape,
                            modifier = Modifier
                                .then(if (isLandscape) Modifier.fillMaxHeight() else Modifier.fillMaxWidth())
                                .graphicsLayer { clip = false }
                                .trackTipTarget(TipTarget.PERK_BAR) { target, rect ->
                                    targetRects[target] = rect
                                },
                        )
                    }
                }
            }
        }

        TipOverlay(
            activeTip = activeTip,
            targetRects = targetRects,
            onDismiss = viewModel::onDismissTip,
        )

        activeAchievement?.let { achievement ->
            AchievementNotification(
                achievement = achievement,
                onClick = {
                    navigator.navigateTo(com.pointlessgames.hexagone.Route.Achievements(achievement.id))
                },
                onFinished = { if (achievementQueue.isNotEmpty()) achievementQueue.removeAt(0) },
            )
        }

        if (showSettings) {
            SettingsDialog(
                onRestart = onRestart,
                onDismiss = { showSettings = false },
            )
        }

        val lastVoucher = remember { mutableStateOf<com.pointlessgames.hexagone.game.logic.PerkCategory?>(null) }
        if (activeVoucherSelection != null) {
            lastVoucher.value = activeVoucherSelection
        }

        AnimatedVisibility(
            visible = activeVoucherSelection != null,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            val voucherToDisplay = activeVoucherSelection ?: lastVoucher.value
            if (voucherToDisplay != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { viewModel.onDismissVoucherSelection() },
                    contentAlignment = Alignment.Center,
                ) {
                    VoucherSelectionDialog(
                        category = voucherToDisplay,
                        isProcessing = isVoucherProcessing,
                        onPerkSelected = { perk ->
                            viewModel.onPerkFromVoucherSelected(perk, voucherToDisplay)
                        },
                        onDismiss = viewModel::onDismissVoucherSelection,
                    )
                }
            }
        }

        if (activeDialog != null) {
            com.pointlessgames.hexagone.game.ui.components.HexAlertDialog(
                state = activeDialog!!,
                onDismiss = viewModel::onDismissDialog
            )
        }
    }
}
