package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GameOverBottomActions(
    modifier: Modifier = Modifier,
    onRestart: () -> Unit,
    onShare: () -> Unit,
    onLeaderboard: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = spacing.extraLarge, vertical = spacing.huge)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            HexagonIconButton(
                onClick = onShare,
                icon = Res.drawable.ic_share,
                label = stringResource(Res.string.share_label),
                tooltip = Res.string.tooltip_share,
                size = 56.dp
            )

            HexagonIconButton(
                onClick = onRestart,
                icon = Res.drawable.ic_play_again,
                label = stringResource(Res.string.play_again_button),
                size = 72.dp,
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            HexagonIconButton(
                onClick = onLeaderboard,
                icon = Res.drawable.ic_leaderboards,
                label = stringResource(Res.string.leaderboard_title),
                tooltip = Res.string.tooltip_leaderboard,
                size = 56.dp
            )
        }
    }
}
