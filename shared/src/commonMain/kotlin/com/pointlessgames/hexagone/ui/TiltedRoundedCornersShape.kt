package com.pointlessgames.hexagone.ui

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.util.lerp

data class TiltedRoundedCornersShape(
    internal val rotation: Float,
    internal val top: CornerSize,
    internal val end: CornerSize,
    internal val bottom: CornerSize,
    internal val start: CornerSize,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val top = top.toPx(size, density)
        val end = end.toPx(size, density)
        val bottom = bottom.toPx(size, density)
        val start = start.toPx(size, density)
        val path = Path().apply {
            this.addRoundRect(
                RoundRect(
                    rect = size.toRect(),
                    topLeft = CornerRadius(top),
                    topRight = CornerRadius(if (layoutDirection == Ltr) end else start),
                    bottomRight = CornerRadius(bottom),
                    bottomLeft = CornerRadius(if (layoutDirection == Ltr) start else end),
                ),
            )
            transform(
                Matrix().apply {
                    resetToPivotedTransform(
                        pivotX = size.width / 2f,
                        pivotY = size.height / 2f,
                        rotationZ = rotation,
                    )
                },
            )
        }

        return Outline.Generic(path)
    }
}

fun TiltedRoundedCornersShape.lerp(
    b: TiltedRoundedCornersShape,
    t: Float,
): TiltedRoundedCornersShape = TiltedRoundedCornersShape(
    rotation = lerp(rotation, b.rotation, t),
    top = lerp(top, b.top, t),
    end = lerp(end, b.end, t),
    bottom = lerp(bottom, b.bottom, t),
    start = lerp(start, b.start, t),
)

internal fun lerp(
    a: CornerSize,
    b: CornerSize,
    t: Float,
): CornerSize = object : CornerSize {
    override fun toPx(shapeSize: Size, density: Density): Float =
        lerp(a.toPx(shapeSize, density), b.toPx(shapeSize, density), t)
}

/**
 * Creates [TiltedRoundedCornersShape] with the same size applied for all four corners.
 *
 * @param corner [CornerSize] to apply.
 */
fun TiltedRoundedCornersShape(rotation: Float, corner: CornerSize) =
    TiltedRoundedCornersShape(rotation, corner, corner, corner, corner)

/**
 * Creates [TiltedRoundedCornersShape] with the same size applied for all four corners.
 *
 * @param size Size in [Dp] to apply.
 */
fun TiltedRoundedCornersShape(rotation: Float, size: Dp) =
    TiltedRoundedCornersShape(rotation, CornerSize(size))

/**
 * Creates [TiltedRoundedCornersShape] with the same size applied for all four corners.
 *
 * @param size Size in pixels to apply.
 */
fun TiltedRoundedCornersShape(rotation: Float, size: Float) =
    TiltedRoundedCornersShape(rotation, CornerSize(size))

/**
 * Creates [TiltedRoundedCornersShape] with the same size applied for all four corners.
 *
 * @param size Size in percents to apply.
 */
fun TiltedRoundedCornersShape(rotation: Float, size: Int) =
    TiltedRoundedCornersShape(rotation, CornerSize(size))
