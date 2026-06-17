package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.utils.rememberPlayButtonSound
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.cancel
import hexagone.shared.generated.resources.confirm
import hexagone.shared.generated.resources.done
import hexagone.shared.generated.resources.ic_delete
import hexagone.shared.generated.resources.ic_diamond
import hexagone.shared.generated.resources.ic_star
import hexagone.shared.generated.resources.shop_insufficient_balance
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
)

/**
 * Atom: A standardized alert dialog for Success, Error, and Confirmation messages.
 */
@Composable
fun HexAlertDialog(
    state: com.pointlessgames.hexagone.game.model.HexDialogState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // Animated Entrance
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(300)) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(300),
                ),
                modifier = Modifier.fillMaxWidth(0.92f),
            ) {
                DialogContainer(modifier = modifier) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = spacing.extraLarge.scaled,
                                vertical = spacing.large.scaled,
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                    ) {
                        val (title, message, isError, onConfirm, icon) = when (state) {
                            is com.pointlessgames.hexagone.game.model.HexDialogState.Confirmation -> {
                                val msg =
                                    stringResource(state.message, *state.formatArgs.toTypedArray())
                                Quintuple(
                                    stringResource(state.title),
                                    msg,
                                    false,
                                    state.onConfirm,
                                    Res.drawable.ic_diamond,
                                )
                            }

                            is com.pointlessgames.hexagone.game.model.HexDialogState.Info -> {
                                val msg = if (state.messageText != null) {
                                    state.messageText
                                } else if (state.message != null) {
                                    stringResource(state.message, *state.formatArgs.toTypedArray())
                                } else ""
                                Quintuple(
                                    stringResource(state.title),
                                    msg,
                                    state.isError,
                                    null,
                                    if (state.isError) Res.drawable.ic_delete else Res.drawable.ic_star,
                                )
                            }
                        }

                        // Large Type-based Icon
                        Box(
                            modifier = Modifier
                                .size(64.dp.scaled)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(12.dp.scaled),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(icon),
                                contentDescription = null,
                                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        Spacer(Modifier.height(spacing.extraSmall.scaled))

                        Text(
                            text = title.uppercase(),
                            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp.scaled,
                            letterSpacing = 2.sp.scaled,
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text = message,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp.scaled,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp.scaled,
                        )

                        Spacer(Modifier.height(spacing.medium.scaled))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                spacing.medium.scaled,
                                Alignment.CenterHorizontally,
                            ),
                        ) {
                            val playButtonSound = rememberPlayButtonSound()
                            if (onConfirm != null) {
                                // Cancel Option
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                                        .border(
                                            1.dp.scaled,
                                            Color.White.copy(alpha = 0.1f),
                                            CircleShape,
                                        )
                                        .clickable {
                                            playButtonSound()
                                            onDismiss()
                                        }
                                        .padding(vertical = spacing.medium.scaled),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.cancel).uppercase(),
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp.scaled,
                                    )
                                }

                                // Primary Confirm Action with Pulse
                                val infiniteTransition =
                                    rememberInfiniteTransition(label = "btn_pulse")
                                val pulseAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.8f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000),
                                        repeatMode = RepeatMode.Reverse,
                                    ),
                                    label = "alpha",
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                                            CircleShape,
                                        )
                                        .clickable {
                                            playButtonSound()
                                            onConfirm()
                                            onDismiss()
                                        }
                                        .padding(vertical = spacing.medium.scaled),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.confirm).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp.scaled,
                                    )
                                }
                            } else {
                                // OK / Dismiss Button
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .clickable {
                                            playButtonSound()
                                            onDismiss()
                                        }
                                        .padding(vertical = spacing.medium.scaled),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.done).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp.scaled,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Atom: A reusable surface for dialogs with the standard Hexagone styling.
 * Padding is removed from here to allow scrollables to reach the edges.
 */
@Composable
fun DialogContainer(
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val cornerRadius = MaterialTheme.cornerRadius

    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .safeDrawingPadding()
            .clip(RoundedCornerShape(cornerRadius.extraLarge.scaled))
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                RoundedCornerShape(cornerRadius.extraLarge.scaled),
            )
            .border(
                1.dp.scaled,
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(cornerRadius.extraLarge.scaled),
            )
            .border(
                2.dp.scaled,
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(cornerRadius.extraLarge.scaled),
            ),
    ) {
        content()

        // Processing Overlay covering the whole dialog surface
        AnimatedVisibility(
            visible = isProcessing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.matchParentSize(),
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/**
 * Atom: Refactored diamond balance display for the top-right corner.
 * Uses a solid background and handles its own positioning.
 */
@Composable
fun DiamondBalanceBadge(
    diamonds: Int,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    var previousDiamonds by remember { mutableStateOf(diamonds) }
    var displayedDiamonds by remember { mutableStateOf(diamonds) }
    var animatedDelta by remember { mutableStateOf(0) }

    val popScale = remember { Animatable(1f) }
    val dropOffset = remember { Animatable(0f) }
    val deltaAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(diamonds) {
        val diff = diamonds - previousDiamonds
        if (diff > 0) {
            // Popup Increase
            displayedDiamonds = diamonds
            popScale.snapTo(1.4f)
            popScale.animateTo(1f, tween(400))
        } else if (diff < 0) {
            // Drop Decrease
            animatedDelta = diff
            deltaAlpha.snapTo(1f)
            dropOffset.snapTo(0f)

            // Parallel animation for falling red text
            scope.launch {
                dropOffset.animateTo(30f, tween(1000))
            }
            scope.launch {
                deltaAlpha.animateTo(0f, tween(1000))
                animatedDelta = 0
            }
            displayedDiamonds = diamonds
        }
        previousDiamonds = diamonds
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = popScale.value
                    scaleY = popScale.value
                }
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp.scaled))
                .border(
                    1.dp.scaled,
                    Color(0xFFFFD54F).copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp.scaled),
                )
                .padding(horizontal = spacing.medium.scaled, vertical = spacing.small.scaled),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_diamond),
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(16.dp.scaled),
            )
            Spacer(Modifier.width(spacing.extraSmall.scaled))
            Text(
                text = displayedDiamonds.toString(),
                color = Color(0xFFFFD54F),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp.scaled,
            )
        }

        // Falling Delta Animation
        if (animatedDelta < 0) {
            Text(
                text = animatedDelta.toString(),
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp.scaled,
                modifier = Modifier
                    .offset(y = dropOffset.value.dp.scaled)
                    .alpha(deltaAlpha.value),
            )
        }
    }
}

/**
 * Atom: Standardized title for shop sections.
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp.scaled,
        letterSpacing = 1.sp.scaled,
        modifier = modifier.padding(vertical = spacing.medium.scaled),
    )
}

/**
 * Molecule: Functional unit for displaying products (diamonds or perk bundles).
 * Uses solid backgrounds.
 */
@Composable
fun ProductCard(
    title: String,
    price: String,
    description: String = "",
    label: String? = null,
    leadingIcon: org.jetbrains.compose.resources.DrawableResource? = null,
    leadingIconTint: Color = Color.Unspecified,
    costInDiamonds: Int? = null,
    hasEnoughDiamonds: Boolean = true,
    isEnabled: Boolean = true,
    footerContent: @Composable (ColumnScope.() -> Unit)? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playSound = rememberPlayButtonSound()
    val spacing = MaterialTheme.spacing
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp.scaled))
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(16.dp.scaled))
                .border(
                    width = if (label != null) 1.dp.scaled else 0.dp,
                    color = if (label != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp.scaled),
                )
                .alpha(if (hasEnoughDiamonds) 1f else 0.6f)
                .clickable(enabled = isEnabled && hasEnoughDiamonds) { playSound(); onClick() }
                .padding(spacing.large.scaled),
            horizontalArrangement = Arrangement.spacedBy(spacing.large.scaled),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp.scaled)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(8.dp.scaled),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(leadingIcon),
                        contentDescription = null,
                        tint = leadingIconTint,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp.scaled,
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp.scaled,
                        lineHeight = 14.sp.scaled,
                    )
                }
                if (!hasEnoughDiamonds) {
                    Text(
                        text = stringResource(Res.string.shop_insufficient_balance).uppercase(),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp.scaled,
                        modifier = Modifier.padding(top = 2.dp.scaled),
                    )
                }

                footerContent?.invoke(this)
            }

            if (costInDiamonds != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = costInDiamonds.toString(),
                        color = if (hasEnoughDiamonds) Color(0xFFFFD54F) else Color.Gray,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp.scaled,
                    )
                    Spacer(Modifier.width(spacing.small.scaled))
                    Icon(
                        painter = painterResource(Res.drawable.ic_diamond),
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(18.dp.scaled),
                    )
                }
            } else {
                Text(
                    text = price,
                    color = Color(0xFF81C784),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp.scaled,
                )
            }
        }

        if (label != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 12.dp.scaled, y = (-10).dp.scaled)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp.scaled))
                    .padding(horizontal = 8.dp.scaled, vertical = 2.dp.scaled),
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp.scaled,
                )
            }
        }
    }
}

/**
 * Molecule: A compact grid item for product display in the Shop.
 */
@Composable
fun ProductGridItem(
    title: String,
    price: String,
    description: String = "",
    label: String? = null,
    iconScale: Float = 1f,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playSound = rememberPlayButtonSound()
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(MaterialTheme.cornerRadius.medium.scaled)

    Box(
        modifier = modifier.aspectRatio(0.8f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .clickable(enabled = isEnabled) { playSound(); onClick() }
                .background(MaterialTheme.colorScheme.background)
                .border(1.dp.scaled, Color.White.copy(alpha = 0.05f), shape)
                .padding(spacing.medium.scaled)
                .padding(bottom = spacing.medium.scaled),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Icon / Visual Area
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_diamond),
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size((48.dp.scaled * iconScale)),
                    )
                }

                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp.scaled,
                    textAlign = TextAlign.Center,
                )

                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp.scaled,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(spacing.medium.scaled))

                Text(
                    text = price,
                    color = Color(0xFF81C784),
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp.scaled,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (label != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp.scaled, y = (-4).dp.scaled)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp.scaled))
                    .padding(horizontal = 6.dp.scaled, vertical = 2.dp.scaled),
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp.scaled,
                )
            }
        }
    }
}

/**
 * Organism: A unified layout for full-screen feature screens.
 */
@Composable
fun ScreenScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    topBarTrailingContent: @Composable (RowScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val spacing = MaterialTheme.spacing
    var headerHeight by remember { mutableStateOf(0) }
    val density = androidx.compose.ui.platform.LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background),
                ),
            ),
    ) {
        // Content
        val topPadding = remember(headerHeight) {
            with(density) { headerHeight.toDp() }
        }
        content(PaddingValues(top = topPadding))

        // Translucent Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { headerHeight = it.size.height }
                .background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        0.7f to MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        1f to Color.Transparent,
                    ),
                )
                .statusBarsPadding()
                .padding(horizontal = spacing.extraLarge.scaled),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.large.scaled, bottom = spacing.extraLarge.scaled),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
            ) {
                HexBackButton(onClick = onBack)

                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp.scaled,
                        letterSpacing = 4.sp.scaled,
                    ),
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )

                topBarTrailingContent?.invoke(this)
            }
        }
    }
}
