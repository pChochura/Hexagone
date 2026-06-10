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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.components.Position
import com.pointlessgames.hexagone.ui.components.Tooltip
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.ui.theme.scaled
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun PerkSelectionDialog(
    modifier: Modifier = Modifier,
    options: List<Perk>,
    pendingLevelUps: Int,
    canReroll: Boolean,
    onPerkSelected: (Perk) -> Unit,
    onRerollClicked: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            // Prevent hiding the sheet entirely to satisfy "cannot be closed without selecting a perk"
            newValue != SheetValue.Hidden
        }
    )
    val scope = rememberCoroutineScope()

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

    ModalBottomSheet(
        onDismissRequest = { /* ModalBottomSheet handles its own state, we prevent Hidden via confirmValueChange */ },
        sheetState = sheetState,
        scrimColor = Color.Black.copy(alpha = 0.2f),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = Color.White,
        tonalElevation = 8.dp.scaled,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color.White.copy(alpha = 0.2f),
                width = spacing.extraHuge.scaled,
                height = spacing.extraSmall.scaled
            )
        },
        shape = RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.extraLarge.scaled)
                .padding(bottom = spacing.extraLarge.scaled),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = if (canReroll) spacing.extraHuge.scaled else 0.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.level_up_title),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp.scaled,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                    )

                    if (pendingLevelUps > 1) {
                        Spacer(Modifier.width(spacing.medium.scaled))
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                                .border(spacing.extraTiny.scaled, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                                .padding(horizontal = spacing.small.scaled, vertical = spacing.tiny.scaled)
                        ) {
                            Text(
                                text = stringResource(Res.string.pending_levels, pendingLevelUps),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp.scaled
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
                                .size(spacing.extraHuge.scaled)
                                .clip(CircleShape)
                                .clickable { onRerollClicked() }
                                .padding(spacing.small.scaled),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_roll),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp.scaled).graphicsLayer { alpha = 0.6f },
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(spacing.small.scaled))

            Text(
                text = stringResource(Res.string.choose_your_perk),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp.scaled,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(spacing.extraHuge.scaled))

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
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                ) {
                    perkOptions.forEach { perk ->
                        PerkButton(
                            perk = perk,
                            onClick = {
                                scope.launch {
                                    sheetState.hide()
                                    onPerkSelected(perk)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            tooltipDescription = perk.descriptionRes,
                            buttonSize = spacing.giant.scaled
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.extraLarge.scaled))

            Text(
                text = stringResource(Res.string.perk_selection_hint),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp.scaled,
                textAlign = TextAlign.Center,
            )
        }
    }
}
