package com.pointlessgames.hexagone.game.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed as lazyRowItemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pointlessgames.hexagone.LocalNavigator
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.ui.components.DiamondBalanceBadge
import com.pointlessgames.hexagone.game.ui.components.HexAlertDialog
import com.pointlessgames.hexagone.game.ui.components.ProductCard
import com.pointlessgames.hexagone.game.ui.components.ProductGridItem
import com.pointlessgames.hexagone.game.ui.components.ScreenScaffold
import com.pointlessgames.hexagone.game.ui.components.ShopSectionTitle
import com.pointlessgames.hexagone.game.ui.components.VoucherItem
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.shop_banked_perks_label
import hexagone.shared.generated.resources.shop_best_value
import hexagone.shared.generated.resources.shop_common_bundle
import hexagone.shared.generated.resources.shop_extra_diamonds
import hexagone.shared.generated.resources.shop_legendary_bundle
import hexagone.shared.generated.resources.shop_products_title
import hexagone.shared.generated.resources.shop_rare_bundle
import hexagone.shared.generated.resources.shop_title
import hexagone.shared.generated.resources.shop_vouchers_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ShopScreen(
    viewModel: GameViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val storeProducts by viewModel.storeProducts.collectAsState()
    val navigator = LocalNavigator.current
    val spacing = MaterialTheme.spacing

    ScreenScaffold(
        title = stringResource(Res.string.shop_title),
        onBack = { navigator.pop() },
        topBarTrailingContent = {
            DiamondBalanceBadge(diamonds = uiState.diamonds)
        }
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { clip = false },
                horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding(),
                    bottom = spacing.extraLarge.scaled
                )
            ) {
                // My Vouchers Section (Full Width)
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Column(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                        ShopSectionTitle(text = stringResource(Res.string.shop_banked_perks_label))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            VoucherItem(
                                category = PerkCategory.COMMON,
                                count = uiState.vouchers[PerkCategory.COMMON] ?: 0,
                                onUse = { viewModel.onUseVoucher(PerkCategory.COMMON) },
                            )
                            VoucherItem(
                                category = PerkCategory.RARE,
                                count = uiState.vouchers[PerkCategory.RARE] ?: 0,
                                onUse = { viewModel.onUseVoucher(PerkCategory.RARE) },
                            )
                            VoucherItem(
                                category = PerkCategory.LEGENDARY,
                                count = uiState.vouchers[PerkCategory.LEGENDARY] ?: 0,
                                onUse = { viewModel.onUseVoucher(PerkCategory.LEGENDARY) },
                            )
                        }
                        Spacer(Modifier.height(spacing.medium.scaled))
                    }
                }

                if (uiState.isShopLoading) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = spacing.extraLarge.scaled)
                                .height(200.dp.scaled),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    // Premium Section Header
                    if (storeProducts.isNotEmpty()) {
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            ShopSectionTitle(
                                text = stringResource(Res.string.shop_products_title),
                                modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)
                            )
                        }
                        
                        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                                contentPadding = PaddingValues(horizontal = spacing.extraLarge.scaled)
                            ) {
                                lazyRowItemsIndexed(storeProducts) { index, product ->
                                    val label = when (index) {
                                        storeProducts.lastIndex -> stringResource(Res.string.shop_best_value)
                                        storeProducts.lastIndex - 1 if storeProducts.size > 2 -> stringResource(Res.string.shop_extra_diamonds, 50)
                                        1 if storeProducts.size > 1 -> stringResource(Res.string.shop_extra_diamonds, 20)
                                        else -> null
                                    }
                                    
                                    ProductGridItem(
                                        title = product.name,
                                        price = product.price,
                                        description = product.description,
                                        label = label,
                                        iconScale = 1f + (index * 0.15f),
                                        isEnabled = !uiState.isShopProcessing,
                                        onClick = { viewModel.onBuyPremiumProduct(product) },
                                        modifier = Modifier.width(160.dp.scaled)
                                    )
                                }
                            }
                        }
                    }

                    // Perk Exchange Section Header
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        ShopSectionTitle(
                            text = stringResource(Res.string.shop_vouchers_title),
                            modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)
                        )
                    }

                    val exchangeItems = listOf(
                        Triple(Res.string.shop_common_bundle, 50, PerkCategory.COMMON),
                        Triple(Res.string.shop_rare_bundle, 150, PerkCategory.RARE),
                        Triple(Res.string.shop_legendary_bundle, 500, PerkCategory.LEGENDARY)
                    )

                    // Exchange items are full-width cards to distinguish from products
                    itemsIndexed(exchangeItems, span = { _, _ -> androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { _, (resId, cost, category) ->
                        Box(modifier = Modifier.padding(horizontal = spacing.extraLarge.scaled)) {
                            ProductCard(
                                title = stringResource(resId),
                                price = "",
                                costInDiamonds = cost,
                                hasEnoughDiamonds = uiState.diamonds >= cost,
                                isEnabled = !uiState.isShopProcessing,
                                onClick = { viewModel.onBuyPerk(category) }
                            )
                        }
                    }
                }
            }

            // Local Dialog Renderer for Shop screen popups
            if (uiState.activeDialog != null) {
                HexAlertDialog(
                    state = uiState.activeDialog!!,
                    onDismiss = viewModel::onDismissDialog
                )
            }
        }
    }
}
