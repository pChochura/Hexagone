package com.pointlessgames.hexagone.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.Route
import com.pointlessgames.hexagone.onboarding.OnboardingViewModel
import com.pointlessgames.hexagone.ui.LocalInnerPadding
import com.pointlessgames.hexagone.ui.TiltedRoundedCornersShape
import com.pointlessgames.hexagone.ui.components.ShapeButton
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.iconsSize
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.icon_play
import hexagone.shared.generated.resources.onboarding_submit
import hexagone.shared.generated.resources.onboarding_subtitle
import hexagone.shared.generated.resources.onboarding_title
import hexagone.shared.generated.resources.onboarding_username_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OnboardingScreen(
    viewModel: OnboardingViewModel,
) {
    val navigator = LocalNavigator.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = MaterialTheme.spacing
    val iconsSizes = MaterialTheme.iconsSize
    val cornerRadius = MaterialTheme.cornerRadius

    LaunchedEffect(uiState.isProfileCreated) {
        if (uiState.isProfileCreated) {
            navigator.navigateTo(Route.Start)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(LocalInnerPadding.current),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.onboarding_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(spacing.medium))

            Text(
                text = stringResource(Res.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(spacing.extraLarge))

            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::onUsernameChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = stringResource(Res.string.onboarding_username_placeholder))
                },
                singleLine = true,
                isError = uiState.error != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                ),
            )

            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(top = spacing.small),
                )
            }

            Spacer(modifier = Modifier.height(spacing.immense))

            ShapeButton(
                size = iconsSizes.extraLarge,
                iconSize = iconsSizes.medium,
                icon = Res.drawable.icon_play,
                contentDescription = Res.string.onboarding_submit,
                defaultShape = TiltedRoundedCornersShape(0f, cornerRadius.medium),
                pressedShape = TiltedRoundedCornersShape(0f, cornerRadius.large),
                defaultBackgroundColor = MaterialTheme.colorScheme.primary,
                pressedBackgroundColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                isLoading = uiState.isLoading,
                onClick = viewModel::onCreateProfile,
            )
        }
    }
}
