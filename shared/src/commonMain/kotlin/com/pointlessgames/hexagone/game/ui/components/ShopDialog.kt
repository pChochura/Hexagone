package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import hexagone.shared.generated.resources.ic_diamond
import hexagone.shared.generated.resources.ic_legendary_perk
import hexagone.shared.generated.resources.ic_rare_perk
import hexagone.shared.generated.resources.ic_roll
import hexagone.shared.generated.resources.shop_banked_perks_label
import hexagone.shared.generated.resources.shop_best_value
import hexagone.shared.generated.resources.shop_common_bundle
import hexagone.shared.generated.resources.shop_extra_diamonds
import hexagone.shared.generated.resources.shop_insufficient_balance
import hexagone.shared.generated.resources.shop_legendary_bundle
import hexagone.shared.generated.resources.shop_products_title
import hexagone.shared.generated.resources.shop_rare_bundle
import hexagone.shared.generated.resources.shop_title
import hexagone.shared.generated.resources.shop_use_banked_perk
import hexagone.shared.generated.resources.shop_vouchers_title
import org.jetbrains.compose.resources.painterResource
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

    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                RoundedCornerShape(48.dp.scaled),
            )
            .border(
                2.dp.scaled,
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(48.dp.scaled),
            )
            .padding(spacing.extraLarge.scaled),
    ) {
        // Diamond Balance (Corner)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp.scaled))
                .border(
                    width = 1.dp.scaled,
                    color = Color(0xFFFFD54F).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp.scaled),
                )
                .padding(horizontal = spacing.medium.scaled, vertical = spacing.small.scaled),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(Res.drawable.ic_diamond),
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(16.dp.scaled),
                )
                Spacer(Modifier.width(spacing.extraSmall.scaled))
                Text(
                    text = diamonds.toString(),
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp.scaled,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.shop_title).uppercase(),
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp.scaled,
                letterSpacing = 6.sp.scaled,
            )

            Spacer(Modifier.height(spacing.extraLarge.scaled))

            // My Perks Section
            Text(
                text = stringResource(Res.string.shop_banked_perks_label).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp.scaled,
                letterSpacing = 1.sp.scaled,
            )

            Spacer(Modifier.height(spacing.medium.scaled))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BankedPerkItem(
                    category = PerkCategory.COMMON,
                    count = commonVouchers,
                    onUse = { if (commonVouchers > 0) onUseVoucher(PerkCategory.COMMON) },
                )
                BankedPerkItem(
                    category = PerkCategory.RARE,
                    count = rareVouchers,
                    onUse = { if (rareVouchers > 0) onUseVoucher(PerkCategory.RARE) },
                )
                BankedPerkItem(
                    category = PerkCategory.LEGENDARY,
                    count = legendaryVouchers,
                    onUse = { if (legendaryVouchers > 0) onUseVoucher(PerkCategory.LEGENDARY) },
                )
            }

            if (isStuck && vouchers.any { it.value > 0 }) {
                Spacer(Modifier.height(spacing.medium.scaled))
                Text(
                    text = stringResource(Res.string.shop_use_banked_perk).uppercase(),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp.scaled,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(spacing.extraLarge.scaled))

            if (isShopLoading) {
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
            } else {
                // Premium Section
                if (storeProducts.isNotEmpty()) {
                    Text(
                        text = stringResource(Res.string.shop_products_title),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp.scaled,
                        letterSpacing = 1.sp.scaled,
                    )
                    Spacer(Modifier.height(spacing.medium.scaled))
                    storeProducts.forEachIndexed { index, product ->
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
                        PremiumProductRow(
                            product = product,
                            onBuy = onBuyPremiumProduct,
                            label = label,
                            isEnabled = !isProcessing,
                        )
                    }
                    Spacer(Modifier.height(spacing.extraLarge.scaled))
                }

                // Perk Exchange Section
                Text(
                    text = stringResource(Res.string.shop_vouchers_title),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp.scaled,
                    letterSpacing = 1.sp.scaled,
                )

                Spacer(Modifier.height(spacing.medium.scaled))

                ShopItemRow(
                    title = stringResource(Res.string.shop_common_bundle),
                    cost = 50,
                    onBuy = { onBuyPerkWithDiamonds(PerkCategory.COMMON) },
                    hasEnough = diamonds >= 50,
                    isEnabled = !isProcessing,
                )
                ShopItemRow(
                    title = stringResource(Res.string.shop_rare_bundle),
                    cost = 150,
                    onBuy = { onBuyPerkWithDiamonds(PerkCategory.RARE) },
                    hasEnough = diamonds >= 150,
                    isEnabled = !isProcessing,
                )
                ShopItemRow(
                    title = stringResource(Res.string.shop_legendary_bundle),
                    cost = 500,
                    onBuy = { onBuyPerkWithDiamonds(PerkCategory.LEGENDARY) },
                    hasEnough = diamonds >= 500,
                    isEnabled = !isProcessing,
                )
            }

            Spacer(Modifier.height(spacing.extraLarge.scaled))

            // Close Button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        CircleShape,
                    )
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

        if (isProcessing) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun BankedPerkItem(category: PerkCategory, count: Int, onUse: () -> Unit) {
    val icon = when (category) {
        PerkCategory.COMMON -> Res.drawable.ic_roll
        PerkCategory.RARE -> Res.drawable.ic_rare_perk
        PerkCategory.LEGENDARY -> Res.drawable.ic_legendary_perk
    }
    val color = when (category) {
        PerkCategory.COMMON -> Color.Gray
        PerkCategory.RARE -> Color(0xFF4FC3F7)
        PerkCategory.LEGENDARY -> Color(0xFFFFD54F)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(64.dp.scaled)
                    .clip(CircleShape)
                    .clickable(enabled = count > 0, onClick = onUse)
                    .background(color.copy(alpha = 0.1f), CircleShape)
                    .border(2.dp.scaled, color.copy(alpha = 0.3f), CircleShape)
                    .padding(14.dp.scaled),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (count > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp.scaled)
                        .background(color, CircleShape)
                        .border(2.dp.scaled, Color(0xFF1A1A1A), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = count.toString(),
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp.scaled,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp.scaled))
        Text(
            text = category.name.uppercase(),
            color = if (count > 0) color else Color.White.copy(alpha = 0.2f),
            fontSize = 10.sp.scaled,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PremiumProductRow(
    product: BillingProduct,
    onBuy: (BillingProduct) -> Unit,
    label: String? = null,
    isEnabled: Boolean = true,
) {
    val spacing = MaterialTheme.spacing
    Box(modifier = Modifier.padding(vertical = 4.dp.scaled)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp.scaled))
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp.scaled))
                .border(
                    width = if (label != null) 1.dp.scaled else 0.dp,
                    color = if (label != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp.scaled),
                )
                .clickable(enabled = isEnabled) { onBuy(product) }
                .padding(spacing.large.scaled),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp.scaled,
                )
                if (product.description.isNotEmpty()) {
                    Text(
                        text = product.description,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp.scaled,
                        lineHeight = 14.sp.scaled,
                    )
                }
            }

            Text(
                text = product.price,
                color = Color(0xFF81C784),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp.scaled,
            )
        }

        if (label != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 12.dp.scaled, y = (-10).dp.scaled)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp.scaled))
                    .padding(horizontal = 8.dp.scaled, vertical = 2.dp.scaled),
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp.scaled,
                )
            }
        }
    }
}

@Composable
private fun ShopItemRow(
    title: String,
    cost: Int,
    onBuy: () -> Unit,
    hasEnough: Boolean,
    isEnabled: Boolean = true,
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp.scaled)
            .clip(RoundedCornerShape(16.dp.scaled))
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp.scaled))
            .alpha(if (hasEnough) 1f else 0.6f)
            .clickable(enabled = hasEnough && isEnabled) { onBuy() }
            .padding(spacing.large.scaled),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = title.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp.scaled,
            )
            if (!hasEnough) {
                Text(
                    text = stringResource(Res.string.shop_insufficient_balance).uppercase(),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp.scaled,
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = cost.toString(),
                color = if (hasEnough) Color(0xFFFFD54F) else Color.Gray,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp.scaled,
            )
            Spacer(Modifier.width(spacing.small.scaled))
            Icon(
                painter = painterResource(Res.drawable.ic_diamond),
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(18.dp.scaled),
            )
        }
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
