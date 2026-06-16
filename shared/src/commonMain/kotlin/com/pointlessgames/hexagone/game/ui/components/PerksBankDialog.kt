package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun PerksBankDialog(
    vouchers: Map<PerkCategory, Int>,
    targetCategory: PerkCategory? = null,
    isProcessing: Boolean = false,
    onPerkSelected: (Perk, PerkCategory) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DialogContainer(
            modifier = Modifier
                .padding(spacing.large.scaled)
                .heightIn(max = 600.dp.scaled),
            isProcessing = isProcessing,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(Modifier.height(spacing.extraLarge.scaled))
                
                val title = if (targetCategory != null) {
                    val categoryName = when (targetCategory) {
                        PerkCategory.COMMON -> stringResource(Res.string.perk_category_common)
                        PerkCategory.RARE -> stringResource(Res.string.perk_category_rare)
                        PerkCategory.LEGENDARY -> stringResource(Res.string.perk_category_legendary)
                    }
                    "CHOOSE $categoryName PERK"
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
                        .padding(horizontal = spacing.extraLarge.scaled)
                )

                Spacer(Modifier.height(spacing.large.scaled))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(horizontal = spacing.extraLarge.scaled),
                    verticalArrangement = Arrangement.spacedBy(spacing.extraLarge.scaled)
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
                            modifier = Modifier.graphicsLayer { alpha = if (isAvailable) 1f else 0.6f },
                            verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
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
                                    letterSpacing = 1.sp.scaled
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp.scaled, vertical = 2.dp.scaled)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.available_vouchers_label, count),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp.scaled
                                    )
                                }
                            }

                            // Grid of perks for this category
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                                verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                perks.forEach { perk ->
                                    PerkButton(
                                        perk = perk,
                                        onClick = { if (isAvailable && !isProcessing) onPerkSelected(perk, category) },
                                        buttonSize = 64.dp.scaled,
                                        isEnabled = isAvailable && !isProcessing,
                                        tooltipDescription = perk.descriptionRes
                                    )
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
                        .clickable(enabled = !isProcessing) { onDismiss() }
                        .padding(horizontal = spacing.extraLarge.scaled, vertical = spacing.medium.scaled)
                ) {
                    Text(
                        text = stringResource(Res.string.cancel).uppercase(),
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp.scaled,
                        letterSpacing = 1.sp.scaled
                    )
                }
                
                Spacer(Modifier.height(spacing.extraLarge.scaled))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}

@Preview
@Composable
private fun PerksBankDialogPreview() {
    MaterialTheme {
        PerksBankDialog(
            vouchers = mapOf(
                PerkCategory.LEGENDARY to 1,
                PerkCategory.RARE to 0,
                PerkCategory.COMMON to 5
            ),
            onPerkSelected = { _, _ -> },
            onDismiss = {}
        )
    }
}
