package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.pointlessgames.hexagone.game.GameEffect
import com.pointlessgames.hexagone.game.PotentialMerge
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeHint
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.OnBoardPerk
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PerkPopup
import com.pointlessgames.hexagone.game.model.PreviewCell
import com.pointlessgames.hexagone.game.model.ScorePopup
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.label_redemption
import hexagone.shared.generated.resources.label_tactical_redemption
import hexagone.shared.generated.resources.label_tactician
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun GameGridOverlay(
    gridStateProvider: () -> List<HexagonCell>,
    onBoardPerksProvider: () -> List<OnBoardPerk>,
    mergeHintsProvider: () -> List<MergeHint>,
    previewStateProvider: () -> List<PreviewCell>,
    pendingMergeProvider: () -> MergeTransition?,
    hoveredMergeState: StateFlow<MergeTransition?>,
    potentialMergesProvider: () -> Map<Pair<Int, Int>, PotentialMerge>,
    activePerkProvider: () -> Perk?,
    selectedCellIdProvider: () -> String?,
    activeMergeStepIndexProvider: () -> Int,
    pendingMergeScoreProvider: () -> Int,
    comboProvider: () -> Int,
    effects: SharedFlow<GameEffect>,
    onEmptySpaceClick: (Int, Int) -> Unit,
    onEmptySpaceTouchDown: (Int, Int) -> Unit,
    onEmptySpaceTouchUp: () -> Unit,
    onCellClick: (HexagonCell) -> Unit,
    onMergeAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val spacing = MaterialTheme.spacing
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val finishedMergeCount = remember { mutableIntStateOf(0) }
    val localParticles = remember { mutableStateListOf<Particle>() }
    val localScorePopups = remember { mutableStateListOf<ScorePopup>() }
    val localPerkPopups = remember { mutableStateListOf<PerkPopup>() }

    val ghostAnimations =
        remember { androidx.compose.runtime.mutableStateMapOf<String, GhostAnimationState>() }
    val ghostStripeOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        ghostStripeOffset.animateTo(
            targetValue = 11.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
            ),
        )
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is GameEffect.Particles -> localParticles.addAll(effect.particles)
                else -> {}
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
                    else iterator.set(
                        p.copy(
                            x = p.x + p.vx * dt,
                            y = p.y + p.vy * dt,
                            life = p.life - dt * 2f,
                        ),
                    )
                }
            }
        }
    }

    val columns = 5;
    val rows = 4;
    val itemGap = spacing.extraSmall;
    val gapPx = with(density) { itemGap.toPx() }
    val moveAnimationSpec = remember {
        spring<IntOffset>(
            Spring.DampingRatioNoBouncy,
            Spring.StiffnessMedium,
            IntOffset(1, 1),
        )
    }

    BoxWithConstraints(modifier.graphicsLayer { clip = false }, Alignment.Center) {
        val cellWidth = constraints.maxWidth / (1f + (columns - 1) * 0.75f)
        val cellHeight = cellWidth * (sqrt(3f) / 2f)
        val itemWidth = (cellWidth - gapPx).coerceAtLeast(0f)
        val itemHeight = (cellHeight - gapPx).coerceAtLeast(0f)
        val totalWidth = (cellWidth * (1f + (columns - 1) * 0.75f))
        val totalHeight = (cellHeight * (rows + 0.5f))

        LaunchedEffect(effects, cellWidth, cellHeight, gapPx, itemWidth, itemHeight) {
            effects.collect { effect ->
                when (effect) {
                    is GameEffect.ScorePopup -> {
                        val offset = HexagonGridDefaults.calculateOffset(
                            effect.gridX,
                            effect.gridY,
                            cellWidth,
                            cellHeight,
                            gapPx,
                        )
                        val centerX = offset.x + itemWidth / 2
                        val centerY = offset.y + itemHeight / 2
                        localScorePopups.add(
                            ScorePopup(
                                Random.nextLong(),
                                centerX,
                                centerY,
                                effect.gridX,
                                effect.gridY,
                                effect.score,
                                1f,
                                effect.color,
                                effect.labelRes,
                            ),
                        )
                    }

                    is GameEffect.PerkPopup -> {
                        val offset = HexagonGridDefaults.calculateOffset(
                            effect.gridX,
                            effect.gridY,
                            cellWidth,
                            cellHeight,
                            gapPx,
                        )
                        val centerX = offset.x + itemWidth / 2
                        val centerY = offset.y + itemHeight / 2
                        localPerkPopups.add(
                            PerkPopup(
                                Random.nextLong(),
                                centerX,
                                centerY,
                                effect.gridX,
                                effect.gridY,
                                effect.perk,
                                1f,
                            ),
                        )
                    }

                    else -> {}
                }
            }
        }

        LaunchedEffect(cellWidth, cellHeight, gapPx) {
            snapshotFlow { previewStateProvider() }.collect { previews ->
                val currentIds = previews.map { it.id }.toSet()
                ghostAnimations.keys.retainAll { it in currentIds }
                previews.forEach { preview ->
                    val targetOffset = HexagonGridDefaults.calculateOffset(
                        preview.x,
                        preview.y,
                        cellWidth,
                        cellHeight,
                        gapPx,
                    )
                    val state = ghostAnimations.getOrPut(preview.id) {
                        GhostAnimationState(preview.id, targetOffset).apply {
                            launch {
                                scale.animateTo(
                                    0.8f,
                                    spring(Spring.DampingRatioLowBouncy, 1800f),
                                )
                            }
                        }
                    }
                    if (state.offset.targetValue != targetOffset) {
                        launch { state.offset.animateTo(targetOffset, moveAnimationSpec) }
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            hoveredMergeState.collect { current ->
                ghostAnimations.forEach { (id, state) ->
                    val preview = previewStateProvider().find { it.id == id }
                    val isOverlapped =
                        current != null && preview != null && current.targetX == preview.x && current.targetY == preview.y
                    launch { state.alpha.animateTo(if (isOverlapped) 0f else 1f, tween(200)) }
                }
            }
        }

        LaunchedEffect(Unit) {
            snapshotFlow { pendingMergeProvider() to activeMergeStepIndexProvider() }.collect { (pendingMerge, activeMergeStepIndex) ->
                val currentStep = pendingMerge?.steps?.getOrNull(activeMergeStepIndex)
                if (currentStep != null) {
                    finishedMergeCount.intValue = 0
                    if (currentStep.mergingCells.isEmpty()) onMergeAnimationFinished()
                    else {
                        snapshotFlow { finishedMergeCount.intValue }.first { it >= currentStep.mergingCells.size }
                        val targetOffset = HexagonGridDefaults.calculateOffset(
                            pendingMerge.targetX,
                            pendingMerge.targetY,
                            cellWidth,
                            cellHeight,
                            gapPx,
                        )
                        val center =
                            Offset(targetOffset.x + itemWidth / 2, targetOffset.y + itemHeight / 2)
                        val color = HexagonGridDefaults.getColorForValue(currentStep.resultValue, colorScheme)
                        if (activeMergeStepIndex >= pendingMerge.steps.lastIndex) {
                            val onBoardPerks = onBoardPerksProvider()
                            val collected =
                                onBoardPerks.find { it.x == pendingMerge.targetX && it.y == pendingMerge.targetY }
                            if (collected != null) {
                                val perkColor = HexagonGridDefaults.getColorForPerk(collected.perk, colorScheme)
                                localParticles.addAll(
                                    List(20) {
                                        val angle = Random.nextFloat() * 2 * PI.toFloat();
                                        val speed = Random.nextFloat() * 300f + 100f
                                        Particle(
                                            Random.nextLong(),
                                            center.x,
                                            center.y,
                                            cos(angle) * speed,
                                            sin(angle) * speed,
                                            perkColor,
                                            1f,
                                            Random.nextFloat() * 6f + 2f,
                                        )
                                    },
                                )
                            }
                        }
                        onMergeAnimationFinished()
                    }
                }
            }
        }

        fun getCellAt(x: Float, y: Float): Pair<Int, Int>? {
            for (col in 0 until columns) {
                val xOffset = col * 0.75f * cellWidth + gapPx / 2;
                val yOffset = (if (col % 2 == 1) cellHeight / 2 else 0f) + gapPx / 2
                for (row in 0 until rows) {
                    val yStart = row * cellHeight + yOffset
                    if (x >= xOffset && x <= xOffset + cellWidth - gapPx && y >= yStart && y <= yStart + cellHeight - gapPx) return col to row
                }
            }
            return null
        }

        val onAnimationFinishedLambda = remember { { finishedMergeCount.intValue += 1 } }
        val infiniteTransition = rememberInfiniteTransition(label = "wiggle_ghost")
        val wiggleState = infiniteTransition.animateFloat(
            -2f,
            2f,
            infiniteRepeatable(tween(500), androidx.compose.animation.core.RepeatMode.Reverse),
            label = "wiggle",
        )
        val pulseState = infiniteTransition.animateFloat(
            0.9f,
            1.0f,
            infiniteRepeatable(tween(800), androidx.compose.animation.core.RepeatMode.Reverse),
            label = "pulse",
        )
        val floatState = infiniteTransition.animateFloat(
            -5f,
            5f,
            infiniteRepeatable(tween(1200), androidx.compose.animation.core.RepeatMode.Reverse),
            label = "float",
        )

        val activeHoverMerge = remember { mutableStateOf<MergeTransition?>(null) }
        val hoverProgress = remember { Animatable(0f) }
        LaunchedEffect(hoveredMergeState) {
            hoveredMergeState.collect {
                activeHoverMerge.value = it
                if (it != null) hoverProgress.animateTo(1f, tween(200))
                else if (hoverProgress.value > 0f) hoverProgress.animateTo(0f, tween(200))
            }
        }

        Box(
            modifier = Modifier.size(
                with(density) { totalWidth.toDp() },
                with(density) { totalHeight.toDp() },
            ).graphicsLayer { clip = false }
                .pointerInput(cellWidth, cellHeight, gapPx) {
                    coroutineScope {
                        awaitEachGesture {
                            val down = awaitFirstDown();
                            val initialCell = getCellAt(down.position.x, down.position.y)
                            val job = launch {
                                delay(200.milliseconds); initialCell?.let { (x, y) ->
                                onEmptySpaceTouchDown(
                                    x,
                                    y,
                                )
                            }
                            }
                            try {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main);
                                    val change = event.changes.first()
                                    if (change.changedToUp()) {
                                        job.cancel();
                                        val upCell = getCellAt(change.position.x, change.position.y)
                                        if (upCell != null && upCell == initialCell) onEmptySpaceClick(
                                            upCell.first,
                                            upCell.second,
                                        )
                                        onEmptySpaceTouchUp(); break
                                    } else if (!change.pressed) break
                                }
                            } finally {
                                job.cancel(); onEmptySpaceTouchUp()
                            }
                        }
                    }
                },
        ) {
            HexagonGrid(
                columns = columns, rows = rows, itemGap = itemGap,
                modifier = Modifier
                    .drawBehind {
                        val hints = mergeHintsProvider()
                        hints.forEach { hint ->
                            val offset = HexagonGridDefaults.calculateOffset(
                                hint.x,
                                hint.y,
                                cellWidth,
                                cellHeight,
                                gapPx,
                            )
                            val center = Offset(offset.x + itemWidth / 2, offset.y + itemHeight / 2)
                            drawCircle(
                                color = Color.White.copy(alpha = 0.2f),
                                radius = (spacing.tiny + spacing.extraSmall * hint.weight).toPx(),
                                center = center,
                            )
                        }

                        val perks = onBoardPerksProvider()
                        perks.forEach { op ->
                            val offset = HexagonGridDefaults.calculateOffset(
                                op.x,
                                op.y,
                                cellWidth,
                                cellHeight,
                                gapPx,
                            )
                            val mysterySize = Size(spacing.semiMedium.toPx(), spacing.semiMedium.toPx())
                            val mysteryOffset = Offset(
                                offset.x + (itemWidth - mysterySize.width) / 2,
                                offset.y + itemHeight - mysterySize.height - spacing.medium.toPx(),
                            )
                            withTransform(
                                {
                                    translate(mysteryOffset.x, mysteryOffset.y)
                                },
                            ) {
                                HexagonGridDefaults.drawPerkIcon(
                                    this,
                                    null, // Pass null to hide the specific perk
                                    mysterySize,
                                    Color.White.copy(alpha = 0.4f),
                                    spacing.extraTiny.toPx(),
                                )
                            }

                            val textLayoutResult = textMeasurer.measure(
                                text = op.lifespan.toString(),
                                style = TextStyle(
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                ),
                            )
                            drawText(
                                textLayoutResult,
                                topLeft = Offset(
                                    offset.x + (itemWidth - textLayoutResult.size.width) / 2,
                                    offset.y + itemHeight - mysterySize.height - spacing.medium.toPx() - textLayoutResult.size.height - spacing.tiny.toPx(),
                                ),
                            )
                        }
                    }
                    .drawWithContent {
                        val activePerk = activePerkProvider()
                        val isSelectable =
                            activePerk == Perk.MOVE_TILE || activePerk == Perk.REMOVE_TILE || activePerk == Perk.SWAP_TILES
                        val selectedCellId = selectedCellIdProvider()
                        val previews = previewStateProvider()
                        previews.forEach { preview ->
                            val animState = ghostAnimations[preview.id] ?: return@forEach
                            if (animState.offset.value == animState.offset.targetValue && selectedCellId != preview.id) {
                                drawGhost(
                                    drawScope = this,
                                    preview = preview,
                                    animState = animState,
                                    itemWidth = itemWidth,
                                    itemHeight = itemHeight,
                                    wiggleValue = if (isSelectable) wiggleState.value else 0f,
                                    stripeOffset = ghostStripeOffset.value,
                                    textMeasurer = textMeasurer,
                                    colorScheme = colorScheme,
                                    spacing = spacing,
                                )
                            }
                        }

                        drawContent()

                        val currentMerge = activeHoverMerge.value
                        val progress = hoverProgress.value
                        if (progress > 0f && currentMerge != null) {
                            val potentialMerge =
                                potentialMergesProvider()[currentMerge.targetX to currentMerge.targetY]
                            if (potentialMerge != null) {
                                drawHoverResult(
                                    drawScope = this,
                                    merge = potentialMerge,
                                    combo = comboProvider(),
                                    cellWidth = cellWidth,
                                    cellHeight = cellHeight,
                                    gapPx = gapPx,
                                    itemWidth = itemWidth,
                                    itemHeight = itemHeight,
                                    progress = progress,
                                    pulseValue = pulseState.value,
                                    floatValue = floatState.value,
                                    stripeOffset = ghostStripeOffset.value,
                                    textMeasurer = textMeasurer,
                                    colorScheme = colorScheme,
                                    spacing = spacing,
                                )
                            }
                        }

                        previews.forEach { preview ->
                            val animState = ghostAnimations[preview.id] ?: return@forEach
                            if (animState.offset.value != animState.offset.targetValue || selectedCellId == preview.id) {
                                drawGhost(
                                    drawScope = this,
                                    preview = preview,
                                    animState = animState,
                                    itemWidth = itemWidth,
                                    itemHeight = itemHeight,
                                    wiggleValue = if (isSelectable) wiggleState.value else 0f,
                                    stripeOffset = ghostStripeOffset.value,
                                    textMeasurer = textMeasurer,
                                    colorScheme = colorScheme,
                                    spacing = spacing,
                                )
                            }
                        }
                    },
                outlineContent = { _, _ ->
                    StaticSlot(activePerkProvider, { selectedCellIdProvider() != null }, spacing)
                },
            ) {
                Box {
                    val gridState = gridStateProvider()
                    gridState.forEach { cell ->
                        key(cell.id) {
                            AnimatedGridHexagon(
                                cell = cell,
                                cellWidth = cellWidth,
                                cellHeight = cellHeight,
                                gapPx = gapPx,
                                moveAnimationSpec = moveAnimationSpec,
                                selectedCellIdProvider = selectedCellIdProvider,
                                activePerkProvider = activePerkProvider,
                                pendingMergeProvider = pendingMergeProvider,
                                activeMergeStepIndexProvider = activeMergeStepIndexProvider,
                                hoveredMergeState = hoveredMergeState,
                                density = density,
                                itemWidth = itemWidth,
                                itemHeight = itemHeight,
                                onCellClick = onCellClick,
                                onAnimationFinished = onAnimationFinishedLambda,
                                spacing = spacing,
                            )
                        }
                    }
                }
            }

            ParticlesLayer(localParticles)
            PopupsLayer(
                scorePopups = localScorePopups,
                perkPopups = localPerkPopups,
                onScoreFinished = { id -> localScorePopups.removeAll { it.id == id } },
                onPerkFinished = { id -> localPerkPopups.removeAll { it.id == id } },
                containerWidth = totalWidth,
                spacing = spacing,
            )
        }
    }
}

@Composable
private fun PopupsLayer(
    scorePopups: List<ScorePopup>,
    perkPopups: List<PerkPopup>,
    onScoreFinished: (Long) -> Unit,
    onPerkFinished: (Long) -> Unit,
    containerWidth: Float,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val density = LocalDensity.current
    val popupSpacing = with(density) { spacing.massive.toPx() }

    // Separate tactical (slow/centered higher) and standard (fast/moving lower) popups.
    // We only group tactical ones for horizontal stacking to avoid "jumps" when fast
    // score popups disappear while a slow perk popup is still visible.
    val tacticalPopups = perkPopups + scorePopups.filter { it.labelRes != null }
    val standardPopups = scorePopups.filter { it.labelRes == null }

    val tacticalGroups = tacticalPopups.groupBy {
        it.gridX to it.gridY
    }

    tacticalGroups.forEach { (_, groupItems) ->
        // Sort items by ID for stable indexing within the group
        val sortedItems = groupItems.sortedBy { it.id }
        val total = sortedItems.size
        
        sortedItems.forEachIndexed { index, item ->
            key(item.id) {
                val horizontalOffset = if (total > 1) {
                    val totalWidth = (total - 1) * popupSpacing
                    -totalWidth / 2 + index * popupSpacing
                } else 0f

                val animOffset by animateFloatAsState(
                    targetValue = horizontalOffset,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "popup_slide"
                )

                when (item) {
                    is ScorePopup -> {
                        ScorePopupItem(item, onScoreFinished, animOffset, containerWidth, spacing)
                    }
                    is PerkPopup -> {
                        PerkPopItem(item, onPerkFinished, animOffset, containerWidth, spacing)
                    }
                }
            }
        }
    }

    // Standard score popups are fast-paced and move vertically, so they don't need
    // to participate in horizontal stacking with slow-paced tactical/perk popups.
    standardPopups.forEach { item ->
        key(item.id) {
            ScorePopupItem(item, onScoreFinished, 0f, containerWidth, spacing)
        }
    }
}

private fun drawGhost(
    drawScope: DrawScope,
    preview: PreviewCell,
    animState: GhostAnimationState,
    itemWidth: Float,
    itemHeight: Float,
    wiggleValue: Float,
    stripeOffset: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    colorScheme: androidx.compose.material3.ColorScheme,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val offset = animState.offset.value
    val scale = animState.scale.value
    val alpha = animState.alpha.value

    drawScope.withTransform(
        {
            translate(offset.x.toFloat(), offset.y.toFloat())
            scale(scale, scale, Offset(itemWidth / 2, itemHeight / 2))
            rotate(wiggleValue, Offset(itemWidth / 2, itemHeight / 2))
        },
    ) {
        val backgroundColor = HexagonGridDefaults.getColorForValue(preview.value, colorScheme).copy(alpha = 0.3f)
        HexagonGridDefaults.drawHexagonPath(
            this,
            Size(itemWidth, itemHeight),
            backgroundColor,
            alpha = alpha,
        )

        if (preview.isTactical) {
            HexagonGridDefaults.drawHexagonPath(
                this,
                Size(itemWidth, itemHeight),
                colorScheme.secondary.copy(alpha = alpha),
                style = Stroke(width = spacing.tiny.toPx())
            )
        }

        clipPath(HexagonGridDefaults.getHexagonPath(Size(itemWidth, itemHeight))) {
            HexagonGridDefaults.drawGhostStripes(
                this,
                Size(itemWidth, itemHeight),
                stripeOffset * drawScope.density,
                spacing,
                alpha = 0.15f * alpha,
            )
        }

        val textLayoutResult = textMeasurer.measure(
            text = preview.value.toString(),
            style = TextStyle(
                color = Color.White.copy(alpha = alpha),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            ),
        )
        drawText(
            textLayoutResult,
            topLeft = Offset(
                (itemWidth - textLayoutResult.size.width) / 2,
                (itemHeight - textLayoutResult.size.height) / 2,
            ),
        )
    }
}

private fun drawHoverResult(
    drawScope: DrawScope,
    merge: PotentialMerge,
    combo: Int,
    cellWidth: Float,
    cellHeight: Float,
    gapPx: Float,
    itemWidth: Float,
    itemHeight: Float,
    progress: Float,
    pulseValue: Float,
    floatValue: Float,
    stripeOffset: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    colorScheme: androidx.compose.material3.ColorScheme,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val targetOffset = HexagonGridDefaults.calculateOffset(
        merge.targetX,
        merge.targetY,
        cellWidth,
        cellHeight,
        gapPx,
    )
    val center = Offset(targetOffset.x + itemWidth / 2, targetOffset.y + itemHeight / 2)

    drawScope.withTransform(
        {
            translate(targetOffset.x.toFloat(), targetOffset.y.toFloat())
            val s = 0.8f + 0.2f * progress * pulseValue
            scale(s, s, Offset(itemWidth / 2, itemHeight / 2))
        },
    ) {
        val backgroundColor = HexagonGridDefaults.getColorForValue(merge.finalValue, colorScheme)
            .copy(alpha = 0.5f * progress * 0.7f)
        HexagonGridDefaults.drawHexagonPath(this, Size(itemWidth, itemHeight), backgroundColor)

        clipPath(HexagonGridDefaults.getHexagonPath(Size(itemWidth, itemHeight))) {
            HexagonGridDefaults.drawGhostStripes(
                this,
                Size(itemWidth, itemHeight),
                stripeOffset * drawScope.density,
                spacing,
                alpha = 0.15f * progress * 0.7f,
            )
        }

        HexagonGridDefaults.drawHexagonPath(
            this,
            Size(itemWidth, itemHeight),
            Color.White.copy(alpha = 0.3f * progress),
            style = Stroke(width = spacing.tiny.toPx()),
        )

        val textLayoutResult = textMeasurer.measure(
            text = merge.finalValue.toString(),
            style = TextStyle(
                color = Color.White.copy(alpha = progress),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            ),
        )
        drawText(
            textLayoutResult,
            topLeft = Offset(
                (itemWidth - textLayoutResult.size.width) / 2,
                (itemHeight - textLayoutResult.size.height) / 2,
            ),
        )
    }

    // Draw score popup
    val scoreText = "+${merge.baseScore * (combo + 1)}"
    val scoreTextLayout = textMeasurer.measure(
        text = scoreText,
        style = TextStyle(color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp),
    )

    val popupWidth = scoreTextLayout.size.width + with(drawScope) { spacing.semiLarge.toPx() }
    val popupHeight = scoreTextLayout.size.height + with(drawScope) { spacing.small.toPx() }
    val popupTopLeft = Offset(
        center.x - popupWidth / 2f,
        center.y - 140f + floatValue - popupHeight / 2f,
    )

    drawScope.withTransform(
        {
            val s = 0.8f + 0.2f * progress * pulseValue
            scale(s, s, Offset(popupTopLeft.x + popupWidth / 2f, popupTopLeft.y + popupHeight / 2f))
        },
    ) {
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.7f * progress),
            topLeft = popupTopLeft,
            size = Size(popupWidth, popupHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                popupHeight / 2f,
                popupHeight / 2f,
            ),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.4f * progress),
            topLeft = popupTopLeft,
            size = Size(popupWidth, popupHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                popupHeight / 2f,
                popupHeight / 2f,
            ),
            style = Stroke(width = spacing.extraTiny.toPx()),
        )
        drawText(
            scoreTextLayout,
            topLeft = Offset(
                popupTopLeft.x + (popupWidth - scoreTextLayout.size.width) / 2f,
                popupTopLeft.y + (popupHeight - scoreTextLayout.size.height) / 2f,
            ),
            alpha = progress,
        )
    }
}

private class GhostAnimationState(
    val id: String,
    initialOffset: IntOffset,
) {
    val offset = Animatable(initialOffset, IntOffset.VectorConverter)
    val scale = Animatable(0f)
    val alpha = Animatable(1f)
}

@Composable
private fun StaticSlot(activePerkProvider: () -> Perk?, isAnySelectedProvider: () -> Boolean, spacing: com.pointlessgames.hexagone.ui.theme.Spacing) {
    val activePerk = activePerkProvider()
    val isAnySelected = isAnySelectedProvider()
    Hexagon(
        modifier = Modifier.fillMaxSize()
            .then(
                if (activePerk == Perk.MOVE_TILE && isAnySelected) Modifier.border(
                    spacing.extraTiny,
                    Color.White.copy(alpha = 0.4f),
                    FlatTopHexagonShape(),
                ) else Modifier,
            ),
        isOutline = true,
    )
}

@Composable
private fun AnimatedGridHexagon(
    cell: HexagonCell, cellWidth: Float, cellHeight: Float, gapPx: Float,
    moveAnimationSpec: androidx.compose.animation.core.AnimationSpec<IntOffset>,
    selectedCellIdProvider: () -> String?, activePerkProvider: () -> Perk?,
    pendingMergeProvider: () -> MergeTransition?, activeMergeStepIndexProvider: () -> Int,
    hoveredMergeState: StateFlow<MergeTransition?>,
    density: androidx.compose.ui.unit.Density, itemWidth: Float, itemHeight: Float,
    onCellClick: (HexagonCell) -> Unit, onAnimationFinished: () -> Unit,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val isMergingProvider = remember(cell.id) {
        {
            val pendingMerge = pendingMergeProvider()
            val activeMergeStepIndex = activeMergeStepIndexProvider()
            val currentStep = pendingMerge?.steps?.getOrNull(activeMergeStepIndex)
            currentStep?.mergingCells?.any { it.id == cell.id } == true
        }
    }
    val targetOffset = remember(
        cell.x,
        cell.y,
        cellWidth,
        cellHeight,
        gapPx,
    ) { HexagonGridDefaults.calculateOffset(cell.x, cell.y, cellWidth, cellHeight, gapPx) }
    val animatedOffset by animateIntOffsetAsState(
        targetOffset,
        moveAnimationSpec,
        label = "cell_offset",
        finishedListener = { if (isMergingProvider()) onAnimationFinished() },
    )
    var targetScale by remember { mutableStateOf(0f) }; LaunchedEffect(Unit) { targetScale = 1f }
    val scale by animateFloatAsState(
        targetScale,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "cell_scale",
    )
    val alpha by animateFloatAsState(
        1f,
        spring(stiffness = Spring.StiffnessMedium),
        label = "cell_alpha",
    )
    val infiniteTransition = rememberInfiniteTransition(label = "wiggle_cell")
    val wiggleState = infiniteTransition.animateFloat(
        -2f,
        2f,
        infiniteRepeatable(tween(500), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "wiggle",
    )

    val isHoveredState = remember { mutableStateOf(false) }
    LaunchedEffect(hoveredMergeState) {
        hoveredMergeState.collect { current ->
            isHoveredState.value =
                current?.steps?.any { step -> step.mergingCells.any { it.id == cell.id } } == true
        }
    }

    val shape = remember { FlatTopHexagonShape() }
    val selectedCellId = selectedCellIdProvider()
    val activePerk = activePerkProvider()
    val isSelected = selectedCellId == cell.id

    Hexagon(
        value = cell.value.toString(),
        backgroundColor = HexagonGridDefaults.getColorForValue(cell.value, MaterialTheme.colorScheme),
        isTactical = cell.isTactical,
        modifier = Modifier.size(
            with(density) { itemWidth.toDp() },
            with(density) { itemHeight.toDp() },
        )
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    val selectedCellId = selectedCellIdProvider()
                    val pendingMerge = pendingMergeProvider()
                    val activeMergeStepIndex = activeMergeStepIndexProvider()
                    val currentStep = pendingMerge?.steps?.getOrNull(activeMergeStepIndex)
                    val isMergingLocal = currentStep?.mergingCells?.any { it.id == cell.id } == true

                    placeable.place(
                        animatedOffset,
                        zIndex = if (selectedCellId == cell.id || isMergingLocal || animatedOffset != targetOffset) 8f else if (isHoveredState.value) 6f else 2f,
                    )
                }
            }
            .graphicsLayer {
                this.alpha = alpha;
                val hovered = isHoveredState.value
                val selectedCellId = selectedCellIdProvider()
                val isSelectedLocal = selectedCellId == cell.id
                val activePerk = activePerkProvider()
                val isSelectableLocal =
                    activePerk == Perk.MOVE_TILE || activePerk == Perk.REMOVE_TILE || activePerk == Perk.SWAP_TILES

                scaleX = scale * (if (isSelectedLocal) 1.2f else if (hovered) 1.1f else 1f)
                scaleY = scale * (if (isSelectedLocal) 1.2f else if (hovered) 1.1f else 1f)
                rotationZ = if (isSelectableLocal && !isSelectedLocal) wiggleState.value else 0f
            }
            .drawWithContent {
                drawContent()
                if (isHoveredState.value) {
                    val outline = shape.createOutline(size, layoutDirection, this)
                    if (outline is Outline.Generic) {
                        drawPath(
                            outline.path,
                            Color.White.copy(alpha = 0.5f),
                            style = Stroke(width = spacing.tiny.toPx()),
                        )
                    }
                }
            }
            .then(
                if (isSelected) Modifier.border(
                    spacing.tiny,
                    Color.White,
                    FlatTopHexagonShape(),
                ) else Modifier,
            ),
        onClick = if (activePerk != null) {
            { onCellClick(cell) }
        } else null,
    )
}




@Composable
private fun ScorePopupItem(
    popup: ScorePopup,
    onFinished: (Long) -> Unit,
    horizontalOffset: Float = 0f,
    containerWidth: Float = Float.MAX_VALUE,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val isSpecial = popup.labelRes != null

    if (isSpecial) {
        SpecialScorePopup(popup, onFinished, horizontalOffset, containerWidth, spacing)
    } else {
        StandardScorePopup(popup, onFinished, horizontalOffset, containerWidth, spacing)
    }
}

@Composable
private fun StandardScorePopup(
    popup: ScorePopup,
    onFinished: (Long) -> Unit,
    horizontalOffset: Float = 0f,
    containerWidth: Float = Float.MAX_VALUE,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val animProgress = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(0f, tween(800))
        onFinished(popup.id)
    }
    Box(
        Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                val xPos = (popup.x + horizontalOffset - placeable.width / 2).coerceIn(0f, containerWidth - placeable.width)
                placeable.placeRelative(
                    xPos.toInt(),
                    (popup.y - (1f - animProgress.value) * 100f - placeable.height / 2).toInt(),
                    zIndex = 100f,
                )
            }
        }
            .graphicsLayer {
                alpha = animProgress.value
                val s = 1f + (1f - animProgress.value) * 0.3f
                scaleX = s
                scaleY = s
            }
            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            .border(spacing.extraTiny, Color.White.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = spacing.medium, vertical = spacing.extraSmall),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "+${popup.score}",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 24.sp,
        )
    }
}

@Composable
private fun SpecialScorePopup(
    popup: ScorePopup,
    onFinished: (Long) -> Unit,
    horizontalOffset: Float = 0f,
    containerWidth: Float = Float.MAX_VALUE,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "special_score_hover")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "hover",
    )

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
        delay(1200.milliseconds)
        launch { alpha.animateTo(0f, tween(400)) }
        scale.animateTo(0.8f, tween(400))
        onFinished(popup.id)
    }

    Box(
        Modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                val xPos = (popup.x + horizontalOffset - placeable.width / 2).coerceIn(0f, containerWidth - placeable.width)
                placeable.placeRelative(
                    xPos.toInt(),
                    (popup.y - 120f + hoverOffset - placeable.height / 2).toInt(),
                    zIndex = 110f,
                )
            }
        }
            .graphicsLayer {
                this.alpha = alpha.value
                scaleX = scale.value
                scaleY = scale.value
            }
            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
            .border(
                spacing.tiny,
                popup.color.copy(alpha = 0.8f),
                CircleShape
            )
            .padding(horizontal = spacing.large, vertical = spacing.small),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (popup.labelRes != null) {
                val labelColor = when (popup.labelRes) {
                    Res.string.label_tactician -> MaterialTheme.colorScheme.secondary
                    Res.string.label_tactical_redemption, Res.string.label_redemption -> MaterialTheme.colorScheme.tertiary
                    else -> popup.color
                }
                Text(
                    stringResource(popup.labelRes),
                    color = labelColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp
                )
            }
            Text(
                "+${popup.score}",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
            )
        }
    }
}



@Composable
private fun PerkPopItem(
    popup: PerkPopup,
    onFinished: (Long) -> Unit,
    horizontalOffset: Float = 0f,
    containerWidth: Float = Float.MAX_VALUE,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    val infiniteTransition = rememberInfiniteTransition(label = "perk_hover")
    val hoverOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "hover",
    )

    LaunchedEffect(Unit) {
        // 1. Lightly Bouncy Scale Entrance
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
        // 2. Stay for a while
        delay(1200.milliseconds)
        // 3. Fade and shrink away
        launch { alpha.animateTo(0f, tween(300)) }
        scale.animateTo(0.5f, tween(300))
        onFinished(popup.id)
    }

    Box(
        Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    val xPos = (popup.x + horizontalOffset - placeable.width / 2).coerceIn(0f, containerWidth - placeable.width)
                    placeable.placeRelative(
                        xPos.toInt(),
                        (popup.y - 120f + hoverOffset - placeable.height / 2).toInt(),
                        zIndex = 105f,
                    )
                }
            }
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            .border(spacing.extraTiny, Color.White.copy(alpha = 0.4f), CircleShape)
            .padding(spacing.medium),
        contentAlignment = Alignment.Center,
    ) {
        PerkIcon(perk = popup.perk, modifier = Modifier.size(spacing.semiLarge), color = Color.White)
    }
}

@Composable
private fun ParticlesLayer(particles: List<Particle>) {
    Canvas(Modifier.fillMaxSize().zIndex(90f)) {
        particles.forEach { p -> drawCircle(p.color, p.size * p.life, Offset(p.x, p.y), p.life) }
    }
}
