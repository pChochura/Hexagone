package com.pointlessgames.hexagone.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import com.pointlessgames.hexagone.ui.CustomIndicationNodeFactory
import com.pointlessgames.hexagone.ui.TiltedRoundedCornersShape
import com.pointlessgames.hexagone.ui.dragIndication
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ShapeButton(
    size: Dp,
    iconSize: Dp,
    icon: DrawableResource,
    contentDescription: StringResource,
    defaultShape: TiltedRoundedCornersShape,
    pressedShape: TiltedRoundedCornersShape,
    defaultBackgroundColor: Color,
    pressedBackgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    dragForce: Float = 0.03f,
    tooltipPosition: Position = Position.ABOVE,
    onClick: () -> Unit,
) {
    Tooltip(
        position = tooltipPosition,
        contentDescription = contentDescription,
    ) {
        Box(
            modifier = modifier
                .size(size)
                .dragIndication(isEnabled, dragForce)
                .clickable(
                    enabled = isEnabled,
                    onClick = onClick,
                    role = Role.Button,
                    indication = CustomIndicationNodeFactory(
                        defaultShape = defaultShape,
                        pressedShape = pressedShape,
                        defaultBackgroundColor = defaultBackgroundColor,
                        pressedBackgroundColor = pressedBackgroundColor,
                    ),
                    interactionSource = remember { MutableInteractionSource() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(isEnabled) { isEnabled ->
                LoadingIcon(
                    isLoading = isLoading,
                    icon = icon,
                    contentColor = contentColor.copy(alpha = if (isEnabled) 1f else 0.2f),
                    contentDescription = stringResource(contentDescription),
                    size = iconSize,
                )
            }
        }
    }
}
