package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.MissionRefreshState
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MissionRefreshPopup(
    state: MissionRefreshState,
    targetRect: Rect?,
    onKeep: () -> Unit,
    onRefresh: () -> Unit,
    onAcknowledge: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state == MissionRefreshState.NONE) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {} // Consume clicks to make it non-dismissible
            )
    ) {
        AnimatedVisibility(
            visible = state != MissionRefreshState.NONE,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            val spacing = MaterialTheme.spacing
            val shape = RoundedCornerShape(MaterialTheme.cornerRadius.large.scaled)
            val density = LocalDensity.current
            
            val offset = if (targetRect != null) {
                IntOffset(
                    x = 0,
                    y = (targetRect.bottom + with(density) { 16.dp.toPx() }).toInt()
                )
            } else {
                IntOffset(0, with(density) { 100.dp.toPx() }.toInt())
            }

            Box(
                modifier = Modifier
                    .offset { offset }
                    .padding(horizontal = spacing.extraLarge.scaled)
                    .widthIn(max = 300.dp.scaled)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .border(1.dp.scaled, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), shape)
                    .padding(spacing.large.scaled)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val title = when (state) {
                        is MissionRefreshState.CAN_KEEP -> stringResource(Res.string.mission_refresh_restore_title)
                        is MissionRefreshState.HARD_REFRESH -> stringResource(Res.string.mission_refresh_lost_title)
                        else -> ""
                    }

                    Text(
                        text = title.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp.scaled,
                        letterSpacing = 1.sp.scaled
                    )

                    val desc = when (state) {
                        is MissionRefreshState.CAN_KEEP -> {
                            val date = state.oldDate
                            val formattedDate = "${date % 100}/${(date % 10000 / 100)}/${date / 10000}"
                            stringResource(Res.string.mission_refresh_restore_desc, formattedDate)
                        }
                        is MissionRefreshState.HARD_REFRESH -> stringResource(Res.string.mission_refresh_lost_desc)
                        else -> ""
                    }

                    Text(
                        text = desc,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp.scaled,
                        lineHeight = 18.sp.scaled,
                        textAlign = TextAlign.Center
                    )

                    if (state is MissionRefreshState.CAN_KEEP) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(spacing.small.scaled)
                        ) {
                            AuthButton(
                                text = stringResource(Res.string.mission_refresh_keep_button),
                                onClick = onKeep,
                                modifier = Modifier.fillMaxWidth()
                            )
                            AuthButton(
                                text = stringResource(Res.string.mission_refresh_reset_button),
                                onClick = onRefresh,
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            )
                        }
                    } else {
                        AuthButton(
                            text = stringResource(Res.string.tip_button_got_it),
                            onClick = onAcknowledge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled))
            .background(backgroundColor)
            .border(1.dp.scaled, borderColor, RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled))
            .clickable { onClick() }
            .padding(vertical = MaterialTheme.spacing.medium.scaled),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 12.sp.scaled,
            letterSpacing = 1.sp.scaled
        )
    }
}
