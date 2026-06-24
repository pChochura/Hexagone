package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.utils.rememberPlayButtonSound
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.available_vouchers_label
import hexagone.shared.generated.resources.cancel
import hexagone.shared.generated.resources.choose_perk
import hexagone.shared.generated.resources.ic_diamond
import hexagone.shared.generated.resources.ic_left
import hexagone.shared.generated.resources.ic_right
import hexagone.shared.generated.resources.perk_category_common
import hexagone.shared.generated.resources.perk_category_legendary
import hexagone.shared.generated.resources.perk_category_rare
import hexagone.shared.generated.resources.perks_bank_title
import hexagone.shared.generated.resources.shop_buy_button
import hexagone.shared.generated.resources.shop_voucher_explanation
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PerksBankDialog(
    vouchers: Map<PerkCategory, Int>,
    diamonds: Int = 0,
    targetCategory: PerkCategory? = null,
    isProcessing: Boolean = false,
    onPerkSelected: (Perk, PerkCategory) -> Unit,
    onBuyClick: (PerkCategory) -> Unit = {},
    onDismiss: () -> Unit,
    onRefreshBalance: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val playButtonSound = rememberPlayButtonSound()

    LaunchedEffect(Unit) {
        onRefreshBalance()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        DialogContainer(
            modifier = Modifier
                .padding(spacing.large.scaled)
                .heightIn(max = 600.dp.scaled),
            isProcessing = isProcessing,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(Modifier.height(spacing.extraLarge.scaled))

                val title = if (targetCategory != null) {
                    val categoryName = when (targetCategory) {
                        PerkCategory.COMMON -> stringResource(Res.string.perk_category_common)
                        PerkCategory.RARE -> stringResource(Res.string.perk_category_rare)
                        PerkCategory.LEGENDARY -> stringResource(Res.string.perk_category_legendary)
                    }
                    stringResource(Res.string.choose_perk, categoryName)
                } else {
                    stringResource(Res.string.perks_bank_title)
                }

                Text(
                    text = title.uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp.scaled,
                    letterSpacing = 2.sp.scaled,
                )

                Spacer(Modifier.height(spacing.medium.scaled))

                Text(
                    text = stringResource(Res.string.shop_voucher_explanation),
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp.scaled,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.extraLarge.scaled),
                )

                Spacer(Modifier.height(spacing.large.scaled))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(spacing.extraLarge.scaled),
                ) {
                    val categories = targetCategory?.let { listOf(it) }
                        ?: listOf(PerkCategory.LEGENDARY, PerkCategory.RARE, PerkCategory.COMMON)

                    items(categories) { category ->
                        val count = vouchers[category] ?: 0
                        val isAvailable = count > 0
                        val perks = when (category) {
                            PerkCategory.COMMON -> Perk.entries.filter { it.baseWeight >= 80 }
                            PerkCategory.RARE -> Perk.entries.filter { it.baseWeight in 21..79 }
                            PerkCategory.LEGENDARY -> Perk.entries.filter { it.isLegendary }
                        }

                        Column(
                            modifier = Modifier.graphicsLayer {
                                alpha = if (isAvailable) 1f else 0.6f
                            },
                            verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = spacing.extraLarge.scaled),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val categoryName = when (category) {
                                    PerkCategory.COMMON -> stringResource(Res.string.perk_category_common)
                                    PerkCategory.RARE -> stringResource(Res.string.perk_category_rare)
                                    PerkCategory.LEGENDARY -> stringResource(Res.string.perk_category_legendary)
                                }
                                Text(
                                    text = categoryName.uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp.scaled,
                                    letterSpacing = 1.sp.scaled,
                                )

                                if (count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp.scaled, vertical = 2.dp.scaled),
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.available_vouchers_label, count),
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp.scaled,
                                        )
                                    }
                                } else {
                                    val cost = when (category) {
                                        PerkCategory.COMMON -> 50
                                        PerkCategory.RARE -> 150
                                        PerkCategory.LEGENDARY -> 500
                                    }
                                    val canAfford = diamonds >= cost
                                    
                                    Row(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (canAfford) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.1f))
                                            .clickable(enabled = canAfford && !isProcessing) { 
                                                playButtonSound()
                                                onBuyClick(category) 
                                            }
                                            .padding(horizontal = 8.dp.scaled, vertical = 2.dp.scaled),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp.scaled)
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.shop_buy_button).uppercase(),
                                            color = if (canAfford) Color.Black else Color.White.copy(alpha = 0.4f),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp.scaled,
                                        )
                                        Text(
                                            text = cost.toString(),
                                            color = if (canAfford) Color.Black else Color.White.copy(alpha = 0.4f),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp.scaled,
                                        )
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_diamond),
                                            contentDescription = null,
                                            tint = if (canAfford) Color.Black else Color.White.copy(alpha = 0.4f),
                                            modifier = Modifier.size(10.dp.scaled)
                                        )
                                    }
                                }
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                val listState = rememberLazyListState()
                                
                                LazyRow(
                                    state = listState,
                                    contentPadding = PaddingValues(horizontal = spacing.extraLarge.scaled),
                                    horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(perks) { perk ->
                                        PerkButton(
                                            perk = perk,
                                            onClick = {
                                                if (isAvailable && !isProcessing)
                                                    onPerkSelected(perk, category)
                                            },
                                            buttonSize = 64.dp.scaled,
                                            isEnabled = isAvailable && !isProcessing,
                                            tooltipDescription = perk.descriptionRes,
                                        )
                                    }
                                }

                                // Scroll Indicators
                                if (listState.canScrollBackward) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .padding(start = spacing.small.scaled)
                                            .size(24.dp.scaled)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.3f))
                                            .padding(spacing.extraSmall.scaled),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_left),
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                if (listState.canScrollForward) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(end = spacing.small.scaled)
                                            .size(24.dp.scaled)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.3f))
                                            .padding(spacing.extraSmall.scaled),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_right),
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(spacing.extraLarge.scaled))

                // Cancel Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable(enabled = !isProcessing) { 
                            playButtonSound()
                            onDismiss() 
                        }
                        .padding(
                            horizontal = spacing.extraLarge.scaled,
                            vertical = spacing.medium.scaled,
                        ),
                ) {
                    Text(
                        text = stringResource(Res.string.cancel).uppercase(),
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp.scaled,
                        letterSpacing = 1.sp.scaled,
                    )
                }

                Spacer(Modifier.height(spacing.extraLarge.scaled))
            }
        }
    }
}

@Preview
@Composable
private fun PerksBankDialogPreview() {
    MaterialTheme {
        PerksBankDialog(
            vouchers = mapOf(
                PerkCategory.LEGENDARY to 1,
                PerkCategory.RARE to 0,
                PerkCategory.COMMON to 5,
            ),
            onPerkSelected = { _, _ -> },
            onDismiss = {},
        )
    }
}
