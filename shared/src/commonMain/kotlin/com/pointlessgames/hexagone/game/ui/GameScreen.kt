package com.pointlessgames.hexagone.game.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val score by viewModel.score.collectAsState()
    val bestScore by viewModel.bestScore.collectAsState()
    val level by viewModel.level.collectAsState()
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
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = IntOffset(1, 1),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1C1C24), Color(0xFF0A0A0E)),
                ),
            )
            .systemBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Score section
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ScoreCard(label = "SCORE", value = score.toString(), modifier = Modifier.weight(1f))
            ScoreCard(
                label = "BEST",
                value = bestScore.toString(),
                modifier = Modifier.weight(1f),
                isBest = true,
            )
        }

        Spacer(Modifier.height(32.dp))

        // Next Piece section
        Text(
            text = "NEXT PIECE",
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )

        Spacer(Modifier.height(8.dp))

        Box(modifier = Modifier.size(60.dp)) {
            val nextValue = previewState.firstOrNull()?.value ?: 1
            Hexagon(
                value = nextValue.toString(),
                backgroundColor = HexagonGridDefaults.getColorForValue(nextValue),
                modifier = Modifier.fillMaxSize().aspectRatio(1 / 0.866f),
            )
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val columns = 5
            val rows = 4
            val itemGap = 4.dp

            BoxWithConstraints {
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
                            isOutline = true,
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
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = 1800f,
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
                                    stiffness = Spring.StiffnessMedium,
                                ),
                                label = "cell_scale",
                            )

                            val alpha by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(
                                    stiffness = Spring.StiffnessMedium,
                                ),
                                label = "cell_alpha",
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
                                        this.alpha = alpha
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

        Spacer(Modifier.height(16.dp))

        // Bottom section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "COMBO X2",
                color = Color(0xFFF06292),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "LEVEL $level",
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { 0.6f },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = Color(0xFFF06292),
            trackColor = Color.White.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Round,
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun ScoreCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isBest: Boolean = false,
) {
    Column(
        modifier = modifier
            .background(Color(0xFF1C1C24), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
        )
        Text(
            text = value,
            color = if (isBest) Color(0xFFF06292) else Color(0xFF9FA8DA),
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
        )
    }
}
