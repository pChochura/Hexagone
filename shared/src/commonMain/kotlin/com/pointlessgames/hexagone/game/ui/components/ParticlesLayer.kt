package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import com.pointlessgames.hexagone.game.model.Particle

@Composable
internal fun ParticlesLayer(particles: List<Particle>, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize().zIndex(200f)) {
        particles.forEach { p ->
            if (p.life > 0) {
                val alpha = p.life.coerceIn(0f, 1f)
                // Draw glow/aura
                drawCircle(
                    color = p.color,
                    radius = p.size * p.life * 1.5f,
                    center = Offset(p.x, p.y),
                    alpha = alpha * 0.3f
                )
                // Draw core
                drawCircle(
                    color = Color.White,
                    radius = p.size * p.life * 0.6f,
                    center = Offset(p.x, p.y),
                    alpha = alpha
                )
            }
        }
    }
}
