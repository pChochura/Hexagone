package com.pointlessgames.hexagone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class IconsSize(
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

val LocalIconsSize = staticCompositionLocalOf { IconsSize() }

val MaterialTheme.iconsSize: IconsSize
    @Composable
    @ReadOnlyComposable
    get() = LocalIconsSize.current
