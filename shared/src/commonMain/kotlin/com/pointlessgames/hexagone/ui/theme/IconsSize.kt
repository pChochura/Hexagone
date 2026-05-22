package com.pointlessgames.hexagone.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class IconsSize(
    val extraSmall: Dp = 16.dp,
    val small: Dp = 24.dp,
    val medium: Dp = 32.dp,
    val large: Dp = 48.dp,
    val extraLarge: Dp = 64.dp,
) {
    fun smaller(size: Dp): Dp = when (size) {
        extraSmall -> throw IllegalArgumentException("Cannot shrink any further")
        small -> extraSmall
        medium -> small
        large -> medium
        extraLarge -> large
        else -> throw IllegalArgumentException("Unknown size")
    }
}

internal val DefaultIconsSize = staticCompositionLocalOf { IconsSize() }
