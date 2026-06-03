package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.PreviewCell
import com.pointlessgames.hexagone.game.model.GhostAnimationState

internal fun drawGhost(
    drawScope: DrawScope,
    preview: PreviewCell,
    animState: GhostAnimationState,
    itemWidth: Float,
    itemHeight: Float,
    wiggleValue: Float,
    stripeOffset: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    colorScheme: androidx.compose.material3.ColorScheme,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
    isHovered: Boolean = false,
    currentHoverMerge: MergeTransition? = null,
    selectedCellId: String? = null,
) {
    val offset = animState.offset.value
    val isSelected = selectedCellId == preview.id
    val scale = animState.scale.value * (if (isSelected) 1.2f else if (isHovered) 1.15f else 1f)
    val alpha = animState.alpha.value

    drawScope.withTransform(
        {
            translate(offset.x.toFloat(), offset.y.toFloat())
            scale(scale, scale, Offset(itemWidth / 2, itemHeight / 2))
            rotate(wiggleValue, Offset(itemWidth / 2, itemHeight / 2))
        },
    ) {
        val visualValue = currentHoverMerge?.previewValues?.get(preview.id) 
            ?: if (currentHoverMerge != null && currentHoverMerge.targetX == preview.x && currentHoverMerge.targetY == preview.y && currentHoverMerge.finalValue != 0) {
                currentHoverMerge.finalValue
            } else preview.value

        val backgroundColor = HexagonGridDefaults.getColorForValue(visualValue, colorScheme)
            .copy(alpha = 0.3f)
        HexagonGridDefaults.drawHexagonPath(
            this,
            Size(itemWidth, itemHeight),
            backgroundColor,
            alpha = alpha,
        )

        if (isHovered || preview.isTactical || isSelected) {
            val borderColor = if (isSelected) Color.White.copy(alpha = alpha) else if (isHovered) Color.White.copy(alpha = 0.5f * alpha) else colorScheme.secondary.copy(alpha = alpha)
            HexagonGridDefaults.drawHexagonPath(
                this,
                Size(itemWidth, itemHeight),
                borderColor,
                style = Stroke(width = spacing.tiny.toPx())
            )
        }

        clipPath(HexagonGridDefaults.getHexagonPath(Size(itemWidth, itemHeight))) {
            HexagonGridDefaults.drawGhostStripes(
                this,
                Size(itemWidth, itemHeight),
                stripeOffset * drawScope.density,
                spacing,
                alpha = 0.15f * alpha,
            )
        }

        val textLayoutResult = textMeasurer.measure(
            text = visualValue.toString(),
            style = TextStyle(
                color = Color.White.copy(alpha = alpha),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            ),
        )
        drawText(
            textLayoutResult,
            topLeft = Offset(
                (itemWidth - textLayoutResult.size.width) / 2,
                (itemHeight - textLayoutResult.size.height) / 2,
            ),
        )
    }
}

internal fun drawHoverResult(
    drawScope: DrawScope,
    merge: MergeTransition,
    combo: Int,
    gridStateProvider: () -> List<HexagonCell>,
    previewStateProvider: () -> List<PreviewCell>,
    cellWidth: Float,
    cellHeight: Float,
    gapPx: Float,
    itemWidth: Float,
    itemHeight: Float,
    progress: Float,
    pulseValue: Float,
    floatValue: Float,
    stripeOffset: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    colorScheme: androidx.compose.material3.ColorScheme,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val targetOffset = HexagonGridDefaults.calculateOffset(
        merge.targetX,
        merge.targetY,
        cellWidth,
        cellHeight,
        gapPx,
    )
    val center = Offset(targetOffset.x + itemWidth / 2, targetOffset.y + itemHeight / 2)

    val currentCell = gridStateProvider().find { it.x == merge.targetX && it.y == merge.targetY }
    val currentValue = currentCell?.value ?: 0
    val isPlacement = merge.resultId.contains("move") || merge.resultId.contains("duplicate") || merge.resultId.contains("swap") || merge.resultId.contains("highlight")

    val currentPreview = previewStateProvider().find { it.x == merge.targetX && it.y == merge.targetY }
    val shouldSkipHexagon = merge.resultId == "preview_move" ||
            (merge.resultId.contains("path_merge") && (currentCell != null || currentPreview != null))

    if (merge.finalValue > 0 && (merge.finalValue != currentValue || isPlacement) && !shouldSkipHexagon) {
        val isForcedSolid = merge.forceSolidIds?.contains(merge.resultId) == true

        drawScope.withTransform(
            {
                translate(targetOffset.x.toFloat(), targetOffset.y.toFloat())
                val s = 0.8f + 0.2f * progress * pulseValue
                scale(s, s, Offset(itemWidth / 2, itemHeight / 2))
            },
        ) {
            val backgroundColor = HexagonGridDefaults.getColorForValue(merge.finalValue, colorScheme)
                .copy(alpha = (if (isForcedSolid) 0.8f else 0.5f) * progress * 0.7f)
            HexagonGridDefaults.drawHexagonPath(this, Size(itemWidth, itemHeight), backgroundColor)

            if (!isForcedSolid) {
                clipPath(HexagonGridDefaults.getHexagonPath(Size(itemWidth, itemHeight))) {
                    HexagonGridDefaults.drawGhostStripes(
                        this,
                        Size(itemWidth, itemHeight),
                        stripeOffset * drawScope.density,
                        spacing,
                        alpha = 0.15f * progress * 0.7f,
                    )
                }
            }

            HexagonGridDefaults.drawHexagonPath(
                this,
                Size(itemWidth, itemHeight),
                Color.White.copy(alpha = 0.3f * progress),
                style = Stroke(width = spacing.tiny.toPx()),
            )

            val textLayoutResult = textMeasurer.measure(
                text = merge.finalValue.toString(),
                style = TextStyle(
                    color = Color.White.copy(alpha = progress),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                ),
            )
            drawText(
                textLayoutResult,
                topLeft = Offset(
                    (itemWidth - textLayoutResult.size.width) / 2,
                    (itemHeight - textLayoutResult.size.height) / 2,
                ),
            )
        }
    }

    val displayScore = when {
        merge.resultId.startsWith("preview_") -> merge.baseScore
        merge.uniqueGroups > 0 -> {
            val multiplier = (combo + 1).coerceAtMost(12)
            merge.baseScore * multiplier
        }
        else -> merge.baseScore
    }
    if (displayScore > 0) {
        val scoreText = "+$displayScore"
        val scoreTextLayout = textMeasurer.measure(
            text = scoreText,
            style = TextStyle(color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp),
        )

        val popupWidth = scoreTextLayout.size.width + with(drawScope) { spacing.semiLarge.toPx() }
        val popupHeight = scoreTextLayout.size.height + with(drawScope) { spacing.small.toPx() }
        val popupTopLeft = Offset(
            center.x - popupWidth / 2f,
            center.y - 140f + floatValue - popupHeight / 2f,
        )

        drawScope.withTransform(
            {
                val s = 0.8f + 0.2f * progress * pulseValue
                scale(s, s, Offset(popupTopLeft.x + popupWidth / 2f, popupTopLeft.y + popupHeight / 2f))
            },
        ) {
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.7f * progress),
                topLeft = popupTopLeft,
                size = Size(popupWidth, popupHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    popupHeight / 2f,
                    popupHeight / 2f,
                ),
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f * progress),
                topLeft = popupTopLeft,
                size = Size(popupWidth, popupHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    popupHeight / 2f,
                    popupHeight / 2f,
                ),
                style = Stroke(width = spacing.extraTiny.toPx()),
            )
            drawText(
                scoreTextLayout,
                topLeft = Offset(
                    popupTopLeft.x + (popupWidth - scoreTextLayout.size.width) / 2f,
                    popupTopLeft.y + (popupHeight - scoreTextLayout.size.height) / 2f,
                ),
                alpha = progress,
            )
        }
    }
}
