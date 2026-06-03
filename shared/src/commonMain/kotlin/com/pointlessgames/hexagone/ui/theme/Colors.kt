package com.pointlessgames.hexagone.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class Colors(
    val pink: Color = Color(0xFFF06292),
    val darkBlue: Color = Color(0xFF1C1C24),
    val deepBlack: Color = Color(0xFF0A0A0E),
    val purple: Color = Color(0xFFBB86FC),
    val yellow: Color = Color(0xFFFFD54F),
    val greyBlue: Color = Color(0xFF90A4AE),
    val lightBlueGrey: Color = Color(0xFFC5CAE9),
    val lightPurple: Color = Color(0xFFD1C4E9),
    val skyBlue: Color = Color(0xFF4FC3F7),
    val tile1: Color = Color(0xFF3BA9F3),
    val tile2: Color = Color(0xFF9345C4),
    val tile4: Color = Color(0xFFD63F7B),
    val tile8: Color = Color(0xFFF98E33),
    val tile16: Color = Color(0xFF4BC2E1),
    val perkMove: Color = Color(0xFF9575CD),
    val perkRemove: Color = Color(0xFFE57373),
    val perkFusion: Color = Color(0xFFFFB74D),
    val perkChain: Color = Color(0xFF81C784),
    val perkAdvance: Color = Color(0xFF4DB6AC),
    val perkFreeze: Color = Color(0xFF81D4FA),
    val surge: Color = Color(0xFF00E5FF),
    val overdrive: Color = Color(0xFFFF00FF),
    val zenith: Color = Color(0xFFFFFF00),
    val gold: Color = Color(0xFFFFD700),
    val orangeRed: Color = Color(0xFFFF3D00),
    val deepPurple: Color = Color(0xFF7C4DFF),
)

internal val DefaultColors = staticCompositionLocalOf { Colors() }
