package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PreviewCell
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun GameGridOverlay(
    gridState: List<HexagonCell>,
    previewState: List<PreviewCell>,
    pendingMerge: MergeTransition?,
    activePerk: Perk?,
    selectedCellId: String?,
    particles: List<Particle>,
    onEmptySpaceClick: (Int, Int) -> Unit,
    onCellClick: (HexagonCell) -> Unit,
    onPreviewClick: (PreviewCell) -> Unit,
    onMergeAnimationFinished: () -> Unit,
    onParticlesUpdate: (List<Particle>) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var finishedMergeCount by remember { mutableStateOf(0) }

    LaunchedEffect(pendingMerge) {
        if (pendingMerge != null) {
            finishedMergeCount = 0
        }
    }

    LaunchedEffect(finishedMergeCount, pendingMerge) {
        val merge = pendingMerge
        if (merge != null && finishedMergeCount >= merge.mergingCells.size) {
            onMergeAnimationFinished()
        }
    }

    val moveAnimationSpec = remember {
        spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = IntOffset(1, 1),
        )
    }

    BoxWithConstraints(modifier = modifier.animateContentSize()) {
        val columns = 5
        val rows = 4
        val itemGap = 4.dp
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
                onParticlesUpdate(particles + newParticles)
            }
        }

        HexagonGrid(
            columns = columns,
            rows = rows,
            itemGap = itemGap,
            outlineContent = { col, row ->
                Hexagon(
                    modifier = Modifier.fillMaxSize(),
                    onClick = { onEmptySpaceClick(col, row) },
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
                        onClick = if (activePerk != null) {
                            { onPreviewClick(preview) }
                        } else null,
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

                    val isSelected = selectedCellId == cell.id

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
                                scaleX = scale * (if (isSelected) 1.2f else 1f)
                                scaleY = scale * (if (isSelected) 1.2f else 1f)
                            }
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp,
                                    Color.White,
                                    FlatTopHexagonShape(),
                                ) else Modifier,
                            ),
                        onClick = if (activePerk != null) {
                            { onCellClick(cell) }
                        } else null,
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
