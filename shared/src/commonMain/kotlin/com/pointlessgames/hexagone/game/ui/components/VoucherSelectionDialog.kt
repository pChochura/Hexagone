package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.cancel
import hexagone.shared.generated.resources.shop_voucher_explanation
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VoucherSelectionDialog(
    category: PerkCategory,
    isProcessing: Boolean = false,
    onPerkSelected: (Perk) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val perks = when (category) {
        PerkCategory.COMMON -> Perk.entries.filter { it.baseWeight >= 80 }
        PerkCategory.RARE -> Perk.entries.filter { (it.baseWeight in 21..79) }
        PerkCategory.LEGENDARY -> Perk.entries.filter { it.isLegendary }
    }

    DialogContainer(
        modifier = modifier,
        isProcessing = isProcessing
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.height(spacing.extraLarge.scaled))
            
            Text(
                text = "CHOOSE ${category.name} PERK".uppercase(),
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp.scaled,
                letterSpacing = 4.sp.scaled,
            )

            Spacer(Modifier.height(spacing.extraLarge.scaled))

            Text(
                text = stringResource(Res.string.shop_voucher_explanation),
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp.scaled,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.extraLarge.scaled)
            )

            Spacer(Modifier.height(spacing.extraLarge.scaled))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 70.dp.scaled),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp.scaled),
                contentPadding = PaddingValues(horizontal = spacing.extraLarge.scaled)
            ) {
                items(perks) { perk ->
                    PerkButton(
                        perk = perk,
                        onClick = { onPerkSelected(perk) },
                        buttonSize = 70.dp.scaled,
                        isEnabled = !isProcessing,
                        tooltipDescription = perk.descriptionRes
                    )
                }
            }

            Spacer(Modifier.height(spacing.extraLarge.scaled))

            // Cancel Button
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = 0.6f }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background, CircleShape)
                    .clickable(enabled = !isProcessing) { onDismiss() }
                    .padding(horizontal = spacing.extraLarge.scaled, vertical = spacing.medium.scaled)
            ) {
                Text(
                    text = stringResource(Res.string.cancel).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp.scaled,
                )
            }
            
            Spacer(Modifier.height(spacing.extraLarge.scaled))
        }
    }
}

@Preview
@Composable
private fun VoucherSelectionDialogPreview() {
    MaterialTheme {
        VoucherSelectionDialog(
            category = PerkCategory.RARE,
            onPerkSelected = {},
            onDismiss = {}
        )
    }
}
