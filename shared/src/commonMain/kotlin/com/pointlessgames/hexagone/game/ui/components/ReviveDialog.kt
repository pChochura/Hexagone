package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.utils.rememberPlayButtonSound
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.ic_diamond
import hexagone.shared.generated.resources.label_level
import hexagone.shared.generated.resources.revive_finish_game
import hexagone.shared.generated.resources.revive_get_more
import hexagone.shared.generated.resources.revive_title
import hexagone.shared.generated.resources.revive_use_voucher
import hexagone.shared.generated.resources.score_label
import hexagone.shared.generated.resources.shop_best_value
import hexagone.shared.generated.resources.shop_quick_revive_hint
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReviveDialog(
    score: Int,
    level: Int,
    diamonds: Int,
    vouchers: Map<PerkCategory, Int>,
    onRevive: (PerkCategory) -> Unit,
    onBuyAndRevive: (PerkCategory) -> Unit,
    onOpenShop: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    val playButtonSound = rememberPlayButtonSound()

    DialogContainer(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.large.scaled, vertical = spacing.extraLarge.scaled),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.revive_title).uppercase(),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp.scaled,
                    letterSpacing = 8.sp.scaled,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        blurRadius = 16f,
                    ),
                ),
            )

            Spacer(Modifier.height(spacing.medium.scaled))

            // Run at Stake Section
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(cornerRadius.medium.scaled))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = spacing.large.scaled, vertical = spacing.small.scaled),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.large.scaled),
            ) {
                StatItem(
                    label = stringResource(Res.string.score_label).uppercase(),
                    value = score.toString(),
                )
                Box(
                    modifier = Modifier.width(1.dp.scaled).height(24.dp.scaled)
                        .background(Color.White.copy(alpha = 0.1f)),
                )
                StatItem(
                    label = stringResource(Res.string.label_level).uppercase(),
                    value = level.toString(),
                )
            }

            Spacer(Modifier.height(spacing.large.scaled))

            Text(
                text = stringResource(Res.string.shop_quick_revive_hint),
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp.scaled,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = spacing.medium.scaled),
            )

            Spacer(Modifier.height(spacing.extraLarge.scaled))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.medium.scaled),
                horizontalArrangement = Arrangement.spacedBy(
                    spacing.medium.scaled,
                    Alignment.CenterHorizontally,
                ),
                verticalAlignment = Alignment.Bottom,
            ) {
                PerkCategory.entries.forEach { category ->
                    ReviveCard(
                        category = category,
                        count = vouchers[category] ?: 0,
                        diamonds = diamonds,
                        onAction = {
                            playButtonSound()
                            if ((vouchers[category] ?: 0) > 0) {
                                onRevive(category)
                            } else {
                                onBuyAndRevive(category)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(spacing.extraHuge.scaled))

            // Bottom Actions
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
            ) {
                // Diamond Balance & Shop
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable {
                            playButtonSound()
                            onOpenShop()
                        }
                        .border(1.dp.scaled, Color(0xFFFFD54F).copy(alpha = 0.2f), CircleShape)
                        .padding(
                            horizontal = spacing.large.scaled,
                            vertical = spacing.small.scaled,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_diamond),
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(16.dp.scaled),
                    )
                    Spacer(Modifier.width(spacing.small.scaled))
                    Text(
                        text = diamonds.toString(),
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp.scaled,
                    )
                    Spacer(Modifier.width(spacing.medium.scaled))
                    Text(
                        text = stringResource(Res.string.revive_get_more).uppercase(),
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp.scaled,
                        letterSpacing = 1.sp.scaled,
                    )
                }

                // Finish Game Button
                Text(
                    text = stringResource(Res.string.revive_finish_game).uppercase(),
                    color = Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp.scaled,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            playButtonSound()
                            onDecline()
                        }
                        .padding(
                            horizontal = spacing.extraLarge.scaled,
                            vertical = spacing.medium.scaled,
                        ),
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp.scaled,
            letterSpacing = 1.sp.scaled,
        )
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp.scaled,
        )
    }
}

@Composable
private fun ReviveCard(
    category: PerkCategory,
    count: Int,
    diamonds: Int,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val cornerRadius = MaterialTheme.cornerRadius
    val color = when (category) {
        PerkCategory.COMMON -> Color.Gray
        PerkCategory.RARE -> Color(0xFF4FC3F7)
        PerkCategory.LEGENDARY -> Color(0xFFFFD54F)
    }
    val cost = when (category) {
        PerkCategory.COMMON -> 50
        PerkCategory.RARE -> 150
        PerkCategory.LEGENDARY -> 500
    }
    val isRecommended = category == PerkCategory.LEGENDARY
    val canAfford = diamonds >= cost
    val isEnabled = count > 0 || canAfford
    val playButtonSound = rememberPlayButtonSound()

    Box(
        modifier = modifier.padding(top = 16.dp.scaled), // Provide space for the offset badge
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(cornerRadius.large.scaled))
                .background(
                    if (isRecommended && isEnabled) {
                        Brush.verticalGradient(
                            listOf(color.copy(alpha = 0.15f), Color.Transparent),
                        )
                    } else {
                        SolidColor(Color.White.copy(alpha = 0.05f))
                    },
                )
                .border(
                    1.dp.scaled,
                    if (isRecommended && isEnabled) color.copy(alpha = 0.4f) else Color.White.copy(
                        alpha = 0.1f,
                    ),
                    RoundedCornerShape(cornerRadius.large.scaled),
                )
                .padding(vertical = spacing.large.scaled),
        ) {
            // Remove the Spacer that was pushing content down, since we added padding to the Box
            // Spacer(Modifier.height(if (isRecommended) spacing.medium.scaled else 0.dp))

            VoucherButton(
                category = category,
                count = count,
                onClick = onAction,
                buttonSize = 64.dp.scaled,
                showGlow = (count > 0 || isRecommended) && isEnabled,
            )

            Spacer(Modifier.height(spacing.large.scaled))

            // Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(CircleShape)
                    .background(if (isEnabled) color else color.copy(alpha = 0.2f), CircleShape)
                    .clickable(enabled = isEnabled) {
                        playButtonSound()
                        onAction()
                    }
                    .padding(vertical = spacing.small.scaled),
                contentAlignment = Alignment.Center,
            ) {
                if (count > 0) {
                    Text(
                        text = stringResource(Res.string.revive_use_voucher).uppercase(),
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp.scaled,
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = cost.toString(),
                            color = Color.Black.copy(alpha = if (canAfford) 1f else 0.5f),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp.scaled,
                        )
                        Spacer(Modifier.width(2.dp.scaled))
                        Icon(
                            painter = painterResource(Res.drawable.ic_diamond),
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = if (canAfford) 1f else 0.5f),
                            modifier = Modifier.size(11.dp.scaled),
                        )
                    }
                }
            }
        }

        if (isRecommended) {
            Box(
                modifier = Modifier
                    .offset(y = (-10).dp.scaled)
                    .background(color, RoundedCornerShape(4.dp.scaled))
                    .padding(horizontal = 6.dp.scaled, vertical = 2.dp.scaled),
            ) {
                Text(
                    text = stringResource(Res.string.shop_best_value),
                    color = Color.Black.copy(alpha = if (isEnabled) 1f else 0.6f),
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp.scaled,
                    textAlign = TextAlign.Center,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 6.sp,
                        maxFontSize = 10.sp,
                    ),
                )
            }
        }
    }
}
