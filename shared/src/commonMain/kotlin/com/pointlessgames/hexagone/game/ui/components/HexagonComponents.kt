package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
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

@Composable
fun Hexagon(
    value: String? = null,
    backgroundColor: Color = Color.Transparent,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    isOutline: Boolean = false
) {
    val baseModifier = modifier
        .clip(FlatTopHexagonShape())
        .background(backgroundColor)
    
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
    }
}

@Composable
fun PerkIcon(
    perk: Perk,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
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
                    startAngle = 0f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
                    size = Size(
                        size.width * 0.8f,
                        size.height * 0.8f,
                    ),
                    style = Stroke(width = strokeWidth),
                )
                val arrowSize = size.width * 0.2f
                drawLine(
                    color,
                    Offset(size.width * 0.1f, size.height * 0.5f),
                    Offset(
                        size.width * 0.1f - arrowSize,
                        size.height * 0.5f - arrowSize,
                    ),
                    strokeWidth,
                )
                drawLine(
                    color,
                    Offset(size.width * 0.1f, size.height * 0.5f),
                    Offset(
                        size.width * 0.1f + arrowSize,
                        size.height * 0.5f - arrowSize,
                    ),
                    strokeWidth,
                )
            }
        }
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
