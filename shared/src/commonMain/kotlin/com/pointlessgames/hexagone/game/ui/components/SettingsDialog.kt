package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing

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
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )

            Spacer(Modifier.height(MaterialTheme.spacing.extraLarge))

            if (!showConfirmRestart) {
                SettingsButton(
                    text = "Restart Game",
                    color = MaterialTheme.colorScheme.error,
                    onClick = { showConfirmRestart = true },
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Are you sure you want to restart?",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(MaterialTheme.spacing.medium))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    ) {
                        SettingsButton(
                            text = "Cancel",
                            modifier = Modifier.weight(1f),
                            onClick = { showConfirmRestart = false },
                        )
                        SettingsButton(
                            text = "Restart",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                onRestart()
                                onDismiss()
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(MaterialTheme.cornerRadius.medium))
            .background(color.copy(alpha = 0.1f))
            .border(
                1.dp,
                color.copy(alpha = 0.4f),
                RoundedCornerShape(MaterialTheme.cornerRadius.medium),
            )
            .clickable { onClick() }
            .padding(MaterialTheme.spacing.medium),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = 1.sp,
        )
    }
}
