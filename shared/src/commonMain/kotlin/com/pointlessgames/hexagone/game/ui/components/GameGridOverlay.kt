package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.pointlessgames.hexagone.game.GameEffect
import com.pointlessgames.hexagone.game.PotentialMerge
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeHint
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.OnBoardPerk
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PreviewCell
import com.pointlessgames.hexagone.game.model.ScorePopup
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun GameGridOverlay(
    gridState: List<HexagonCell>,
    onBoardPerks: List<OnBoardPerk>,
    mergeHints: List<MergeHint>,
    previewState: List<PreviewCell>,
    pendingMerge: MergeTransition?,
    hoveredMergeState: StateFlow<MergeTransition?>,
    potentialMerges: Map<Pair<Int, Int>, PotentialMerge>,
    activePerk: Perk?,
    selectedCellId: String?,
    activeMergeStepIndex: Int,
    pendingMergeScore: Int,
    combo: Int,
    effects: SharedFlow<GameEffect>,
    onEmptySpaceClick: (Int, Int) -> Unit,
    onEmptySpaceTouchDown: (Int, Int) -> Unit,
    onEmptySpaceTouchUp: () -> Unit,
    onCellClick: (HexagonCell) -> Unit,
    onMergeAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val finishedMergeCount = remember { mutableIntStateOf(0) }
    val localParticles = remember { mutableStateListOf<Particle>() }
    val localScorePopups = remember { mutableStateListOf<ScorePopup>() }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is GameEffect.Particles -> localParticles.addAll(effect.particles)
                is GameEffect.ScorePopup -> {
                    localScorePopups.add(ScorePopup(Random.nextLong(), effect.x, effect.y, effect.score, 1f, effect.color))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            val dt = 0.016f
            if (localParticles.isNotEmpty()) {
                val iterator = localParticles.listIterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    if (p.life <= 0) iterator.remove()
                    else iterator.set(p.copy(x = p.x + p.vx * dt, y = p.y + p.vy * dt, life = p.life - dt * 2f))
                }
            }
        }
    }

    val columns = 5; val rows = 4; val itemGap = 4.dp; val gapPx = with(density) { itemGap.toPx() }
    val moveAnimationSpec = remember { spring<IntOffset>(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium, IntOffset(1, 1)) }

    BoxWithConstraints(modifier.graphicsLayer { clip = false }, Alignment.Center) {
        val cellWidth = constraints.maxWidth / (1f + (columns - 1) * 0.75f)
        val cellHeight = cellWidth * (sqrt(3f) / 2f)
        val itemWidth = (cellWidth - gapPx).coerceAtLeast(0f)
        val itemHeight = (cellHeight - gapPx).coerceAtLeast(0f)
        val totalWidth = (cellWidth * (1f + (columns - 1) * 0.75f))
        val totalHeight = (cellHeight * (rows + 0.5f))

        val currentStep = pendingMerge?.steps?.getOrNull(activeMergeStepIndex)
        val mergingIds = remember(currentStep) { currentStep?.mergingCells?.map { it.id }?.toSet() ?: emptySet() }

        LaunchedEffect(pendingMerge, activeMergeStepIndex) {
            if (currentStep != null) {
                finishedMergeCount.intValue = 0
                if (currentStep.mergingCells.isEmpty()) onMergeAnimationFinished()
                else {
                    snapshotFlow { finishedMergeCount.intValue }.first { it >= currentStep.mergingCells.size }
                    val targetOffset = HexagonGridDefaults.calculateOffset(pendingMerge.targetX, pendingMerge.targetY, cellWidth, cellHeight, gapPx)
                    val center = Offset(targetOffset.x + itemWidth / 2, targetOffset.y + itemHeight / 2)
                    val color = HexagonGridDefaults.getColorForValue(currentStep.resultValue)
                    localParticles.addAll(List(30) {
                        val angle = Random.nextFloat() * 2 * PI.toFloat(); val speed = Random.nextFloat() * 400f + 200f
                        Particle(Random.nextLong(), center.x, center.y, cos(angle) * speed, sin(angle) * speed, color, 1f, Random.nextFloat() * 8f + 4f)
                    })
                    if (activeMergeStepIndex >= pendingMerge.steps.lastIndex) {
                        localScorePopups.add(ScorePopup(Random.nextLong(), center.x, center.y, pendingMergeScore, 1f, color))
                    }
                    onMergeAnimationFinished()
                }
            }
        }

        fun getCellAt(x: Float, y: Float): Pair<Int, Int>? {
            for (col in 0 until columns) {
                val xOffset = col * 0.75f * cellWidth + gapPx / 2; val yOffset = (if (col % 2 == 1) cellHeight / 2 else 0f) + gapPx / 2
                for (row in 0 until rows) {
                    val yStart = row * cellHeight + yOffset
                    if (x >= xOffset && x <= xOffset + cellWidth - gapPx && y >= yStart && y <= yStart + cellHeight - gapPx) return col to row
                }
            }
            return null
        }

        val onAnimationFinishedLambda = remember { { finishedMergeCount.intValue += 1 } }

        Box(
            modifier = Modifier.size(with(density) { totalWidth.toDp() }, with(density) { totalHeight.toDp() }).graphicsLayer { clip = false }
                .pointerInput(cellWidth, cellHeight, gapPx) {
                    coroutineScope {
                        awaitEachGesture {
                            val down = awaitFirstDown(); val initialCell = getCellAt(down.position.x, down.position.y)
                            val job = launch { delay(200.milliseconds); initialCell?.let { (x, y) -> onEmptySpaceTouchDown(x, y) } }
                            try {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main); val change = event.changes.first()
                                    if (change.changedToUp()) {
                                        job.cancel(); val upCell = getCellAt(change.position.x, change.position.y)
                                        if (upCell != null && upCell == initialCell) onEmptySpaceClick(upCell.first, upCell.second)
                                        onEmptySpaceTouchUp(); break
                                    } else if (!change.pressed) break
                                }
                            } finally { job.cancel(); onEmptySpaceTouchUp() }
                        }
                    }
                },
        ) {
            HexagonGrid(
                columns = columns, rows = rows, itemGap = itemGap,
                outlineContent = { col, row ->
                    val hint = remember(mergeHints, col, row) { mergeHints.find { it.x == col && it.y == row } }
                    val onBoardPerk = remember(onBoardPerks, col, row) { onBoardPerks.find { it.x == col && it.y == row } }
                    StaticSlot(hint, onBoardPerk, activePerk, selectedCellId)
                },
            ) {
                previewState.forEach { preview ->
                    key(preview.id) {
                        AnimatedPreviewHexagon(
                            preview = preview, cellWidth = cellWidth, cellHeight = cellHeight, gapPx = gapPx,
                            moveAnimationSpec = moveAnimationSpec, selectedCellId = selectedCellId, activePerk = activePerk,
                            hoveredMergeState = hoveredMergeState,
                            density = density, itemWidth = itemWidth, itemHeight = itemHeight,
                        )
                    }
                }

                gridState.forEach { cell ->
                    key(cell.id) {
                        AnimatedGridHexagon(
                            cell = cell, cellWidth = cellWidth, cellHeight = cellHeight, gapPx = gapPx,
                            moveAnimationSpec = moveAnimationSpec, selectedCellId = selectedCellId, activePerk = activePerk,
                            hoveredMergeState = hoveredMergeState,
                            isMerging = cell.id in mergingIds,
                            density = density, itemWidth = itemWidth, itemHeight = itemHeight,
                            onCellClick = onCellClick,
                            onAnimationFinished = onAnimationFinishedLambda
                        )
                    }
                }

                // Hover Result Previews (Placed last to naturally draw on top if zIndex is equal)
                potentialMerges.forEach { (pos, merge) ->
                    key(pos) {
                        HoverResultHexagon(
                            merge = merge, combo = combo, cellWidth = cellWidth, cellHeight = cellHeight, gapPx = gapPx,
                            density = density, itemWidth = itemWidth, itemHeight = itemHeight,
                            hoveredMergeState = hoveredMergeState
                        )
                    }
                }
            }

            ParticlesLayer(localParticles)
            ScorePopupsLayer(localScorePopups, { id -> localScorePopups.removeAll { it.id == id } })
        }
    }
}

@Composable
private fun StaticSlot(hint: MergeHint?, onBoardPerk: OnBoardPerk?, activePerk: Perk?, selectedCellId: String?) {
    Box(contentAlignment = Alignment.Center) {
        Hexagon(
            modifier = Modifier.fillMaxSize()
                .then(if (activePerk == Perk.MOVE_TILE && selectedCellId != null) Modifier.border(1.dp, Color.White.copy(alpha = 0.4f), FlatTopHexagonShape()) else Modifier),
            isOutline = true,
        )
        if (onBoardPerk != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PerkIcon(
                    perk = onBoardPerk.perk,
                    modifier = Modifier.size(16.dp),
                    color = HexagonGridDefaults.getColorForPerk(onBoardPerk.perk).copy(alpha = 0.6f)
                )
                Text(
                    text = onBoardPerk.lifespan.toString(),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 9.sp
                )
            }
        }
        if (hint != null) Box(Modifier.size((4 + hint.weight * 6).dp).background(Color.White.copy(alpha = 0.2f), CircleShape))
    }
}

@Composable
private fun AnimatedPreviewHexagon(
    preview: PreviewCell, cellWidth: Float, cellHeight: Float, gapPx: Float,
    moveAnimationSpec: androidx.compose.animation.core.AnimationSpec<IntOffset>,
    selectedCellId: String?, activePerk: Perk?,
    hoveredMergeState: StateFlow<MergeTransition?>,
    density: androidx.compose.ui.unit.Density, itemWidth: Float, itemHeight: Float,
) {
    val targetOffset = remember(preview.x, preview.y, cellWidth, cellHeight, gapPx) { HexagonGridDefaults.calculateOffset(preview.x, preview.y, cellWidth, cellHeight, gapPx) }
    val animatedOffset by animateIntOffsetAsState(targetOffset, moveAnimationSpec, label = "ghost_offset")
    val animatedScale by animateFloatAsState(0.8f, spring(Spring.DampingRatioLowBouncy, 1800f), label = "ghost_scale")
    val infiniteTransition = rememberInfiniteTransition(label = "wiggle_ghost")
    val wiggleState = infiniteTransition.animateFloat(-2f, 2f, infiniteRepeatable(tween(500), androidx.compose.animation.core.RepeatMode.Reverse), label = "wiggle")
    val isSelectable = activePerk == Perk.MOVE_TILE || activePerk == Perk.REMOVE_TILE || activePerk == Perk.SWAP_TILES
    val isSelected = selectedCellId == preview.id

    val isOverlappedState = remember { mutableStateOf(false) }
    LaunchedEffect(hoveredMergeState) {
        hoveredMergeState.collect { current ->
            isOverlappedState.value = current != null && current.targetX == preview.x && current.targetY == preview.y
        }
    }

    Hexagon(
        value = preview.value.toString(),
        backgroundColor = HexagonGridDefaults.getColorForValue(preview.value).copy(alpha = 0.3f),
        modifier = Modifier.size(with(density) { itemWidth.toDp() }, with(density) { itemHeight.toDp() })
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(animatedOffset, zIndex = if (isSelected || animatedOffset != targetOffset) 7f else 1f)
                }
            }
            .graphicsLayer {
                scaleX = animatedScale; scaleY = animatedScale
                rotationZ = if (isSelectable && !isSelected) wiggleState.value else 0f
                alpha = if (isOverlappedState.value) 0f else 1f
            }
            .then(if (isSelected) Modifier.border(2.dp, Color.White, FlatTopHexagonShape()) else Modifier),
        isGhost = true,
    )
}

@Composable
private fun AnimatedGridHexagon(
    cell: HexagonCell, cellWidth: Float, cellHeight: Float, gapPx: Float,
    moveAnimationSpec: androidx.compose.animation.core.AnimationSpec<IntOffset>,
    selectedCellId: String?, activePerk: Perk?,
    hoveredMergeState: StateFlow<MergeTransition?>, isMerging: Boolean,
    density: androidx.compose.ui.unit.Density, itemWidth: Float, itemHeight: Float,
    onCellClick: (HexagonCell) -> Unit, onAnimationFinished: () -> Unit
) {
    val targetOffset = remember(cell.x, cell.y, cellWidth, cellHeight, gapPx) { HexagonGridDefaults.calculateOffset(cell.x, cell.y, cellWidth, cellHeight, gapPx) }
    val animatedOffset by animateIntOffsetAsState(targetOffset, moveAnimationSpec, label = "cell_offset", finishedListener = { if (isMerging) onAnimationFinished() })
    var targetScale by remember { mutableStateOf(0f) }; LaunchedEffect(Unit) { targetScale = 1f }
    val scale by animateFloatAsState(targetScale, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "cell_scale")
    val alpha by animateFloatAsState(1f, spring(stiffness = Spring.StiffnessMedium), label = "cell_alpha")
    val infiniteTransition = rememberInfiniteTransition(label = "wiggle_cell")
    val wiggleState = infiniteTransition.animateFloat(-2f, 2f, infiniteRepeatable(tween(500), androidx.compose.animation.core.RepeatMode.Reverse), label = "wiggle")
    val isSelected = selectedCellId == cell.id
    val isSelectable = activePerk == Perk.MOVE_TILE || activePerk == Perk.REMOVE_TILE || activePerk == Perk.SWAP_TILES

    val isHoveredState = remember { mutableStateOf(false) }
    LaunchedEffect(hoveredMergeState) {
        hoveredMergeState.collect { current ->
            isHoveredState.value = current?.steps?.any { step -> step.mergingCells.any { it.id == cell.id } } == true
        }
    }

    val shape = remember { FlatTopHexagonShape() }

    Hexagon(
        value = cell.value.toString(),
        backgroundColor = HexagonGridDefaults.getColorForValue(cell.value),
        modifier = Modifier.size(with(density) { itemWidth.toDp() }, with(density) { itemHeight.toDp() })
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        animatedOffset,
                        zIndex = if (isSelected || isMerging || animatedOffset != targetOffset) 8f else if (isHoveredState.value) 6f else 2f
                    )
                }
            }
            .graphicsLayer {
                this.alpha = alpha; val hovered = isHoveredState.value
                scaleX = scale * (if (isSelected) 1.2f else if (hovered) 1.1f else 1f)
                scaleY = scale * (if (isSelected) 1.2f else if (hovered) 1.1f else 1f)
                rotationZ = if (isSelectable && !isSelected) wiggleState.value else 0f
            }
            .drawWithContent {
                drawContent()
                if (isHoveredState.value) {
                    val outline = shape.createOutline(size, layoutDirection, this)
                    if (outline is Outline.Generic) {
                        drawPath(outline.path, Color.White.copy(alpha = 0.5f), style = Stroke(width = 2.dp.toPx()))
                    }
                }
            }
            .then(if (isSelected) Modifier.border(2.dp, Color.White, FlatTopHexagonShape()) else Modifier),
        onClick = if (activePerk != null) { { onCellClick(cell) } } else null,
    )
}

@Composable
private fun HoverResultHexagon(
    merge: PotentialMerge, combo: Int, cellWidth: Float, cellHeight: Float, gapPx: Float,
    density: androidx.compose.ui.unit.Density, itemWidth: Float, itemHeight: Float,
    hoveredMergeState: StateFlow<MergeTransition?>,
) {
    val targetOffset = remember(merge.targetX, merge.targetY, cellWidth, cellHeight, gapPx) {
        HexagonGridDefaults.calculateOffset(merge.targetX, merge.targetY, cellWidth, cellHeight, gapPx)
    }
    
    val hoverProgress = remember { Animatable(0f) }
    LaunchedEffect(hoveredMergeState) {
        hoveredMergeState.collect { current ->
            val isTarget = current != null && current.targetX == merge.targetX && current.targetY == merge.targetY
            if (isTarget) hoverProgress.animateTo(1f, tween(200))
            else if (hoverProgress.value > 0f) hoverProgress.animateTo(0f, tween(200))
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hover_anim")
    val pulseState = infiniteTransition.animateFloat(0.9f, 1.0f, infiniteRepeatable(tween(800), androidx.compose.animation.core.RepeatMode.Reverse), label = "pulse")
    val floatState = infiniteTransition.animateFloat(-5f, 5f, infiniteRepeatable(tween(1200), androidx.compose.animation.core.RepeatMode.Reverse), label = "float")

    Box(Modifier.fillMaxSize().zIndex(20f)) {
        Hexagon(
            value = merge.finalValue.toString(),
            backgroundColor = HexagonGridDefaults.getColorForValue(merge.finalValue).copy(alpha = 0.5f),
            modifier = Modifier.size(with(density) { itemWidth.toDp() }, with(density) { itemHeight.toDp() })
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.place(targetOffset, zIndex = 21f)
                    }
                }
                .graphicsLayer {
                    val progress = hoverProgress.value
                    alpha = progress * 0.7f
                    val s = 0.8f + 0.2f * progress * pulseState.value
                    scaleX = s; scaleY = s
                }
                .border(2.dp, Color.White.copy(alpha = 0.3f), FlatTopHexagonShape()),
            isGhost = true,
        )
        val center = Offset(targetOffset.x + itemWidth / 2, targetOffset.y + itemHeight / 2)
        Box(
            Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.placeRelative(
                        (center.x - placeable.width / 2).toInt(),
                        (center.y - 140 + floatState.value - placeable.height / 2).toInt(),
                        zIndex = 22f
                    )
                }
            }
            .graphicsLayer {
                val progress = hoverProgress.value
                alpha = progress
                val s = 0.8f + 0.2f * progress * pulseState.value
                scaleX = s; scaleY = s
            }
            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("+${merge.baseScore * (combo + 1)}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@Composable
private fun ScorePopupsLayer(scorePopups: List<ScorePopup>, onPopupFinished: (Long) -> Unit) {
    scorePopups.forEach { popup -> key(popup.id) { ScorePopupItem(popup, onPopupFinished) } }
}

@Composable
private fun ScorePopupItem(popup: ScorePopup, onFinished: (Long) -> Unit) {
    val animProgress = remember { Animatable(1f) }
    LaunchedEffect(Unit) { animProgress.animateTo(0f, tween(800)); onFinished(popup.id) }
    Box(
        Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                placeable.placeRelative(
                    (popup.x - placeable.width / 2).toInt(),
                    (popup.y - (1f - animProgress.value) * 100f - placeable.height / 2).toInt(),
                    zIndex = 100f
                )
            }
        }
        .graphicsLayer {
            alpha = animProgress.value; val s = 1f + (1f - animProgress.value) * 0.3f; scaleX = s; scaleY = s
        }
        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
        .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) { Text("+${popup.score}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp) }
}

@Composable
private fun ParticlesLayer(particles: List<Particle>) {
    Canvas(Modifier.fillMaxSize().zIndex(90f)) {
        particles.forEach { p -> drawCircle(p.color, p.size * p.life, Offset(p.x, p.y), p.life) }
    }
}
