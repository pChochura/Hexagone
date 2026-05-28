package com.pointlessgames.hexagone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val none: Dp = 0.dp,
    val extraTiny: Dp = 1.dp,
    val tiny: Dp = 2.dp,
    val extraSmall: Dp = 4.dp,
    val semiSmall: Dp = 6.dp,
    val small: Dp = 8.dp,
    val semiMedium: Dp = 10.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val semiLarge: Dp = 20.dp,
    val extraLarge: Dp = 24.dp,
    val huge: Dp = 32.dp,
    val extraHuge: Dp = 48.dp,
    val giant: Dp = 64.dp,
    val massive: Dp = 80.dp,
    val colossal: Dp = 100.dp,
    val immense: Dp = 128.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
