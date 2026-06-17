package com.pointlessgames.hexagone.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.Route
import com.pointlessgames.hexagone.auth.LoginViewModel
import com.pointlessgames.hexagone.auth.ui.components.AuthButton
import com.pointlessgames.hexagone.auth.ui.components.NicknamePopup
import com.pointlessgames.hexagone.game.ui.components.GameGridOverlay
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.app_name
import hexagone.shared.generated.resources.login_as_guest
import hexagone.shared.generated.resources.login_subtitle
import hexagone.shared.generated.resources.login_with_apple
import hexagone.shared.generated.resources.login_with_google
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginScreen(
    viewModel: LoginViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val spacing = MaterialTheme.spacing

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.consumeSuccess()
            navigator.replaceAll(Route.Game)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Simulation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f)
                .graphicsLayer { scaleX = 1.2f; scaleY = 1.2f }, // Slightly zoomed for effect
        ) {
            GameGridOverlay(
                gridState = uiState.backgroundGrid,
                onBoardPerksProvider = { emptyList() },
                mergeHintsProvider = { emptyList() },
                previewState = uiState.backgroundPreviews,
                pendingMergeProvider = { uiState.pendingMerge },
                hoveredMergeState = remember { MutableStateFlow(null) },
                potentialMergesProvider = { emptyMap() },
                activePerkProvider = { null },
                selectedCellIdProvider = { null },
                activeMergeStepIndexProvider = { uiState.activeMergeStepIndex },
                effects = viewModel.effects,
                onEmptySpaceClick = { _, _ -> },
                onEmptySpaceTouchDown = { _, _ -> },
                onEmptySpaceTouchUp = { },
                onCellTouchDown = { },
                onCellTouchUp = { },
                onCellClick = { },
                onMergeAnimationFinished = viewModel::onMergeAnimationFinished,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Overlay Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                        ),
                    ),
                ),
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.extraLarge.scaled),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.app_name).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 32.sp.scaled,
                    letterSpacing = 2.sp.scaled,
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center,
                autoSize = TextAutoSize.StepBased(),
            )

            Text(
                text = stringResource(Res.string.login_subtitle).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp.scaled,
                    letterSpacing = 2.sp.scaled,
                ),
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(80.dp.scaled))

            Column(
                modifier = Modifier.fillMaxWidth(0.8f),
                verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
            ) {
                AuthButton(
                    text = stringResource(Res.string.login_with_google),
                    onClick = viewModel::onSignInWithGoogle,
                    containerColor = Color.White.copy(alpha = 0.05f),
                    contentColor = Color.White,
                )

                AuthButton(
                    text = stringResource(Res.string.login_with_apple),
                    onClick = viewModel::onSignInWithApple,
                    containerColor = Color.White.copy(alpha = 0.05f),
                    contentColor = Color.White,
                )

                Spacer(modifier = Modifier.height(spacing.medium.scaled))

                AuthButton(
                    text = stringResource(Res.string.login_as_guest),
                    onClick = viewModel::onSignInAnonymously,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                )
            }
        }

        // Nickname Popup
        NicknamePopup(
            visible = uiState.showNicknamePopup,
            name = uiState.name,
            onNameChanged = viewModel::onNameChanged,
            onConfirm = viewModel::onCreateProfile,
            onDismiss = viewModel::onDismissNicknamePopup,
            isLoading = uiState.isLoading,
            error = uiState.error,
        )
    }
}
