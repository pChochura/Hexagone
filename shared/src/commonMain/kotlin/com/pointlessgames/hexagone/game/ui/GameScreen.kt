package com.pointlessgames.hexagone.game.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.pointlessgames.hexagone.game.GameViewModel
import kotlin.math.sqrt

@Composable
internal fun GameScreen(viewModel: GameViewModel) {
    val gridState by viewModel.gridState.collectAsState()
    val previewState by viewModel.previewState.collectAsState()
    val pendingMerge by viewModel.pendingMerge.collectAsState()
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
            viewModel.onMergeAnimationFinished()
        }
    }

    val moveAnimationSpec = remember {
        spring<androidx.compose.ui.unit.IntOffset>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
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

            BoxWithConstraints(modifier = Modifier.padding(16.dp)) {
                val gapPx = with(density) { itemGap.toPx() }
                val cellWidth = constraints.maxWidth / (1f + (columns - 1) * 0.75f)
                val cellHeight = cellWidth * (sqrt(3f) / 2f)

                val itemWidth = (cellWidth - gapPx).coerceAtLeast(0f)
                val itemHeight = (cellHeight - gapPx).coerceAtLeast(0f)

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
                                }
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
            }
        }
    }
}
