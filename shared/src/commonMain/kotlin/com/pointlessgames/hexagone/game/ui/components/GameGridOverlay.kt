package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeHint
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PreviewCell
import com.pointlessgames.hexagone.game.model.ScorePopup
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GameGridOverlay(
    gridState: List<HexagonCell>,
    mergeHints: List<MergeHint>,
    previewState: List<PreviewCell>,
    pendingMerge: MergeTransition?,
    hoveredMerge: MergeTransition?,
    activePerk: Perk?,
    selectedCellId: String?,
    activeMergeStepIndex: Int,
    pendingMergeScore: Int,
    particles: List<Particle>,
    scorePopups: List<ScorePopup>,
    combo: Int,
    onEmptySpaceClick: (Int, Int) -> Unit,
    onEmptySpaceTouchDown: (Int, Int) -> Unit,
    onEmptySpaceTouchUp: () -> Unit,
    onCellClick: (HexagonCell) -> Unit,
    onMergeAnimationFinished: () -> Unit,
    onAddParticles: (List<Particle>) -> Unit,
    onAddScorePopup: (Float, Float, Int, Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var finishedMergeCount by remember { mutableStateOf(0) }

    val columns = 5
    val rows = 4
    val itemGap = 4.dp
    val gapPx = with(density) { itemGap.toPx() }

    val moveAnimationSpec = remember {
        spring<IntOffset>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = IntOffset(1, 1),
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val wiggleRotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "wiggle",
    )

    val previewPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val previewFloat by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "float",
    )

    val previewAlpha by animateFloatAsState(
        targetValue = if (hoveredMerge != null) 1f else 0f,
        animationSpec = tween(300),
        label = "preview_alpha",
    )

    val previewScaleTarget by animateFloatAsState(
        targetValue = if (hoveredMerge != null) 1f else 0.8f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "preview_scale",
    )

    BoxWithConstraints(
        modifier = modifier.graphicsLayer { clip = false },
        contentAlignment = Alignment.Center,
    ) {
        val cellWidth = constraints.maxWidth / (1f + (columns - 1) * 0.75f)
        val cellHeight = cellWidth * (sqrt(3f) / 2f)

        val itemWidth = (cellWidth - gapPx).coerceAtLeast(0f)
        val itemHeight = (cellHeight - gapPx).coerceAtLeast(0f)

        val totalWidth = (cellWidth * (1f + (columns - 1) * 0.75f))
        val totalHeight = (cellHeight * (rows + 0.5f))

        val currentStep = pendingMerge?.steps?.getOrNull(activeMergeStepIndex)
        val mergingCells = currentStep?.mergingCells ?: emptyList()

        LaunchedEffect(pendingMerge, activeMergeStepIndex) {
            val step = currentStep
            if (step != null && pendingMerge != null) {
                finishedMergeCount = 0
                if (step.mergingCells.isEmpty()) {
                    onMergeAnimationFinished()
                } else {
                    snapshotFlow { finishedMergeCount }
                        .filter { it >= step.mergingCells.size }
                        .first()

                    val targetOffset = HexagonGridDefaults.calculateOffset(
                        pendingMerge.targetX,
                        pendingMerge.targetY,
                        cellWidth,
                        cellHeight,
                        gapPx,
                    )
                    val center = Offset(
                        targetOffset.x + itemWidth / 2,
                        targetOffset.y + itemHeight / 2,
                    )
                    val color = HexagonGridDefaults.getColorForValue(step.resultValue)

                    // 1. Add Particles
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
                    onAddParticles(newParticles)

                    // 2. Add Score Popup with correct value (including combo)
                    // Only on the last step to avoid intermediate score discrepancies
                    val isLastStep = activeMergeStepIndex >= pendingMerge.steps.lastIndex
                    if (isLastStep) {
                        onAddScorePopup(center.x, center.y, pendingMergeScore, color)
                    }

                    onMergeAnimationFinished()
                }
            }
        }

        fun getCellAt(x: Float, y: Float): Pair<Int, Int>? {
            for (col in 0 until columns) {
                val xOffset = col * 0.75f * cellWidth + gapPx / 2
                val yOffset = (if (col % 2 == 1) cellHeight / 2 else 0f) + gapPx / 2
                for (row in 0 until rows) {
                    val yStart = row * cellHeight + yOffset
                    if (x >= xOffset && x <= xOffset + cellWidth - gapPx &&
                        y >= yStart && y <= yStart + cellHeight - gapPx
                    ) {
                        return col to row
                    }
                }
            }
            return null
        }

        Box(
            modifier = Modifier
                .size(
                    width = with(density) { totalWidth.toDp() },
                    height = with(density) { totalHeight.toDp() }
                )
                .graphicsLayer { clip = false }
                .pointerInput(cellWidth, cellHeight, gapPx) {
                    coroutineScope {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val initialCell = getCellAt(down.position.x, down.position.y)

                            val job = launch {
                                delay(200)
                                initialCell?.let { (x, y) ->
                                    onEmptySpaceTouchDown(x, y)
                                }
                            }

                            try {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    val change = event.changes.first()
                                    if (change.changedToUp()) {
                                        job.cancel()
                                        val upCell = getCellAt(change.position.x, change.position.y)
                                        // Only activate if release is on the same cell we started/locked on
                                        if (upCell != null && upCell == initialCell) {
                                            onEmptySpaceClick(upCell.first, upCell.second)
                                        }
                                        onEmptySpaceTouchUp()
                                        break
                                    } else if (!change.pressed) {
                                        break
                                    }
                                }
                            } finally {
                                job.cancel()
                                onEmptySpaceTouchUp()
                            }
                        }
                    }
                }
        ) {
            HexagonGrid(
                columns = columns,
                rows = rows,
                itemGap = itemGap,
                outlineContent = { col, row ->
                    val hint = mergeHints.find { it.x == col && it.y == row }
                    Box(contentAlignment = Alignment.Center) {
                        Hexagon(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (activePerk == Perk.MOVE_TILE && selectedCellId != null) {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.4f),
                                            shape = FlatTopHexagonShape(),
                                        )
                                    } else Modifier,
                                ),
                            isOutline = true,
                        )
                        if (hint != null) {
                            val dotSize = (4 + hint.weight * 6).dp
                            Box(
                                modifier = Modifier
                                    .size(dotSize)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            )
                        }
                    }
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
                        val isMoving = animatedOffset != targetOffset
                        val targetScale = 0.8f
                        val animatedScale by animateFloatAsState(
                            targetValue = targetScale,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = 1800f,
                            ),
                            label = "ghost_scale",
                        )

                        val isSelected = selectedCellId == preview.id
                        val isSelectable =
                            activePerk == Perk.MOVE_TILE || activePerk == Perk.REMOVE_TILE || activePerk == Perk.SWAP_TILES
                        val isOverlappedByHover = hoveredMerge != null &&
                                hoveredMerge.targetX == preview.x &&
                                hoveredMerge.targetY == preview.y

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
                                .zIndex(if (isSelected || isMoving) 5f else 1f)
                                .graphicsLayer {
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                    rotationZ = if (isSelectable && !isSelected) wiggleRotation else 0f
                                    alpha = if (isOverlappedByHover) 0f else 1f
                                }
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.dp,
                                        Color.White,
                                        FlatTopHexagonShape(),
                                    ) else Modifier,
                                ),
                            onClick = null,
                            isGhost = true,
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
                                if (mergingCells.any { it.id == cell.id }) {
                                    finishedMergeCount++
                                }
                            },
                        )
                        val isMoving = animatedOffset != targetOffset

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
                        val isHovered = hoveredMerge?.steps?.any { step -> step.mergingCells.any { it.id == cell.id } } == true
                        val isMerging = mergingCells.any { it.id == cell.id } == true
                        val isSelectable =
                            activePerk == Perk.MOVE_TILE || activePerk == Perk.REMOVE_TILE || activePerk == Perk.SWAP_TILES

                        Hexagon(
                            value = cell.value.toString(),
                            backgroundColor = HexagonGridDefaults.getColorForValue(cell.value),
                            modifier = Modifier
                                .size(
                                    width = with(density) { itemWidth.toDp() },
                                    height = with(density) { itemHeight.toDp() },
                                )
                                .offset { animatedOffset }
                                .zIndex(if (isSelected || isHovered || isMerging || isMoving) 5f else 1f)
                                .graphicsLayer {
                                    this.alpha = alpha
                                    scaleX =
                                        scale * (if (isSelected) 1.2f else if (isHovered) 1.1f else 1f)
                                    scaleY =
                                        scale * (if (isSelected) 1.2f else if (isHovered) 1.1f else 1f)
                                    rotationZ = if (isSelectable && !isSelected) wiggleRotation else 0f
                                }
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.dp,
                                        Color.White,
                                        FlatTopHexagonShape(),
                                    ) else if (isHovered) Modifier.border(
                                        2.dp,
                                        Color.White.copy(alpha = 0.5f),
                                        FlatTopHexagonShape(),
                                    ) else Modifier,
                                ),
                            onClick = if (activePerk != null) {
                                { onCellClick(cell) }
                            } else null,
                        )
                    }
                }

                // Hover Result Preview
                hoveredMerge?.let { merge ->
                    val targetOffset = HexagonGridDefaults.calculateOffset(
                        merge.targetX,
                        merge.targetY,
                        cellWidth,
                        cellHeight,
                        gapPx,
                    )

                    Hexagon(
                        value = merge.finalValue.toString(),
                        backgroundColor = HexagonGridDefaults.getColorForValue(merge.finalValue)
                            .copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(
                                width = with(density) { itemWidth.toDp() },
                                height = with(density) { itemHeight.toDp() },
                            )
                            .offset { targetOffset }
                            .zIndex(3f)
                            .graphicsLayer {
                                scaleX = 0.9f * previewPulse * previewScaleTarget
                                scaleY = 0.9f * previewPulse * previewScaleTarget
                                alpha = 0.7f * previewAlpha
                            }
                            .border(
                                width = 2.dp,
                                color = Color.White.copy(alpha = 0.3f * previewAlpha),
                                shape = FlatTopHexagonShape(),
                            ),
                        isOutline = false,
                        isGhost = true,
                    )
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEach { p ->
                    drawCircle(
                        color = p.color,
                        radius = p.size * p.life,
                        center = Offset(p.x, p.y),
                        alpha = p.life,
                    )
                }
            }

            scorePopups.forEach { popup ->
                key(popup.id) {
                    Box(
                        modifier = Modifier
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                layout(placeable.width, placeable.height) {
                                    placeable.placeRelative(
                                        (popup.x - placeable.width / 2).toInt(),
                                        (popup.y - placeable.height / 2).toInt(),
                                    )
                                }
                            }
                            .zIndex(100f)
                            .graphicsLayer {
                                alpha = popup.life
                                val s = 1f + (1f - popup.life) * 0.3f
                                scaleX = s
                                scaleY = s
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "+${popup.score}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.6f * popup.life), CircleShape)
                                .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.4f * popup.life),
                                    CircleShape,
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            // Predicted score preview
            hoveredMerge?.let { merge ->
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
                val predictedScore = merge.baseScore * (combo + 1)

                Box(
                    modifier = Modifier
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            layout(placeable.width, placeable.height) {
                                placeable.placeRelative(
                                    (center.x - placeable.width / 2).toInt(),
                                    (center.y - 140 + previewFloat - placeable.height / 2).toInt(),
                                )
                            }
                        }
                        .zIndex(101f)
                        .graphicsLayer {
                            scaleX = previewPulse * previewScaleTarget
                            scaleY = previewPulse * previewScaleTarget
                            alpha = previewAlpha
                        }
                        .background(Color.Black.copy(alpha = 0.7f * previewAlpha), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.4f * previewAlpha), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+$predictedScore",
                        color = Color.White.copy(alpha = previewAlpha),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}
