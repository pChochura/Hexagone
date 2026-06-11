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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    cell: HexagonCell,
    cellWidth: Float,
    cellHeight: Float,
    gapPx: Float,
    moveAnimationSpec: AnimationSpec<IntOffset>,
    gridStateProvider: () -> List<HexagonCell>,
    selectedCellIdProvider: () -> String?,
    activePerkProvider: () -> Perk?,
    pendingMerge: MergeTransition?,
    activeMergeStepIndex: Int,
    hoveredMergeState: StateFlow<MergeTransition?>,
    density: Density,
    itemWidth: Float,
    itemHeight: Float,
    onAnimationFinished: () -> Unit,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val currentHoverMergeValue by hoveredMergeState.collectAsState()
    val currentHoverMerge = currentHoverMergeValue
    val visualPos = remember(cell.x, cell.y, cell.id, currentHoverMerge) {
        currentHoverMerge?.previewSwaps?.get(cell.id) ?: (cell.x to cell.y)
    }

    val currentStep = pendingMerge?.steps?.getOrNull(activeMergeStepIndex)
    val isMerging = currentStep?.mergingCells?.any { it.id == cell.id } == true
    val isTargetMerging =
        pendingMerge != null && pendingMerge.targetX == cell.x && pendingMerge.targetY == cell.y

    val targetOffset = remember(
        visualPos.first,
        visualPos.second,
        cellWidth,
        cellHeight,
        gapPx,
    ) {
        HexagonGridDefaults.calculateOffset(
            visualPos.first,
            visualPos.second,
            cellWidth,
            cellHeight,
            gapPx,
        )
    }

    val animatedOffset by animateIntOffsetAsState(
        targetOffset,
        moveAnimationSpec,
        label = "cell_offset",
        finishedListener = { if (isMerging) onAnimationFinished() },
    )

    LaunchedEffect(isMerging, targetOffset) {
        if (isMerging && animatedOffset == targetOffset) {
            // Already at target, no animation will trigger finishedListener
            onAnimationFinished()
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

    val isHovered = remember(currentHoverMerge, cell.x, cell.y, cell.id) {
        currentHoverMerge != null && (
                (currentHoverMerge.targetX == cell.x && currentHoverMerge.targetY == cell.y) ||
                        currentHoverMerge.participatingIds?.contains(cell.id) == true ||
                        currentHoverMerge.steps.any { step -> step.mergingCells.any { it.id == cell.id } }
                )
    }

    val isTargetHovered = remember(currentHoverMerge, cell.x, cell.y) {
        currentHoverMerge != null && currentHoverMerge.targetX == cell.x && currentHoverMerge.targetY == cell.y
    }

    val shape = remember { FlatTopHexagonShape() }
    val selectedCellId = selectedCellIdProvider()
    val activePerk = activePerkProvider()
    val isSelected = selectedCellId == cell.id

    val isSelectableLocal = remember(activePerk, cell.x, cell.y, cell.id, gridStateProvider(), selectedCellId) {
        when (activePerk) {
            Perk.PATH_MERGE -> {
                val neighbors = gridStateProvider().filter { n ->
                    val coords = if (cell.x % 2 == 0) {
                        listOf(
                            cell.x to cell.y - 1,
                            cell.x to cell.y + 1,
                            cell.x - 1 to cell.y - 1,
                            cell.x - 1 to cell.y,
                            cell.x + 1 to cell.y - 1,
                            cell.x + 1 to cell.y,
                        )
                    } else {
                        listOf(
                            cell.x to cell.y - 1,
                            cell.x to cell.y + 1,
                            cell.x - 1 to cell.y,
                            cell.x - 1 to cell.y + 1,
                            cell.x + 1 to cell.y,
                            cell.x + 1 to cell.y + 1,
                        )
                    }
                    coords.any { it.first == n.x && it.second == n.y }
                }
                neighbors.any { it.value == cell.value }
            }

            Perk.REMOVE_TILE, Perk.INCREMENT_TILE, Perk.FREEZE_TILE -> true
            Perk.SWAP_TILES -> selectedCellId != cell.id
            Perk.MOVE_TILE, Perk.DUPLICATE_TILE -> selectedCellId == null
            else -> false
        }
    }

    val visualValue = currentHoverMerge?.previewValues?.get(cell.id)
        ?: if (currentHoverMerge?.targetX == cell.x && currentHoverMerge.targetY == cell.y && currentHoverMerge.finalValue != 0) currentHoverMerge.finalValue else cell.value

    val isGhostedInPreview = currentHoverMerge?.forceGhostIds?.contains(cell.id) == true
    val isFrozen = currentHoverMerge?.previewFrozenIds?.contains(cell.id) == true || cell.isFrozen

    val isMimicking = currentHoverMerge?.previewValues?.containsKey(cell.id) == true && cell.isMimic

    Hexagon(
        value = visualValue.toString(),
        backgroundColor = if (cell.isMimic && !isMimicking) Color.DarkGray else HexagonGridDefaults.getColorForValue(
            visualValue,
            MaterialTheme.colorScheme,
        ).let { if (isGhostedInPreview) it.copy(alpha = 0.3f) else it },
        isTactical = cell.isTactical,
        isGhost = isGhostedInPreview,
        isFrozen = isFrozen,
        isMimic = cell.isMimic && !isMimicking,
        seed = cell.id.hashCode(),
        modifier = modifier.size(
            with(density) { itemWidth.toDp() },
            with(density) { itemHeight.toDp() },
        )
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.width, placeable.height) {
                    placeable.place(
                        animatedOffset,
                        zIndex = when {
                            selectedCellId == cell.id || animatedOffset != targetOffset -> 12f
                            isTargetMerging -> 11f
                            isMerging -> 10f
                            isTargetHovered -> 9f
                            isHovered -> 6f
                            else -> 2f
                        },
                    )
                }
            }
            .graphicsLayer {
                this.alpha = alpha;
                val hovered = isHovered
                val selectedCellId = selectedCellIdProvider()
                val isSelectedLocal = selectedCellId == cell.id

                scaleX = scale * (if (isSelectedLocal) 1.2f else if (hovered) 1.15f else 1f)
                scaleY = scale * (if (isSelectedLocal) 1.2f else if (hovered) 1.15f else 1f)
                rotationZ = if (isSelectableLocal && !isSelectedLocal) wiggleState.value else 0f
            }
            .drawWithContent {
                drawContent()
                if (isHovered) {
                    val outline = shape.createOutline(size, layoutDirection, this)
                    if (outline is Outline.Generic) {
                        val selectedCellId = selectedCellIdProvider()
                        val isSelectedLocal = selectedCellId == cell.id
                        val borderColor =
                            if (isSelectedLocal) Color.White else Color.White.copy(alpha = 0.5f)
                        drawPath(
                            outline.path,
                            borderColor,
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
    )
}
