package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.GameTip
import com.pointlessgames.hexagone.game.model.TipTarget
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.tip_button_got_it
import org.jetbrains.compose.resources.stringResource

@Composable
fun TipOverlay(
    activeTip: GameTip?,
    targetRects: Map<TipTarget, Rect>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tip = activeTip ?: return
    val targetRect = targetRects[tip.targetType]
    val playButtonSound = com.pointlessgames.hexagone.utils.rememberPlayButtonSound()

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                enabled = true,
                onClick = {
                    playButtonSound()
                    onDismiss()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
    ) {
        if (targetRect != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val spotlightPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = targetRect.inflate(8.dp.toPx()),
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                    )
                }
                clipPath(spotlightPath, clipOp = ClipOp.Difference) {
                    drawRect(Color.Black.copy(alpha = 0.7f))
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(MaterialTheme.spacing.extraLarge)
                .align(if (tip.targetType == TipTarget.PERK_BAR || tip.targetType == TipTarget.GAME_OVER_BUTTONS) Alignment.TopCenter else Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(MaterialTheme.cornerRadius.medium))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(enabled = true, onClick = {}) // Consume clicks
                    .padding(MaterialTheme.spacing.large)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                ) {
                    Text(
                        text = stringResource(tip.message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )

                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(MaterialTheme.cornerRadius.small)
                    ) {
                        Text(
                            text = stringResource(Res.string.tip_button_got_it),
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

fun Modifier.trackTipTarget(
    target: TipTarget,
    onTargetPositioned: (TipTarget, Rect) -> Unit
): Modifier = this.onGloballyPositioned { coordinates ->
    val position = coordinates.positionInRoot()
    val size = coordinates.size
    onTargetPositioned(
        target,
        Rect(position.x, position.y, position.x + size.width, position.y + size.height)
    )
}
