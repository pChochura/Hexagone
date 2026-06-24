package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.pointlessgames.hexagone.game.model.GameTip
import com.pointlessgames.hexagone.game.model.TipTarget
import com.pointlessgames.hexagone.ui.components.Position
import com.pointlessgames.hexagone.ui.components.Tooltip
import com.pointlessgames.hexagone.ui.components.rememberTooltipState
import kotlin.math.roundToInt

@Composable
fun TipOverlay(
    activeTip: GameTip?,
    targetRects: Map<String, Rect>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tip = activeTip ?: return
    val targetRect = targetRects[tip.targetId] ?: return
    val density = LocalDensity.current

    val tooltipState = rememberTooltipState(initialIsVisible = true, isPersistent = true)
    LaunchedEffect(tooltipState.isVisible) {
        if (!tooltipState.isVisible) {
            onDismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset { IntOffset(targetRect.left.roundToInt(), targetRect.top.roundToInt()) }
                .size(
                    width = with(density) { targetRect.width.toDp() },
                    height = with(density) { targetRect.height.toDp() },
                ),
        ) {
            Tooltip(
                position = if (tip.targetId.startsWith("PERK_") || tip.targetId == TipTarget.GAME_OVER_BUTTONS) Position.ABOVE else Position.BELOW,
                contentDescription = tip.message,
                icon = tip.icon,
                state = tooltipState,
                allowUserInput = false,
                content = { Box(Modifier.fillMaxSize()) },
            )
        }
    }
}

fun Modifier.trackTipTarget(
    targetId: String,
    onTargetPositioned: (String, Rect) -> Unit,
): Modifier = this.onGloballyPositioned { coordinates ->
    val position = coordinates.positionInRoot()
    val size = coordinates.size
    onTargetPositioned(
        targetId,
        Rect(position.x, position.y, position.x + size.width, position.y + size.height),
    )
}
