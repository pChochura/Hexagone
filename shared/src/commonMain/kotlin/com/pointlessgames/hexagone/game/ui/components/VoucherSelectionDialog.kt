package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.scaled
import com.pointlessgames.hexagone.ui.theme.spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VoucherSelectionDialog(
    category: PerkCategory,
    onPerkSelected: (Perk) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = MaterialTheme.spacing
    val perks = when (category) {
        PerkCategory.COMMON -> Perk.entries.filter { it.baseWeight >= 80 }
        PerkCategory.RARE -> Perk.entries.filter { it.baseWeight in 21..79 }
        PerkCategory.LEGENDARY -> Perk.entries.filter { it.isLegendary }
    }

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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "CHOOSE ${category.name} PERK".uppercase(),
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp.scaled,
                letterSpacing = 4.sp.scaled,
            )

            Spacer(Modifier.height(spacing.large.scaled))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                modifier = Modifier.heightIn(max = 400.dp.scaled)
            ) {
                items(perks) { perk ->
                    PerkSelectionItem(perk = perk, onClick = { onPerkSelected(perk) })
                }
            }

            Spacer(Modifier.height(spacing.large.scaled))

            // Cancel Button
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
private fun PerkSelectionItem(perk: Perk, onClick: () -> Unit) {
    val spacing = MaterialTheme.spacing
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp.scaled))
            .clickable { onClick() }
            .padding(spacing.medium.scaled),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(perk.displayNameRes).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp.scaled,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp.scaled))
            Text(
                text = "SELECT",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp.scaled
            )
        }
    }
}
