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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.ui.components.GameGridOverlay
import com.pointlessgames.hexagone.game.ui.components.GameOverlays
import com.pointlessgames.hexagone.game.ui.components.PerkBar
import com.pointlessgames.hexagone.game.ui.components.ScoreSection

@Composable
internal fun GameScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val gridAlpha by animateFloatAsState(
        targetValue = if (uiState.isGameOver) 0.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "grid_alpha",
    )

    val stuckDimAlpha by animateFloatAsState(
        targetValue = if (uiState.isStuck) 0.6f else 0f,
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1C1C24), Color(0xFF0A0A0E)),
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ScoreSection(
                    score = uiState.score,
                    bestScore = uiState.bestScore,
                    combo = uiState.combo,
                    level = uiState.level,
                    progress = viewModel.getLevelProgress(),
                    highestValue = uiState.highestValue,
                    activePerk = uiState.activePerk,
                    selectedCellId = uiState.selectedCellId,
                )

                Spacer(Modifier.weight(0.1f))

                GameGridOverlay(
                    gridState = uiState.grid,
                    mergeHints = uiState.mergeHints,
                    previewState = uiState.preview,
                    pendingMerge = uiState.pendingMerge,
                    hoveredMerge = uiState.hoveredMerge,
                    activePerk = uiState.activePerk,
                    selectedCellId = uiState.selectedCellId,
                    activeMergeStepIndex = uiState.activeMergeStepIndex,
                    pendingMergeScore = uiState.pendingMergeScore,
                    particles = uiState.particles,
                    scorePopups = uiState.scorePopups,
                    combo = uiState.combo,
                    onEmptySpaceClick = viewModel::onEmptySpaceClicked,
                    onEmptySpaceTouchDown = viewModel::onEmptySpaceTouchDown,
                    onEmptySpaceTouchUp = viewModel::onEmptySpaceTouchUp,
                    onCellClick = viewModel::onCellClicked,
                    onMergeAnimationFinished = viewModel::onMergeAnimationFinished,
                    onAddParticles = viewModel::addParticles,
                    onAddScorePopup = viewModel::addScorePopup,
                    modifier = Modifier.weight(1f, fill = false).fillMaxWidth(),
                )

                Spacer(Modifier.weight(0.1f))
            }
            
            // Placeholder to keep space for PerkBar
            Spacer(Modifier.height(130.dp))
        }

        // Dimming Layer (above board, below PerkBar)
        if (stuckDimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = stuckDimAlpha }
                    .background(Color.Black)
                    .clickable(
                        enabled = uiState.isStuck,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { /* Consume board clicks while stuck */ }
            )
        }

        // Floating UI Elements (Always on top)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            if (uiState.isStuck) {
                Box(
                    modifier = Modifier
                        .offset(y = (-10).dp + stuckBounce.dp)
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(8.dp))
                        .background(Color(0xFFF06292), RoundedCornerShape(8.dp))
                        .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "NO MOVES LEFT! USE A PERK",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            PerkBar(
                collectedPerks = uiState.collectedPerks,
                activePerk = uiState.activePerk,
                isStuck = uiState.isStuck,
                onPerkClick = viewModel::onUsePerkClicked,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    GameOverlays(
        isGameOver = uiState.isGameOver,
        perkOptions = uiState.perkOptions,
        collectedPerks = uiState.collectedPerks,
        onPerkSelected = viewModel::onPerkSelected,
        onUsePerk = viewModel::onUsePerkClicked,
        onRestart = viewModel::onRestartClicked,
    )
}
