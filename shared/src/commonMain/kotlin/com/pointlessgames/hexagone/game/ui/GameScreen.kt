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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.style.TextAlign
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.ScorePopup
import com.pointlessgames.hexagone.game.ui.components.GameGridOverlay
import com.pointlessgames.hexagone.game.ui.components.GameOverlays
import com.pointlessgames.hexagone.game.ui.components.LevelProgressSection
import com.pointlessgames.hexagone.game.ui.components.NextPieceSection
import com.pointlessgames.hexagone.game.ui.components.PerkBar
import com.pointlessgames.hexagone.game.ui.components.ScoreSection

@Composable
internal fun GameScreen(viewModel: GameViewModel) {
    val gridState by viewModel.gridState.collectAsState()
    val previewState by viewModel.previewState.collectAsState()
    val pendingMerge by viewModel.pendingMerge.collectAsState()
    val score by viewModel.score.collectAsState()
    val bestScore by viewModel.bestScore.collectAsState()
    val level by viewModel.level.collectAsState()
    val isStuck by viewModel.isStuck.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val perkOptions by viewModel.perkOptions.collectAsState()
    val collectedPerks by viewModel.collectedPerks.collectAsState()
    val activePerk by viewModel.activePerk.collectAsState()
    val selectedCellId by viewModel.selectedCellId.collectAsState()
    val hoveredMerge by viewModel.hoveredMerge.collectAsState()
    val combo by viewModel.combo.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val scorePopups by viewModel.scorePopups.collectAsState()

    val gridAlpha by animateFloatAsState(
        targetValue = if (isGameOver) 0.1f else 1f,
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
        ScoreSection(score = score, bestScore = bestScore)

        Box(modifier = Modifier.height(48.dp), contentAlignment = Alignment.Center) {
            this@Column.AnimatedVisibility(
                visible = combo > 0,
                enter = fadeIn() + scaleIn(initialScale = 0.5f),
                exit = fadeOut() + scaleOut(targetScale = 0.5f)
            ) {
                Text(
                    text = "COMBO x${combo + 1}!",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        NextPieceSection(
            previewState = previewState,
            activePerk = activePerk,
            selectedCellId = selectedCellId
        )

        Spacer(Modifier.height(24.dp))

        GameGridOverlay(
            gridState = gridState,
            previewState = previewState,
            pendingMerge = pendingMerge,
            hoveredMerge = hoveredMerge,
            activePerk = activePerk,
            selectedCellId = selectedCellId,
            particles = particles,
            scorePopups = scorePopups,
            combo = combo,
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
            collectedPerks = collectedPerks,
            activePerk = activePerk,
            onPerkClick = viewModel::onUsePerkClicked
        )

        Spacer(Modifier.height(16.dp))

        LevelProgressSection(
            level = level,
            progress = viewModel.getLevelProgress()
        )

        Spacer(Modifier.height(16.dp))
    }

    GameOverlays(
        isGameOver = isGameOver,
        isStuck = isStuck,
        perkOptions = perkOptions,
        collectedPerks = collectedPerks,
        onPerkSelected = viewModel::onPerkSelected,
        onUsePerk = viewModel::onUsePerkClicked,
        onRestart = viewModel::onRestartClicked
    )
}
