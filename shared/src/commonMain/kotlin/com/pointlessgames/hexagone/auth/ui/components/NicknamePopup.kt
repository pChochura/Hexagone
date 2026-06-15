package com.pointlessgames.hexagone.auth.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.ui.components.DialogContainer
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.utils.BackHandler
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NicknamePopup(
    visible: Boolean,
    name: String,
    onNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    val spacing = MaterialTheme.spacing
    
    BackHandler(enabled = visible) {
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            DialogContainer(
                modifier = Modifier.clickable(enabled = false) {},
                isProcessing = isLoading
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
                        value = name,
                        onValueChange = onNameChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.onboarding_username_placeholder),
                                color = Color.White.copy(alpha = 0.3f),
                            )
                        },
                        singleLine = true,
                        isError = error != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        ),
                    )

                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp.scaled),
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(top = spacing.small.scaled),
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.extraLarge.scaled))

                    AuthButton(
                        text = stringResource(Res.string.login_continue),
                        onClick = onConfirm,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }
            }
        }
    }
}
