package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.billing.BillingProduct
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ShopDialog(
    modifier: Modifier = Modifier,
    diamonds: Int,
    vouchers: Map<PerkCategory, Int>,
    storeProducts: List<BillingProduct> = emptyList(),
    isShopLoading: Boolean = false,
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

            Spacer(Modifier.height(spacing.medium.scaled))

            // Diamond Balance
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp.scaled))
                    .padding(horizontal = spacing.large.scaled, vertical = spacing.small.scaled)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_diamond),
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(24.dp.scaled)
                )
                Spacer(Modifier.width(spacing.small.scaled))
                Text(
                    text = diamonds.toString(),
                    color = Color(0xFFFFD54F),
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp.scaled,
                )
                Spacer(Modifier.width(spacing.small.scaled))
                Text(
                    text = stringResource(Res.string.shop_diamonds_label).uppercase(),
                    color = Color.White.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp.scaled,
                )
            }

            Spacer(Modifier.height(spacing.large.scaled))

            // My Perks Section
            Text(
                text = stringResource(Res.string.shop_banked_perks_label).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp.scaled,
                letterSpacing = 1.sp.scaled,
            )

            Spacer(Modifier.height(spacing.small.scaled))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BankedPerkItem(
                    label = "Common", 
                    count = commonVouchers, 
                    color = Color.Gray,
                    onUse = { if (commonVouchers > 0) onUseVoucher(PerkCategory.COMMON) }
                )
                BankedPerkItem(
                    label = "Rare", 
                    count = rareVouchers, 
                    color = Color(0xFF4FC3F7),
                    onUse = { if (rareVouchers > 0) onUseVoucher(PerkCategory.RARE) }
                )
                BankedPerkItem(
                    label = "Legendary", 
                    count = legendaryVouchers, 
                    color = Color(0xFFFFD54F),
                    onUse = { if (legendaryVouchers > 0) onUseVoucher(PerkCategory.LEGENDARY) }
                )
            }

            if (isStuck && vouchers.any { it.value > 0 }) {
                Spacer(Modifier.height(spacing.medium.scaled))
                Text(
                    text = stringResource(Res.string.shop_use_banked_perk).uppercase(),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp.scaled,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(spacing.large.scaled))

            if (isShopLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp.scaled),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp.scaled
                    )
                }
            } else {
                // Premium Section
                if (storeProducts.isNotEmpty()) {
                    Text(
                        text = "PREMIUM STORE",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp.scaled,
                        letterSpacing = 1.sp.scaled,
                    )
                    Spacer(Modifier.height(spacing.small.scaled))
                    storeProducts.forEach { product ->
                        PremiumProductRow(product, onBuyPremiumProduct)
                    }
                    Spacer(Modifier.height(spacing.large.scaled))
                }

                // Perk Exchange Section
                Text(
                    text = "PERK EXCHANGE",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp.scaled,
                    letterSpacing = 1.sp.scaled,
                )

                Spacer(Modifier.height(spacing.small.scaled))

                ShopItemRow(
                    title = stringResource(Res.string.shop_common_bundle),
                    cost = 50,
                    onBuy = { onBuyPerkWithDiamonds(PerkCategory.COMMON) },
                    hasEnough = diamonds >= 50
                )
                ShopItemRow(
                    title = stringResource(Res.string.shop_rare_bundle),
                    cost = 150,
                    onBuy = { onBuyPerkWithDiamonds(PerkCategory.RARE) },
                    hasEnough = diamonds >= 150
                )
                ShopItemRow(
                    title = stringResource(Res.string.shop_legendary_bundle),
                    cost = 500,
                    onBuy = { onBuyPerkWithDiamonds(PerkCategory.LEGENDARY) },
                    hasEnough = diamonds >= 500
                )
            }

            Spacer(Modifier.height(spacing.large.scaled))

            // Close Button
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(MaterialTheme.cornerRadius.full))
                    .clickable { onDismiss() }
                    .padding(horizontal = spacing.extraLarge.scaled, vertical = spacing.medium.scaled)
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
}

@Composable
private fun BankedPerkItem(label: String, count: Int, color: Color, onUse: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = count > 0) { onUse() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp.scaled)
                .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp.scaled))
                .border(1.dp.scaled, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp.scaled)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "x$count",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp.scaled
            )
        }
        Spacer(Modifier.height(4.dp.scaled))
        Text(
            text = label.uppercase(),
            color = if (count > 0) color else Color.White.copy(alpha = 0.2f),
            fontSize = 10.sp.scaled,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PremiumProductRow(product: BillingProduct, onBuy: (BillingProduct) -> Unit) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp.scaled)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp.scaled))
            .clickable { onBuy(product) }
            .padding(spacing.medium.scaled),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp.scaled
            )
            if (product.description.isNotEmpty()) {
                Text(
                    text = product.description,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp.scaled,
                    lineHeight = 12.sp.scaled
                )
            }
        }
        
        Text(
            text = product.price,
            color = Color(0xFF81C784), // Price color
            fontWeight = FontWeight.Black,
            fontSize = 16.sp.scaled
        )
    }
}

@Composable
private fun ShopItemRow(title: String, cost: Int, onBuy: () -> Unit, hasEnough: Boolean) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp.scaled)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp.scaled))
            .clickable(enabled = hasEnough) { onBuy() }
            .padding(spacing.medium.scaled),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp.scaled
        )
        
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = cost.toString(),
                    color = if (hasEnough) Color(0xFFFFD54F) else Color.Gray,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp.scaled
                )
                Spacer(Modifier.width(4.dp.scaled))
                Icon(
                    painter = painterResource(Res.drawable.ic_diamond),
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(16.dp.scaled)
                )
            }
    }
}
