package com.pointlessgames.hexagone.game.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.model.Particle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
internal fun GameScreen(viewModel: GameViewModel) {
    val gridState by viewModel.gridState.collectAsState()
    val previewState by viewModel.previewState.collectAsState()
    val pendingMerge by viewModel.pendingMerge.collectAsState()
    val density = LocalDensity.current

    var finishedMergeCount by remember { mutableStateOf(0) }
    var particles by remember { mutableStateOf(emptyList<Particle>()) }

    LaunchedEffect(particles.isNotEmpty()) {
        if (particles.isEmpty()) return@LaunchedEffect
        var lastTime = withFrameNanos { it }
        while (particles.isNotEmpty()) {
            val currentTime = withFrameNanos { it }
            val dt = (currentTime - lastTime) / 1_000_000_000f
            lastTime = currentTime

            particles = particles.mapNotNull { p ->
                if (p.life <= 0) null
                else p.copy(
                    x = p.x + p.vx * dt,
                    y = p.y + p.vy * dt,
                    life = p.life - dt * 2f,
                )
            }
        }
    }

    LaunchedEffect(pendingMerge) {
        if (pendingMerge != null) {
            finishedMergeCount = 0
        }
    }

    val moveAnimationSpec = remember {
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = IntOffset(10, 10),
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val columns = 5
            val rows = 4
            val itemGap = 4.dp

            BoxWithConstraints(
                modifier = Modifier.padding(16.dp),
            ) {
                val gapPx = with(density) { itemGap.toPx() }
                val cellWidth = constraints.maxWidth / (1f + (columns - 1) * 0.75f)
                val cellHeight = cellWidth * (sqrt(3f) / 2f)

                val itemWidth = (cellWidth - gapPx).coerceAtLeast(0f)
                val itemHeight = (cellHeight - gapPx).coerceAtLeast(0f)

                LaunchedEffect(finishedMergeCount, pendingMerge) {
                    val merge = pendingMerge
                    if (merge != null && finishedMergeCount >= merge.mergingCells.size) {
                        val targetOffset = HexagonGridDefaults.calculateOffset(
                            merge.targetX,
                            merge.targetY,
                            cellWidth,
                            cellHeight,
                            gapPx,
                        )
                        val center = Offset(
                            targetOffset.x + itemWidth / 2,
                            targetOffset.y + itemHeight / 2,
                        )
                        val color = HexagonGridDefaults.getColorForValue(merge.newValue)

                        val newParticles = List(30) {
                            val angle = Random.nextFloat() * 2 * PI.toFloat()
                            val speed = Random.nextFloat() * 400f + 200f
                            Particle(
                                id = Random.nextLong(),
                                x = center.x,
                                y = center.y,
                                vx = cos(angle) * speed,
                                vy = sin(angle) * speed,
                                color = color,
                                life = 1f,
                                size = Random.nextFloat() * 8f + 4f,
                            )
                        }
                        particles = particles + newParticles
                        viewModel.onMergeAnimationFinished()
                    }
                }

                HexagonGrid(
                    columns = columns,
                    rows = rows,
                    itemGap = itemGap,
                    outlineContent = { col, row ->
                        Hexagon(
                            modifier = Modifier.fillMaxSize(),
                            onClick = { viewModel.onEmptySpaceClicked(col, row) },
                        )
                    },
                ) {
                    // Draw preview hexagons (ghosts)
                    previewState.forEach { preview ->
                        key(preview.id) {
                            val targetOffset = HexagonGridDefaults.calculateOffset(
                                preview.x,
                                preview.y,
                                cellWidth,
                                cellHeight,
                                gapPx,
                            )
                            val animatedOffset by animateIntOffsetAsState(
                                targetValue = targetOffset,
                                animationSpec = moveAnimationSpec,
                                label = "ghost_offset",
                            )
                            val targetScale = when (preview.rank) {
                                0 -> 0.8f
                                1 -> 0.6f
                                else -> 0.4f
                            }
                            val animatedScale by animateFloatAsState(
                                targetValue = targetScale,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                ),
                                label = "ghost_scale",
                            )
                            Hexagon(
                                value = preview.value.toString(),
                                backgroundColor = HexagonGridDefaults.getColorForValue(preview.value)
                                    .copy(alpha = 0.3f),
                                modifier = Modifier
                                    .size(
                                        width = with(density) { itemWidth.toDp() },
                                        height = with(density) { itemHeight.toDp() },
                                    )
                                    .offset { animatedOffset }
                                    .graphicsLayer {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                    },
                            )
                        }
                    }

                    // Draw actual grid tiles
                    gridState.forEach { cell ->
                        key(cell.id) {
                            val targetOffset = HexagonGridDefaults.calculateOffset(
                                cell.x,
                                cell.y,
                                cellWidth,
                                cellHeight,
                                gapPx,
                            )
                            val animatedOffset by animateIntOffsetAsState(
                                targetValue = targetOffset,
                                animationSpec = moveAnimationSpec,
                                label = "cell_offset",
                                finishedListener = {
                                    if (pendingMerge?.mergingCells?.any { it.id == cell.id } == true) {
                                        finishedMergeCount++
                                    }
                                },
                            )

                            var targetScale by remember { mutableStateOf(0f) }
                            val scale by animateFloatAsState(
                                targetValue = targetScale,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow,
                                ),
                                label = "cell_scale",
                            )
                            LaunchedEffect(Unit) {
                                targetScale = 1f
                            }

                            Hexagon(
                                value = cell.value.toString(),
                                backgroundColor = HexagonGridDefaults.getColorForValue(cell.value),
                                modifier = Modifier
                                    .size(
                                        width = with(density) { itemWidth.toDp() },
                                        height = with(density) { itemHeight.toDp() },
                                    )
                                    .offset { animatedOffset }
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    },
                            )
                        }
                    }
                }

                Canvas(modifier = Modifier.matchParentSize()) {
                    particles.forEach { p ->
                        drawCircle(
                            color = p.color,
                            radius = p.size * p.life,
                            center = Offset(p.x, p.y),
                            alpha = p.life,
                        )
                    }
                }
            }
        }
    }
}
