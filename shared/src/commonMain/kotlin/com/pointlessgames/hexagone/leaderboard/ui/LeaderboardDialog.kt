package com.pointlessgames.hexagone.leaderboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.hexagone.game.model.DetailedGameResult
import com.pointlessgames.hexagone.leaderboard.LeaderboardViewModel
import com.pointlessgames.hexagone.ui.TiltedRoundedCornersShape
import com.pointlessgames.hexagone.ui.components.ShapeButton
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.iconsSize
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LeaderboardDialog(
    viewModel: LeaderboardViewModel,
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(cornerRadius.large),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(spacing.medium)
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
                            onFilterChanged = viewModel::onFilterChanged,
                            onDismiss = onDismiss
                        )
                    }
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
    val iconsSizes = MaterialTheme.iconsSize

    Column(
        modifier = Modifier.fillMaxSize().padding(spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.onboarding_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(spacing.medium))

        Text(
            text = stringResource(Res.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(spacing.extraLarge))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = stringResource(Res.string.onboarding_username_placeholder))
            },
            singleLine = true,
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
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

        Spacer(modifier = Modifier.height(spacing.large))

        ShapeButton(
            size = iconsSizes.extraLarge,
            iconSize = iconsSizes.medium,
            icon = Res.drawable.icon_play,
            contentDescription = Res.string.onboarding_submit,
            defaultShape = TiltedRoundedCornersShape(0f, cornerRadius.medium),
            pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.large),
            defaultBackgroundColor = MaterialTheme.colorScheme.primary,
            pressedBackgroundColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            isLoading = isLoading,
            onClick = onCreateProfile,
        )
    }
}

@Composable
private fun LeaderboardContent(
    uiState: LeaderboardViewModel.UiState,
    onFilterChanged: (LeaderboardViewModel.Filter) -> Unit,
    onDismiss: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val iconsSizes = MaterialTheme.iconsSize
    val cornerRadius = MaterialTheme.cornerRadius

    Header(
        onDismiss = onDismiss,
        onFilterChanged = onFilterChanged,
        currentFilter = uiState.filter,
    )

    Spacer(modifier = Modifier.height(spacing.medium))

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else if (uiState.rankings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(Res.string.leaderboard_empty),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(spacing.small),
        ) {
            item {
                RankHeader()
            }
            itemsIndexed(uiState.rankings) { index, result ->
                RankItem(rank = index + 1, result = result)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = spacing.small),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
private fun Header(
    onDismiss: () -> Unit,
    onFilterChanged: (LeaderboardViewModel.Filter) -> Unit,
    currentFilter: LeaderboardViewModel.Filter,
) {
    val spacing = MaterialTheme.spacing
    val iconsSizes = MaterialTheme.iconsSize
    val cornerRadius = MaterialTheme.cornerRadius

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ShapeButton(
            size = iconsSizes.medium,
            iconSize = iconsSizes.small,
            icon = Res.drawable.icon_arrow_left,
            contentDescription = Res.string.go_back,
            defaultShape = TiltedRoundedCornersShape(45f, cornerRadius.small),
            pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.small),
            defaultBackgroundColor = MaterialTheme.colorScheme.surface,
            pressedBackgroundColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = onDismiss,
        )

        Text(
            text = stringResource(Res.string.leaderboard_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            FilterButton(
                text = stringResource(Res.string.leaderboard_global),
                isSelected = currentFilter == LeaderboardViewModel.Filter.Global,
                onClick = { onFilterChanged(LeaderboardViewModel.Filter.Global) },
            )
            FilterButton(
                text = stringResource(Res.string.leaderboard_regional),
                isSelected = currentFilter == LeaderboardViewModel.Filter.Regional,
                onClick = { onFilterChanged(LeaderboardViewModel.Filter.Regional) },
            )
        }
    }
}

@Composable
private fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val cornerRadius = MaterialTheme.cornerRadius
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(cornerRadius.small),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun RankHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.leaderboard_rank),
            modifier = Modifier.width(40.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.leaderboard_name),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.leaderboard_score),
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
        Text(
            text = stringResource(Res.string.leaderboard_max_piece),
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun RankItem(rank: Int, result: DetailedGameResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#$rank",
            modifier = Modifier.width(40.dp),
            style = MaterialTheme.typography.bodySmall,
            color = if (rank <= 3) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (rank <= 3) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            text = result.username ?: "Unknown",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = result.score.toString(),
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
        )
        Text(
            text = result.maxPiece.toString(),
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.End,
        )
    }
}
