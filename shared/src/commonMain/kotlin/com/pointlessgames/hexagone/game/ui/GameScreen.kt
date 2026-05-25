package com.pointlessgames.hexagone.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.ui.components.GameGridOverlay
import com.pointlessgames.hexagone.game.ui.components.GameOverlays
import com.pointlessgames.hexagone.game.ui.components.LevelProgressSection
import com.pointlessgames.hexagone.game.ui.components.NextPieceSection
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1C1C24), Color(0xFF0A0A0E)),
                ),
            )
            .systemBarsPadding()
            .graphicsLayer { alpha = gridAlpha }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScoreSection(score = uiState.score, bestScore = uiState.bestScore)

        Box(modifier = Modifier.height(48.dp), contentAlignment = Alignment.Center) {
            this@Column.AnimatedVisibility(
                visible = uiState.combo > 0,
                enter = fadeIn() + scaleIn(initialScale = 0.5f),
                exit = fadeOut() + scaleOut(targetScale = 0.5f)
            ) {
                Text(
                    text = "COMBO x${uiState.combo + 1}!",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        NextPieceSection(
            previewState = uiState.preview,
            activePerk = uiState.activePerk,
            selectedCellId = uiState.selectedCellId
        )

        Spacer(Modifier.height(24.dp))

        GameGridOverlay(
            gridState = uiState.grid,
            previewState = uiState.preview,
            pendingMerge = uiState.pendingMerge,
            hoveredMerge = uiState.hoveredMerge,
            activePerk = uiState.activePerk,
            selectedCellId = uiState.selectedCellId,
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
            modifier = Modifier.weight(1f).fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        PerkBar(
            collectedPerks = uiState.collectedPerks,
            activePerk = uiState.activePerk,
            onPerkClick = viewModel::onUsePerkClicked
        )

        Spacer(Modifier.height(16.dp))

        LevelProgressSection(
            level = uiState.level,
            progress = viewModel.getLevelProgress()
        )

        Spacer(Modifier.height(16.dp))
    }

    GameOverlays(
        isGameOver = uiState.isGameOver,
        isStuck = uiState.isStuck,
        perkOptions = uiState.perkOptions,
        collectedPerks = uiState.collectedPerks,
        onPerkSelected = viewModel::onPerkSelected,
        onUsePerk = viewModel::onUsePerkClicked,
        onRestart = viewModel::onRestartClicked
    )
}
