package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.PerkPopup
import com.pointlessgames.hexagone.game.model.ScorePopup
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.label_redemption
import hexagone.shared.generated.resources.label_tactical_redemption
import hexagone.shared.generated.resources.label_tactician
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
internal fun PopupsLayer(
    scorePopupsProvider: () -> List<ScorePopup>,
    perkPopupsProvider: () -> List<PerkPopup>,
    onScoreFinished: (Long) -> Unit,
    onPerkFinished: (Long) -> Unit,
    containerWidth: Float,
    spacing: com.pointlessgames.hexagone.ui.theme.Spacing,
) {
    val scorePopups = scorePopupsProvider()
    val perkPopups = perkPopupsProvider()
    val density = LocalDensity.current
    val popupSpacing = with(density) { spacing.massive.toPx() }

    // Combine all tactical/special popups to group them by position
    val tacticalPopups = perkPopups + scorePopups.filter { it.labelRes != null }
    val standardPopups = scorePopups.filter { it.labelRes == null }

    // Group and calculate offsets once to ensure stability
    val positionedPopups = remember(tacticalPopups, standardPopups, popupSpacing) {
        val list = mutableListOf<PositionedPopup>()
        
        // Handle tactical groups
        tacticalPopups.groupBy { it.gridX to it.gridY }
            .forEach { (_, groupItems) ->
                val sortedItems = groupItems.sortedBy { it.id }
                val total = sortedItems.size
                sortedItems.forEachIndexed { index, item ->
                    val offset = if (total > 1) {
                        val totalWidth = (total - 1) * popupSpacing
                        -totalWidth / 2 + index * popupSpacing
                    } else 0f
                    list.add(PositionedPopup(item, offset))
                }
            }
            
        // Handle standard popups
        standardPopups.forEach { item ->
            list.add(PositionedPopup(item, 0f))
        }
        list
    }

    positionedPopups.forEach { (item, targetOffset) ->
        key(item.id) {
            val animOffset by animateFloatAsState(
                targetValue = targetOffset,
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

private data class PositionedPopup(
    val item: com.pointlessgames.hexagone.game.model.GridPopup,
    val horizontalOffset: Float
)

@Composable
internal fun ScorePopupItem(
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
internal fun StandardScorePopup(
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
internal fun SpecialScorePopup(
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
internal fun PerkPopItem(
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
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        )
        delay(1200.milliseconds)
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
