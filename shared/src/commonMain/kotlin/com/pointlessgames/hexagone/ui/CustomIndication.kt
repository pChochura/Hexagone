package com.pointlessgames.hexagone.ui

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.invalidateMeasurement
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import com.pointlessgames.hexagone.utils.defaultAnimationSpecFloat
import com.pointlessgames.hexagone.utils.defaultAnimationSpecOffset
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.atan2

class CustomIndicationNodeFactory(
    private val defaultShape: TiltedRoundedCornersShape,
    private val pressedShape: TiltedRoundedCornersShape,
    private val defaultBackgroundColor: Color,
    private val pressedBackgroundColor: Color,
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        CustomIndicationNode(
            defaultShape = defaultShape,
            pressedShape = pressedShape,
            defaultBackgroundColor = defaultBackgroundColor,
            pressedBackgroundColor = pressedBackgroundColor,
            interactionSource = interactionSource,
        )

    override fun hashCode(): Int = -1

    override fun equals(other: Any?) = other === this
}

private class CustomIndicationNode(
    private val defaultShape: TiltedRoundedCornersShape,
    private val pressedShape: TiltedRoundedCornersShape,
    private val defaultBackgroundColor: Color,
    private val pressedBackgroundColor: Color,
    private val interactionSource: InteractionSource,
) : Modifier.Node(), DrawModifierNode {

    private val path = Path()
    private val animatedBackgroundColor = Animatable(defaultBackgroundColor)
    private val animatedShapeProgress = Animatable(0f)

    private suspend fun animateToPressed() {
        coroutineScope {
            launch { animatedBackgroundColor.animateTo(pressedBackgroundColor) }
            launch { animatedShapeProgress.animateTo(1f, defaultAnimationSpecFloat) }
        }
    }

    private suspend fun animateToResting() {
        coroutineScope {
            launch { animatedBackgroundColor.animateTo(defaultBackgroundColor) }
            launch { animatedShapeProgress.animateTo(0f, defaultAnimationSpecFloat) }
        }
    }

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> animateToPressed()
                    is PressInteraction.Release -> animateToResting()
                    is PressInteraction.Cancel -> animateToResting()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        path.reset()
        path.addOutline(
            defaultShape.lerp(pressedShape, animatedShapeProgress.value).createOutline(
                size = size,
                layoutDirection = layoutDirection,
                density = this,
            ),
        )
        drawPath(path, color = animatedBackgroundColor.value)

        drawContent()
    }
}

private class DragIndicationNode(
    var isEnabled: Boolean,
    var dragForce: Float,
) : Modifier.Node(),
    PointerInputModifierNode,
    LayoutModifierNode,
    DrawModifierNode {

    private val animatedDragOffset = Animatable(Offset.Zero, Offset.VectorConverter)
    private val animatedScale = Animatable(1f)
    private var initialPointerPosition: Offset? = null

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (pass != PointerEventPass.Initial || !isEnabled) return

        pointerEvent.changes.firstOrNull()?.let {
            when (pointerEvent.type) {
                PointerEventType.Press -> {
                    initialPointerPosition = it.position
                    invalidateMeasurement()
                    coroutineScope.launch {
                        launch { animatedDragOffset.stop() }
                        launch { animatedScale.animateTo(1.1f, defaultAnimationSpecFloat) }
                    }
                }

                PointerEventType.Move -> {
                    initialPointerPosition?.let { initialPos ->
                        val currentPos = it.position
                        val dragDelta = currentPos - initialPos
                        if (animatedDragOffset.value != dragDelta) {
                            coroutineScope.launch {
                                animatedDragOffset.snapTo(dragDelta)
                            }
                        }
                    }
                }

                PointerEventType.Release, PointerEventType.Exit -> onCancelPointerInput()

                else -> Unit
            }
        }
    }

    override fun onCancelPointerInput() {
        initialPointerPosition = null
        coroutineScope.launch {
            launch { animatedDragOffset.animateTo(Offset.Zero, defaultAnimationSpecOffset) }
            launch { animatedScale.animateTo(1f, defaultAnimationSpecFloat) }
        }
        invalidateMeasurement()
    }

    override fun ContentDrawScope.draw() {
        if (!isEnabled) {
            return drawContent()
        }

        scale(scale = animatedScale.value) {
            val distance = animatedDragOffset.value.getDistance()
            val angle = atan2(animatedDragOffset.value.y, animatedDragOffset.value.x)
            val stretchFactor = distance / (distance + 6000f)
            with(drawContext) {
                transform.rotateRad(angle)
                transform.scale(
                    scaleX = 1f + stretchFactor * 1.5f,
                    scaleY = 1f - stretchFactor * 0.2f,
                )
                transform.rotateRad(-angle)
                transform.translate(
                    left = animatedDragOffset.value.x * dragForce,
                    top = animatedDragOffset.value.y * dragForce,
                )
            }
            this@draw.drawContent()
        }
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(
                x = 0,
                y = 0,
                zIndex = if (initialPointerPosition != null) 10f else 0f,
            )
        }
    }
}

private data class DragIndicationElement(
    private val isEnabled: Boolean,
    private val dragForce: Float,
) : ModifierNodeElement<DragIndicationNode>() {
    override fun create() = DragIndicationNode(isEnabled, dragForce)
    override fun update(node: DragIndicationNode) {
        node.isEnabled = isEnabled
        node.dragForce = dragForce
    }
}

internal fun Modifier.dragIndication(isEnabled: Boolean = true, dragForce: Float = 0.03f) =
    this then DragIndicationElement(isEnabled, dragForce)
