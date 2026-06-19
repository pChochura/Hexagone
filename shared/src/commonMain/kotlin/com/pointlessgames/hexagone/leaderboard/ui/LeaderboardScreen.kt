package com.pointlessgames.hexagone.leaderboard.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.game.model.DetailedGameResult
import com.pointlessgames.hexagone.game.ui.components.Hexagon
import com.pointlessgames.hexagone.game.ui.components.HexagonGridDefaults
import com.pointlessgames.hexagone.game.ui.components.ScreenScaffold
import com.pointlessgames.hexagone.leaderboard.LeaderboardViewModel
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.leaderboard_empty
import hexagone.shared.generated.resources.leaderboard_title
import hexagone.shared.generated.resources.unknown
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LeaderboardScreen(
    viewModel: LeaderboardViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val spacing = MaterialTheme.spacing

    LaunchedEffect(Unit) {
        viewModel.loadRankings()
    }

    ScreenScaffold(
        title = stringResource(Res.string.leaderboard_title),
        onBack = { navigator.pop() },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { clip = false },
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + spacing.medium.scaled,
                bottom = spacing.extraLarge.scaled,
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.small.scaled),
        ) {
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp.scaled),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else if (uiState.rankings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp.scaled),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.leaderboard_empty),
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp.scaled,
                        )
                    }
                }
            } else {
                itemsIndexed(uiState.rankings) { index, result ->
                    Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                        RankItem(rank = index + 1, result = result)
                    }
                }
            }
        }
    }
}

@Composable
private fun RankItem(rank: Int, result: DetailedGameResult) {
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled)
    val colorScheme = MaterialTheme.colorScheme

    val podiumColor = when (rank) {
        1 -> Color(0xFFFFD54F) // Gold
        2 -> Color(0xFFE0E0E0) // Silver
        3 -> Color(0xFFFFB74D) // Bronze
        else -> null
    }

    val isFirst = rank == 1
    val isPodium = rank <= 3

    val cardBackground = if (isPodium) {
        podiumColor!!.copy(alpha = if (isFirst) 0.1f else 0.05f)
    } else {
        MaterialTheme.colorScheme.background
    }

    val cardBorderColor = if (isPodium) {
        podiumColor!!.copy(alpha = 0.2f)
    } else {
        Color.White.copy(alpha = 0.05f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(shape)
            .background(cardBackground)
            .then(
                if (isFirst) {
                    val infiniteTransition =
                        rememberInfiniteTransition(label = "first_glow")
                    val glowAlpha = infiniteTransition.animateFloat(
                        initialValue = 0.1f,
                        targetValue = 0.4f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000),
                            repeatMode = RepeatMode.Reverse,
                        ),
                        label = "glow",
                    )
                    val strokeWidth = 1.dp.scaled
                    Modifier.drawWithCache {
                        val outline = shape.createOutline(size, layoutDirection, this)
                        onDrawBehind {
                            drawOutline(
                                outline = outline,
                                color = podiumColor!!.copy(alpha = glowAlpha.value),
                                style = Stroke(width = strokeWidth.toPx()),
                            )
                        }
                    }
                } else {
                    Modifier.border(1.dp.scaled, cardBorderColor, shape)
                }
            )
            .padding(spacing.large.scaled),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.large.scaled),
        ) {
            // Player Name with Integrated Rank
            val nameText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = podiumColor?.copy(alpha = 0.8f) ?: Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Black,
                    ),
                ) {
                    append("$rank. ")
                }
                withStyle(
                    style = SpanStyle(
                        color = if (isFirst) Color.White else Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append(result.username ?: stringResource(Res.string.unknown))
                }
            }

            Text(
                text = nameText,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp.scaled,
            )

            // Max Piece Hexagon
            Hexagon(
                value = result.maxPiece.toString(),
                backgroundColor = HexagonGridDefaults.getColorForValue(
                    result.maxPiece,
                    colorScheme,
                ).copy(alpha = 0.4f),
                isOutline = true,
                maxFontSize = 12.sp.scaled,
                modifier = Modifier
                    .size(24.dp.scaled)
                    .aspectRatio(1 / 0.866f),
                ignoreContrast = true,
            )

            // Score
            Text(
                text = result.score.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 20.sp.scaled,
                    letterSpacing = 1.sp.scaled,
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.End,
            )
        }
    }
}
