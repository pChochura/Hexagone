package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.play_again_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GameOverBottomActions(
    modifier: Modifier = Modifier,
    onRestart: () -> Unit,
    onShare: () -> Unit,
    onLeaderboard: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
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
            horizontalArrangement = Arrangement.spacedBy(spacing.large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SecondaryGameButton(
                onClick = onShare,
                icon = "⤴",
                modifier = Modifier.size(spacing.giant)
            )

            Button(
                onClick = onRestart,
                modifier = Modifier
                    .weight(1f)
                    .height(spacing.giant)
                    .graphicsLayer {
                        shadowElevation = spacing.small.toPx()
                        shape = RoundedCornerShape(cornerRadius.medium)
                        clip = true
                    },
                shape = RoundedCornerShape(cornerRadius.medium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = spacing.small)
            ) {
                Text(
                    text = stringResource(Res.string.play_again_button).uppercase(),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.5.sp
                )
            }

            SecondaryGameButton(
                onClick = onLeaderboard,
                icon = "🏆",
                modifier = Modifier.size(spacing.giant)
            )
        }
    }
}
