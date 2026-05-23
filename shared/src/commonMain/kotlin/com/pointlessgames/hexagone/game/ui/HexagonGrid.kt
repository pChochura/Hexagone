package com.pointlessgames.hexagone.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.math.sqrt

object HexagonGridDefaults {
    private val palette = listOf(
        Color(0xFFFF5722), Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFFC107),
        Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFE91E63), Color(0xFFCDDC39),
        Color(0xFF3F51B5), Color(0xFFFF9800), Color(0xFF009688), Color(0xFF795548),
        Color(0xFF673AB7), Color(0xFF8BC34A), Color(0xFF03A9F4), Color(0xFFF44336)
    )

    fun getColorForValue(value: Int): Color {
        if (value <= 0) return Color.Transparent
        return palette[(value - 1) % palette.size]
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
            .border(
                width = 1.dp,
                color = Color.DarkGray,
                shape = FlatTopHexagonShape(),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (value != null) {
            Text(text = value)
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
