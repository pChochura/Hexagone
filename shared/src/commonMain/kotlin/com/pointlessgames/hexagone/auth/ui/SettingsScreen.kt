package com.pointlessgames.hexagone.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.Route
import com.pointlessgames.hexagone.auth.SettingsViewModel
import com.pointlessgames.hexagone.auth.ui.components.AuthButton
import com.pointlessgames.hexagone.auth.ui.components.NicknamePopup
import com.pointlessgames.hexagone.game.ui.components.HexAlertDialog
import com.pointlessgames.hexagone.game.ui.components.ScreenScaffold
import com.pointlessgames.hexagone.game.model.HexDialogState
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val spacing = MaterialTheme.spacing
    var showRemoveAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAccountInfo()
    }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            viewModel.consumeLoggedOut()
            navigator.replaceAll(Route.Login)
        }
    }

    ScreenScaffold(
        title = stringResource(Res.string.settings_label),
        onBack = { navigator.pop() },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
                .padding(bottom = spacing.extraLarge.scaled),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.extraLarge.scaled)
                    .padding(top = spacing.medium.scaled),
                verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled)
            ) {
                AccountCard(
                    nickname = uiState.nickname,
                    isAnonymous = uiState.isAnonymous,
                    onClick = viewModel::onShowNicknamePopup
                )
                
                ToggleCard(
                    label = stringResource(Res.string.settings_sound),
                    checked = uiState.isSoundEnabled,
                    onCheckedChange = { viewModel.toggleSound() }
                )

                Spacer(modifier = Modifier.height(spacing.medium.scaled))

                AuthButton(
                    text = stringResource(Res.string.settings_logout_button),
                    onClick = viewModel::logout,
                    containerColor = Color.White.copy(alpha = 0.05f),
                    contentColor = Color.White
                )
            }

            Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                AuthButton(
                    text = stringResource(Res.string.settings_remove_account_button),
                    onClick = { showRemoveAccountDialog = true },
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.error,
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                )
            }
        }
    }

    if (showRemoveAccountDialog) {
        HexAlertDialog(
            state = HexDialogState.Confirmation(
                title = Res.string.settings_remove_account_button,
                message = Res.string.settings_remove_account_confirmation,
                onConfirm = {
                    viewModel.removeAccount()
                    showRemoveAccountDialog = false
                }
            ),
            onDismiss = { showRemoveAccountDialog = false }
        )
    }

    NicknamePopup(
        visible = uiState.showNicknamePopup,
        name = uiState.nickname,
        onNameChanged = viewModel::onNicknameChanged,
        onConfirm = viewModel::updateNickname,
        onDismiss = viewModel::onDismissNicknamePopup,
        isLoading = uiState.isLoading,
        error = uiState.error
    )
}

@Composable
private fun AccountCard(
    nickname: String,
    isAnonymous: Boolean,
    onClick: () -> Unit
) {
    val playSound = com.pointlessgames.hexagone.utils.rememberPlayButtonSound()
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .clickable { playSound(); onClick() }
            .padding(spacing.large.scaled)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (isAnonymous) stringResource(Res.string.settings_anonymous_account)
                else stringResource(Res.string.settings_logged_in_as, nickname),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp.scaled,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(spacing.extraSmall.scaled))

            Text(
                text = nickname,
                color = Color.White,
                fontSize = 20.sp.scaled,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun ToggleCard(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val playSound = com.pointlessgames.hexagone.utils.rememberPlayButtonSound()
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .clickable { playSound(); onCheckedChange(!checked) }
            .padding(spacing.large.scaled),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp.scaled,
            fontWeight = FontWeight.Bold
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
            )
        )
    }
}
