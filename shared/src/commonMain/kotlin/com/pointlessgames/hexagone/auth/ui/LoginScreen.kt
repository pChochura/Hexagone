package com.pointlessgames.hexagone.auth.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.pointlessgames.hexagone.auth.ui.components.PlayfulTitle
import com.pointlessgames.hexagone.game.ui.components.DialogContainer
import com.pointlessgames.hexagone.game.ui.components.GameGridOverlay
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.*
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
            navigator.navigateTo(Route.Game)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Simulation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f)
                .graphicsLayer { scaleX = 1.2f; scaleY = 1.2f } // Slightly zoomed for effect
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
                comboProvider = { 0 },
                effects = viewModel.effects,
                onEmptySpaceClick = { _, _ -> },
                onEmptySpaceTouchDown = { _, _ -> },
                onEmptySpaceTouchUp = { },
                onCellTouchDown = { },
                onCellTouchUp = { },
                onCellClick = { },
                onMergeAnimationFinished = viewModel::onMergeAnimationFinished,
                modifier = Modifier.fillMaxSize()
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
                            MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                        ),
                    ),
                )
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.extraLarge.scaled),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PlayfulTitle()

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
                verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled)
            ) {
                LoginButton(
                    text = stringResource(Res.string.login_with_google),
                    onClick = viewModel::onSignInWithGoogle,
                    containerColor = Color.White.copy(alpha = 0.05f),
                    contentColor = Color.White
                )

                LoginButton(
                    text = stringResource(Res.string.login_with_apple),
                    onClick = viewModel::onSignInWithApple,
                    containerColor = Color.White.copy(alpha = 0.05f),
                    contentColor = Color.White
                )

                Spacer(modifier = Modifier.height(spacing.medium.scaled))

                LoginButton(
                    text = stringResource(Res.string.login_as_guest),
                    onClick = viewModel::onSignInAnonymously,
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            }
        }

        // Nickname Popup
        AnimatedVisibility(
            visible = uiState.showNicknamePopup,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { viewModel.onDismissNicknamePopup() },
                contentAlignment = Alignment.Center
            ) {
                DialogContainer(
                    modifier = Modifier.clickable(enabled = false) {},
                    isProcessing = uiState.isLoading
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.extraLarge.scaled),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.onboarding_title).uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                        
                        Spacer(modifier = Modifier.height(spacing.medium.scaled))
                        
                        Text(
                            text = stringResource(Res.string.onboarding_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(spacing.extraLarge.scaled))

                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = viewModel::onNameChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = stringResource(Res.string.onboarding_username_placeholder),
                                    color = Color.White.copy(alpha = 0.3f),
                                )
                            },
                            singleLine = true,
                            isError = uiState.error != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                            ),
                        )

                        if (uiState.error != null) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp.scaled),
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(top = spacing.small.scaled),
                            )
                        }

                        Spacer(modifier = Modifier.height(spacing.extraLarge.scaled))

                        LoginButton(
                            text = stringResource(Res.string.login_continue),
                            onClick = viewModel::onCreateProfile,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.1f)
) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(containerColor)
            .border(1.dp.scaled, borderColor, CircleShape)
            .clickable { onClick() }
            .padding(vertical = spacing.medium.scaled + 4.dp.scaled),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = contentColor,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp.scaled,
            letterSpacing = 1.sp.scaled
        )
    }
}
