package com.pointlessgames.hexagone.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal data class Colors(
    val beige: Color = Color(0xFFF3E9DC),
    val tan: Color = Color(0xFFECDAB6),
    val rose: Color = Color(0xFFDAB49D),
    val ochre: Color = Color(0xFFC08552),
    val brown: Color = Color(0xFF895737),
    val honeyOak: Color = Color(0xFFEAD3AD),
    val softCream: Color = Color(0xFFF9F3EB),
)

internal val DefaultColors = staticCompositionLocalOf { Colors() }
