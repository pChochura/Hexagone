package com.pointlessgames.hexagone.leaderboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.hexagone.game.model.DetailedGameResult
import com.pointlessgames.hexagone.game.ui.components.BottomSheetTitle
import com.pointlessgames.hexagone.game.ui.components.DialogTabButton
import com.pointlessgames.hexagone.game.ui.components.HexagonIconButton
import com.pointlessgames.hexagone.leaderboard.LeaderboardViewModel
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.ic_roll
import hexagone.shared.generated.resources.leaderboard_empty
import hexagone.shared.generated.resources.leaderboard_global
import hexagone.shared.generated.resources.leaderboard_max_piece
import hexagone.shared.generated.resources.leaderboard_name
import hexagone.shared.generated.resources.leaderboard_rank
import hexagone.shared.generated.resources.leaderboard_regional
import hexagone.shared.generated.resources.leaderboard_score
import hexagone.shared.generated.resources.leaderboard_title
import hexagone.shared.generated.resources.onboarding_submit
import hexagone.shared.generated.resources.onboarding_subtitle
import hexagone.shared.generated.resources.onboarding_title
import hexagone.shared.generated.resources.onboarding_username_placeholder
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LeaderboardDialog(
    viewModel: LeaderboardViewModel,
    onDismiss: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing

    LaunchedEffect(Unit) {
        viewModel.loadRankings()
    }

    ModalBottomSheet(
        contentWindowInsets = { WindowInsets.statusBars },
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Transparent,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BottomSheetTitle(text = stringResource(Res.string.leaderboard_title))

            Spacer(Modifier.height(spacing.extraLarge))

            if (uiState.playerName == null) {
                OnboardingContent(
                    onNameChanged = viewModel::onOnboardingNameChanged,
                    onCreateProfile = viewModel::onCreateProfile,
                    name = uiState.onboardingName,
                    isLoading = uiState.isCreatingProfile,
                    error = uiState.error,
                )
            } else {
                LeaderboardContent(
                    uiState = uiState,
                    onFilterChanged = viewModel::onFilterChanged,
                )
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
    error: String?,
) {
    val spacing = MaterialTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.onboarding_title).uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
        )

        Spacer(modifier = Modifier.height(spacing.medium))

        Text(
            text = stringResource(Res.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(spacing.extraLarge))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(Res.string.onboarding_username_placeholder),
                    color = Color.White.copy(alpha = 0.3f),
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
                unfocusedTextColor = Color.White,
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

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
        } else {
            HexagonIconButton(
                onClick = onCreateProfile,
                icon = Res.drawable.ic_roll,
                label = stringResource(Res.string.onboarding_submit),
                size = 80.dp,
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun LeaderboardContent(
    uiState: LeaderboardViewModel.UiState,
    onFilterChanged: (LeaderboardViewModel.Filter) -> Unit,
) {
    val spacing = MaterialTheme.spacing

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            DialogTabButton(
                text = stringResource(Res.string.leaderboard_global),
                isSelected = uiState.filter == LeaderboardViewModel.Filter.Global,
                onClick = { onFilterChanged(LeaderboardViewModel.Filter.Global) },
                modifier = Modifier.weight(1f),
            )
            DialogTabButton(
                text = stringResource(Res.string.leaderboard_regional),
                isSelected = uiState.filter == LeaderboardViewModel.Filter.Regional,
                onClick = { onFilterChanged(LeaderboardViewModel.Filter.Regional) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(spacing.medium))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.rankings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.leaderboard_empty),
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + MaterialTheme.spacing.small,
                    ),
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
                                thickness = 0.5.dp,
                            )
                        }
                    }
                }
            }
        }
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
            letterSpacing = 1.sp,
        )
        Text(
            text = stringResource(Res.string.leaderboard_name).uppercase(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
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
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.bodyMedium,
                color = if (rank <= 3) MaterialTheme.colorScheme.tertiary else Color.White.copy(
                    alpha = 0.6f,
                ),
                fontWeight = FontWeight.Black,
            )
        }
        Text(
            text = result.username ?: "Unknown",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
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
