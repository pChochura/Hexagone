package com.pointlessgames.hexagone.leaderboard.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.hexagone.game.model.DetailedGameResult
import com.pointlessgames.hexagone.game.ui.components.SecondaryGameButton
import com.pointlessgames.hexagone.leaderboard.LeaderboardViewModel
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LeaderboardOverlay(
    modifier: Modifier = Modifier,
    viewModel: LeaderboardViewModel,
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        viewModel.loadRankings()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "leaderboard_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.85f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Header
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(bottom = spacing.large),
            contentAlignment = Alignment.Center
        ) {
            SecondaryGameButton(
                onClick = onDismiss,
                icon = "⬅",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(spacing.huge)
            )

            Text(
                text = stringResource(Res.string.leaderboard_title).uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    shadow = Shadow(
                        color = primaryColor.copy(alpha = 0.5f),
                        offset = Offset(0f, 0f),
                        blurRadius = 20f * (0.8f + 0.2f * (glowAlpha / 0.3f))
                    )
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
                .drawBehind {
                    val cr = cornerRadius.extraLarge.toPx()
                    val path = Path().apply {
                        addRoundRect(RoundRect(Rect(0f, 0f, size.width, size.height), cr, cr))
                    }

                    for (i in 1..3) {
                        drawPath(
                            path = path,
                            color = primaryColor.copy(alpha = glowAlpha / (i * 2f)),
                            style = Stroke(width = (spacing.extraSmall * i).toPx())
                        )
                    }
                }
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    RoundedCornerShape(cornerRadius.extraLarge)
                )
                .border(
                    spacing.extraTiny,
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(cornerRadius.extraLarge)
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.playerName == null) {
                    OnboardingContent(
                        onNameChanged = viewModel::onOnboardingNameChanged,
                        onCreateProfile = viewModel::onCreateProfile,
                        name = uiState.onboardingName,
                        isLoading = uiState.isCreatingProfile,
                        error = uiState.error
                    )
                } else {
                    LeaderboardContent(
                        uiState = uiState,
                        onFilterChanged = viewModel::onFilterChanged
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingContent(
    onNameChanged: (String) -> Unit,
    onCreateProfile: () -> Unit,
    name: String,
    isLoading: Boolean,
    error: String?
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.onboarding_title).uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(spacing.medium))

        Text(
            text = stringResource(Res.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(spacing.extraLarge))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(Res.string.onboarding_username_placeholder),
                    color = Color.White.copy(alpha = 0.3f)
                )
            },
            singleLine = true,
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = spacing.small),
            )
        }

        Spacer(modifier = Modifier.height(spacing.extraLarge))

        Button(
            onClick = onCreateProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.giant),
            enabled = !isLoading,
            shape = RoundedCornerShape(cornerRadius.medium),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(Res.string.onboarding_submit).uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

@Composable
private fun LeaderboardContent(
    uiState: LeaderboardViewModel.UiState,
    onFilterChanged: (LeaderboardViewModel.Filter) -> Unit
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge)
                )
                .padding(spacing.small),
            horizontalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            FilterButton(
                text = stringResource(Res.string.leaderboard_global),
                isSelected = uiState.filter == LeaderboardViewModel.Filter.Global,
                onClick = { onFilterChanged(LeaderboardViewModel.Filter.Global) },
                modifier = Modifier.weight(1f)
            )
            FilterButton(
                text = stringResource(Res.string.leaderboard_regional),
                isSelected = uiState.filter == LeaderboardViewModel.Filter.Regional,
                onClick = { onFilterChanged(LeaderboardViewModel.Filter.Regional) },
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.large)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.rankings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.leaderboard_empty),
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = spacing.small),
                ) {
                    item {
                        RankHeader()
                    }
                    itemsIndexed(uiState.rankings) { index, result ->
                        RankItem(rank = index + 1, result = result)
                        if (index < uiState.rankings.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = spacing.extraSmall),
                                color = Color.White.copy(alpha = 0.08f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cornerRadius = MaterialTheme.cornerRadius
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(cornerRadius.medium)
    val primaryColor = MaterialTheme.colorScheme.primary

    val brush = remember(isSelected, primaryColor) {
        if (isSelected) {
            Brush.verticalGradient(
                listOf(primaryColor, primaryColor.copy(alpha = 0.7f))
            )
        } else {
            Brush.verticalGradient(
                listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.01f))
            )
        }
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .background(brush, shape)
            .border(
                width = spacing.extraTiny,
                color = if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                shape = shape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun RankHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.leaderboard_rank).uppercase(),
            modifier = Modifier.width(45.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Text(
            text = stringResource(Res.string.leaderboard_name).uppercase(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        Text(
            text = stringResource(Res.string.leaderboard_score).uppercase(),
            modifier = Modifier.width(65.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            textAlign = TextAlign.End,
        )
        Text(
            text = stringResource(Res.string.leaderboard_max_piece).uppercase(),
            modifier = Modifier.width(65.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun RankItem(rank: Int, result: DetailedGameResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(45.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.bodyMedium,
                color = if (rank <= 3) MaterialTheme.colorScheme.tertiary else Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Black,
            )
        }
        Text(
            text = result.username ?: "Unknown",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = result.score.toString(),
            modifier = Modifier.width(65.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.End,
        )
        Text(
            text = result.maxPiece.toString(),
            modifier = Modifier.width(65.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.End,
        )
    }
}
