package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import com.pointlessgames.hexagone.game.model.HexagonCell
import com.pointlessgames.hexagone.game.model.MergeTransition
import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun AnimatedGridHexagon(
    modifier: Modifier = Modifier,
    cellId: String,
    cellWidth: Float,
    cellHeight: Float,
    gapPx: Float,
    moveAnimationSpec: AnimationSpec<IntOffset>,
    gridStateProvider: () -> List<HexagonCell>,
    selectedCellIdProvider: () -> String?,
    activePerkProvider: () -> Perk?,
    pendingMergeProvider: () -> MergeTransition?,
    activeMergeStepIndexProvider: () -> Int,
    currentHoverMergeProvider: () -> MergeTransition?,
    density: Density,
    itemWidth: Float,
    itemHeight: Float,
    onAnimationFinished: () -> Unit,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val cellProvider = remember { { gridStateProvider().find { it.id == cellId } } }

    val initialCell = remember { cellProvider() }
    val initialOffset = remember {
        if (initialCell != null) {
            val visualPos = currentHoverMergeProvider()?.previewSwaps?.get(cellId) ?: (initialCell.x to initialCell.y)
            HexagonGridDefaults.calculateOffset(visualPos.first, visualPos.second, cellWidth, cellHeight, gapPx)
        } else IntOffset.Zero
    }

    val animatedOffset = remember { androidx.compose.animation.core.Animatable(initialOffset, IntOffset.VectorConverter) }

    LaunchedEffect(cellWidth, cellHeight, gapPx) {
        snapshotFlow {
            val cell = cellProvider() ?: return@snapshotFlow null
            val currentHoverMerge = currentHoverMergeProvider()
            val pendingMerge = pendingMergeProvider()
            val activeMergeStepIndex = activeMergeStepIndexProvider()

            val visualPos = currentHoverMerge?.previewSwaps?.get(cell.id) ?: (cell.x to cell.y)
            val currentStep = pendingMerge?.steps?.getOrNull(activeMergeStepIndex)
            val isMerging = currentStep?.mergingCells?.any { it.id == cell.id } == true

            val targetX = if (isMerging) pendingMerge!!.targetX else visualPos.first
            val targetY = if (isMerging) pendingMerge!!.targetY else visualPos.second

            val offset = HexagonGridDefaults.calculateOffset(targetX, targetY, cellWidth, cellHeight, gapPx)
            Triple(offset, isMerging, pendingMerge != null && pendingMerge.targetX == cell.x && pendingMerge.targetY == cell.y)
        }.collect { triple ->
            if (triple != null) {
                val (targetOffset, isMerging, _) = triple
                if (animatedOffset.targetValue != targetOffset) {
                    animatedOffset.animateTo(targetOffset, moveAnimationSpec)
                    if (isMerging) onAnimationFinished()
                } else if (isMerging && animatedOffset.value == targetOffset) {
                    onAnimationFinished()
                }
            }
        }
    }

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
        infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "wiggle",
    )

    val isHovered = remember {
        derivedStateOf {
            val cell = cellProvider() ?: return@derivedStateOf false
            val currentHoverMerge = currentHoverMergeProvider()
            currentHoverMerge != null && (
                (currentHoverMerge.targetX == cell.x && currentHoverMerge.targetY == cell.y) ||
                currentHoverMerge.participatingIds?.contains(cell.id) == true ||
                currentHoverMerge.steps.any { step -> step.mergingCells.any { it.id == cell.id } }
            )
        }
    }

    val isTargetHovered = remember {
        derivedStateOf {
            val cell = cellProvider() ?: return@derivedStateOf false
            val currentHoverMerge = currentHoverMergeProvider()
            currentHoverMerge != null && currentHoverMerge.targetX == cell.x && currentHoverMerge.targetY == cell.y
        }
    }

    val isTargetMergingState = remember {
        derivedStateOf {
            val cell = cellProvider() ?: return@derivedStateOf false
            val pendingMerge = pendingMergeProvider()
            pendingMerge != null && pendingMerge.targetX == cell.x && pendingMerge.targetY == cell.y
        }
    }

    val isMergingState = remember {
        derivedStateOf {
            val cell = cellProvider() ?: return@derivedStateOf false
            val pendingMerge = pendingMergeProvider()
            val activeMergeStepIndex = activeMergeStepIndexProvider()
            val currentStep = pendingMerge?.steps?.getOrNull(activeMergeStepIndex)
            currentStep?.mergingCells?.any { it.id == cell.id } == true
        }
    }

    val shape = remember { FlatTopHexagonShape() }

    val isSelectableLocal = remember {
        derivedStateOf {
            val cell = cellProvider() ?: return@derivedStateOf false
            val activePerk = activePerkProvider()
            val selectedCellId = selectedCellIdProvider()
            when (activePerk) {
                Perk.PATH_MERGE -> {
                    val neighbors = gridStateProvider().filter { n ->
                        val coords = if (cell.x % 2 == 0) {
                            listOf(
                                cell.x to cell.y - 1, cell.x to cell.y + 1,
                                cell.x - 1 to cell.y - 1, cell.x - 1 to cell.y,
                                cell.x + 1 to cell.y - 1, cell.x + 1 to cell.y,
                            )
                        } else {
                            listOf(
                                cell.x to cell.y - 1, cell.x to cell.y + 1,
                                cell.x - 1 to cell.y, cell.x - 1 to cell.y + 1,
                                cell.x + 1 to cell.y, cell.x + 1 to cell.y + 1,
                            )
                        }
                        coords.any { it.first == n.x && it.second == n.y }
                    }
                    neighbors.any { it.value == cell.value || it.isMimic || cell.isMimic }
                }
                Perk.REMOVE_TILE, Perk.FREEZE_TILE -> true
                Perk.INCREMENT_TILE, Perk.MIMIC -> !cell.isMimic
                Perk.SWAP_TILES -> selectedCellId != cell.id
                Perk.MOVE_TILE, Perk.DUPLICATE_TILE -> selectedCellId == null
                else -> false
            }
        }
    }

    val visualValue = remember {
        derivedStateOf {
            val cell = cellProvider() ?: return@derivedStateOf 0
            val currentHoverMerge = currentHoverMergeProvider()
            currentHoverMerge?.previewValues?.get(cell.id)
                ?: if (currentHoverMerge?.targetX == cell.x && currentHoverMerge.targetY == cell.y && currentHoverMerge.finalValue != 0) currentHoverMerge.finalValue else cell.value
        }
    }

    val isGhostedInPreview = remember {
        derivedStateOf {
            currentHoverMergeProvider()?.forceGhostIds?.contains(cellId) == true
        }
    }

    val isFrozen = remember {
        derivedStateOf {
            val cell = cellProvider() ?: return@derivedStateOf false
            currentHoverMergeProvider()?.previewFrozenIds?.contains(cell.id) == true || cell.isFrozen
        }
    }

    val isVisualMimic = remember {
        derivedStateOf {
            val cell = cellProvider() ?: return@derivedStateOf false
            val currentHoverMerge = currentHoverMergeProvider()
            cell.isMimic || (currentHoverMerge?.resultId == "preview_mimic" && currentHoverMerge.participatingIds?.contains(cell.id) == true)
        }
    }

    val isMimicking = remember {
        derivedStateOf {
            val cell = cellProvider() ?: return@derivedStateOf false
            currentHoverMergeProvider()?.previewValues?.containsKey(cell.id) == true && cell.isMimic
        }
    }

    val isTactical = remember { derivedStateOf { cellProvider()?.isTactical == true } }

    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = remember {
        derivedStateOf {
            if (isVisualMimic.value && !isMimicking.value) Color.DarkGray else HexagonGridDefaults.getColorForValue(
                visualValue.value,
                colorScheme,
            ).let { if (isGhostedInPreview.value) it.copy(alpha = 0.3f) else it }
        }
    }

    Hexagon(
        value = visualValue.value.toString(),
        backgroundColor = backgroundColor.value,
        isTactical = isTactical.value,
        isGhost = isGhostedInPreview.value,
        isFrozen = isFrozen.value,
        isMimic = isVisualMimic.value && !isMimicking.value,
        seed = cellId.hashCode(),
        modifier = modifier.size(
            with(density) { itemWidth.toDp() },
            with(density) { itemHeight.toDp() },
        )
            .graphicsLayer {
                val hovered = isHovered.value
                val selectedCellId = selectedCellIdProvider()
                val isSelectedLocal = selectedCellId == cellId

                translationX = animatedOffset.value.x.toFloat()
                translationY = animatedOffset.value.y.toFloat()

                this.alpha = alpha
                scaleX = scale * (if (isSelectedLocal) 1.2f else if (hovered) 1.15f else 1f)
                scaleY = scale * (if (isSelectedLocal) 1.2f else if (hovered) 1.15f else 1f)
                rotationZ = if ((isSelectableLocal.value || hovered) && !isSelectedLocal) wiggleState.value else 0f
            }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    val hovered = isHovered.value
                    val isSelectedLocal = selectedCellIdProvider() == cellId
                    val isTargetMerging = isTargetMergingState.value
                    val isMerging = isMergingState.value
                    val isTargetHovered = isTargetHovered.value

                    placeable.place(
                        0, 0,
                        zIndex = when {
                            isSelectedLocal || animatedOffset.value != animatedOffset.targetValue -> 12f
                            isTargetMerging -> 11f
                            isMerging -> 10f
                            isTargetHovered -> 9f
                            hovered -> 6f
                            else -> 2f
                        },
                    )
                }
            }
            .drawWithContent {
                drawContent()
                val selectedCellId = selectedCellIdProvider()
                val isSelectedLocal = selectedCellId == cellId
                if (isSelectedLocal) {
                    val outline = shape.createOutline(size, layoutDirection, this)
                    if (outline is Outline.Generic) {
                        drawPath(
                            outline.path,
                            Color.White,
                            style = Stroke(width = spacing.tiny.toPx()),
                        )
                    }
                }
                if (isHovered.value) {
                    val outline = shape.createOutline(size, layoutDirection, this)
                    if (outline is Outline.Generic) {
                        val borderColor =
                            if (isSelectedLocal) Color.White else Color.White.copy(alpha = 0.5f)
                        drawPath(
                            outline.path,
                            borderColor,
                            style = Stroke(width = spacing.tiny.toPx()),
                        )
                    }
                }
            },
    )
}
