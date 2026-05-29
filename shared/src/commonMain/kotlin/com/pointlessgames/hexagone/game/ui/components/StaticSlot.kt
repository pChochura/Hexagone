package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pointlessgames.hexagone.game.model.Perk
import com.pointlessgames.hexagone.ui.theme.Spacing

@Composable
internal fun StaticSlot(activePerkProvider: () -> Perk?, isAnySelectedProvider: () -> Boolean, spacing: Spacing) {
    val activePerk = activePerkProvider()
    val isAnySelected = isAnySelectedProvider()
    val isHighlighted = remember(activePerk, isAnySelected) {
        when (activePerk) {
            Perk.MOVE_TILE, Perk.DUPLICATE_TILE -> isAnySelected
            Perk.FUSION, Perk.CHAIN_MERGE -> true
            else -> false
        }
    }
    Hexagon(
        modifier = Modifier.fillMaxSize()
            .then(
                if (isHighlighted) Modifier.border(
                    spacing.extraTiny,
                    Color.White.copy(alpha = 0.4f),
                    FlatTopHexagonShape(),
                ) else Modifier,
            ),
        isOutline = true,
    )
}
