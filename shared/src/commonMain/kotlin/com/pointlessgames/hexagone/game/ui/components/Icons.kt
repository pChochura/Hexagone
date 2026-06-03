package com.pointlessgames.hexagone.game.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pointlessgames.hexagone.ui.theme.Spacing
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun LevelIcon(spacing: Spacing, modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(Res.drawable.ic_advance),
        contentDescription = null,
        modifier = modifier.size(spacing.semiLarge),
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
internal fun ComboIcon(spacing: Spacing, modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(Res.drawable.ic_chain_merge),
        contentDescription = null,
        modifier = modifier.size(spacing.semiLarge),
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
internal fun MergeIcon(spacing: Spacing, modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(Res.drawable.ic_fusion),
        contentDescription = null,
        modifier = modifier.size(spacing.semiLarge),
        tint = MaterialTheme.colorScheme.primary
    )
}
