package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.GameEffect
import com.pointlessgames.hexagone.game.model.GhostAnimationState
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeHint
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.OnBoardPerk
import com.pointlessgames.hexagone.game.model.Particle
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.PerkPopup
import com.pointlessgames.hexagone.game.model.PotentialMerge
import com.pointlessgames.hexagone.game.model.PreviewCell
import com.pointlessgames.hexagone.game.model.ScorePopup
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.ic_roll
import hexagone.shared.generated.resources.ic_star
import hexagone.shared.generated.resources.score_popup
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun GameGridOverlay(
    modifier: Modifier = Modifier,
    gridState: List<HexagonCell>,
    onBoardPerksProvider: () -> List<OnBoardPerk>,
    mergeHintsProvider: () -> List<MergeHint>,
    previewState: List<PreviewCell>,
    pendingMergeProvider: () -> MergeTransition?,
    hoveredMergeState: StateFlow<MergeTransition?>,
    potentialMergesProvider: () -> Map<Pair<Int, Int>, PotentialMerge>,
    activePerkProvider: () -> Perk?,
    selectedCellIdProvider: () -> String?,
    activeMergeStepIndexProvider: () -> Int,
    comboProvider: () -> Int,
    effects: SharedFlow<GameEffect>,
    onEmptySpaceClick: (Int, Int) -> Unit,
    onEmptySpaceTouchDown: (Int, Int) -> Unit,
    onEmptySpaceTouchUp: () -> Unit,
    onCellTouchDown: (HexagonCell) -> Unit,
    onCellTouchUp: () -> Unit,
    onCellClick: (HexagonCell) -> Unit,
    onMergeAnimationFinished: () -> Unit,
) {
    val currentGridState by androidx.compose.runtime.rememberUpdatedState(gridState)
    val currentPreviewState by androidx.compose.runtime.rememberUpdatedState(previewState)
    val colorScheme = MaterialTheme.colorScheme
    val spacing = MaterialTheme.spacing
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val starPainter = painterResource(Res.drawable.ic_star)
    val finishedMergeCount = remember { mutableIntStateOf(0) }
    val localParticles = remember { mutableStateListOf<Particle>() }
    val localScorePopups = remember { mutableStateListOf<ScorePopup>() }
    val localPerkPopups = remember { mutableStateListOf<PerkPopup>() }
    var popupIdCounter by remember { mutableStateOf(0L) }

    val hoverProgress = remember { Animatable(0f) }
    val activeHoverMerge = remember { mutableStateOf<MergeTransition?>(null) }
    val scoreText = activeHoverMerge.value?.let { merge ->
        val potentialMerge = potentialMergesProvider()[merge.targetX to merge.targetY]
        val isPlacementPerk = merge.resultId.contains("move") ||
                merge.resultId.contains("swap") ||
                merge.resultId.contains("duplicate") ||
                merge.resultId.contains("increment") ||
                merge.resultId.contains("highlight")
        val displayScore = if (isPlacementPerk) {
            merge.baseScore
        } else {
            potentialMerge?.baseScore ?: merge.baseScore
        }
        if (displayScore > 0) stringResource(Res.string.score_popup, displayScore) else ""
    } ?: ""

    LaunchedEffect(hoveredMergeState) {
        hoveredMergeState.collect {
            activeHoverMerge.value = it
            if (it != null) hoverProgress.animateTo(1f, tween(200))
            else if (hoverProgress.value > 0f) hoverProgress.animateTo(0f, tween(200))
        }
    }

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

    LaunchedEffect(Unit) {
        var lastTimeMillis = withFrameMillis { it }
        while (true) {
            val currentTimeMillis = withFrameMillis { it }
            val dt = ((currentTimeMillis - lastTimeMillis).coerceAtLeast(0L) / 1000f).coerceAtMost(0.1f)
            lastTimeMillis = currentTimeMillis

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

    val columns = 5
    val rows = 4
    val itemGap = spacing.extraSmall
    val gapPx = with(density) { itemGap.toPx() }
    val moveAnimationSpec = remember {
        spring<IntOffset>(
            Spring.DampingRatioNoBouncy,
            Spring.StiffnessMedium,
            IntOffset(1, 1),
        )
    }

    val mysteryPainter = painterResource(Res.drawable.ic_roll)

    BoxWithConstraints(modifier.graphicsLayer { clip = false }, Alignment.Center) {
        // Calculate width-based constraints
        val cellWidthFromWidth = constraints.maxWidth / (1f + (columns - 1) * 0.75f)
        
        // Calculate height-based constraints
        val cellHeightFromHeight = constraints.maxHeight / (rows + 0.5f)
        val cellWidthFromHeight = cellHeightFromHeight / (sqrt(3f) / 2f)

        // Take the smaller one to fit in both
        val cellWidth = minOf(cellWidthFromWidth, cellWidthFromHeight)
        val cellHeight = cellWidth * (sqrt(3f) / 2f)

        val itemWidth = (cellWidth - gapPx).coerceAtLeast(0f)
        val itemHeight = (cellHeight - gapPx).coerceAtLeast(0f)
        val totalWidth = (cellWidth * (1f + (columns - 1) * 0.75f))
        val totalHeight = (cellHeight * (rows + 0.5f))

        LaunchedEffect(effects, cellWidth, cellHeight, gapPx, itemWidth, itemHeight) {
            effects.collect { effect ->
                when (effect) {
                    is GameEffect.Particles -> localParticles.addAll(effect.particles)
                    is GameEffect.ScorePopup -> {
                        val offsetPos = HexagonGridDefaults.calculateOffset(
                            effect.gridX,
                            effect.gridY,
                            cellWidth,
                            cellHeight,
                            gapPx,
                        )
                        val centerX = offsetPos.x + itemWidth / 2
                        val centerY = offsetPos.y + itemHeight / 2
                        localScorePopups.add(
                            ScorePopup(
                                popupIdCounter++,
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

                    is GameEffect.MergeParticles -> {
                        val offsetPos = HexagonGridDefaults.calculateOffset(
                            effect.gridX,
                            effect.gridY,
                            cellWidth,
                            cellHeight,
                            gapPx,
                        )
                        val centerX = offsetPos.x + itemWidth / 2
                        val centerY = offsetPos.y + itemHeight / 2
                        val color = if (effect.isPerk) {
                            HexagonGridDefaults.getColorForPerk(Perk.entries[effect.value], colorScheme)
                        } else {
                            HexagonGridDefaults.getColorForValue(effect.value, colorScheme)
                        }
                        val intensity = effect.intensity
                        val count = (25 * intensity).toInt().coerceIn(8, 60)
                        localParticles.addAll(
                            List(count) {
                                val angle = Random.nextFloat() * 2 * PI.toFloat()
                                val speed = (Random.nextFloat() * 300f + 100f) * (0.6f + intensity * 0.4f)
                                Particle(
                                    Random.nextLong(),
                                    centerX,
                                    centerY,
                                    cos(angle) * speed,
                                    sin(angle) * speed,
                                    color,
                                    1f,
                                    (Random.nextFloat() * 4f + 3f) * density.density * (0.7f + intensity * 0.3f),
                                )
                            },
                        )
                    }

                    is GameEffect.PerkPopup -> {
                        val offsetPos = HexagonGridDefaults.calculateOffset(
                            effect.gridX,
                            effect.gridY,
                            cellWidth,
                            cellHeight,
                            gapPx,
                        )
                        val centerX = offsetPos.x + itemWidth / 2
                        val centerY = offsetPos.y + itemHeight / 2
                        localPerkPopups.add(
                            PerkPopup(
                                popupIdCounter++,
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
            snapshotFlow { currentPreviewState to activeHoverMerge.value }.collect { (previews, currentHoverMerge) ->
                val currentIds = previews.map { it.id }.toSet()
                ghostAnimations.keys.retainAll { it in currentIds }
                previews.forEach { preview ->
                    val visualPos = currentHoverMerge?.previewSwaps?.get(preview.id) ?: (preview.x to preview.y)
                    val targetOffset = HexagonGridDefaults.calculateOffset(
                        visualPos.first,
                        visualPos.second,
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
                    val preview = currentPreviewState.find { it.id == id }
                    val isOverlapped = current != null && preview != null && current.targetX == preview.x && current.targetY == preview.y && (
                        current.resultId == "preview_move" || 
                        current.resultId == "preview_duplicate" || 
                        current.resultId.contains("mimic") ||
                        current.steps.isNotEmpty()
                    )
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
                        onMergeAnimationFinished()
                    }
                }
            }
        }

        fun getCellAt(x: Float, y: Float): Pair<Int, Int>? {
            for (col in 0 until columns) {
                val xOffset = col * 0.75f * cellWidth + gapPx / 2
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


        Box(
            modifier = Modifier.size(
                with(density) { totalWidth.toDp() },
                with(density) { totalHeight.toDp() },
            ).graphicsLayer { clip = false }
                .pointerInput(cellWidth, cellHeight, gapPx) {
                    coroutineScope {
                        awaitEachGesture {
                            val down = awaitFirstDown()
                            val initialCellPos = getCellAt(down.position.x, down.position.y)
                            var longPressTriggered = false
                            val job = launch {
                                delay(200.milliseconds)
                                longPressTriggered = true
                                initialCellPos?.let { (x, y) ->
                                    val cell = currentGridState.find { it.x == x && it.y == y }
                                    if (cell != null) {
                                        onCellTouchDown(cell)
                                    } else {
                                        onEmptySpaceTouchDown(x, y)
                                    }
                                }
                            }
                            try {
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    val change = event.changes.first()
                                    if (change.changedToUp()) {
                                        job.cancel()
                                        onEmptySpaceTouchUp()
                                        onCellTouchUp()
                                        if (!longPressTriggered) {
                                            val upCellPos = getCellAt(change.position.x, change.position.y)
                                            if (upCellPos != null && upCellPos == initialCellPos) {
                                                val cell = currentGridState.find { it.x == upCellPos.first && it.y == upCellPos.second }
                                                if (cell != null) {
                                                    onCellClick(cell)
                                                } else {
                                                    onEmptySpaceClick(upCellPos.first, upCellPos.second)
                                                }
                                            }
                                        }
                                        break
                                    } else if (!change.pressed) break
                                }
                            } finally {
                                job.cancel()
                                onEmptySpaceTouchUp()
                                onCellTouchUp()
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
                            val offsetPos = HexagonGridDefaults.calculateOffset(
                                hint.x,
                                hint.y,
                                cellWidth,
                                cellHeight,
                                gapPx,
                            )
                            val center = Offset(offsetPos.x + itemWidth / 2, offsetPos.y + itemHeight / 2)
                            drawCircle(
                                color = Color.White.copy(alpha = 0.2f),
                                radius = (spacing.tiny + spacing.extraSmall * hint.weight).toPx(),
                                center = center,
                            )
                        }

                        val perks = onBoardPerksProvider()
                        perks.forEach { op ->
                            val offsetPos = HexagonGridDefaults.calculateOffset(
                                op.x,
                                op.y,
                                cellWidth,
                                cellHeight,
                                gapPx,
                            )
                            val mysterySize = Size(spacing.semiMedium.toPx(), spacing.semiMedium.toPx())
                            val mysteryOffset = Offset(
                                offsetPos.x + (itemWidth - mysterySize.width) / 2,
                                offsetPos.y + itemHeight - mysterySize.height - spacing.medium.toPx(),
                            )
                            withTransform(
                                {
                                    translate(mysteryOffset.x, mysteryOffset.y)
                                },
                            ) {
                                with(mysteryPainter) {
                                    draw(
                                        size = mysterySize,
                                        alpha = 0.4f,
                                    )
                                }
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
                                    offsetPos.x + (itemWidth - textLayoutResult.size.width) / 2,
                                    offsetPos.y + itemHeight - mysterySize.height - spacing.medium.toPx() - textLayoutResult.size.height - spacing.tiny.toPx(),
                                ),
                            )
                        }
                    }
                    .drawWithContent {
                        val progress = hoverProgress.value
                        val activePerk = activePerkProvider()
                        val selectedCellId = selectedCellIdProvider()
                        val previews = currentPreviewState
                        val currentHoverMerge = activeHoverMerge.value

                        val isGhostSelectable = { preview: PreviewCell ->
                            when (activePerk) {
                                Perk.PATH_MERGE -> false
                                Perk.REMOVE_TILE -> true
                                Perk.INCREMENT_TILE, Perk.MIMIC -> !preview.isMimic
                                Perk.SWAP_TILES -> selectedCellId != preview.id
                                Perk.MOVE_TILE, Perk.DUPLICATE_TILE -> selectedCellId == null
                                else -> false
                            }
                        }

                        previews.forEach { preview ->
                            val animState = ghostAnimations[preview.id] ?: return@forEach
                            val isHovered = currentHoverMerge?.let { 
                                (it.targetX == preview.x && it.targetY == preview.y) ||
                                it.participatingIds?.contains(preview.id) == true
                            } == true
                            if (animState.offset.value == animState.offset.targetValue && selectedCellId != preview.id && !isHovered) {
                                drawGhost(
                                    drawScope = this,
                                    preview = preview,
                                    animState = animState,
                                    itemWidth = itemWidth,
                                    itemHeight = itemHeight,
                                    wiggleValue = if (isGhostSelectable(preview)) wiggleState.value else 0f,
                                    stripeOffset = ghostStripeOffset.value,
                                    starPainter = starPainter,
                                    textMeasurer = textMeasurer,
                                    colorScheme = colorScheme,
                                    spacing = spacing,
                                    isHovered = isHovered,
                                    currentHoverMerge = currentHoverMerge,
                                    selectedCellId = selectedCellId,
                                )
                            }
                        }

                        drawContent()

                        if (progress > 0f && currentHoverMerge != null) {
                            val potentialMerge =
                                potentialMergesProvider()[currentHoverMerge.targetX to currentHoverMerge.targetY]
                            
                            val isPlacementPerk = currentHoverMerge.resultId.contains("move") || 
                                    currentHoverMerge.resultId.contains("swap") ||
                                    currentHoverMerge.resultId.contains("duplicate") ||
                                    currentHoverMerge.resultId.contains("increment") ||
                                    currentHoverMerge.resultId.contains("highlight")

                            val displayMerge = if (isPlacementPerk) {
                                currentHoverMerge
                            } else {
                                potentialMerge?.let {
                                    currentHoverMerge.copy(baseScore = it.baseScore)
                                } ?: currentHoverMerge
                            }

                            drawHoverResult(
                                drawScope = this,
                                merge = displayMerge,
                                combo = comboProvider(),
                                gridStateProvider = { currentGridState },
                                previewStateProvider = { currentPreviewState },
                                cellWidth = cellWidth,
                                cellHeight = cellHeight,
                                gapPx = gapPx,
                                itemWidth = itemWidth,
                                itemHeight = itemHeight,
                                progress = progress,
                                pulseValue = pulseState.value,
                                floatValue = floatState.value,
                                stripeOffset = ghostStripeOffset.value,
                                starPainter = starPainter,
                                textMeasurer = textMeasurer,
                                colorScheme = colorScheme,
                                spacing = spacing,
                                scoreText = scoreText,
                            )
                        }

                        previews.forEach { preview ->
                            val animState = ghostAnimations[preview.id] ?: return@forEach
                            val isHovered = currentHoverMerge?.let { 
                                (it.targetX == preview.x && it.targetY == preview.y) ||
                                it.participatingIds?.contains(preview.id) == true
                            } == true
                            if (animState.offset.value != animState.offset.targetValue || selectedCellId == preview.id || isHovered) {
                                drawGhost(
                                    drawScope = this,
                                    preview = preview,
                                    animState = animState,
                                    itemWidth = itemWidth,
                                    itemHeight = itemHeight,
                                    wiggleValue = if (isGhostSelectable(preview)) wiggleState.value else 0f,
                                    stripeOffset = ghostStripeOffset.value,
                                    starPainter = starPainter,
                                    textMeasurer = textMeasurer,
                                    colorScheme = colorScheme,
                                    spacing = spacing,
                                    isHovered = isHovered,
                                    currentHoverMerge = currentHoverMerge,
                                    selectedCellId = selectedCellId,
                                )
                            }
                        }
                    },
                outlineContent = { _, _ ->
                    StaticSlot(activePerkProvider, { selectedCellIdProvider() != null }, spacing)
                },
            ) {
                Box {
                    gridState.forEach { cell ->
                        key(cell.id) {
                            AnimatedGridHexagon(
                                cell = cell,
                                cellWidth = cellWidth,
                                cellHeight = cellHeight,
                                gapPx = gapPx,
                                moveAnimationSpec = moveAnimationSpec,
                                gridStateProvider = { currentGridState },
                                selectedCellIdProvider = selectedCellIdProvider,
                                activePerkProvider = activePerkProvider,
                                pendingMerge = pendingMergeProvider(),
                                activeMergeStepIndex = activeMergeStepIndexProvider(),
                                hoveredMergeState = hoveredMergeState,
                                density = density,
                                itemWidth = itemWidth,
                                itemHeight = itemHeight,
                                onAnimationFinished = onAnimationFinishedLambda,
                                spacing = spacing,
                            )
                        }
                    }
                }
            }

            ParticlesLayer({ localParticles })
            PopupsLayer(
                scorePopupsProvider = { localScorePopups },
                perkPopupsProvider = { localPerkPopups },
                onScoreFinished = { id -> localScorePopups.removeAll { it.id == id } },
                onPerkFinished = { id -> localPerkPopups.removeAll { it.id == id } },
                containerWidth = totalWidth,
                spacing = spacing,
            )
        }
    }
}
