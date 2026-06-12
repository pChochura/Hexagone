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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
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

            Spacer(Modifier.height(spacing.extraLarge.scaled))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 70.dp.scaled),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium.scaled, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(spacing.medium.scaled),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp.scaled),
                contentPadding = PaddingValues(horizontal = spacing.medium.scaled)
            ) {
                items(perks) { perk ->
                    PerkButton(
                        perk = perk,
                        onClick = { onPerkSelected(perk) },
                        buttonSize = 70.dp.scaled
                    )
                }
            }

            Spacer(Modifier.height(spacing.extraLarge.scaled))

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
