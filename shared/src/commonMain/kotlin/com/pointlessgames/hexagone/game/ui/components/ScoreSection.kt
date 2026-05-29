package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.app_name
import hexagone.shared.generated.resources.best_score_label
import hexagone.shared.generated.resources.level_label
import hexagone.shared.generated.resources.max_label
import hexagone.shared.generated.resources.perk_action_first
import hexagone.shared.generated.resources.perk_action_fuse
import hexagone.shared.generated.resources.perk_action_move
import hexagone.shared.generated.resources.perk_action_pick
import hexagone.shared.generated.resources.perk_action_second
import hexagone.shared.generated.resources.perk_action_select
import hexagone.shared.generated.resources.perk_active_label
import hexagone.shared.generated.resources.score_label
import hexagone.shared.generated.resources.tier_overdrive
import hexagone.shared.generated.resources.tier_surge
import hexagone.shared.generated.resources.tier_zenith
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ScoreSection(
    score: Int,
    bestScore: Int,
    combo: Int,
    level: Int,
    progress: Float,
    highestValue: Int,
    activePerk: Perk?,
    selectedCellId: String?,
    modifier: Modifier = Modifier,
    onLevelClick: () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress_animation"
    )

    val waveIntensity = remember { Animatable(0f) }
    var previousScore by remember { mutableStateOf(score) }
    LaunchedEffect(score) {
        val addedScore = score - previousScore
        if (addedScore > 0) {
            // Intensity scales from 0.3 to 1.0 based on the amount scored
            // 200 points is considered a "major" move at higher levels
            val threshold = 100f + 25f * level
            val intensity = (addedScore / threshold).coerceIn(0.3f, 1.0f)
            waveIntensity.snapTo(intensity)
            waveIntensity.animateTo(0f, tween(1000))
        }
        previousScore = score
    }

    val waveOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        var lastTimeNanos = withFrameNanos { it }
        while (true) {
            val currentTimeNanos = withFrameNanos { it }
            val dt = (currentTimeNanos - lastTimeNanos) / 1_000_000_000f
            lastTimeNanos = currentTimeNanos
            val baseSpeed = PI.toFloat() / 2f
            val speed = baseSpeed * (1f + waveIntensity.value * 4f)
            val delta = dt * speed
            waveOffset.snapTo((waveOffset.value + delta) % (2 * PI.toFloat()))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false } // Allow massive combo pop to breathe
            .padding(top = MaterialTheme.spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val colorScheme = MaterialTheme.colorScheme
        val spacing = MaterialTheme.spacing
        val cornerRadius = MaterialTheme.cornerRadius
        // Top Header with Game Name and Icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO */ }) {
                Canvas(modifier = Modifier.size(spacing.extraLarge)) {
                    val barWidth = size.width * 0.2f
                    val gap = size.width * 0.1f
                    drawRect(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset(0f, size.height * 0.6f),
                        size = Size(barWidth, size.height * 0.4f)
                    )
                    drawRect(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset(barWidth + gap, size.height * 0.2f),
                        size = Size(barWidth, size.height * 0.8f)
                    )
                    drawRect(
                        color = Color.White.copy(alpha = 0.7f),
                        topLeft = Offset((barWidth + gap) * 2, size.height * 0.4f),
                        size = Size(barWidth, size.height * 0.6f)
                    )
                }
            }

            Text(
                text = stringResource(Res.string.app_name).uppercase(),
                color = MaterialTheme.colorScheme.outlineVariant,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                letterSpacing = 2.sp
            )

            IconButton(onClick = { /* TODO */ }) {
                Canvas(modifier = Modifier.size(spacing.extraLarge)) {
                    val outerRadius = size.minDimension / 2.5f
                    val innerRadius = size.minDimension / 5f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        radius = outerRadius,
                        style = Stroke(width = spacing.tiny.toPx())
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.7f),
                        radius = innerRadius
                    )
                    // Gear teeth
                    for (i in 0 until 8) {
                        val angle = i * (2 * PI / 8).toFloat()
                        val start = center + Offset(cos(angle) * outerRadius, sin(angle) * outerRadius)
                        val end = center + Offset(cos(angle) * (outerRadius + spacing.extraSmall.toPx()), sin(angle) * (outerRadius + spacing.extraSmall.toPx()))
                        drawLine(
                            color = Color.White.copy(alpha = 0.7f),
                            start = start,
                            end = end,
                            strokeWidth = spacing.extraSmall.toPx() * 0.75f
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(spacing.large))

        // Unified Score Section with Progress and Max/Perk Integrated
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(cornerRadius.large))
                .border(spacing.extraTiny, Color.White.copy(alpha = 0.05f), RoundedCornerShape(cornerRadius.large))
                .graphicsLayer { clip = false }, // Allow children (combo) to pop outside
        ) {
            // Background Progress Bar with Wavy Edge
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(cornerRadius.large)) // Keep bar inside the rounded container
            ) {
                val width = size.width * animatedProgress
                val height = size.height
                
                if (width > 0) {
                    val currentWaveOffset = waveOffset.value
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(width, 0f)
                        
                        // Wavy edge at the progress boundary
                        val waveAmplitude = (spacing.extraSmall + spacing.semiMedium * waveIntensity.value).toPx()
                        val wavePeriod = height * 0.8f
                        
                        val steps = 30
                        for (i in 0..steps) {
                            val y = (i / steps.toFloat()) * height
                            val dx = sin(y / wavePeriod * 2 * PI.toFloat() + currentWaveOffset) * waveAmplitude
                            lineTo(width + dx, y)
                        }
                        
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            listOf(
                                colorScheme.scrim.copy(alpha = 0.1f + 0.1f * waveIntensity.value),
                                colorScheme.onPrimaryContainer.copy(alpha = 0.1f + 0.1f * waveIntensity.value),
                                colorScheme.primary.copy(alpha = 0.1f + 0.1f * waveIntensity.value)
                            )
                        )
                    )

                    // Add a subtle highlight at the very edge
                    val edgePath = Path().apply {
                        val waveAmplitude = (spacing.extraSmall + spacing.semiMedium * waveIntensity.value).toPx()
                        val wavePeriod = height * 0.8f
                        
                        val firstY = 0f
                        val firstDx = sin(firstY / wavePeriod * 2 * PI.toFloat() + currentWaveOffset) * waveAmplitude
                        moveTo(width + firstDx, firstY)
                        
                        val steps = 30
                        for (i in 1..steps) {
                            val y = (i / steps.toFloat()) * height
                            val dx = sin(y / wavePeriod * 2 * PI.toFloat() + currentWaveOffset) * waveAmplitude
                            lineTo(width + dx, y)
                        }
                    }
                    
                    drawPath(
                        path = edgePath,
                        color = Color.White.copy(alpha = 0.1f + 0.2f * waveIntensity.value),
                        style = Stroke(width = (spacing.extraTiny + spacing.extraTiny * waveIntensity.value).toPx())
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.extraLarge, vertical = spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.score_label),
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.width(spacing.small))
                    Text(
                        text = "•",
                        color = Color.White.copy(alpha = 0.2f),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.width(spacing.small))
                    Text(
                        text = stringResource(Res.string.level_label, level),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            onLevelClick()
                        }
                    )
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.wrapContentHeight()) {
                    Text(
                        text = score.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(spacing.tiny))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.best_score_label),
                        color = Color.White.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                    )
                    Text(
                        text = bestScore.toString(),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                    )
                }
            }

            // Integrated Combo Section
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = spacing.extraLarge)
                    .width(spacing.giant),
                contentAlignment = Alignment.Center
            ) {
                val comboMultiplier = combo + 1
                AnimatedContent(
                    targetState = comboMultiplier,
                    transitionSpec = {
                        val settleDuration = when {
                            targetState > 8 -> 5000
                            targetState > 4 -> 4000
                            else -> 3000
                        }
                        (fadeIn(animationSpec = tween(200)) + 
                            scaleIn(
                                initialScale = 3f,
                                animationSpec = tween(
                                    durationMillis = settleDuration,
                                    easing = EaseOutExpo
                                )
                            ))
                            .togetherWith(fadeOut(animationSpec = tween(200)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "combo_pop"
                ) { targetCombo ->
                    if (targetCombo > 1) {
                        val tier = when {
                            targetCombo >= 31 -> Res.string.tier_zenith
                            targetCombo >= 21 -> Res.string.tier_overdrive
                            targetCombo >= 13 -> Res.string.tier_surge
                            else -> null
                        }
                        
                        val colorFraction = ((targetCombo - 1) / 9f).coerceIn(0f, 1f)
                        val baseColor = lerp(
                            MaterialTheme.colorScheme.surfaceDim, // Yellow/Gold
                            MaterialTheme.colorScheme.errorContainer, // Intense Orange/Red
                            colorFraction
                        )
                        
                        val comboColor = when (tier) {
                            Res.string.tier_surge -> MaterialTheme.colorScheme.scrim
                            Res.string.tier_overdrive -> MaterialTheme.colorScheme.inverseSurface
                            Res.string.tier_zenith -> MaterialTheme.colorScheme.surfaceBright
                            else -> baseColor
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "x$targetCombo",
                                color = comboColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                style = TextStyle(
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = Offset(4f, 4f),
                                        blurRadius = 8f
                                    )
                                ),
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = -5f + colorFraction * 10f
                                }
                            )
                            if (tier != null) {
                                Text(
                                    text = stringResource(tier),
                                    color = comboColor,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.offset(y = -spacing.extraSmall)
                                )
                            }
                        }
                    }
                }
            }

            // Integrated Max Value / Perk Section
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = spacing.extraLarge)
                    .width(spacing.giant),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = activePerk,
                    transitionSpec = {
                        (fadeIn() togetherWith fadeOut()).using(SizeTransform(clip = false))
                    },
                    label = "integrated_max_value"
                ) { perk ->
                    if (perk != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(Res.string.perk_active_label),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            )
                            val actionRes = when (perk) {
                                Perk.MOVE_TILE -> if (selectedCellId == null) Res.string.perk_action_select else Res.string.perk_action_move
                                Perk.REMOVE_TILE -> Res.string.perk_action_pick
                                Perk.SWAP_TILES -> if (selectedCellId == null) Res.string.perk_action_first else Res.string.perk_action_second
                                else -> Res.string.perk_action_fuse
                            }
                            Text(
                                text = stringResource(actionRes),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 12.sp
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(Res.string.max_label),
                                color = Color.White.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                            )
                            Spacer(Modifier.height(spacing.extraSmall))
                            val colorScheme = MaterialTheme.colorScheme
                            Hexagon(
                                value = highestValue.toString(),
                                backgroundColor = HexagonGridDefaults.getColorForValue(highestValue, colorScheme).copy(alpha = 0.2f),
                                isOutline = true,
                                modifier = Modifier.size(spacing.huge).aspectRatio(1 / 0.866f),
                            )
                        }
                    }
                }
            }
        }
    }
}
