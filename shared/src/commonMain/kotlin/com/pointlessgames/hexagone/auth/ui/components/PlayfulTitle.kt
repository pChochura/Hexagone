package com.pointlessgames.hexagone.auth.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.ui.components.Hexagon
import com.pointlessgames.hexagone.ui.theme.scaled
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun PlayfulTitle(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val title = "HEXAGONE"
    var waveTrigger by remember { mutableStateOf(0L) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(3000, 6000).milliseconds)
            waveTrigger++
            delay(3000.milliseconds) // Cooldown between waves
        }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val maxTitleWidth = maxWidth * 0.95f // 5% margin
        val idealCharWidth = 44.dp.scaled
        val charWidth = if (idealCharWidth * title.length > maxTitleWidth) {
            maxTitleWidth / title.length
        } else {
            idealCharWidth
        }

        Row(
            modifier = Modifier.wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            title.forEachIndexed { index, char ->
                AnimatedCharacter(
                    char = char,
                    index = index,
                    waveTrigger = waveTrigger,
                    color = color,
                    baseSize = charWidth,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun AnimatedCharacter(
    char: Char,
    index: Int,
    waveTrigger: Long,
    color: Color,
    baseSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    var isWaving by remember { mutableStateOf(false) }
    var isHex by remember { mutableStateOf(false) }
    
    val colorScheme = MaterialTheme.colorScheme
    val hexColor = remember {
        listOf(
            colorScheme.primary,
            colorScheme.secondary,
            colorScheme.tertiary,
            colorScheme.surfaceContainerHigh,
            colorScheme.surfaceContainerHighest,
        ).random()
    }

    // Independent Hex Pop Logic
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2000, 10000).milliseconds)
            isHex = true
            delay(Random.nextLong(1500, 4000).milliseconds)
            isHex = false
        }
    }

    // Wave Logic
    LaunchedEffect(waveTrigger) {
        if (waveTrigger > 0) {
            delay((index * 100).milliseconds)
            isWaving = true
            delay(800.milliseconds)
            isWaving = false
        }
    }

    val offsetY by animateDpAsState(
        targetValue = if (isWaving) (-16).dp.scaled else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "wave_y"
    )

    val contentScale by animateFloatAsState(
        targetValue = if (isHex || isWaving) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "pop_scale"
    )

    val fontSize = (baseSize.value * 0.9f).sp

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .scale(contentScale)
            .size(width = baseSize, height = baseSize * 0.866f),
        contentAlignment = Alignment.Center
    ) {
        if (isHex) {
            Hexagon(
                value = char.toString(),
                backgroundColor = hexColor,
                modifier = Modifier.fillMaxSize(),
                maxFontSize = (fontSize.value * 0.4f).sp,
            )
        } else {
            Text(
                text = char.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = fontSize,
                    letterSpacing = 0.sp
                ),
                color = color,
                fontWeight = FontWeight.Black,
            )
        }
    }
}
