package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun StatusDialog(
    isGameOver: Boolean,
    collectedPerks: List<Perk>,
    onUsePerk: (Perk) -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                ),
            )
            .padding(top = spacing.giant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge))
                .border(
                    spacing.extraTiny,
                    Color.White.copy(alpha = 0.08f),
                    RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge),
                )
                .navigationBarsPadding()
                .padding(horizontal = spacing.extraLarge, vertical = spacing.semiLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .padding(bottom = spacing.semiLarge)
                    .size(width = spacing.extraHuge, height = spacing.extraSmall)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                Canvas(
                    modifier = Modifier
                        .size(spacing.extraHuge)
                        .border(spacing.extraTiny, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
                        .padding(spacing.small)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                ) {
                    val strokeWidth = spacing.tiny.toPx()
                    drawCircle(
                        color = primaryColor,
                        radius = size.minDimension / 2.5f,
                        style = Stroke(width = strokeWidth),
                    )
                    val radius = size.minDimension / 2.5f
                    val angle = 45f * (PI.toFloat() / 180f)
                    drawLine(
                        color = primaryColor,
                        start = center + Offset(cos(angle) * radius, sin(angle) * radius),
                        end = center - Offset(cos(angle) * radius, sin(angle) * radius),
                        strokeWidth = strokeWidth,
                    )
                }

                Spacer(Modifier.width(spacing.large))

                Column {
                    Text(
                        text = if (isGameOver) stringResource(Res.string.game_over_title) else stringResource(Res.string.no_more_moves_title),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                    Text(
                        text = if (isGameOver) stringResource(Res.string.game_over_subtitle) else stringResource(Res.string.try_perk_subtitle),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                    )
                }
            }

            Spacer(Modifier.height(spacing.extraLarge))

            val displayPerks = collectedPerks.distinct().filter { it.canSaveFromStuck }

            if (displayPerks.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    displayPerks.forEach { perk ->
                        val count = collectedPerks.count { it == perk }
                        PerkButton(
                            perk = perk,
                            onClick = { onUsePerk(perk) },
                            count = count,
                            tooltipDescription = perk.descriptionRes,
                            buttonSize = spacing.extraHuge
                        )
                        Spacer(Modifier.width(spacing.medium))
                    }
                }
                Spacer(Modifier.height(spacing.medium))
            }

            HexagonIconButton(
                onClick = onRestart,
                icon = Res.drawable.ic_play_again,
                label = stringResource(Res.string.restart_game_button),
                size = 72.dp,
                backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
            )
        }
    }
}
