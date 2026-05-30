package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.perk_bar_empty_hint
import org.jetbrains.compose.resources.stringResource

@Composable
fun PerkBar(
    modifier: Modifier = Modifier,
    collectedPerks: List<Perk>,
    activePerk: Perk?,
    isStuck: Boolean,
    stuckPerks: Set<Perk>,
    onPerkClick: (Perk) -> Unit,
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius

    val listState = rememberLazyListState()
    val distinctPerks = remember(collectedPerks) { collectedPerks.distinct() }
    val counts = remember(collectedPerks) { collectedPerks.groupingBy { it }.eachCount() }
    
    // We use a separate state to track which perk is currently "popping"
    val previousCounts = remember { mutableMapOf<Perk, Int>() }
    val animationStates = remember { mutableStateMapOf<Perk, Animatable<Float, AnimationVector1D>>() }

    LaunchedEffect(counts) {
        counts.forEach { (perk, count) ->
            val prevCount = previousCounts[perk] ?: 0
            if (count > prevCount) {
                val index = distinctPerks.indexOf(perk)
                if (index != -1) {
                    listState.animateScrollToItem(index)
                }
                val anim = animationStates.getOrPut(perk) { Animatable(1f) }
                anim.snapTo(1.5f)
                anim.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
            }
        }
        previousCounts.clear()
        previousCounts.putAll(counts)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (collectedPerks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor, RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge))
                    .border(
                        spacing.extraTiny,
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge),
                    )
                    .navigationBarsPadding()
                    .padding(spacing.large),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.perk_bar_empty_hint),
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(spacing.large),
                )
            }
        } else {
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor, RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge))
                    .border(
                        spacing.extraTiny,
                        Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge),
                    )
                    .clip(RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge))
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(spacing.large),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIndexed(distinctPerks, key = { _, perk -> perk.name }) { _, perk ->
                    val isActive = activePerk == perk
                    val isEnabled = !isStuck || stuckPerks.contains(perk)
                    val count = counts[perk] ?: 0
                    val scale = animationStates[perk]?.value ?: 1f

                    PerkButton(
                        perk = perk,
                        count = count,
                        isActive = isActive,
                        isEnabled = isEnabled,
                        tooltipDescription = perk.descriptionRes,
                        onClick = { onPerkClick(perk) },
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = spacing.semiSmall)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                    )
                }
            }
        }
    }
}
