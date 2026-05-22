package com.pointlessgames.hexagone.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.BasicTooltipState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import com.pointlessgames.hexagone.ui.theme.DefaultSpacing
import com.pointlessgames.hexagone.utils.defaultAnimationSpecFloat
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal enum class Position { ABOVE, BELOW }

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun Tooltip(
    position: Position,
    contentDescription: StringResource,
    allowUserInput: Boolean = true,
    state: BasicTooltipState = rememberBasicTooltipState(isPersistent = false),
    content: @Composable () -> Unit,
) {
    BasicTooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = when (position) {
                Position.ABOVE -> TooltipAnchorPosition.Above
                Position.BELOW -> TooltipAnchorPosition.Below
            },
        ),
        tooltip = {
            val alpha = Animatable(0f)
            val scale = Animatable(0.4f)

            LaunchedEffect(Unit) {
                launch { alpha.animateTo(1f, defaultAnimationSpecFloat) }
                launch { scale.animateTo(1f, defaultAnimationSpecFloat) }
            }

            Text(
                modifier = Modifier
                    .graphicsLayer {
                        this.alpha = alpha.value
                        this.scaleX = scale.value
                        this.scaleY = scale.value
                        this.transformOrigin = when (position) {
                            Position.ABOVE -> TransformOrigin(0.5f, 1f)
                            Position.BELOW -> TransformOrigin(0.5f, 0f)
                        }
                    }
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
        focusable = false,
        enableUserInput = allowUserInput,
        content = content,
    )
}
