package com.pointlessgames.hexagone.game.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import com.pointlessgames.hexagone.leaderboard.LeaderboardViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.ui.components.DebugOverlay
import com.pointlessgames.hexagone.game.ui.components.GameGridOverlay
import com.pointlessgames.hexagone.game.ui.components.GameOverlays
import com.pointlessgames.hexagone.game.ui.components.PerkBar
import com.pointlessgames.hexagone.game.ui.components.ScoreSection
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.utils.BackHandler
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.no_moves_left_warning
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    var showLeaderboard by remember { mutableStateOf(false) }
    val leaderboardViewModel: LeaderboardViewModel = koinViewModel()

    BackHandler(enabled = showLeaderboard) {
        showLeaderboard = false
    }

    val onEmptySpaceClick = remember(viewModel) { viewModel::onEmptySpaceClicked }
    val onEmptySpaceTouchDown = remember(viewModel) { viewModel::onEmptySpaceTouchDown }
    val onEmptySpaceTouchUp = remember(viewModel) { viewModel::onEmptySpaceTouchUp }
    val onCellTouchDown = remember(viewModel) { viewModel::onCellTouchDown }
    val onCellTouchUp = remember(viewModel) { viewModel::onCellTouchUp }
    val onCellClick = remember(viewModel) { viewModel::onCellClicked }
    val onMergeAnimationFinished = remember(viewModel) { viewModel::onMergeAnimationFinished }
    val onPerkClick = remember(viewModel) { viewModel::onUsePerkClicked }
    val onPerkSelected = remember(viewModel) { viewModel::onPerkSelected }
    val onRestart = remember(viewModel) { viewModel::onRestartClicked }
    val onViewBoardToggle = remember(viewModel) { viewModel::onViewBoardToggled }
    val onDebugToggle = remember(viewModel) { viewModel::toggleDebugMode }
    val onDebugCellClick = remember(viewModel) { viewModel::onDebugCellClicked }

    val gridAlpha by animateFloatAsState(
        targetValue = if (uiState.isGameOver && !uiState.showGameOverBoard) 0.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "grid_alpha",
    )

    val stuckDimAlpha by animateFloatAsState(
        targetValue = if (uiState.isStuck && uiState.activePerk == null) 0.6f else 0f,
        animationSpec = tween(500),
        label = "stuck_dim_alpha",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "stuck_pulse")
    val stuckBounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "stuck_bounce",
    )

    // Stable Providers for GameGridOverlay
    val mergeHintsState = androidx.compose.runtime.rememberUpdatedState(uiState.mergeHints)
    val previewState = androidx.compose.runtime.rememberUpdatedState(uiState.preview)
    val gridState = androidx.compose.runtime.rememberUpdatedState(uiState.grid)
    val onBoardPerksState = androidx.compose.runtime.rememberUpdatedState(uiState.onBoardPerks)
    val potentialMergesState = remember {
        androidx.compose.runtime.derivedStateOf {
            uiState.grid
            uiState.activePerk
            viewModel.getPotentialMerges()
        }
    }
    val pendingMergeState = androidx.compose.runtime.rememberUpdatedState(uiState.pendingMerge)
    val activeMergeStepIndexState = androidx.compose.runtime.rememberUpdatedState(uiState.activeMergeStepIndex)
    val pendingMergeScoreState = androidx.compose.runtime.rememberUpdatedState(uiState.pendingMergeScore)
    val comboState = androidx.compose.runtime.rememberUpdatedState(uiState.combo)
    val activePerkState = androidx.compose.runtime.rememberUpdatedState(uiState.activePerk)
    val selectedCellIdState = androidx.compose.runtime.rememberUpdatedState(uiState.selectedCellId)

    val mergeHintsProvider = remember { { mergeHintsState.value } }
    val previewStateProvider = remember { { previewState.value } }
    val gridStateProvider = remember { { gridState.value } }
    val onBoardPerksProvider = remember { { onBoardPerksState.value } }
    val potentialMergesProvider = remember { { potentialMergesState.value } }
    val pendingMergeProvider = remember { { pendingMergeState.value } }
    val activeMergeStepIndexProvider = remember { { activeMergeStepIndexState.value } }
    val pendingMergeScoreProvider = remember { { pendingMergeScoreState.value } }
    val comboProvider = remember { { comboState.value } }
    val activePerkProvider = remember { { activePerkState.value } }
    val selectedCellIdProvider = remember { { selectedCellIdState.value } }

    LaunchedEffect(uiState.pendingResult) {
        if (uiState.pendingResult != null) {
            leaderboardViewModel.setPendingResult(uiState.pendingResult)
            showLeaderboard = true
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
            )
    ) {
        // Main Board Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .graphicsLayer {
                    alpha = gridAlpha
                    clip = false
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { clip = false }
                    .padding(horizontal = MaterialTheme.spacing.large, vertical = MaterialTheme.spacing.small),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!uiState.isDebugMode) {
                    ScoreSection(
                        score = uiState.score,
                        bestScore = uiState.bestScore,
                        combo = uiState.combo,
                        level = uiState.level,
                        progress = viewModel.getLevelProgress(),
                        highestValue = uiState.highestValue,
                        activePerk = uiState.activePerk,
                        selectedCellId = uiState.selectedCellId,
                        onLevelClick = onDebugToggle,
                        onLeaderboardClick = { showLeaderboard = true }
                    )

                    Spacer(Modifier.weight(0.1f))
                }

                GameGridOverlay(
                    gridStateProvider = gridStateProvider,
                    onBoardPerksProvider = onBoardPerksProvider,
                    mergeHintsProvider = mergeHintsProvider,
                    previewStateProvider = previewStateProvider,
                    pendingMergeProvider = pendingMergeProvider,
                    hoveredMergeState = viewModel.hoveredMerge,
                    potentialMergesProvider = potentialMergesProvider,
                    activePerkProvider = activePerkProvider,
                    selectedCellIdProvider = selectedCellIdProvider,
                    activeMergeStepIndexProvider = activeMergeStepIndexProvider,
                    pendingMergeScoreProvider = pendingMergeScoreProvider,
                    comboProvider = comboProvider,
                    effects = viewModel.effects,
                    onEmptySpaceClick = if (uiState.isDebugMode) onDebugCellClick else onEmptySpaceClick,
                    onEmptySpaceTouchDown = if (uiState.isDebugMode) { _, _ -> } else onEmptySpaceTouchDown,
                    onEmptySpaceTouchUp = onEmptySpaceTouchUp,
                    onCellTouchDown = if (uiState.isDebugMode) { _ -> } else onCellTouchDown,
                    onCellTouchUp = onCellTouchUp,
                    onCellClick = if (uiState.isDebugMode) { cell -> onDebugCellClick(cell.x, cell.y) } else onCellClick,
                    onMergeAnimationFinished = onMergeAnimationFinished,
                    modifier = Modifier.weight(1f, fill = false).fillMaxWidth(),
                )

                if (!uiState.isDebugMode) {
                    Spacer(Modifier.weight(0.1f))
                }
            }
            
            if (!uiState.isDebugMode) {
                // Placeholder to keep space for PerkBar
                Spacer(Modifier.height(MaterialTheme.spacing.immense))
            } else {
                DebugOverlay(
                    isVisible = true,
                    selectedValue = uiState.debugSelectedValue,
                    isGhostMode = uiState.debugAddAsGhost,
                    onValueSelected = viewModel::setDebugSelectedValue,
                    onGhostModeToggled = viewModel::toggleDebugAddAsGhost,
                    onPerkClick = viewModel::addPerkManually,
                    onClose = onDebugToggle,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Dimming Layer (above board, below PerkBar)
        if (stuckDimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = stuckDimAlpha }
                    .background(Color.Black)
                    .clickable(
                        enabled = uiState.isStuck && uiState.activePerk == null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { /* Consume board clicks while stuck */ }
            )
        }

        // Floating UI Elements (Always on top)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { clip = false }
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            if (!uiState.isDebugMode) {
                if (uiState.isStuck && uiState.activePerk == null) {
                    Box(
                        modifier = Modifier
                            .offset(y = -MaterialTheme.spacing.semiMedium + stuckBounce.dp)
                            .shadow(
                                elevation = MaterialTheme.spacing.medium,
                                shape = RoundedCornerShape(MaterialTheme.cornerRadius.small)
                            )
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(MaterialTheme.cornerRadius.small)
                            )
                            .border(
                                MaterialTheme.spacing.tiny,
                                Color.White.copy(alpha = 0.5f),
                                RoundedCornerShape(MaterialTheme.cornerRadius.small)
                            )
                            .padding(
                                horizontal = MaterialTheme.spacing.medium,
                                vertical = MaterialTheme.spacing.semiSmall
                            ),
                    ) {
                        Text(
                            text = stringResource(Res.string.no_moves_left_warning),
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                        )
                    }
                    Spacer(Modifier.height(MaterialTheme.spacing.medium))
                }

                PerkBar(
                    collectedPerks = uiState.collectedPerks,
                    activePerk = uiState.activePerk,
                    isStuck = uiState.isStuck,
                    stuckPerks = uiState.stuckPerks,
                    onPerkClick = onPerkClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { clip = false }
                )
            }
        }

        GameOverlays(
            isGameOver = uiState.isGameOver,
            scoreProvider = { uiState.score },
            bestScore = uiState.bestScore,
            level = uiState.level,
            maxCombo = uiState.maxCombo,
            totalMerges = uiState.totalMerges,
            highestValue = uiState.highestValue,
            showBoard = uiState.showGameOverBoard,
            perkOptions = uiState.perkOptions,
            pendingLevelUps = uiState.pendingLevelUps,
            canReroll = uiState.canReroll,
            onPerkSelected = onPerkSelected,
            onRerollClicked = viewModel::onRerollClicked,
            onRestart = onRestart,
            onViewBoardToggle = onViewBoardToggle,
            onShare = { /* TODO: Implement snapshot and share */ },
            onLeaderboard = { showLeaderboard = true },
            showLeaderboard = showLeaderboard,
            onLeaderboardDismiss = { showLeaderboard = false },
            leaderboardViewModel = leaderboardViewModel
        )
    }
}
