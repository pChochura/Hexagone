package com.pointlessgames.hexagone.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BasicTooltipDefaults
import androidx.compose.foundation.BasicTooltipState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Tooltip(
    position: Position,
    contentDescription: StringResource,
    modifier: Modifier = Modifier,
    allowUserInput: Boolean = true,
    state: TooltipState = rememberTooltipState(isPersistent = true),
    content: @Composable () -> Unit,
) {
    Tooltip(
        position = position,
        tooltipContent = @Composable {
            val primaryColor = MaterialTheme.colorScheme.primary
            val cornerRadius = MaterialTheme.cornerRadius.small
            Box(
                modifier = Modifier
                    .widthIn(max = 240.dp)
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent)
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius.toPx()),
                            style = Stroke(width = strokeWidth * 2f)
                        )
                    }
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.5f),
                                primaryColor.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.small,
                    )
            ) {
                Text(
                    text = stringResource(contentDescription),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
        },
        state = state,
        modifier = modifier,
        allowUserInput = allowUserInput,
        content = content,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Tooltip(
    position: Position,
    tooltipContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    allowUserInput: Boolean = true,
    state: TooltipState = rememberTooltipState(isPersistent = true),
    content: @Composable () -> Unit,
) {
    var anchorScreenPosition by remember { mutableStateOf(Offset.Zero) }
    var anchorWidth by remember { mutableStateOf(0f) }
    var anchorHeight by remember { mutableStateOf(0f) }

    BasicTooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = when (position) {
                Position.ABOVE -> TooltipAnchorPosition.Above
                Position.BELOW -> TooltipAnchorPosition.Below
            },
        ),
        tooltip = @Composable {
            var tooltipScreenPosition by remember { mutableStateOf(Offset.Zero) }
            var tooltipWidth by remember { mutableStateOf(0f) }
            var tooltipHeight by remember { mutableStateOf(0f) }
            var isReady by remember { mutableStateOf(false) }

            val alphaAnimatable = remember { Animatable(0f) }
            val scaleAnimatable = remember { Animatable(0f) }

            val infiniteTransition = rememberInfiniteTransition(label = "tooltip_hover")
            val hoverOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = if (position == Position.ABOVE) -2f else 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "hover"
            )

            LaunchedEffect(state.isVisible, state.isHiding) {
                if (state.isHiding) {
                    coroutineScope {
                        launch { alphaAnimatable.animateTo(0f, springSpec) }
                        launch { scaleAnimatable.animateTo(0f, springSpec) }
                    }
                    state.markHidden()
                    isReady = false
                } else if (state.isVisible) {
                    launch { alphaAnimatable.animateTo(1f, springSpec) }
                    launch { scaleAnimatable.animateTo(1f, springSpec) }
                }
            }

            Box(
                modifier = Modifier
                    .onGloballyPositioned { 
                        tooltipScreenPosition = it.positionOnScreen()
                        tooltipWidth = it.size.width.toFloat()
                        tooltipHeight = it.size.height.toFloat()
                        if (tooltipWidth > 0 && anchorWidth > 0) {
                            isReady = true
                        }
                    }
                    .graphicsLayer {
                        this.alpha = alphaAnimatable.value
                        this.scaleX = scaleAnimatable.value
                        this.scaleY = scaleAnimatable.value
                        this.translationY = hoverOffset

                        if (isReady) {
                            val anchorCenterX = anchorScreenPosition.x + anchorWidth / 2f
                            val relativeX = (anchorCenterX - tooltipScreenPosition.x) / tooltipWidth
                            
                            val anchorCenterY = anchorScreenPosition.y + anchorHeight / 2f
                            val tooltipCenterY = tooltipScreenPosition.y + tooltipHeight / 2f
                            val isFlipped = tooltipCenterY > anchorCenterY
                            
                            val relativeY = if (isFlipped) 0f else 1f

                            this.transformOrigin = TransformOrigin(
                                pivotFractionX = relativeX.coerceIn(0f, 1f),
                                pivotFractionY = relativeY
                            )
                        } else {
                            this.transformOrigin = when (position) {
                                Position.ABOVE -> TransformOrigin(0.5f, 1f)
                                Position.BELOW -> TransformOrigin(0.5f, 0f)
                            }
                        }
                    },
                content = tooltipContent,
            )
        },
        state = state,
        focusable = false,
        enableUserInput = allowUserInput,
        content = {
            Box(
                modifier = Modifier.onGloballyPositioned {
                    anchorScreenPosition = it.positionOnScreen()
                    anchorWidth = it.size.width.toFloat()
                    anchorHeight = it.size.height.toFloat()
                },
                content = { content() }
            )
        },
    )
}

private val springSpec =
    spring(0.7f, Spring.StiffnessMedium, visibilityThreshold = 0.01f)

enum class Position { ABOVE, BELOW }

@OptIn(ExperimentalFoundationApi::class)
@Stable
class TooltipState(
    initialIsVisible: Boolean,
    override val isPersistent: Boolean,
    private val mutatorMutex: MutatorMutex,
) : BasicTooltipState {

    override var isVisible by mutableStateOf(initialIsVisible)
    var isHiding by mutableStateOf(false)

    private var job: (CancellableContinuation<Unit>)? = null

    override suspend fun show(mutatePriority: MutatePriority) {
        val cancellableShow: suspend () -> Unit = {
            suspendCancellableCoroutine { continuation ->
                isVisible = true
                isHiding = false
                job = continuation
            }
        }

        mutatorMutex.mutate(mutatePriority) {
            try {
                if (isPersistent) {
                    cancellableShow()
                } else {
                    withTimeout(BasicTooltipDefaults.TooltipDuration.milliseconds) {
                        cancellableShow()
                    }
                }
            } finally {
                isHiding = true
            }
        }
    }

    override fun dismiss() {
        isHiding = true
    }

    override fun onDispose() {
        job?.cancel()
    }

    fun markHidden() {
        isVisible = false
        isHiding = false
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberTooltipState(
    initialIsVisible: Boolean = false,
    isPersistent: Boolean = true,
    mutatorMutex: MutatorMutex = BasicTooltipDefaults.GlobalMutatorMutex,
): TooltipState = remember(isPersistent, mutatorMutex) {
    TooltipState(
        initialIsVisible = initialIsVisible,
        isPersistent = isPersistent,
        mutatorMutex = mutatorMutex,
    )
}
