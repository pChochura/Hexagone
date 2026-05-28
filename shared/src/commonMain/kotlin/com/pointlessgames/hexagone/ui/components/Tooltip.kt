package com.pointlessgames.hexagone.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BasicTooltipDefaults
import androidx.compose.foundation.BasicTooltipState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.pointlessgames.hexagone.ui.theme.DefaultSpacing
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
            Text(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.inverseSurface)
                    .padding(
                        horizontal = DefaultSpacing.current.medium,
                        vertical = DefaultSpacing.current.small,
                    ),
                text = stringResource(contentDescription),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                textAlign = TextAlign.Center,
            )
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
    BasicTooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = when (position) {
                Position.ABOVE -> TooltipAnchorPosition.Above
                Position.BELOW -> TooltipAnchorPosition.Below
            },
        ),
        tooltip = @Composable {
            val alphaAnimatable = remember { Animatable(0f) }
            val scaleAnimatable = remember { Animatable(0.4f) }

            LaunchedEffect(state.isHiding) {
                if (state.isHiding) {
                    coroutineScope {
                        launch { alphaAnimatable.animateTo(0f, springSpec) }
                        launch { scaleAnimatable.animateTo(0.4f, springSpec) }
                    }
                    state.markHidden()
                } else {
                    launch { alphaAnimatable.animateTo(1f, springSpec) }
                    launch { scaleAnimatable.animateTo(1f, springSpec) }
                }
            }

            Box(
                modifier = Modifier.graphicsLayer {
                    this.alpha = alphaAnimatable.value
                    this.scaleX = scaleAnimatable.value
                    this.scaleY = scaleAnimatable.value
                    this.transformOrigin = when (position) {
                        Position.ABOVE -> TransformOrigin(0.5f, 1f)
                        Position.BELOW -> TransformOrigin(0.5f, 0f)
                    }
                },
                content = tooltipContent,
            )
        },
        state = state,
        focusable = false,
        enableUserInput = allowUserInput,
        content = content,
    )
}

private val springSpec =
    spring(DampingRatioMediumBouncy, Spring.StiffnessLow, visibilityThreshold = 0.1f)

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
