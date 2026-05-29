package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.components.Position
import com.pointlessgames.hexagone.ui.components.Tooltip
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.choose_your_perk
import hexagone.shared.generated.resources.level_up_title
import hexagone.shared.generated.resources.perk_selection_hint
import hexagone.shared.generated.resources.reroll_perks
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PerkSelectionDialog(
    modifier: Modifier = Modifier,
    options: List<Perk>,
    pendingLevelUps: Int,
    canReroll: Boolean,
    onPerkSelected: (Perk) -> Unit,
    onRerollClicked: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "levelup_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                ),
            )
            .padding(top = spacing.colossal),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge))
                .border(
                    spacing.tiny,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge),
                )
                .navigationBarsPadding()
                .padding(horizontal = spacing.extraLarge, vertical = spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .padding(bottom = spacing.extraLarge)
                    .size(width = spacing.extraHuge, height = spacing.extraSmall)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.level_up_title),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                    )

                    if (pendingLevelUps > 1) {
                        Spacer(Modifier.width(spacing.medium))
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                                .border(spacing.extraTiny, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                                .padding(horizontal = spacing.small, vertical = spacing.tiny)
                        ) {
                            Text(
                                text = "+$pendingLevelUps",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (canReroll) {
                    Tooltip(
                        position = Position.BELOW,
                        contentDescription = Res.string.reroll_perks,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(spacing.extraHuge)
                                .clip(CircleShape)
                                .clickable { onRerollClicked() }
                                .padding(spacing.small),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎲",
                                fontSize = 22.sp,
                                modifier = Modifier.graphicsLayer { alpha = 0.6f }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(spacing.small))

            Text(
                text = stringResource(Res.string.choose_your_perk),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(spacing.extraHuge))

            AnimatedContent(
                targetState = options,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.8f))
                        .togetherWith(fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 0.8f))
                        .using(SizeTransform(clip = false))
                },
                label = "perk_refresh"
            ) { perkOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                ) {
                    perkOptions.forEach { perk ->
                        PerkButton(
                            perk = perk,
                            onClick = { onPerkSelected(perk) },
                            modifier = Modifier.weight(1f),
                            tooltipDescription = perk.descriptionRes,
                            buttonSize = spacing.giant
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.extraLarge))

            Text(
                text = stringResource(Res.string.perk_selection_hint),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
