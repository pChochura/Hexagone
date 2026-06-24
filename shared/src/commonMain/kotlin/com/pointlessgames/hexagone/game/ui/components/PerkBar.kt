package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.game.model.TipTarget.SHOP_BUTTON
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.add_label
import hexagone.shared.generated.resources.ic_add
import hexagone.shared.generated.resources.ic_diamond
import hexagone.shared.generated.resources.shop_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PerkBar(
    modifier: Modifier = Modifier,
    collectedPerksProvider: () -> List<Perk>,
    activePerkProvider: () -> Perk?,
    isStuckProvider: () -> Boolean,
    stuckPerksProvider: () -> Set<Perk>,
    onPerkClick: (Perk) -> Unit,
    onVoucherClick: () -> Unit = {},
    onShopClick: () -> Unit,
    isVertical: Boolean = false,
    onTargetPosition: (String, androidx.compose.ui.geometry.Rect) -> Unit = { _, _ -> },
) {
    val collectedPerks = collectedPerksProvider()
    val activePerk = activePerkProvider()
    val isStuck = isStuckProvider()
    val stuckPerks = stuckPerksProvider()

    val surfaceColor = MaterialTheme.colorScheme.surface
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius

    val listState = rememberLazyListState()
    val distinctPerks =
        remember(collectedPerks) { collectedPerks.distinct().sortedBy { it.ordinal } }
    val counts = remember(collectedPerks) { collectedPerks.groupingBy { it }.eachCount() }

    // We use a separate state to track which perk is currently "popping"
    var isFirstRun by remember { mutableStateOf(true) }
    val previousCounts = remember { mutableMapOf<Perk, Int>() }
    val animationStates =
        remember { mutableStateMapOf<Perk, Animatable<Float, AnimationVector1D>>() }

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

    val barBackground = surfaceColor.copy(alpha = 0.95f)

    // Calculate content size dynamically to avoid hardcoded heights and squishing.
    // PerkButton height = (48.dp * 0.866) + 12.dp + 11.sp + padding
    val buttonWidth = spacing.extraHuge.scaled
    val buttonHeight = buttonWidth * 0.866f
    val labelHeight = 36.dp.scaled // Sufficient for 2 lines of 11.sp label and spacing
    val verticalPadding = spacing.medium.scaled * 2
    val expectedContentSize = buttonHeight + labelHeight + verticalPadding

    if (isVertical) {
        val shelfShape = RoundedCornerShape(
            topStart = cornerRadius.large,
            bottomStart = cornerRadius.large,
        )
        Box(
            modifier = modifier
                .fillMaxHeight()
                .width(
                    expectedContentSize + WindowInsets.safeDrawing.asPaddingValues()
                        .calculateRightPadding(LayoutDirection.Ltr),
                )
                .background(barBackground, shelfShape)
                .border(spacing.extraTiny, Color.White.copy(alpha = 0.1f), shelfShape)
                .clip(shelfShape)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.End)),
            contentAlignment = Alignment.Center,
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    vertical = spacing.large.scaled,
                    horizontal = spacing.medium.scaled,
                ),
                verticalArrangement = Arrangement.spacedBy(
                    spacing.large.scaled,
                    Alignment.CenterVertically,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxHeight(),
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
                        }.trackTipTarget("PERK_${distinctPerks.indexOf(perk)}", onTargetPosition),
                    )
                }

                if (distinctPerks.isNotEmpty()) {
                    item { VoucherSeparator(isVertical = true) }
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AddPerkActionButton(
                            onClick = onVoucherClick,
                            size = buttonWidth,
                        )
                        ShopActionButton(
                            onClick = onShopClick,
                            isHighlighted = isStuck,
                            size = buttonWidth,
                        )
                    }
                }
            }
        }
    } else {
        val shelfShape = RoundedCornerShape(
            topStart = cornerRadius.large,
            topEnd = cornerRadius.large,
        )
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(
                    expectedContentSize + WindowInsets.safeDrawing.asPaddingValues()
                        .calculateBottomPadding(),
                )
                .background(barBackground, shelfShape)
                .border(spacing.extraTiny, Color.White.copy(alpha = 0.1f), shelfShape)
                .clip(shelfShape)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
            contentAlignment = Alignment.Center,
        ) {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(
                    horizontal = spacing.large.scaled,
                    vertical = spacing.medium.scaled,
                ),
                horizontalArrangement = Arrangement.spacedBy(
                    spacing.large.scaled,
                    Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
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
                        }.trackTipTarget("PERK_${distinctPerks.indexOf(perk)}", onTargetPosition),
                    )
                }

                if (distinctPerks.isNotEmpty()) {
                    item { VoucherSeparator(isVertical = false) }
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AddPerkActionButton(
                            onClick = onVoucherClick,
                            size = buttonWidth,
                        )
                        ShopActionButton(
                            onClick = onShopClick,
                            isHighlighted = isStuck,
                            size = buttonWidth,
                            modifier = Modifier.trackTipTarget(SHOP_BUTTON, onTargetPosition),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPerkActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp.scaled,
) {
    val spacing = MaterialTheme.spacing
    val heightScale = 0.866f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(spacing.tiny.scaled),
    ) {
        Box(
            modifier = Modifier
                .size(width = size, height = size * heightScale)
                .background(Color.White.copy(alpha = 0.1f), FlatTopHexagonShape())
                .border(1.dp.scaled, Color.White.copy(alpha = 0.2f), FlatTopHexagonShape()),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_add),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.45f),
            )
        }

        Spacer(Modifier.height(spacing.extraSmall.scaled))

        Text(
            text = stringResource(Res.string.add_label).uppercase(),
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp.scaled,
            letterSpacing = 0.5.sp.scaled,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.width(size + spacing.semiSmall.scaled),
        )
    }
}

@Composable
private fun ShopActionButton(
    onClick: () -> Unit,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp.scaled,
) {
    val spacing = MaterialTheme.spacing
    val perkColor = Color(0xFFFFD54F) // Diamond color
    val heightScale = 0.866f

    val infiniteTransition = rememberInfiniteTransition(label = "shop_highlight")
    val glowAlphaState = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(spacing.tiny.scaled),
    ) {
        Box(
            modifier = Modifier
                .size(width = size, height = size * heightScale)
                .drawBehind {
                    val outline =
                        FlatTopHexagonShape().createOutline(this.size, layoutDirection, this)
                    if (outline is androidx.compose.ui.graphics.Outline.Generic) {
                        drawPath(
                            outline.path,
                            color = perkColor.copy(alpha = if (isHighlighted) glowAlphaState.value else 0.1f),
                        )
                        drawPath(
                            outline.path,
                            color = if (isHighlighted) perkColor else perkColor.copy(alpha = 0.4f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_diamond),
                contentDescription = null,
                tint = perkColor,
                modifier = Modifier.size(size * 0.45f),
            )
        }

        Spacer(Modifier.height(spacing.extraSmall.scaled))

        Text(
            text = stringResource(Res.string.shop_title),
            color = perkColor.copy(alpha = 0.8f),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp.scaled,
            letterSpacing = 0.5.sp.scaled,
            textAlign = TextAlign.Center,
            maxLines = 2,
            minLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.width(size + spacing.semiSmall.scaled),
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
                .background(color),
        )
    } else {
        Box(
            modifier = Modifier
                .padding(horizontal = spacing.small.scaled)
                .width(1.dp.scaled)
                .height(32.dp.scaled)
                .background(color),
        )
    }
}

@Preview
@Composable
private fun PerkBarPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            PerkBar(
                collectedPerksProvider = { listOf() },
                activePerkProvider = { null },
                isStuckProvider = { false },
                stuckPerksProvider = { emptySet() },
                onPerkClick = {},
                onVoucherClick = {},
                onShopClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
