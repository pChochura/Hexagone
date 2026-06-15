package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.components.Position
import com.pointlessgames.hexagone.ui.components.Tooltip
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.ic_advance
import hexagone.shared.generated.resources.ic_chain_merge
import hexagone.shared.generated.resources.ic_delete
import hexagone.shared.generated.resources.ic_diamond
import hexagone.shared.generated.resources.ic_duplicate
import hexagone.shared.generated.resources.ic_freeze
import hexagone.shared.generated.resources.ic_fusion
import hexagone.shared.generated.resources.ic_legendary_perk
import hexagone.shared.generated.resources.ic_move
import hexagone.shared.generated.resources.ic_path_merge
import hexagone.shared.generated.resources.ic_pause
import hexagone.shared.generated.resources.ic_rare_perk
import hexagone.shared.generated.resources.ic_roll
import hexagone.shared.generated.resources.ic_star
import hexagone.shared.generated.resources.ic_swap
import hexagone.shared.generated.resources.ic_undo
import hexagone.shared.generated.resources.ic_upgrade
import hexagone.shared.generated.resources.perk_advance_queue_desc
import hexagone.shared.generated.resources.perk_advance_queue_name
import hexagone.shared.generated.resources.perk_chain_merge_desc
import hexagone.shared.generated.resources.perk_chain_merge_name
import hexagone.shared.generated.resources.perk_duplicate_tile_desc
import hexagone.shared.generated.resources.perk_duplicate_tile_name
import hexagone.shared.generated.resources.perk_freeze_tile_desc
import hexagone.shared.generated.resources.perk_freeze_tile_name
import hexagone.shared.generated.resources.perk_fusion_desc
import hexagone.shared.generated.resources.perk_fusion_name
import hexagone.shared.generated.resources.perk_increment_tile_desc
import hexagone.shared.generated.resources.perk_increment_tile_name
import hexagone.shared.generated.resources.perk_mimic_desc
import hexagone.shared.generated.resources.perk_mimic_name
import hexagone.shared.generated.resources.perk_move_tile_desc
import hexagone.shared.generated.resources.perk_move_tile_name
import hexagone.shared.generated.resources.perk_path_merge_desc
import hexagone.shared.generated.resources.perk_path_merge_name
import hexagone.shared.generated.resources.perk_remove_tile_desc
import hexagone.shared.generated.resources.perk_remove_tile_name
import hexagone.shared.generated.resources.perk_skip_spawn_desc
import hexagone.shared.generated.resources.perk_skip_spawn_name
import hexagone.shared.generated.resources.perk_swap_tiles_desc
import hexagone.shared.generated.resources.perk_swap_tiles_name
import hexagone.shared.generated.resources.perk_undo_desc
import hexagone.shared.generated.resources.perk_undo_name
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sqrt

object HexagonGridDefaults {
    fun getColorForValue(value: Int, colorScheme: ColorScheme): Color {
        if (value <= 0) return Color.Transparent

        // Base signature colors from screenshot
        val baseColors = mapOf(
            1 to colorScheme.surfaceContainerLowest,
            2 to colorScheme.surfaceContainerLow,
            4 to colorScheme.surfaceContainer,
            8 to colorScheme.surfaceContainerHigh,
            16 to colorScheme.surfaceContainerHighest,
        )

        baseColors[value]?.let { return it }

        // Algorithmic color generation for infinite granularity
        // Using golden ratio for hue distribution to keep colors distinct
        val hue = (value * 137.508f) % 360f
        val saturation = 0.6f + (value % 4) * 0.1f
        val brightness = 0.7f + (value % 3) * 0.1f

        return Color.hsv(hue, saturation.coerceAtMost(1f), brightness.coerceAtMost(1f))
    }

    fun getColorForPerk(perk: Perk, colorScheme: ColorScheme): Color {
        return when (perk) {
            Perk.UNDO -> colorScheme.outline
            Perk.MOVE_TILE -> colorScheme.inversePrimary
            Perk.REMOVE_TILE -> colorScheme.error
            Perk.FUSION -> colorScheme.tertiaryContainer
            Perk.SWAP_TILES -> colorScheme.secondaryContainer
            Perk.CHAIN_MERGE -> colorScheme.primaryContainer
            Perk.ADVANCE_QUEUE -> colorScheme.onTertiaryContainer
            Perk.DUPLICATE_TILE -> colorScheme.secondary
            Perk.SKIP_SPAWN -> colorScheme.tertiary
            Perk.INCREMENT_TILE -> colorScheme.primary
            Perk.PATH_MERGE -> colorScheme.errorContainer
            Perk.FREEZE_TILE -> colorScheme.onSecondaryContainer
            Perk.MIMIC -> colorScheme.inversePrimary
        }
    }

    fun getHexagonPath(size: Size): Path {
        val width = size.width
        val height = size.height
        return Path().apply {
            moveTo(width * 0.25f, 0f)
            lineTo(width * 0.75f, 0f)
            lineTo(width, height * 0.5f)
            lineTo(width * 0.75f, height)
            lineTo(width * 0.25f, height)
            lineTo(0f, height * 0.5f)
            close()
        }
    }

    fun drawHexagonPath(
        drawScope: DrawScope,
        size: Size,
        brush: Brush,
        alpha: Float = 1f,
        style: androidx.compose.ui.graphics.drawscope.DrawStyle = androidx.compose.ui.graphics.drawscope.Fill,
    ) {
        drawScope.drawPath(getHexagonPath(size), brush, alpha = alpha, style = style)
    }

    fun drawGhostStripes(
        drawScope: DrawScope,
        size: Size,
        stripeOffsetPx: Float,
        spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
        alpha: Float = 0.15f,
    ) {
        val stripeWidth = with(drawScope) { spacing.extraTiny.toPx() * 1.5f }
        val gap = with(drawScope) { spacing.semiMedium.toPx() }
        val color = Color.White.copy(alpha = alpha)
        val step = stripeWidth + gap

        val dx = size.height / 1.732f
        val iterations = ((size.width + dx) / step).toInt() + 2
        for (i in -iterations..iterations) {
            val xStart = i * step + stripeOffsetPx
            drawScope.drawLine(
                color = color,
                start = Offset(xStart, 0f),
                end = Offset(xStart + dx, size.height),
                strokeWidth = stripeWidth,
            )
        }
    }

    fun calculateOffset(
        col: Int,
        row: Int,
        cellWidth: Float,
        cellHeight: Float,
        gapPx: Float,
    ): IntOffset {
        val x = (col * 0.75f * cellWidth + gapPx / 2).roundToInt()
        val yOffset = if (col % 2 == 1) cellHeight / 2 else 0f
        val y = (row * cellHeight + yOffset + gapPx / 2).roundToInt()
        return IntOffset(x, y)
    }
}

class FlatTopHexagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height
            moveTo(width * 0.25f, 0f)
            lineTo(width * 0.75f, 0f)
            lineTo(width, height * 0.5f)
            lineTo(width * 0.75f, height)
            lineTo(width * 0.25f, height)
            lineTo(0f, height * 0.5f)
            close()
        }
        return Outline.Generic(path)
    }
}

val Perk.displayNameRes
    get() = when (this) {
        Perk.UNDO -> Res.string.perk_undo_name
        Perk.MOVE_TILE -> Res.string.perk_move_tile_name
        Perk.REMOVE_TILE -> Res.string.perk_remove_tile_name
        Perk.ADVANCE_QUEUE -> Res.string.perk_advance_queue_name
        Perk.SWAP_TILES -> Res.string.perk_swap_tiles_name
        Perk.FUSION -> Res.string.perk_fusion_name
        Perk.CHAIN_MERGE -> Res.string.perk_chain_merge_name
        Perk.DUPLICATE_TILE -> Res.string.perk_duplicate_tile_name
        Perk.SKIP_SPAWN -> Res.string.perk_skip_spawn_name
        Perk.INCREMENT_TILE -> Res.string.perk_increment_tile_name
        Perk.PATH_MERGE -> Res.string.perk_path_merge_name
        Perk.FREEZE_TILE -> Res.string.perk_freeze_tile_name
        Perk.MIMIC -> Res.string.perk_mimic_name
    }

val Perk.descriptionRes
    get() = when (this) {
        Perk.UNDO -> Res.string.perk_undo_desc
        Perk.MOVE_TILE -> Res.string.perk_move_tile_desc
        Perk.REMOVE_TILE -> Res.string.perk_remove_tile_desc
        Perk.ADVANCE_QUEUE -> Res.string.perk_advance_queue_desc
        Perk.SWAP_TILES -> Res.string.perk_swap_tiles_desc
        Perk.FUSION -> Res.string.perk_fusion_desc
        Perk.CHAIN_MERGE -> Res.string.perk_chain_merge_desc
        Perk.DUPLICATE_TILE -> Res.string.perk_duplicate_tile_desc
        Perk.SKIP_SPAWN -> Res.string.perk_skip_spawn_desc
        Perk.INCREMENT_TILE -> Res.string.perk_increment_tile_desc
        Perk.PATH_MERGE -> Res.string.perk_path_merge_desc
        Perk.FREEZE_TILE -> Res.string.perk_freeze_tile_desc
        Perk.MIMIC -> Res.string.perk_mimic_desc
    }

@Composable
fun Hexagon(
    modifier: Modifier = Modifier,
    value: String? = null,
    backgroundColor: Color = Color.Transparent,
    perk: Perk? = null,
    onClick: (() -> Unit)? = null,
    isOutline: Boolean = false,
    isGhost: Boolean = false,
    isTactical: Boolean = false,
    isFrozen: Boolean = false,
    isMimic: Boolean = false,
    seed: Int = 0,
    maxFontSize: androidx.compose.ui.unit.TextUnit = 24.sp.scaled,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hexagon_animations")

    val stripeOffset = if (isGhost) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 11.5f, // step = stripeWidth(1.5) + gap(10)
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
            ),
            label = "stripe_offset",
        )
    } else null

    // Shine sweeping across the hex (very slow)
    val mimicShine = if (isMimic) {
        infiniteTransition.animateFloat(
            initialValue = -1f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing, delayMillis = 4000),
                repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
                initialStartOffset = StartOffset(
                    offsetMillis = seed.absoluteValue % 8000,
                    offsetType = StartOffsetType.Delay,
                ),
            ),
            label = "mimic_shine",
        )
    } else null

    val tacticalPulse = if (isTactical) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
            ),
            label = "tactical_pulse",
        )
    } else null

    val spacing = MaterialTheme.spacing

    // Subtle linear gradient for a shine effect
    val mimicGradientColors = listOf(
        Color.Transparent,
        Color.White,
        Color.Transparent,
    )

    val ghostModifier = if (isGhost && stripeOffset != null) {
        Modifier.drawWithContent {
            drawContent()
            HexagonGridDefaults.drawGhostStripes(
                drawScope = this,
                size = size,
                stripeOffsetPx = stripeOffset.value.dp.toPx(),
                spacing = spacing,
            )
        }
    } else Modifier

    val frozenModifier = if (isFrozen) {
        Modifier.border(
            width = spacing.tiny.scaled,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = FlatTopHexagonShape(),
        )
    } else Modifier

    val finalBackgroundColor = if (isMimic) {
        Brush.linearGradient(
            listOf(
                Color(0xFF424242), // Dark Gray
                Color(0xFF616161), // Gray
            ),
        )
    } else {
        SolidColor(backgroundColor)
    }

    val baseModifier = modifier
        .clip(FlatTopHexagonShape())
        .background(finalBackgroundColor)
        .then(
            // Sweeping shine effect for mimic tiles
            if (isMimic && mimicShine != null) {
                val shine = mimicShine
                val gradColors = mimicGradientColors
                Modifier.drawBehind {
                    val w = size.width
                    val h = size.height
                    val offset = shine.value * w
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = gradColors,
                            start = Offset(offset, offset),
                            end = Offset(offset + w * 0.4f, offset + h * 0.4f),
                        ),
                        alpha = 0.1f,
                    )
                }
            } else Modifier,
        )
        .then(ghostModifier)
        .then(frozenModifier)

    val finalModifier = if (onClick != null) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    val tacticalPulseModifier = if (isTactical && tacticalPulse != null) {
        val secondaryColor = MaterialTheme.colorScheme.secondary
        val strokeWidth = spacing.tiny.scaled
        Modifier.drawBehind {
            HexagonGridDefaults.drawHexagonPath(
                drawScope = this,
                size = size,
                brush = SolidColor(secondaryColor),
                alpha = tacticalPulse.value,
                style = Stroke(width = strokeWidth.toPx()),
            )
        }
    } else Modifier

    Box(
        modifier = finalModifier
            .then(tacticalPulseModifier)
            .then(
                if (isOutline) {
                    Modifier.border(
                        width = spacing.extraTiny.scaled,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = FlatTopHexagonShape(),
                    )
                } else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (value != null || isMimic) {
            if (isMimic) {
                Icon(
                    painter = painterResource(Res.drawable.ic_star),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp.scaled),
                    tint = Color.White,
                )
            } else if (value != null) {
                Text(
                    text = value,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = maxFontSize,
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 8.sp.scaled,
                        maxFontSize = maxFontSize,
                    ),
                )
            }
        }

        if (perk != null) {
            PerkIcon(
                perk = perk,
                modifier = Modifier
                    .size(spacing.large.scaled)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-6).dp.scaled),
                color = Color.White.copy(alpha = 0.6f),
            )
        }

        if (isFrozen) {
            PerkIcon(
                perk = Perk.FREEZE_TILE,
                modifier = Modifier
                    .size(spacing.semiLarge.scaled)
                    .align(Alignment.TopCenter)
                    .offset(y = spacing.tiny.scaled)
                    .zIndex(5f),
                color = Color.White,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShopButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isHighlighted: Boolean = false,
    buttonSize: Dp = MaterialTheme.spacing.extraHuge.scaled,
) {
    val spacing = MaterialTheme.spacing
    val perkColor = Color(0xFFFFD54F) // Diamond color
    val heightScale = 0.866f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(spacing.tiny.scaled),
    ) {
        Box(
            modifier = Modifier.size(width = buttonSize, height = buttonSize * heightScale),
            contentAlignment = Alignment.Center,
        ) {
            if (isHighlighted) {
                val infiniteTransition = rememberInfiniteTransition(label = "shop_highlight")
                val glowAlpha = infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "glow",
                )
                Box(
                    modifier = Modifier
                        .size(
                            width = buttonSize + 8.dp.scaled,
                            height = (buttonSize + 8.dp.scaled) * heightScale,
                        )
                        .background(
                            perkColor.copy(alpha = glowAlpha.value * 0.4f),
                            FlatTopHexagonShape(),
                        ),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(FlatTopHexagonShape())
                    .background(perkColor.copy(alpha = 0.1f))
                    .border(
                        width = 2.dp.scaled,
                        color = perkColor.copy(alpha = 0.5f),
                        shape = FlatTopHexagonShape(),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_diamond),
                    contentDescription = null,
                    tint = perkColor,
                    modifier = Modifier.size(buttonSize * 0.45f),
                )
            }
        }

        Spacer(Modifier.height(spacing.medium.scaled))

        Text(
            text = "SHOP",
            color = perkColor.copy(alpha = 0.8f),
            fontSize = 11.sp.scaled,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp.scaled,
            modifier = Modifier.width(buttonSize + spacing.semiMedium.scaled),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VoucherButton(
    category: PerkCategory,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = MaterialTheme.spacing.extraHuge.scaled,
    showGlow: Boolean = false,
) {
    val spacing = MaterialTheme.spacing
    val color = when (category) {
        PerkCategory.COMMON -> Color.Gray
        PerkCategory.RARE -> Color(0xFF4FC3F7)
        PerkCategory.LEGENDARY -> Color(0xFFFFD54F)
    }
    val icon = when (category) {
        PerkCategory.COMMON -> Res.drawable.ic_roll
        PerkCategory.RARE -> Res.drawable.ic_rare_perk
        PerkCategory.LEGENDARY -> Res.drawable.ic_legendary_perk
    }
    val heightScale = 0.866f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = spacing.tiny.scaled)
            .graphicsLayer { alpha = if (count > 0 || showGlow) 1f else 0.4f },
    ) {
        BadgedBox(
            badge = {
                if (count > 0) {
                    Badge(
                        containerColor = color,
                        contentColor = Color.Black,
                        modifier = Modifier.offset(x = (-4).dp, y = 4.dp),
                    ) {
                        Text(
                            text = count.toString(),
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp.scaled,
                        )
                    }
                }
            },
            modifier = Modifier.size(width = buttonSize, height = buttonSize * heightScale),
        ) {
            // Blueprint Glow
            if (showGlow) {
                val infiniteTransition = rememberInfiniteTransition(label = "voucher_glow")
                val glowAlpha = infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200),
                        repeatMode = RepeatMode.Reverse,
                    ),
                    label = "glow",
                )

                Box(
                    modifier = Modifier
                        .size(
                            width = buttonSize + spacing.medium.scaled,
                            height = (buttonSize + spacing.medium.scaled) * heightScale,
                        )
                        .align(Alignment.Center)
                        .clip(FlatTopHexagonShape())
                        .drawBehind {
                            drawRect(color.copy(alpha = glowAlpha.value * 0.3f))
                        },
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(FlatTopHexagonShape())
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(
                        width = 1.dp.scaled,
                        brush = SolidColor(color.copy(alpha = if (showGlow) 0.8f else 0.4f)),
                        shape = FlatTopHexagonShape(),
                    )
                    .clickable(enabled = count > 0 || showGlow, onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = color.copy(alpha = if (showGlow) 0.9f else 0.6f),
                    modifier = Modifier.size(buttonSize * 0.45f),
                )
            }
        }

        Spacer(Modifier.height(spacing.medium.scaled))

        Text(
            text = category.name.uppercase(),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp.scaled,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp.scaled,
            modifier = Modifier.width(buttonSize + spacing.semiMedium.scaled),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PerkButton(
    modifier: Modifier = Modifier,
    perk: Perk,
    onClick: () -> Unit,
    count: Int? = null,
    isActive: Boolean = false,
    isEnabled: Boolean = true,
    tooltipDescription: StringResource? = null,
    buttonSize: Dp = MaterialTheme.spacing.extraHuge.scaled,
) {
    val spacing = MaterialTheme.spacing
    val colorScheme = MaterialTheme.colorScheme
    val perkColor = remember(perk, isEnabled) {
        if (isEnabled) HexagonGridDefaults.getColorForPerk(perk, colorScheme) else Color.Gray
    }
    val isLegendary = perk.isLegendary
    val heightScale = 0.866f // Flat-top hexagon height/width ratio

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = spacing.tiny.scaled)
            .graphicsLayer { alpha = if (isEnabled) 1f else 0.1f },
    ) {
        val hexagonContent = @Composable {
            BadgedBox(
                badge = @Composable {
                    if (count != null) {
                        Badge(
                            containerColor = perkColor,
                            contentColor = Color.White,
                        ) {
                            Text(text = count.toString())
                        }
                    }
                },
                modifier = Modifier.size(width = buttonSize, height = buttonSize * heightScale),
            ) {
                // Legendary Glow
                if (isLegendary && isEnabled) {
                    val infiniteTransition = rememberInfiniteTransition(label = "legendary_glow")
                    val glowAlpha = infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
                        ),
                        label = "glow",
                    )

                    Box(
                        modifier = Modifier
                            .size(
                                width = buttonSize + spacing.small.scaled,
                                height = (buttonSize + spacing.small.scaled) * heightScale,
                            )
                            .clip(FlatTopHexagonShape())
                            .drawBehind {
                                drawRect(perkColor.copy(alpha = glowAlpha.value * 0.4f))
                            },
                    )
                }

                // Main Hexagon Body
                val brush = remember<Brush>(isActive, perkColor) {
                    Brush.verticalGradient(
                        if (isActive) {
                            listOf(perkColor, perkColor.copy(alpha = 0.7f))
                        } else {
                            listOf(perkColor.copy(alpha = 0.2f), perkColor.copy(alpha = 0.05f))
                        },
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(FlatTopHexagonShape())
                        .background(brush)
                        .border(
                            width = if (isActive) spacing.tiny.scaled else if (isLegendary) spacing.tiny.scaled else spacing.extraTiny.scaled,
                            color = if (isActive) Color.White.copy(alpha = 0.5f) else if (isLegendary) perkColor else perkColor.copy(
                                alpha = 0.3f,
                            ),
                            shape = FlatTopHexagonShape(),
                        )
                        .clickable(enabled = isEnabled, onClick = onClick),
                    contentAlignment = Alignment.Center,
                ) {
                    PerkIcon(
                        perk = perk,
                        modifier = Modifier.size(buttonSize * 0.45f),
                        color = Color.White.copy(alpha = if (isActive) 1f else if (isEnabled) 0.7f else 0.3f),
                    )
                }
            }
        }

        if (tooltipDescription != null) {
            Tooltip(
                position = Position.ABOVE,
                contentDescription = tooltipDescription,
                content = hexagonContent,
            )
        } else {
            hexagonContent()
        }

        Spacer(Modifier.height(spacing.medium.scaled))

        Text(
            text = stringResource(perk.displayNameRes),
            color = if (isActive) perkColor else Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp.scaled,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp.scaled,
            modifier = Modifier.width(buttonSize + spacing.semiMedium.scaled),
        )
    }
}

@Composable
fun PerkIcon(
    modifier: Modifier = Modifier,
    perk: Perk?,
    color: Color = Color.White,
) {
    if (perk == null) return

    val iconRes = when (perk) {
        Perk.UNDO -> Res.drawable.ic_undo
        Perk.MOVE_TILE -> Res.drawable.ic_move
        Perk.REMOVE_TILE -> Res.drawable.ic_delete
        Perk.ADVANCE_QUEUE -> Res.drawable.ic_advance
        Perk.SWAP_TILES -> Res.drawable.ic_swap
        Perk.FUSION -> Res.drawable.ic_fusion
        Perk.CHAIN_MERGE -> Res.drawable.ic_chain_merge
        Perk.DUPLICATE_TILE -> Res.drawable.ic_duplicate
        Perk.SKIP_SPAWN -> Res.drawable.ic_pause
        Perk.INCREMENT_TILE -> Res.drawable.ic_upgrade
        Perk.FREEZE_TILE -> Res.drawable.ic_freeze
        Perk.PATH_MERGE -> Res.drawable.ic_path_merge
        Perk.MIMIC -> Res.drawable.ic_star
    }

    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = modifier,
        tint = color,
    )
}

@Composable
fun HexagonGrid(
    modifier: Modifier = Modifier,
    columns: Int,
    rows: Int,
    itemGap: Dp = MaterialTheme.spacing.tiny,
    outlineContent: @Composable (col: Int, row: Int) -> Unit = { _, _ -> },
    content: @Composable () -> Unit = {},
) {
    Layout(
        content = {
            for (row in 0 until rows) {
                for (col in 0 until columns) {
                    outlineContent(col, row)
                }
            }
            content()
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val gapPx = itemGap.toPx()
        val cellCount = columns * rows

        // Calculate width-based constraints
        val cellWidthFromWidth = constraints.maxWidth / (1f + (columns - 1) * 0.75f)

        // Calculate height-based constraints
        val cellHeightFromHeight = constraints.maxHeight / (rows + 0.5f)
        val cellWidthFromHeight = cellHeightFromHeight / (sqrt(3f) / 2f)

        // Take the smaller one to fit in both
        val cellWidth = minOf(cellWidthFromWidth, cellWidthFromHeight)
        val cellHeight = cellWidth * (sqrt(3f) / 2f)

        val itemWidthMeasured = (cellWidth - gapPx).coerceAtLeast(0f)
        val itemHeightMeasured = (cellHeight - gapPx).coerceAtLeast(0f)

        val childConstraints = constraints.copy(
            minWidth = itemWidthMeasured.toInt(),
            maxWidth = itemWidthMeasured.toInt(),
            minHeight = itemHeightMeasured.toInt(),
            maxHeight = itemHeightMeasured.toInt(),
        )

        val outlineMeasurables = measurables.take(cellCount)
        val contentMeasurables = measurables.drop(cellCount)

        val outlinePlaceables = outlineMeasurables.map { it.measure(childConstraints) }
        val contentPlaceables = contentMeasurables.map {
            it.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        val totalWidth = (cellWidth * (1f + (columns - 1) * 0.75f)).toInt()
        val totalHeight = (cellHeight * (rows + 0.5f)).toInt()

        layout(totalWidth, totalHeight) {
            outlinePlaceables.forEachIndexed { index, placeable ->
                val row = index / columns
                val col = index % columns
                val offset =
                    HexagonGridDefaults.calculateOffset(col, row, cellWidth, cellHeight, gapPx)
                placeable.placeRelative(offset)
            }
            contentPlaceables.forEach { it.placeRelative(0, 0) }
        }
    }
}
