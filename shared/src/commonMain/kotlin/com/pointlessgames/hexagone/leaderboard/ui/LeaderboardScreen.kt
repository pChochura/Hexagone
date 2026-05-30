package com.pointlessgames.hexagone.leaderboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.leaderboard.LeaderboardViewModel
import com.pointlessgames.hexagone.ui.LocalInnerPadding
import com.pointlessgames.hexagone.ui.TiltedRoundedCornersShape
import com.pointlessgames.hexagone.ui.components.ShapeButton
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.iconsSize
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.go_back
import hexagone.shared.generated.resources.icon_arrow_left
import hexagone.shared.generated.resources.leaderboard_empty
import hexagone.shared.generated.resources.leaderboard_global
import hexagone.shared.generated.resources.leaderboard_max_piece
import hexagone.shared.generated.resources.leaderboard_name
import hexagone.shared.generated.resources.leaderboard_rank
import hexagone.shared.generated.resources.leaderboard_regional
import hexagone.shared.generated.resources.leaderboard_score
import hexagone.shared.generated.resources.leaderboard_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LeaderboardScreen(
    viewModel: LeaderboardViewModel,
) {
    val navigator = LocalNavigator.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing
    val iconsSizes = MaterialTheme.iconsSize
    val cornerRadius = MaterialTheme.cornerRadius

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(LocalInnerPadding.current),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Header(
                onBack = { navigator.pop() },
                onFilterChanged = viewModel::onFilterChanged,
                currentFilter = uiState.filter,
            )

            Spacer(modifier = Modifier.height(spacing.medium))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Loading...", color = MaterialTheme.colorScheme.primary)
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
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(spacing.medium),
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
    }
}

@Composable
private fun Header(
    onBack: () -> Unit,
    onFilterChanged: (LeaderboardViewModel.Filter) -> Unit,
    currentFilter: LeaderboardViewModel.Filter,
) {
    val spacing = MaterialTheme.spacing
    val iconsSizes = MaterialTheme.iconsSize
    val cornerRadius = MaterialTheme.cornerRadius

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.medium),
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
            onClick = onBack,
        )

        Text(
            text = stringResource(Res.string.leaderboard_title),
            style = MaterialTheme.typography.headlineSmall,
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
            modifier = Modifier.width(50.dp),
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
            modifier = Modifier.width(80.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
        Text(
            text = stringResource(Res.string.leaderboard_max_piece),
            modifier = Modifier.width(80.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}

@Composable
private fun RankItem(rank: Int, result: com.pointlessgames.hexagone.game.model.DetailedGameResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#$rank",
            modifier = Modifier.width(50.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (rank <= 3) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (rank <= 3) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            text = result.username ?: "Unknown",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = result.score.toString(),
            modifier = Modifier.width(80.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
        Text(
            text = result.maxPiece.toString(),
            modifier = Modifier.width(80.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}
