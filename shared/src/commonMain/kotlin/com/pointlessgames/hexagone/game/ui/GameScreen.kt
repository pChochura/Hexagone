package com.pointlessgames.hexagone.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.pointlessgames.hexagone.LocalMediaPlayer
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.Route
import com.pointlessgames.hexagone.achievements.GameAchievement
import com.pointlessgames.hexagone.auth.ui.components.NicknamePopup
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.model.ComboTier
import com.pointlessgames.hexagone.game.model.GameEffect
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.TipTarget
import com.pointlessgames.hexagone.game.ui.components.AchievementNotification
import com.pointlessgames.hexagone.game.ui.components.DebugOverlay
import com.pointlessgames.hexagone.game.ui.components.GameGridOverlay
import com.pointlessgames.hexagone.game.ui.components.GameOverlays
import com.pointlessgames.hexagone.game.ui.components.HexAlertDialog
import com.pointlessgames.hexagone.game.ui.components.MissionRefreshPopup
import com.pointlessgames.hexagone.game.ui.components.PerkBar
import com.pointlessgames.hexagone.game.ui.components.PerksBankDialog
import com.pointlessgames.hexagone.game.ui.components.ScoreSection
import com.pointlessgames.hexagone.game.ui.components.ShareableGameOverLayout
import com.pointlessgames.hexagone.game.ui.components.TipOverlay
import com.pointlessgames.hexagone.game.ui.components.trackTipTarget
import com.pointlessgames.hexagone.share.ShareManager
import com.pointlessgames.hexagone.ui.theme.IsSmallDevice
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.utils.BackHandler
import com.pointlessgames.hexagone.utils.SoundManager
import com.pointlessgames.hexagone.utils.isDebug
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.share_text
import hexagone.shared.generated.resources.share_title
import hexagone.shared.generated.resources.no_moves_left_warning
import hexagone.shared.generated.resources.restart_button
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
internal fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
) {
    val navigator = LocalNavigator.current
    val player = LocalMediaPlayer.current
    val coroutineScope = rememberCoroutineScope()

    val shareManager = koinInject<ShareManager>()
    val shareGraphicsLayer = rememberGraphicsLayer()


    val uiState = viewModel.uiState
    val isSoundEnabledState = remember(uiState) {
        uiState.map { it.isSoundEnabled }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.isSoundEnabled)

    val playMoveSound = remember(player, coroutineScope) {
        {
            if (isSoundEnabledState.value) {
                SoundManager.playSound(player, "move.wav", coroutineScope)
            }
        }
    }

    val playButtonSound = remember(player, coroutineScope) {
        {
            if (isSoundEnabledState.value) {
                SoundManager.playSound(player, "button.wav", coroutineScope)
            }
        }
    }
    // Fine-grained state collection for reactivity and optimization.
    // We collect individual fields into Compose State objects.
    // Reading from these State objects in providers ensures children and snapshotFlows are reactive.
    val isGameOverState = remember(uiState) {
        uiState.map { it.isGameOver }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.isGameOver)
    val showGameOverBoardState = remember(uiState) {
        uiState.map { it.showGameOverBoard }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.showGameOverBoard)
    val isStuckState = remember(uiState) {
        uiState.map { it.isStuck }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.isStuck)
    val gridState = remember(uiState) {
        uiState.map { it.grid }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.grid)
    val activePerkState = remember(uiState) {
        uiState.map { it.activePerk }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.activePerk)
    val isDebugModeState = remember(uiState) {
        uiState.map { it.isDebugMode }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.isDebugMode)
    val activeTipState = remember(uiState) {
        uiState.map { it.activeTip }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.activeTip)
    val isVoucherProcessingState = remember(uiState) {
        uiState.map { it.isVoucherProcessing }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.isVoucherProcessing)
    val isPerksBankVisibleState = remember(uiState) {
        uiState.map { it.isPerksBankVisible }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.isPerksBankVisible)
    val isNicknamePopupVisibleState = remember(uiState) {
        uiState.map { it.isNicknamePopupVisible }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.isNicknamePopupVisible)
    val tempNicknameState = remember(uiState) {
        uiState.map { it.tempNickname }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.tempNickname)
    val nicknameErrorState = remember(uiState) {
        uiState.map { it.nicknameError }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.nicknameError)
    val playerNameState = remember(uiState) {
        uiState.map { it.playerName }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.playerName)

    // Helper delegates for GameScreen's own logic.
    // Accessing these 'by' variables will trigger recomposition of GameScreen.
    val isGameOver by isGameOverState

    val showGameOverBoard by showGameOverBoardState
    val isDebugMode by isDebugModeState
    val activeTip by activeTipState
    val isVoucherProcessing by isVoucherProcessingState
    val isPerksBankVisible by isPerksBankVisibleState
    val showReviveOptionState = remember(uiState) {
        uiState.map { it.showReviveOption }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.showReviveOption)
    val showReviveOption by showReviveOptionState

    val activeDialogState = remember(uiState) {
        uiState.map { it.activeDialog }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.activeDialog)
    val activeDialog by activeDialogState
    val missionRefreshState = remember(uiState) {
        uiState.map { it.missionRefreshState }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.missionRefreshState)

    // Other states primarily used by providers passed to children.
    // GameScreen won't recompose when these change unless it reads them directly.
    val scoreState = remember(uiState) {
        uiState.map { it.score }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.score)
    val bestScoreState = remember(uiState) {
        uiState.map { it.bestScore }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.bestScore)
    val comboState = remember(uiState) {
        uiState.map { it.combo }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.combo)
    val levelState = remember(uiState) {
        uiState.map { it.level }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.level)
    val highestValueState = remember(uiState) {
        uiState.map { it.highestValue }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.highestValue)
    val dailyChallengesState = remember(uiState) {
        uiState.map { it.dailyChallenges }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.dailyChallenges)
    val collectedPerksState = remember(uiState) {
        uiState.map { it.collectedPerks }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.collectedPerks)
    val vouchersState = remember(uiState) {
        uiState.map { it.vouchers }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.vouchers)
    val sessionBestScoreState = remember(uiState) {
        uiState.map { it.sessionBestScore }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.sessionBestScore)
    val maxComboState = remember(uiState) {
        uiState.map { it.maxCombo }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.maxCombo)
    val perkOptionsState = remember(uiState) {
        uiState.map { it.perkOptions }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.perkOptions)
    val pendingLevelUpsState = remember(uiState) {
        uiState.map { it.pendingLevelUps }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.pendingLevelUps)
    val canRerollState = remember(uiState) {
        uiState.map { it.canReroll }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.canReroll)
    val currentRankState = remember(uiState) {
        uiState.map { it.currentRank }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.currentRank)
    val finalResultState = remember(uiState) {
        uiState.map { it.finalResult }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.finalResult)
    val debugSelectedValueState = remember(uiState) {
        uiState.map { it.debugSelectedValue }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.debugSelectedValue)
    val debugAddAsGhostState = remember(uiState) {
        uiState.map { it.debugAddAsGhost }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.debugAddAsGhost)
    val challengeStreakState = remember(uiState) {
        uiState.map { it.challengeStreak }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.challengeStreak)
    val debugUsedState = remember(uiState) {
        uiState.map { it.debugUsed }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.debugUsed)
    val stuckPerksState = remember(uiState) {
        uiState.map { it.stuckPerks }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.stuckPerks)
    val mergeHintsState = remember(uiState) {
        uiState.map { it.mergeHints }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.mergeHints)
    val previewState = remember(uiState) {
        uiState.map { it.preview }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.preview)
    val onBoardPerksState = remember(uiState) {
        uiState.map { it.onBoardPerks }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.onBoardPerks)
    val pendingMergeState = remember(uiState) {
        uiState.map { it.pendingMerge }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.pendingMerge)
    val activeMergeStepIndexState = remember(uiState) {
        uiState.map { it.activeMergeStepIndex }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.activeMergeStepIndex)
    val selectedCellIdState = remember(uiState) {
        uiState.map { it.selectedCellId }.distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.selectedCellId)
    val targetRects = remember { mutableStateMapOf<TipTarget, Rect>() }

    val tierRewardQueue = remember { mutableStateListOf<Pair<ComboTier, Perk>>() }
    val challengeRewardQueue = remember { mutableStateListOf<GameEffect.DailyChallengeComplete>() }

    val activeTierReward = tierRewardQueue.firstOrNull()
    val activeChallengeReward = challengeRewardQueue.firstOrNull()

    val achievementQueue = remember { mutableStateListOf<GameAchievement>() }
    val activeAchievement = achievementQueue.firstOrNull()

    val onEmptySpaceClick = remember(viewModel, playMoveSound) {
        { x: Int, y: Int -> playMoveSound(); viewModel.onEmptySpaceClicked(x, y) }
    }
    val onEmptySpaceTouchDown = remember(viewModel) { viewModel::onEmptySpaceTouchDown }
    val onEmptySpaceTouchUp = remember(viewModel) { viewModel::onEmptySpaceTouchUp }
    val onCellTouchDown = remember(viewModel) { viewModel::onCellTouchDown }
    val onCellTouchUp = remember(viewModel) { viewModel::onCellTouchUp }
    val onCellClick = remember(viewModel, playMoveSound) {
        { cell: HexagonCell -> playMoveSound(); viewModel.onCellClicked(cell) }
    }
    val onMergeAnimationFinished = remember(viewModel) { viewModel::onMergeAnimationFinished }
    val onPerkClick = remember(viewModel, playButtonSound) {
        { perk: Perk -> playButtonSound(); viewModel.onUsePerkClicked(perk) }
    }
    val onAddPerkClick = remember(viewModel, playButtonSound) {
        { playButtonSound(); viewModel.onUseVoucher() }
    }
    val onReviveWithCategory = remember(viewModel, playButtonSound) {
        { category: PerkCategory -> playButtonSound(); viewModel.onUseVoucher(category) }
    }
    val onShopClick = remember(viewModel, playButtonSound) {
        { playButtonSound(); navigator.navigateTo(Route.Shop) }
    }
    val onPerkSelected = remember(viewModel, playButtonSound) {
        { perk: Perk -> playButtonSound(); viewModel.onPerkSelected(perk) }
    }
    val onRestart = remember(viewModel, playButtonSound) {
        { playButtonSound(); viewModel.onRestartClicked() }
    }
    val onViewBoardToggle = remember(viewModel, playButtonSound) {
        { playButtonSound(); viewModel.onViewBoardToggled() }
    }
    val onBack = remember(viewModel, playButtonSound) {
        { playButtonSound(); viewModel.onBackClicked() }
    }
    val onDebugToggle = remember(viewModel) { viewModel::toggleDebugMode }
    val onDebugCellClick = remember(viewModel) { viewModel::onDebugCellClicked }

    BackHandler(enabled = isPerksBankVisible) {
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
    val diamondsProvider = remember { { uiState.value.diamonds } }
    val comboProvider = remember { { comboState.value } }
    val levelProvider = remember { { levelState.value } }
    val highestValueProvider = remember { { highestValueState.value } }
    val activePerkProvider = remember { { activePerkState.value } }
    val isDailyChallengeCompletedProvider =
        remember { { dailyChallengesState.value.all { it.isCompleted } } }
    val collectedPerksProvider = remember { { collectedPerksState.value } }
    val vouchersProvider = remember { { vouchersState.value } }
    val isStuckProvider = remember { { isStuckState.value } }
    val stuckPerksProvider = remember { { stuckPerksState.value } }
    val isGameOverProvider = remember { { isGameOverState.value } }
    val showGameOverBoardProvider = remember { { showGameOverBoardState.value } }
    val sessionBestScoreProvider = remember { { sessionBestScoreState.value } }
    val maxComboProvider = remember { { maxComboState.value } }
    val perkOptionsProvider = remember { { perkOptionsState.value } }
    val pendingLevelUpsProvider = remember { { pendingLevelUpsState.value } }
    val canRerollProvider = remember { { canRerollState.value } }
    val currentRankProvider = remember { { currentRankState.value } }
    val finalResultProvider = remember { { finalResultState.value } }
    val isDebugModeProvider = remember { { isDebugModeState.value } }
    val debugSelectedValueProvider = remember { { debugSelectedValueState.value } }
    val debugAddAsGhostProvider = remember { { debugAddAsGhostState.value } }
    val challengeStreakProvider = remember { { challengeStreakState.value } }
    val showReviveOptionProvider = remember { { showReviveOptionState.value } }
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
    val selectedCellIdProvider = remember { { selectedCellIdState.value } }

    LaunchedEffect(viewModel.effects) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is GameEffect.TierReward -> {
                    tierRewardQueue.add(effect.tier to effect.perk)
                    if (isSoundEnabledState.value) SoundManager.playSound(
                        player,
                        "combo_tier_${effect.tier.ordinal}.wav",
                        coroutineScope,
                    )
                }

                is GameEffect.AchievementUnlock -> {
                    achievementQueue.add(effect.achievement)
                    if (isSoundEnabledState.value) SoundManager.playSound(
                        player,
                        "achievement.wav",
                        coroutineScope,
                    )
                }

                is GameEffect.DailyChallengeComplete -> {
                    challengeRewardQueue.add(effect)
                    if (isSoundEnabledState.value) SoundManager.playSound(
                        player,
                        "daily_mission.wav",
                        coroutineScope,
                    )
                }

                is GameEffect.MergeParticles -> {
                    if (isSoundEnabledState.value) SoundManager.playSound(
                        player,
                        "merge_${minOf(effect.combo, 7)}.wav",
                        coroutineScope,
                    )
                }

                is GameEffect.TileRemoved -> {
                    if (isSoundEnabledState.value) SoundManager.playSound(
                        player,
                        "remove.wav",
                        coroutineScope,
                    )
                }

                is GameEffect.PerkPopup -> {
                    if (isSoundEnabledState.value) SoundManager.playSound(
                        player,
                        "perk_collect.wav",
                        coroutineScope,
                    )
                }

                is GameEffect.GameOver -> {
                    if (isSoundEnabledState.value) SoundManager.playSound(
                        player,
                        "game_over.wav",
                        coroutineScope,
                    )
                }

                is GameEffect.ComboBroken -> {
                    if (isSoundEnabledState.value) SoundManager.playSound(
                        player,
                        "invalid.wav",
                        coroutineScope,
                    )
                }

                else -> {}
            }
        }
    }

    val swipeOffset = remember { Animatable(0f) }
    val isOverlayVisibleState = remember(uiState) {
        uiState.map { it.isGameOver || it.activeDialog != null || it.isPerksBankVisible || it.isNicknamePopupVisible }
            .distinctUntilChanged()
    }.collectAsState(viewModel.uiState.value.let { it.isGameOver || it.activeDialog != null || it.isPerksBankVisible || it.isNicknamePopupVisible })
    val pauseThreshold = 200f

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { clip = false }
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
            .pointerInput(isOverlayVisibleState.value) {
                if (isOverlayVisibleState.value) return@pointerInput
                var triggered = false
                detectVerticalDragGestures(
                    onDragStart = {
                        triggered = false
                    },
                    onDragEnd = {
                        if (!triggered) {
                            if (swipeOffset.value >= pauseThreshold) {
                                onBack()
                                triggered = true
                            }
                            coroutineScope.launch { swipeOffset.animateTo(0f, spring()) }
                        }
                    },
                    onDragCancel = {
                        if (!triggered) {
                            coroutineScope.launch { swipeOffset.animateTo(0f, spring()) }
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        if (triggered || viewModel.hoveredMerge.value != null || selectedCellIdState.value != null) return@detectVerticalDragGestures
                        val newOffset =
                            (swipeOffset.value + dragAmount).coerceIn(0f, pauseThreshold + 150f)
                        coroutineScope.launch { swipeOffset.snapTo(newOffset) }
                        change.consume()
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = MaterialTheme.spacing.medium.scaled)
                .graphicsLayer {
                    alpha = (swipeOffset.value / pauseThreshold).coerceIn(0f, 1f)
                },
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(
                text = stringResource(Res.string.restart_button) + "?",
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp.scaled,
                letterSpacing = 2.sp,
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .graphicsLayer {
                    val progress = (swipeOffset.value / pauseThreshold).coerceIn(0f, 1f)
                    translationY = swipeOffset.value * 0.5f
                    alpha = 1f - (progress * 0.5f)
                },
        ) {
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
                                onLevelClick = if (isDebug) onDebugToggle else ({}),
                                onLeaderboardClick = { navigator.navigateTo(Route.Leaderboard) },
                                onAchievementsClick = { navigator.navigateTo(Route.Achievements()) },
                                onSettingsClick = { navigator.navigateTo(Route.Settings) },
                                onDailyChallengeClick = { navigator.navigateTo(Route.DailyMissions) },
                                isDailyChallengeCompletedProvider = isDailyChallengeCompletedProvider,
                                onTargetPosition = { target, rect ->
                                    if (swipeOffset.value == 0f) {
                                        targetRects[target] = rect
                                    }
                                },
                                modifier = Modifier.trackTipTarget(TipTarget.SCORE_SECTION) { target, rect ->
                                    if (swipeOffset.value == 0f) {
                                        targetRects[target] = rect
                                    }
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
                            isSwiping = { swipeOffset.value > 0f || isOverlayVisibleState.value },
                            modifier = Modifier
                                .fillMaxSize()
                                .trackTipTarget(TipTarget.GRID) { target, rect ->
                                    if (swipeOffset.value == 0f) {
                                        targetRects[target] = rect
                                    }
                                },
                        )
                    }

                    // Placeholder for Perk Bar (to keep layout consistent)
                    if (!isDebugModeProvider()) {
                        val expectedWidth =
                            (MaterialTheme.spacing.extraHuge.scaled * 0.866f) + 24.dp.scaled + (MaterialTheme.spacing.medium.scaled * 2)
                        val safePadding = WindowInsets.safeDrawing.asPaddingValues()
                            .calculateRightPadding(LayoutDirection.Ltr)
                        Spacer(Modifier.width(expectedWidth + safePadding))
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
                                onLevelClick = if (isDebug) onDebugToggle else ({}),
                                onLeaderboardClick = { navigator.navigateTo(Route.Leaderboard) },
                                onAchievementsClick = { navigator.navigateTo(Route.Achievements()) },
                                onSettingsClick = { navigator.navigateTo(Route.Settings) },
                                onDailyChallengeClick = { navigator.navigateTo(Route.DailyMissions) },
                                isDailyChallengeCompletedProvider = isDailyChallengeCompletedProvider,
                                onTargetPosition = { target, rect ->
                                    if (swipeOffset.value == 0f) {
                                        targetRects[target] = rect
                                    }
                                },
                                modifier = Modifier.trackTipTarget(TipTarget.SCORE_SECTION) { target, rect ->
                                    if (swipeOffset.value == 0f) {
                                        targetRects[target] = rect
                                    }
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
                            isSwiping = { swipeOffset.value > 0f || isOverlayVisibleState.value },
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                                .trackTipTarget(TipTarget.GRID) { target, rect ->
                                    if (swipeOffset.value == 0f) {
                                        targetRects[target] = rect
                                    }
                                },
                        )

                        if (!isDebugModeProvider()) {
                            Spacer(Modifier.weight(if (IsSmallDevice) 0.05f else 0.1f))
                        }
                    }

                    if (!isDebugModeProvider()) {
                        // Placeholder to keep space for PerkBar Shelf
                        val expectedHeight =
                            (MaterialTheme.spacing.extraHuge.scaled * 0.866f) + 24.dp.scaled + (MaterialTheme.spacing.medium.scaled * 2)
                        val safePadding =
                            WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
                        Spacer(Modifier.height(expectedHeight + safePadding))
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

        // Persistent Perk Bar (Moved before GameOverlays to be covered)
        if (!isDebugModeProvider()) {
            val isOverlayVisible =
                showReviveOption || isGameOver || activeTierReward != null || activeChallengeReward != null || perkOptionsProvider().isNotEmpty()

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (isOverlayVisible) 0f else 1f },
            ) {
                val isLandscape = maxWidth > maxHeight
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter,
                ) {
                    val isStuck = isStuckProvider() && activePerkProvider() == null

                    val warning = @Composable {
                        if (isStuck) {
                            Box(
                                modifier = Modifier
                                    .padding(MaterialTheme.spacing.medium.scaled)
                                    .offset {
                                        IntOffset(0, stuckBounceProvider().dp.roundToPx())
                                    }
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
                        }
                    }

                    if (isLandscape) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxHeight(),
                        ) {
                            warning()
                            PerkBar(
                                collectedPerksProvider = collectedPerksProvider,
                                activePerkProvider = activePerkProvider,
                                isStuckProvider = isStuckProvider,
                                stuckPerksProvider = stuckPerksProvider,
                                onPerkClick = onPerkClick,
                                onVoucherClick = onAddPerkClick,
                                onShopClick = onShopClick,
                                isVertical = true,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .graphicsLayer { clip = false }
                                    .trackTipTarget(TipTarget.PERK_BAR) { target, rect ->
                                        if (swipeOffset.value == 0f) {
                                            targetRects[target] = rect
                                        }
                                    },
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            warning()
                            PerkBar(
                                collectedPerksProvider = collectedPerksProvider,
                                activePerkProvider = activePerkProvider,
                                isStuckProvider = isStuckProvider,
                                stuckPerksProvider = stuckPerksProvider,
                                onPerkClick = onPerkClick,
                                onVoucherClick = onAddPerkClick,
                                onShopClick = onShopClick,
                                isVertical = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer { clip = false }
                                    .trackTipTarget(TipTarget.PERK_BAR) { target, rect ->
                                        if (swipeOffset.value == 0f) {
                                            targetRects[target] = rect
                                        }
                                    },
                            )
                        }
                    }
                }
            }
        }



        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(-1f)
                .graphicsLayer(alpha = 0.01f)
                .drawWithCache {
                    onDrawWithContent {
                        shareGraphicsLayer.record {
                            this@onDrawWithContent.drawContent()
                        }
                        drawContent()
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            ShareableGameOverLayout(
                score = scoreProvider(),
                level = levelProvider(),
                maxCombo = maxComboProvider(),
                highestValue = highestValueProvider(),
                rankingInfo = currentRankProvider(),
                playerName = playerNameState.value,
                boardContent = {
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
                        effects = viewModel.effects,
                        onEmptySpaceClick = { _, _ -> },
                        onEmptySpaceTouchDown = { _, _ -> },
                        onEmptySpaceTouchUp = {},
                        onCellTouchDown = {},
                        onCellTouchUp = {},
                        onCellClick = {},
                        onMergeAnimationFinished = {},
                        isSwiping = { false },
                        modifier = Modifier.fillMaxSize(),
                    )
                },
            )
        }

        GameOverlays(
            isGameOverProvider = isGameOverProvider,
            scoreProvider = scoreProvider,
            bestScoreProvider = bestScoreProvider,
            sessionBestScoreProvider = sessionBestScoreProvider,
            levelProvider = levelProvider,
            diamondsProvider = diamondsProvider,
            maxComboProvider = maxComboProvider,
            highestValueProvider = highestValueProvider,
            showBoardProvider = showGameOverBoardProvider,
            perkOptionsProvider = perkOptionsProvider,
            pendingLevelUpsProvider = pendingLevelUpsProvider,
            canRerollProvider = canRerollProvider,
            onPerkSelected = onPerkSelected,
            onRerollClicked = viewModel::onRerollClicked,
            onRestart = onRestart,
            onViewBoardToggle = onViewBoardToggle,
            onShare = {
                coroutineScope.launch {
                    val bitmap = shareGraphicsLayer.toImageBitmap()
                    val title = org.jetbrains.compose.resources.getString(hexagone.shared.generated.resources.Res.string.share_title)
                    val text = org.jetbrains.compose.resources.getString(hexagone.shared.generated.resources.Res.string.share_text, scoreProvider())
                    shareManager.shareImage(bitmap, title, text)
                }
            },
            onLeaderboard = { navigator.navigateTo(Route.Leaderboard) },
            activeTierReward = activeTierReward,
            onTierRewardFinished = {
                if (tierRewardQueue.isNotEmpty()) tierRewardQueue.removeAt(0)
            },
            activeChallengeReward = activeChallengeReward,
            persistentCompletedMissionIdsProvider = remember { { uiState.value.persistentCompletedMissionIds } },
            onChallengeRewardFinished = {
                if (challengeRewardQueue.isNotEmpty())
                    challengeRewardQueue.removeAt(0)
            },
            rankingInfoProvider = currentRankProvider,
            showReviveOptionProvider = showReviveOptionProvider,
            vouchersProvider = vouchersProvider,
            onRevive = onReviveWithCategory,
            onBuyAndRevive = viewModel::onBuyAndRevive,
            onOpenShop = onShopClick,
            onDeclineRevive = viewModel::onDeclineRevive,
            debugUsedProvider = debugUsedProvider,
            finalResultProvider = finalResultProvider,
            onNicknamePrompt = viewModel::onShowNicknamePopup,
            playerNameProvider = { playerNameState.value },
            modifier = Modifier.trackTipTarget(TipTarget.GAME_OVER_BUTTONS) { target, rect ->
                if (swipeOffset.value == 0f) {
                    targetRects[target] = rect
                }
            },
        )

        TipOverlay(
            activeTip = activeTip,
            targetRects = targetRects,
            onDismiss = viewModel::onDismissTip,
        )

        MissionRefreshPopup(
            state = missionRefreshState.value,
            targetRect = targetRects[TipTarget.DAILY_MISSIONS_BUTTON],
            onKeep = viewModel::onKeepMissionsClicked,
            onRefresh = viewModel::onRefreshMissionsClicked,
            onAcknowledge = viewModel::onAcknowledgeHardRefresh,
        )

        activeAchievement?.let { achievement ->
            AchievementNotification(
                achievement = achievement,
                onClick = {
                    navigator.navigateTo(Route.Achievements(achievement.id))
                },
                onFinished = { if (achievementQueue.isNotEmpty()) achievementQueue.removeAt(0) },
            )
        }

        AnimatedVisibility(
            visible = isPerksBankVisible,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            PerksBankDialog(
                vouchers = vouchersState.value,
                diamonds = diamondsProvider(),
                targetCategory = uiState.value.perksBankCategory,
                isProcessing = isVoucherProcessing,
                onPerkSelected = viewModel::onPerkFromVoucherSelected,
                onBuyClick = viewModel::onBuyPerk,
                onDismiss = viewModel::onDismissVoucherSelection,
            )
        }

        NicknamePopup(
            visible = isNicknamePopupVisibleState.value,
            name = tempNicknameState.value,
            onNameChanged = viewModel::onNicknameChanged,
            onConfirm = viewModel::onConfirmNickname,
            onDismiss = viewModel::onDismissNicknamePopup,
            isLoading = uiState.value.isBusy,
            error = nicknameErrorState.value,
        )

        if (uiState.value.activeDialog != null) {
            HexAlertDialog(
                state = uiState.value.activeDialog!!,
                onDismiss = viewModel::onDismissDialog,
            )
        }
    }
}
