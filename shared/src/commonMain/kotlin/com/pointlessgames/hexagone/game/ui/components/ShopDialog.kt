package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.billing.BillingProduct
import com.pointlessgames.hexagone.billing.ProductType
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.cancel
import hexagone.shared.generated.resources.shop_banked_perks_label
import hexagone.shared.generated.resources.shop_best_value
import hexagone.shared.generated.resources.shop_common_bundle
import hexagone.shared.generated.resources.shop_extra_diamonds
import hexagone.shared.generated.resources.shop_legendary_bundle
import hexagone.shared.generated.resources.shop_products_title
import hexagone.shared.generated.resources.shop_rare_bundle
import hexagone.shared.generated.resources.shop_title
import hexagone.shared.generated.resources.shop_use_banked_perk
import hexagone.shared.generated.resources.shop_vouchers_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ShopDialog(
    modifier: Modifier = Modifier,
    diamonds: Int,
    vouchers: Map<PerkCategory, Int>,
    storeProducts: List<BillingProduct> = emptyList(),
    isShopLoading: Boolean = false,
    isProcessing: Boolean = false,
    onDismiss: () -> Unit,
    onBuyPerkWithDiamonds: (PerkCategory) -> Unit,
    onBuyPremiumProduct: (BillingProduct) -> Unit,
    onUseVoucher: (PerkCategory) -> Unit,
    isStuck: Boolean = false,
) {
    val spacing = MaterialTheme.spacing

    val commonVouchers = vouchers[PerkCategory.COMMON] ?: 0
    val rareVouchers = vouchers[PerkCategory.RARE] ?: 0
    val legendaryVouchers = vouchers[PerkCategory.LEGENDARY] ?: 0

    DialogContainer(
        modifier = modifier,
        isProcessing = isProcessing
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(spacing.extraLarge.scaled)
        ) {
            item {
                Text(
                    text = stringResource(Res.string.shop_title).uppercase(),
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp.scaled,
                    letterSpacing = 6.sp.scaled,
                )
                Spacer(Modifier.height(spacing.extraLarge.scaled))
            }

            // My Perks Section
            item {
                ShopSectionTitle(text = stringResource(Res.string.shop_banked_perks_label))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    VoucherItem(
                        category = PerkCategory.COMMON,
                        count = commonVouchers,
                        onUse = { onUseVoucher(PerkCategory.COMMON) },
                    )
                    VoucherItem(
                        category = PerkCategory.RARE,
                        count = rareVouchers,
                        onUse = { onUseVoucher(PerkCategory.RARE) },
                    )
                    VoucherItem(
                        category = PerkCategory.LEGENDARY,
                        count = legendaryVouchers,
                        onUse = { onUseVoucher(PerkCategory.LEGENDARY) },
                    )
                }

                if (isStuck && vouchers.any { it.value > 0 }) {
                    Spacer(Modifier.height(spacing.medium.scaled))
                    Text(
                        text = stringResource(Res.string.shop_use_banked_perk).uppercase(),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp.scaled,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Spacer(Modifier.height(spacing.extraLarge.scaled))
            }

            if (isShopLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp.scaled),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp.scaled,
                        )
                    }
                }
            } else {
                // Premium Section
                if (storeProducts.isNotEmpty()) {
                    item {
                        ShopSectionTitle(text = stringResource(Res.string.shop_products_title))
                    }
                    
                    itemsIndexed(storeProducts) { index, product ->
                        val label = when (index) {
                            storeProducts.lastIndex -> stringResource(Res.string.shop_best_value)
                            storeProducts.lastIndex - 1 if storeProducts.size > 2 -> stringResource(
                                Res.string.shop_extra_diamonds,
                                50,
                            )
                            1 if storeProducts.size > 1 -> stringResource(
                                Res.string.shop_extra_diamonds,
                                20,
                            )
                            else -> null
                        }
                        
                        ProductCard(
                            title = product.name,
                            price = product.price,
                            description = product.description,
                            label = label,
                            isEnabled = !isProcessing,
                            onClick = { onBuyPremiumProduct(product) }
                        )
                    }
                    item { Spacer(Modifier.height(spacing.extraLarge.scaled)) }
                }

                // Perk Exchange Section
                item {
                    ShopSectionTitle(text = stringResource(Res.string.shop_vouchers_title))
                }

                val exchangeItems = listOf(
                    Triple(Res.string.shop_common_bundle, 50, PerkCategory.COMMON),
                    Triple(Res.string.shop_rare_bundle, 150, PerkCategory.RARE),
                    Triple(Res.string.shop_legendary_bundle, 500, PerkCategory.LEGENDARY)
                )

                items(exchangeItems.size) { index ->
                    val (resId, cost, category) = exchangeItems[index]
                    ProductCard(
                        title = stringResource(resId),
                        price = "",
                        costInDiamonds = cost,
                        hasEnoughDiamonds = diamonds >= cost,
                        isEnabled = !isProcessing,
                        onClick = { onBuyPerkWithDiamonds(category) }
                    )
                }
            }

            item {
                Spacer(Modifier.height(spacing.extraLarge.scaled))
                // Close Button
                Box(
                    modifier = Modifier
                        .graphicsLayer { alpha = 0.6f }
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                        .clickable(enabled = !isProcessing) { onDismiss() }
                        .padding(
                            horizontal = spacing.extraLarge.scaled,
                            vertical = spacing.medium.scaled,
                        ),
                ) {
                    Text(
                        text = stringResource(Res.string.cancel).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp.scaled,
                    )
                }
            }
        }

        // Diamond Balance (Corner) - Needs to be at top level of Box to stay fixed
        DiamondBalanceBadge(
            diamonds = diamonds,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Preview
@Composable
private fun ShopDialogPreview() {
    MaterialTheme {
        ShopDialog(
            diamonds = 1250,
            vouchers = mapOf(
                PerkCategory.COMMON to 3,
                PerkCategory.RARE to 1,
                PerkCategory.LEGENDARY to 0,
            ),
            storeProducts = listOf(
                BillingProduct("1", "Small Pack", "100 Diamonds", "$0.99", ProductType.CONSUMABLE),
                BillingProduct("2", "Medium Pack", "550 Diamonds", "$4.99", ProductType.CONSUMABLE),
                BillingProduct("3", "Large Pack", "1200 Diamonds", "$9.99", ProductType.CONSUMABLE),
            ),
            onDismiss = {},
            onBuyPerkWithDiamonds = {},
            onBuyPremiumProduct = {},
            onUseVoucher = {},
        )
    }
}
