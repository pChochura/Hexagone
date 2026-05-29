package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.zIndex
import com.pointlessgames.hexagone.game.model.Particle

@Composable
internal fun ParticlesLayer(particles: List<Particle>, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize().zIndex(90f)) {
        particles.forEach { p -> drawCircle(p.color, p.size * p.life, Offset(p.x, p.y), p.life) }
    }
}
