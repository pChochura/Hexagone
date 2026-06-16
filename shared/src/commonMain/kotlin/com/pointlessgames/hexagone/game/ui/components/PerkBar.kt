package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import com.pointlessgames.hexagone.game.ui.components.FlatTopHexagonShape
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.add_label
import hexagone.shared.generated.resources.ic_add
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
    onVoucherClick: () -> Unit = {},
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

    val shape = RoundedCornerShape(cornerRadius.full)
    val barBackground = surfaceColor.copy(alpha = 0.95f)

    Box(
        modifier = modifier
            .padding(spacing.large.scaled)
            .safeDrawingPadding()
            .graphicsLayer { clip = false },
        contentAlignment = if (isVertical) Alignment.CenterEnd else Alignment.BottomCenter
    ) {
        if (isVertical) {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled, Alignment.Bottom),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Floating Item Bar
                Box(
                    modifier = Modifier
                        .background(barBackground, shape)
                        .border(spacing.extraTiny, Color.White.copy(alpha = 0.1f), shape)
                        .clip(shape)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (collectedPerks.isEmpty() && vouchers.all { it.value == 0 }) {
                        Text(
                            text = stringResource(Res.string.perk_bar_empty_hint),
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 12.sp.scaled,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = spacing.extraLarge.scaled, horizontal = spacing.large.scaled).width(64.dp.scaled),
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(vertical = spacing.large.scaled, horizontal = spacing.medium.scaled),
                            verticalArrangement = Arrangement.spacedBy(spacing.large.scaled, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
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
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    },
                                )
                            }

                            if (vouchers.any { it.value > 0 }) {
                                if (collectedPerks.isNotEmpty()) {
                                    item { VoucherSeparator(isVertical = true) }
                                }

                                item {
                                    AddPerkButton(
                                        onClick = onVoucherClick,
                                    )
                                }
                            }
                        }
                    }
                }

                // Floating Shop Pod
                Box(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    contentAlignment = Alignment.Center
                ) {
                    ShopButton(
                        onClick = onShopClick,
                        isHighlighted = isStuck,
                    )
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Floating Item Bar
                Box(
                    modifier = Modifier
                        .background(barBackground, shape)
                        .border(spacing.extraTiny, Color.White.copy(alpha = 0.1f), shape)
                        .clip(shape)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (collectedPerks.isEmpty() && vouchers.all { it.value == 0 }) {
                        Text(
                            text = stringResource(Res.string.perk_bar_empty_hint),
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 13.sp.scaled,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = spacing.large.scaled, horizontal = spacing.extraHuge.scaled),
                        )
                    } else {
                        LazyRow(
                            state = listState,
                            contentPadding = PaddingValues(horizontal = spacing.large.scaled, vertical = spacing.medium.scaled),
                            horizontalArrangement = Arrangement.spacedBy(spacing.large.scaled, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
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
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    },
                                )
                            }

                            if (vouchers.any { it.value > 0 }) {
                                if (collectedPerks.isNotEmpty()) {
                                    item { VoucherSeparator(isVertical = false) }
                                }

                                item {
                                    AddPerkButton(
                                        onClick = onVoucherClick,
                                    )
                                }
                            }
                        }
                    }
                }

                // Floating Shop Pod
                Box(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    contentAlignment = Alignment.Center
                ) {
                    ShopButton(
                        onClick = onShopClick,
                        isHighlighted = isStuck,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddPerkButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val size = 36.dp.scaled

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .padding(spacing.tiny.scaled)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(width = size, height = size * 0.866f)
                .background(Color.White.copy(alpha = 0.1f), FlatTopHexagonShape())
                .border(1.dp.scaled, Color.White.copy(alpha = 0.2f), FlatTopHexagonShape()),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = org.jetbrains.compose.resources.painterResource(Res.drawable.ic_add),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp.scaled)
            )
        }
        
        Spacer(Modifier.height(spacing.extraSmall.scaled))
        
        Text(
            text = stringResource(Res.string.add_label).uppercase(),
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp.scaled,
            letterSpacing = 0.5.sp.scaled,
        )
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
