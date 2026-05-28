package com.pointlessgames.hexagone.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.pointlessgames.hexagone.ui.CustomIndicationNodeFactory
import com.pointlessgames.hexagone.ui.TiltedRoundedCornersShape
import com.pointlessgames.hexagone.ui.dragIndication
import com.pointlessgames.hexagone.ui.theme.spacing
import org.jetbrains.compose.resources.DrawableResource

@Composable
internal fun Button(
    icon: DrawableResource?,
    text: String,
    defaultShape: TiltedRoundedCornersShape,
    pressedShape: TiltedRoundedCornersShape,
    defaultBackgroundColor: Color,
    pressedBackgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    val spacing = MaterialTheme.spacing

    Row(
        modifier = modifier
            .dragIndication()
            .clickable(
                onClick = onClick,
                role = Role.Button,
                indication = CustomIndicationNodeFactory(
                    defaultShape = defaultShape,
                    pressedShape = pressedShape,
                    defaultBackgroundColor = defaultBackgroundColor,
                    pressedBackgroundColor = pressedBackgroundColor,
                ),
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(
                horizontal = spacing.medium,
                vertical = spacing.small,
            ),
        horizontalArrangement = Arrangement.spacedBy(
            space = spacing.extraSmall,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LoadingIcon(
            isLoading = isLoading,
            icon = icon,
            contentColor = contentColor,
        )

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}
