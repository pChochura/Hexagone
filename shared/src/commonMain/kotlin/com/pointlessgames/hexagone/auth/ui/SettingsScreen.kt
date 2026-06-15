package com.pointlessgames.hexagone.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.pointlessgames.hexagone.game.ui.components.HexAlertDialog
import com.pointlessgames.hexagone.game.ui.components.HexagonIconButton
import com.pointlessgames.hexagone.game.ui.components.ScreenScaffold
import com.pointlessgames.hexagone.game.ui.components.ShopSectionTitle
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

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            navigator.navigateTo(Route.Login)
        }
    }

    ScreenScaffold(
        title = stringResource(Res.string.settings_label),
        onBack = { navigator.pop() },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + spacing.medium.scaled,
                bottom = spacing.extraLarge.scaled,
            ),
        ) {
            item {
                Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                    ShopSectionTitle(text = stringResource(Res.string.settings_account_section))
                }
            }

            item {
                Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                    AccountCard(
                        nickname = uiState.nickname,
                        isAnonymous = uiState.isAnonymous,
                        onNicknameChanged = viewModel::onNicknameChanged,
                        onUpdateNickname = viewModel::updateNickname,
                        isLoading = uiState.isLoading,
                        error = uiState.error
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(spacing.large.scaled))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.extraLarge.scaled),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled)
                ) {
                    val buttonWeight = 1f
                    
                    HexagonIconButton(
                        label = stringResource(Res.string.settings_logout_button),
                        icon = Res.drawable.ic_back,
                        onClick = viewModel::logout,
                        modifier = Modifier.weight(buttonWeight),
                        size = 80.dp.scaled,
                        backgroundColor = Color.White.copy(alpha = 0.05f),
                        borderColor = Color.White.copy(alpha = 0.1f)
                    )

                    HexagonIconButton(
                        label = stringResource(Res.string.settings_remove_account_button),
                        icon = Res.drawable.ic_delete,
                        onClick = { showRemoveAccountDialog = true },
                        modifier = Modifier.weight(buttonWeight),
                        size = 80.dp.scaled,
                        backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                }
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
}

@Composable
private fun AccountCard(
    nickname: String,
    isAnonymous: Boolean,
    onNicknameChanged: (String) -> Unit,
    onUpdateNickname: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .padding(spacing.large.scaled)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled)) {
            Text(
                text = if (isAnonymous) stringResource(Res.string.settings_anonymous_account)
                else stringResource(Res.string.settings_logged_in_as, nickname),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp.scaled,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.settings_nickname_label)) },
                placeholder = { Text(stringResource(Res.string.settings_nickname_hint)) },
                singleLine = true,
                isError = error != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp.scaled
                )
            }

            Button(
                onClick = onUpdateNickname,
                modifier = Modifier.align(Alignment.End),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp.scaled), strokeWidth = 2.dp.scaled)
                } else {
                    Text(stringResource(Res.string.settings_change_nickname_button))
                }
            }
        }
    }
}
