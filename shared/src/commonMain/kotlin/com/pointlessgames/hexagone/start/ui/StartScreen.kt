package com.pointlessgames.hexagone.start.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.start.StartViewModel
import com.pointlessgames.hexagone.ui.LocalInnerPadding
import com.pointlessgames.hexagone.ui.TiltedRoundedCornersShape
import com.pointlessgames.hexagone.ui.components.ShapeButton
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.iconsSize
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.app_name
import hexagone.shared.generated.resources.continue_where_you_left_off
import hexagone.shared.generated.resources.go_to_daily_challenge
import hexagone.shared.generated.resources.go_to_level_creator
import hexagone.shared.generated.resources.icon_calendar
import hexagone.shared.generated.resources.icon_play
import hexagone.shared.generated.resources.icon_wrench
import hexagone.shared.generated.resources.start_game
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun StartScreen(
    viewModel: StartViewModel,
) {
    val navigator = LocalNavigator.current
    val spacing = MaterialTheme.spacing
    val iconsSizes = MaterialTheme.iconsSize
    val cornerRadius = MaterialTheme.cornerRadius

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(LocalInnerPadding.current),
    ) {
        Logo()

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = spacing.immense),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShapeButton(
                size = iconsSizes.extraLarge,
                iconSize = iconsSizes.medium,
                icon = Res.drawable.icon_play,
                contentDescription = Res.string.start_game,
                defaultShape = TiltedRoundedCornersShape(0f, cornerRadius.medium),
                pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.large),
                defaultBackgroundColor = MaterialTheme.colorScheme.primary,
                pressedBackgroundColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {},
            )

            Text(
                modifier = Modifier.fillMaxWidth(0.5f),
                text = stringResource(Res.string.continue_where_you_left_off),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(spacing.extraLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShapeButton(
                size = iconsSizes.large,
                iconSize = iconsSizes.small,
                icon = Res.drawable.icon_wrench,
                contentDescription = Res.string.go_to_level_creator,
                defaultShape = TiltedRoundedCornersShape(45f, cornerRadius.medium),
                pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.medium),
                defaultBackgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                pressedBackgroundColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                onClick = {},
            )

            ShapeButton(
                size = iconsSizes.large,
                iconSize = iconsSizes.small,
                icon = Res.drawable.icon_calendar,
                contentDescription = Res.string.go_to_daily_challenge,
                defaultShape = TiltedRoundedCornersShape(-45f, cornerRadius.medium),
                pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.medium),
                defaultBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                pressedBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = {},
            )
        }
    }
}

@Composable
private fun Logo() {
    val appName = stringResource(Res.string.app_name).uppercase()
    val letters = appName.map { it.toString() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MaterialTheme.spacing.extraLarge,
                vertical = MaterialTheme.spacing.immense,
            ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        letters.forEach { letter ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(
                        color = Color.DarkGray,
                        shape = MaterialTheme.shapes.small,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
