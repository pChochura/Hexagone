package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.ui.theme.scaled
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.perk_bar_empty_hint
import org.jetbrains.compose.resources.stringResource

@Composable
fun PerkBar(
    modifier: Modifier = Modifier,
    collectedPerksProvider: () -> List<Perk>,
    vouchersProvider: () -> Map<PerkCategory, Int> = { emptyMap() },
    activePerkProvider: () -> Perk?,
    isStuckProvider: () -> Boolean,
    stuckPerksProvider: () -> Set<Perk>,
    onPerkClick: (Perk) -> Unit,
    onVoucherClick: (PerkCategory) -> Unit = {},
    onShopClick: () -> Unit,
    isVertical: Boolean = false,
) {
    val collectedPerks = collectedPerksProvider()
    val vouchers = vouchersProvider()
    val activePerk = activePerkProvider()
    val isStuck = isStuckProvider()
    val stuckPerks = stuckPerksProvider()

    val surfaceColor = MaterialTheme.colorScheme.surface
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius

    val listState = rememberLazyListState()
    val distinctPerks = remember(collectedPerks) { collectedPerks.distinct().sortedBy { it.ordinal } }
    val counts = remember(collectedPerks) { collectedPerks.groupingBy { it }.eachCount() }
    
    // We use a separate state to track which perk is currently "popping"
    var isFirstRun by remember { mutableStateOf(true) }
    val previousCounts = remember { mutableMapOf<Perk, Int>() }
    val animationStates = remember { mutableStateMapOf<Perk, Animatable<Float, AnimationVector1D>>() }

    LaunchedEffect(counts) {
        if (isFirstRun) {
            previousCounts.clear()
            previousCounts.putAll(counts)
            if (counts.isNotEmpty()) {
                isFirstRun = false
            }
            return@LaunchedEffect
        }

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

    val shape = if (isVertical) {
        RoundedCornerShape(topStart = cornerRadius.extraLarge, bottomStart = cornerRadius.extraLarge)
    } else {
        RoundedCornerShape(topStart = cornerRadius.extraLarge, topEnd = cornerRadius.extraLarge)
    }

    val persistentActions = @Composable {
        ShopButton(
            onClick = onShopClick,
            isHighlighted = isStuck,
            modifier = Modifier.padding(
                horizontal = if (isVertical) 0.dp else spacing.small.scaled,
                vertical = if (isVertical) spacing.small.scaled else 0.dp
            )
        )
    }

    Box(
        modifier = modifier
            .then(if (isVertical) Modifier.fillMaxHeight() else Modifier.fillMaxWidth())
            .graphicsLayer { clip = false },
        contentAlignment = if (isVertical) Alignment.CenterEnd else Alignment.BottomCenter
    ) {
        if (collectedPerks.isEmpty() && vouchers.isEmpty()) {
            Row(
                modifier = Modifier
                    .then(if (isVertical) Modifier.width(100.dp.scaled).fillMaxHeight() else Modifier.fillMaxWidth())
                    .background(surfaceColor, shape)
                    .border(
                        spacing.extraTiny,
                        Color.White.copy(alpha = 0.08f),
                        shape,
                    )
                    .navigationBarsPadding()
                    .padding(spacing.large.scaled),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.perk_bar_empty_hint),
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 13.sp.scaled,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp.scaled,
                    modifier = Modifier.weight(1f).padding(spacing.large.scaled),
                )
                persistentActions()
            }
        } else {
            val content: LazyListScope.() -> Unit = {
                items(distinctPerks, key = { it.name }) { perk ->
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
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                    )
                }

                if (vouchers.any { it.value > 0 }) {
                    item {
                        VoucherSeparator(isVertical = isVertical)
                    }

                    val sortedCategories = vouchers.filter { it.value > 0 }.keys.toList().sortedBy { it.ordinal }
                    items(sortedCategories, key = { it.name }) { category ->
                        VoucherButton(
                            category = category,
                            count = vouchers[category] ?: 0,
                            onClick = { onVoucherClick(category) }
                        )
                    }
                }
            }

            if (isVertical) {
                Column(
                    modifier = Modifier
                        .width(100.dp.scaled)
                        .fillMaxHeight()
                        .background(surfaceColor, shape)
                        .border(
                            spacing.extraTiny,
                            Color.White.copy(alpha = 0.08f),
                            shape,
                        )
                        .clip(shape)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = spacing.large.scaled, horizontal = spacing.medium.scaled),
                        verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = content
                    )
                    persistentActions()
                    Spacer(Modifier.height(spacing.medium.scaled))
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor, shape)
                        .border(
                            spacing.extraTiny,
                            Color.White.copy(alpha = 0.08f),
                            shape,
                        )
                        .clip(shape)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LazyRow(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = spacing.large.scaled, vertical = spacing.medium.scaled),
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        content = content
                    )
                    persistentActions()
                    Spacer(Modifier.width(spacing.medium.scaled))
                }
            }
        }
    }
}

@Composable
private fun VoucherSeparator(isVertical: Boolean) {
    val spacing = MaterialTheme.spacing
    val color = Color.White.copy(alpha = 0.15f)

    if (isVertical) {
        Box(
            modifier = Modifier
                .padding(vertical = spacing.small.scaled)
                .width(32.dp.scaled)
                .height(1.dp.scaled)
                .background(color)
        )
    } else {
        Box(
            modifier = Modifier
                .padding(horizontal = spacing.small.scaled)
                .width(1.dp.scaled)
                .height(32.dp.scaled)
                .background(color)
        )
    }
}

@Preview
@Composable
private fun PerkBarPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            PerkBar(
                collectedPerksProvider = { listOf(Perk.UNDO, Perk.MOVE_TILE) },
                vouchersProvider = {
                    mapOf(
                        PerkCategory.COMMON to 2,
                        PerkCategory.RARE to 1,
                        PerkCategory.LEGENDARY to 3
                    )
                },
                activePerkProvider = { null },
                isStuckProvider = { false },
                stuckPerksProvider = { emptySet() },
                onPerkClick = {},
                onVoucherClick = {},
                onShopClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
