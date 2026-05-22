package com.pointlessgames.hexagone.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class CornerRadius(
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,

    // 50 percent
    val full: Int = 50,
)

internal val DefaultCornerRadius = staticCompositionLocalOf { CornerRadius() }
