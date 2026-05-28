package com.pointlessgames.hexagone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pointlessgames.hexagone.ui.TiltedRoundedCornersShape
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun Dialog(
    icon: DrawableResource,
    title: StringResource,
    primaryButtonText: StringResource,
    secondaryButtonText: StringResource,
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spacing = MaterialTheme.spacing

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.extraLarge)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.medium,
                )
                .padding(
                    vertical = spacing.extraLarge,
                    horizontal = spacing.extraLarge,
                ),
            verticalArrangement = Arrangement.spacedBy(spacing.medium, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopRow(
                icon = icon,
                title = title,
            )

            content()

            BottomRow(
                primaryButtonText = primaryButtonText,
                secondaryButtonText = secondaryButtonText,
                onPrimaryButtonClick = onPrimaryButtonClick,
                onSecondaryButtonClick = onSecondaryButtonClick,
            )
        }
    }
}

@Composable
private fun TopRow(
    icon: DrawableResource,
    title: StringResource,
) {
    val spacing = MaterialTheme.spacing
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun BottomRow(
    primaryButtonText: StringResource,
    secondaryButtonText: StringResource,
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit,
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.medium, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            icon = null,
            text = stringResource(secondaryButtonText),
            defaultShape = TiltedRoundedCornersShape(0f, cornerRadius.small),
            pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.medium),
            defaultBackgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0f),
            pressedBackgroundColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            onClick = onSecondaryButtonClick,
        )

        Button(
            icon = null,
            text = stringResource(primaryButtonText),
            defaultShape = TiltedRoundedCornersShape(0f, cornerRadius.small),
            pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.medium),
            defaultBackgroundColor = MaterialTheme.colorScheme.primary,
            pressedBackgroundColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onClick = onPrimaryButtonClick,
        )
    }
}
