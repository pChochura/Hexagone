package com.pointlessgames.hexagone.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.Route
import com.pointlessgames.hexagone.auth.LoginViewModel
import com.pointlessgames.hexagone.game.ui.components.HexagonIconButton
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.*
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background),
                ),
            )
            .padding(spacing.extraLarge.scaled),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.login_title).uppercase(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 42.sp.scaled,
                    letterSpacing = 4.sp.scaled,
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
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

            Spacer(modifier = Modifier.height(64.dp.scaled))

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

            Spacer(modifier = Modifier.height(48.dp.scaled))

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp.scaled),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp.scaled,
                )
            } else {
                HexagonIconButton(
                    onClick = viewModel::onCreateProfile,
                    icon = Res.drawable.ic_roll,
                    label = stringResource(Res.string.onboarding_submit),
                    size = 100.dp.scaled,
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                )
            }
        }
    }
}
