package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.ui.theme.Spacing

@Composable
internal fun LevelIcon(spacing: Spacing, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.size(spacing.semiLarge)) {
        val stroke = spacing.tiny.toPx()
        // Staircase shape
        drawLine(color, Offset(0f, size.height), Offset(size.width, 0f), stroke)
        drawLine(color, Offset(size.width, 0f), Offset(size.width * 0.6f, 0f), stroke)
        drawLine(color, Offset(size.width, 0f), Offset(size.width, size.height * 0.4f), stroke)
    }
}

@Composable
internal fun ComboIcon(spacing: Spacing, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.size(spacing.semiLarge)) {
        val stroke = spacing.tiny.toPx()
        // Lightning bolt / Flash shape
        val path = Path().apply {
            moveTo(size.width * 0.6f, 0f)
            lineTo(size.width * 0.2f, size.height * 0.6f)
            lineTo(size.width * 0.5f, size.height * 0.6f)
            lineTo(size.width * 0.4f, size.height)
            lineTo(size.width * 0.8f, size.height * 0.4f)
            lineTo(size.width * 0.5f, size.height * 0.4f)
            close()
        }
        drawPath(path, color, style = Stroke(stroke))
    }
}

@Composable
internal fun MergeIcon(spacing: Spacing, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.size(spacing.semiLarge)) {
        val stroke = spacing.extraTiny.toPx() * 1.5f
        // Three merging circles
        drawCircle(color, radius = size.width / 4, center = Offset(size.width / 2, size.height / 3), style = Stroke(stroke))
        drawCircle(color, radius = size.width / 4, center = Offset(size.width / 3, size.height * 0.7f), style = Stroke(stroke))
        drawCircle(color, radius = size.width / 4, center = Offset(size.width * 0.66f, size.height * 0.7f), style = Stroke(stroke))
    }
}
