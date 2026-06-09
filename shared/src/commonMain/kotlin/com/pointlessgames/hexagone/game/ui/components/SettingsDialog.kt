package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.cancel
import hexagone.shared.generated.resources.ic_back
import hexagone.shared.generated.resources.ic_play_again
import hexagone.shared.generated.resources.restart_confirmation
import hexagone.shared.generated.resources.restart_game_button
import hexagone.shared.generated.resources.settings_label
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onRestart: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showConfirmRestart by remember { mutableStateOf(false) }

    ModalBottomSheet(
        contentWindowInsets = { WindowInsets.statusBars },
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color.Transparent,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.2f)) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.extraLarge)
                .padding(bottom = MaterialTheme.spacing.extraLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BottomSheetTitle(text = stringResource(Res.string.settings_label))

            Spacer(Modifier.height(MaterialTheme.spacing.extraLarge))

            if (!showConfirmRestart) {
                HexagonIconButton(
                    label = stringResource(Res.string.restart_game_button),
                    icon = Res.drawable.ic_play_again,
                    onClick = { showConfirmRestart = true },
                    backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                    size = 80.dp,
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.restart_confirmation),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.extraLarge))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        HexagonIconButton(
                            label = stringResource(Res.string.cancel),
                            icon = Res.drawable.ic_back,
                            onClick = { showConfirmRestart = false },
                            size = 72.dp,
                        )
                        Spacer(Modifier.width(MaterialTheme.spacing.extraLarge))
                        HexagonIconButton(
                            label = stringResource(Res.string.restart_game_button),
                            icon = Res.drawable.ic_play_again,
                            onClick = {
                                onRestart()
                                onDismiss()
                            },
                            backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                            borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                            size = 72.dp,
                        )
                    }
                }
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
