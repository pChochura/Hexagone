package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
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
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.components.Position
import com.pointlessgames.hexagone.ui.components.Tooltip
import hexagone.shared.generated.resources.Res
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import hexagone.shared.generated.resources.perk_advance_queue_desc
import hexagone.shared.generated.resources.perk_advance_queue_name
import hexagone.shared.generated.resources.perk_chain_merge_desc
import hexagone.shared.generated.resources.perk_chain_merge_name
import hexagone.shared.generated.resources.perk_fusion_desc
import hexagone.shared.generated.resources.perk_fusion_name
import hexagone.shared.generated.resources.perk_move_tile_desc
import hexagone.shared.generated.resources.perk_move_tile_name
import hexagone.shared.generated.resources.perk_remove_tile_desc
import hexagone.shared.generated.resources.perk_remove_tile_name
import hexagone.shared.generated.resources.perk_swap_tiles_desc
import hexagone.shared.generated.resources.perk_swap_tiles_name
import hexagone.shared.generated.resources.perk_undo_desc
import hexagone.shared.generated.resources.perk_undo_name
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt
import kotlin.math.sqrt

object HexagonGridDefaults {
    fun getColorForValue(value: Int): Color {
        if (value <= 0) return Color.Transparent
        
        // Base signature colors from screenshot
        val baseColors = mapOf(
            1 to Color(0xFF3BA9F3),
            2 to Color(0xFF9345C4),
            4 to Color(0xFFD63F7B),
            8 to Color(0xFFF98E33),
            16 to Color(0xFF4BC2E1)
        )
        
        baseColors[value]?.let { return it }

        // Algorithmic color generation for infinite granularity
        // Using golden ratio for hue distribution to keep colors distinct
        val hue = (value * 137.508f) % 360f
        val saturation = 0.6f + (value % 4) * 0.1f
        val brightness = 0.7f + (value % 3) * 0.1f
        
        return Color.hsv(hue, saturation.coerceAtMost(1f), brightness.coerceAtMost(1f))
    }

    fun getColorForPerk(perk: Perk): Color {
        return when (perk) {
            Perk.UNDO -> Color(0xFF90A4AE)
            Perk.MOVE_TILE -> Color(0xFF9575CD)
            Perk.REMOVE_TILE -> Color(0xFFE57373)
            Perk.FUSION -> Color(0xFFFFB74D)
            Perk.SWAP_TILES -> Color(0xFF4FC3F7)
            Perk.CHAIN_MERGE -> Color(0xFF81C784)
            Perk.ADVANCE_QUEUE -> Color(0xFF4DB6AC)
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
        color: Color,
        alpha: Float = 1f,
        style: androidx.compose.ui.graphics.drawscope.DrawStyle = androidx.compose.ui.graphics.drawscope.Fill
    ) {
        drawScope.drawPath(getHexagonPath(size), color, alpha = alpha, style = style)
    }

    fun drawGhostStripes(
        drawScope: DrawScope,
        size: Size,
        stripeOffsetPx: Float,
        alpha: Float = 0.15f
    ) {
        val stripeWidth = with(drawScope) { 1.5.dp.toPx() }
        val gap = with(drawScope) { 10.dp.toPx() }
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
                strokeWidth = stripeWidth
            )
        }
    }

    fun calculateOffset(
        col: Int,
        row: Int,
        cellWidth: Float,
        cellHeight: Float,
        gapPx: Float
    ): IntOffset {
        val x = (col * 0.75f * cellWidth + gapPx / 2).roundToInt()
        val yOffset = if (col % 2 == 1) cellHeight / 2 else 0f
        val y = (row * cellHeight + yOffset + gapPx / 2).roundToInt()
        return IntOffset(x, y)
    }

    fun drawPerkIcon(
        drawScope: DrawScope,
        perk: Perk?,
        size: Size,
        color: Color,
        strokeWidth: Float
    ) {
        with(drawScope) {
            if (perk == null) {
                // Draw Mystery Icon (?)
                val path = Path().apply {
                    // Stylized ?
                    moveTo(size.width * 0.35f, size.height * 0.25f)
                    cubicTo(size.width * 0.35f, 0f, size.width * 0.75f, 0f, size.width * 0.75f, size.height * 0.3f)
                    cubicTo(size.width * 0.75f, size.height * 0.45f, size.width * 0.55f, size.height * 0.45f, size.width * 0.55f, size.height * 0.6f)
                    moveTo(size.width * 0.55f, size.height * 0.75f)
                    lineTo(size.width * 0.55f, size.height * 0.85f)
                }
                drawPath(path, color = color, style = Stroke(width = strokeWidth))
                return@with
            }
            when (perk) {
                Perk.ADVANCE_QUEUE -> {
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width * 0.5f, size.height * 0.5f)
                        lineTo(0f, size.height)
                        moveTo(size.width * 0.5f, 0f)
                        lineTo(size.width, size.height * 0.5f)
                        lineTo(size.width * 0.5f, size.height)
                    }
                    drawPath(path, color = color, style = Stroke(width = strokeWidth))
                }

                Perk.MOVE_TILE -> {
                    drawLine(
                        color,
                        Offset(size.width * 0.5f, 0f),
                        Offset(size.width * 0.5f, size.height),
                        strokeWidth,
                    )
                    drawLine(
                        color,
                        Offset(0f, size.height * 0.5f),
                        Offset(size.width, size.height * 0.5f),
                        strokeWidth,
                    )
                }

                Perk.REMOVE_TILE -> {
                    drawRect(
                        color,
                        Offset(size.width * 0.2f, size.height * 0.3f),
                        Size(size.width * 0.6f, size.height * 0.6f),
                        style = Stroke(width = strokeWidth),
                    )
                    drawLine(
                        color,
                        Offset(size.width * 0.1f, size.height * 0.2f),
                        Offset(size.width * 0.9f, size.height * 0.2f),
                        strokeWidth,
                    )
                }

                Perk.FUSION -> {
                    drawCircle(
                        color,
                        radius = size.minDimension * 0.4f,
                        style = Stroke(width = strokeWidth),
                    )
                    drawCircle(color, radius = size.minDimension * 0.2f)
                }

                Perk.SWAP_TILES -> {
                    val arrowSize = size.width * 0.3f
                    drawLine(
                        color,
                        Offset(0f, size.height * 0.3f),
                        Offset(size.width, size.height * 0.3f),
                        strokeWidth
                    )
                    drawLine(
                        color,
                        Offset(size.width, size.height * 0.3f),
                        Offset(size.width - arrowSize, size.height * 0.15f),
                        strokeWidth
                    )
                    drawLine(
                        color,
                        Offset(size.width, size.height * 0.3f),
                        Offset(size.width - arrowSize, size.height * 0.45f),
                        strokeWidth
                    )

                    drawLine(
                        color,
                        Offset(0f, size.height * 0.7f),
                        Offset(size.width, size.height * 0.7f),
                        strokeWidth
                    )
                    drawLine(
                        color,
                        Offset(0f, size.height * 0.7f),
                        Offset(arrowSize, size.height * 0.55f),
                        strokeWidth
                    )
                    drawLine(
                        color,
                        Offset(0f, size.height * 0.7f),
                        Offset(arrowSize, size.height * 0.85f),
                        strokeWidth
                    )
                }

                Perk.CHAIN_MERGE -> {
                    drawCircle(
                        color,
                        radius = size.width * 0.15f,
                        center = Offset(size.width * 0.25f, size.height * 0.25f),
                        style = Stroke(width = strokeWidth)
                    )
                    drawLine(
                        color,
                        Offset(size.width * 0.35f, size.height * 0.35f),
                        Offset(size.width * 0.65f, size.height * 0.65f),
                        strokeWidth
                    )
                    drawCircle(
                        color,
                        radius = size.width * 0.15f,
                        center = Offset(size.width * 0.75f, size.height * 0.75f),
                        style = Stroke(width = strokeWidth)
                    )
                }

                Perk.UNDO -> {
                    drawArc(
                        color = color,
                        startAngle = 180f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(size.width * 0.15f, size.height * 0.15f),
                        size = Size(size.width * 0.7f, size.height * 0.7f),
                        style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    )
                    val arrowSize = size.width * 0.2f
                    val arrowPath = Path().apply {
                        moveTo(size.width * 0.15f, size.height * 0.5f - arrowSize * 0.5f)
                        lineTo(size.width * 0.15f, size.height * 0.5f + arrowSize * 0.5f)
                        lineTo(size.width * 0.15f - arrowSize * 0.8f, size.height * 0.5f)
                        close()
                    }
                    drawPath(arrowPath, color = color)
                }
            }
        }
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

val Perk.displayNameRes get() = when(this) {
    Perk.UNDO -> Res.string.perk_undo_name
    Perk.MOVE_TILE -> Res.string.perk_move_tile_name
    Perk.REMOVE_TILE -> Res.string.perk_remove_tile_name
    Perk.ADVANCE_QUEUE -> Res.string.perk_advance_queue_name
    Perk.SWAP_TILES -> Res.string.perk_swap_tiles_name
    Perk.FUSION -> Res.string.perk_fusion_name
    Perk.CHAIN_MERGE -> Res.string.perk_chain_merge_name
}

val Perk.descriptionRes get() = when(this) {
    Perk.UNDO -> Res.string.perk_undo_desc
    Perk.MOVE_TILE -> Res.string.perk_move_tile_desc
    Perk.REMOVE_TILE -> Res.string.perk_remove_tile_desc
    Perk.ADVANCE_QUEUE -> Res.string.perk_advance_queue_desc
    Perk.SWAP_TILES -> Res.string.perk_swap_tiles_desc
    Perk.FUSION -> Res.string.perk_fusion_desc
    Perk.CHAIN_MERGE -> Res.string.perk_chain_merge_desc
}

@Composable
fun Hexagon(
    value: String? = null,
    backgroundColor: Color = Color.Transparent,
    perk: Perk? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isOutline: Boolean = false,
    isGhost: Boolean = false,
    isTactical: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hexagon_animations")
    
    val stripeOffset = if (isGhost) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 11.5f, // step = stripeWidth(1.5) + gap(10)
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Restart
            ),
            label = "stripe_offset"
        )
    } else null

    val tacticalPulse = if (isTactical) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
            ),
            label = "tactical_pulse"
        )
    } else null

    val ghostModifier = if (isGhost) {
        Modifier.drawWithContent {
            drawContent()
            val stripeWidth = 1.5.dp.toPx()
            val gap = 10.dp.toPx()
            val color = Color.White.copy(alpha = 0.15f)
            val step = stripeWidth + gap
            val offsetPx = (stripeOffset?.value ?: 0f).dp.toPx()
            
            // 60 degree slope: tan(60) = sqrt(3) ~= 1.732
            // dx = dy / tan(60)
            val dx = size.height / 1.732f
            
            val iterations = ((size.width + dx) / step).toInt() + 2
            for (i in -iterations..iterations) {
                val xStart = i * step + offsetPx
                drawLine(
                    color = color,
                    start = Offset(xStart, 0f),
                    end = Offset(xStart + dx, size.height),
                    strokeWidth = stripeWidth
                )
            }
        }
    } else Modifier

    val baseModifier = modifier
        .clip(FlatTopHexagonShape())
        .background(backgroundColor)
        .then(ghostModifier)
    
    val finalModifier = if (onClick != null) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    Box(
        modifier = finalModifier
            .then(
                if (isOutline) {
                    Modifier.border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = FlatTopHexagonShape(),
                    )
                } else if (isTactical) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color(0xFFBB86FC).copy(alpha = tacticalPulse?.value ?: 1f),
                        shape = FlatTopHexagonShape(),
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (value != null) {
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 24.sp)
            )
        }

        if (perk != null) {
            PerkIcon(
                perk = perk,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-6).dp),
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PerkButton(
    perk: Perk,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
    isActive: Boolean = false,
    isEnabled: Boolean = true,
    tooltipDescription: StringResource? = null,
    buttonSize: Dp = 54.dp
) {
    val perkColor = remember(perk, isEnabled) {
        if (isEnabled) HexagonGridDefaults.getColorForPerk(perk) else Color.Gray
    }
    val isLegendary = perk.isLegendary
    val heightScale = 0.866f // Flat-top hexagon height/width ratio
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(horizontal = 2.dp)
            .graphicsLayer { alpha = if (isEnabled) 1f else 0.1f }
    ) {
        val hexagonContent = @Composable {
            Box(
                modifier = Modifier.size(width = buttonSize, height = buttonSize * heightScale),
                contentAlignment = Alignment.Center
            ) {
                // Legendary Glow
                if (isLegendary && isEnabled) {
                    val infiniteTransition = rememberInfiniteTransition(label = "legendary_glow")
                    val glowAlpha = infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.8f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ),
                        label = "glow"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(width = buttonSize + 8.dp, height = (buttonSize + 8.dp) * heightScale)
                            .clip(FlatTopHexagonShape())
                            .drawBehind {
                                drawRect(perkColor.copy(alpha = glowAlpha.value * 0.4f))
                            }
                    )
                }

                // Main Hexagon Body
                val brush = remember<Brush>(isActive, perkColor) {
                    Brush.verticalGradient(
                        if (isActive) {
                            listOf(perkColor, perkColor.copy(alpha = 0.7f))
                        } else {
                            listOf(perkColor.copy(alpha = 0.2f), perkColor.copy(alpha = 0.05f))
                        }
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(FlatTopHexagonShape())
                        .background(brush)
                        .border(
                            width = if (isActive) 2.dp else if (isLegendary) 2.dp else 1.dp,
                            color = if (isActive) Color.White.copy(alpha = 0.5f) else if (isLegendary) perkColor else perkColor.copy(alpha = 0.3f),
                            shape = FlatTopHexagonShape(),
                        )
                        .clickable(enabled = isEnabled, onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    PerkIcon(
                        perk = perk,
                        modifier = Modifier.size(buttonSize * 0.45f),
                        color = Color.White.copy(alpha = if (isActive) 1f else if (isEnabled) 0.7f else 0.3f)
                    )
                }

                // Badge counter
                if (count != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-2).dp)
                            .size(20.dp)
                            .background(perkColor, CircleShape)
                            .border(1.dp, Color(0xFF1C1C24), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = count.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        if (tooltipDescription != null) {
            Tooltip(
                position = Position.ABOVE,
                contentDescription = tooltipDescription,
                content = hexagonContent
            )
        } else {
            hexagonContent()
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(perk.displayNameRes),
            color = if (isActive) perkColor else Color.White.copy(alpha = 0.8f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp,
            modifier = Modifier.width(buttonSize + 10.dp)
        )
    }
}

@Composable
fun PerkIcon(
    perk: Perk?,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        HexagonGridDefaults.drawPerkIcon(
            drawScope = this,
            perk = perk,
            size = size,
            color = color,
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun HexagonGrid(
    columns: Int,
    rows: Int,
    modifier: Modifier = Modifier,
    itemGap: Dp = 2.dp,
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

        val cellWidth = constraints.maxWidth / (1f + (columns - 1) * 0.75f)
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
                val offset = HexagonGridDefaults.calculateOffset(col, row, cellWidth, cellHeight, gapPx)
                placeable.placeRelative(offset)
            }
            contentPlaceables.forEach { it.placeRelative(0, 0) }
        }
    }
}
