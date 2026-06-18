package com.pointlessgames.hexagone.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

open class Colors(
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

class NeonGlowColors : Colors()

class OceanColors : Colors(
    darkBlue = Color(0xFF0B192C),
    deepBlack = Color(0xFF06101E),
    tile1 = Color(0xFF00E5FF),  // Cyan
    tile2 = Color(0xFF2979FF),  // Deep Blue
    tile4 = Color(0xFF1DE9B6),  // Teal
    tile8 = Color(0xFF00E5FF),  // Neon blue
    tile16 = Color(0xFF651FFF)  // Purple-Blue
)

class FireflyColors : Colors(
    darkBlue = Color(0xFF261A15),
    deepBlack = Color(0xFF1A100B),
    tile1 = Color(0xFFFFC107),  // Amber
    tile2 = Color(0xFFF44336),  // Red
    tile4 = Color(0xFFFF5722),  // Deep Orange
    tile8 = Color(0xFFFF9800),  // Orange
    tile16 = Color(0xFFFFEB3B)  // Yellow
)

class MidnightColors : Colors(
    darkBlue = Color(0xFF131022),
    deepBlack = Color(0xFF0B0914),
    tile1 = Color(0xFF7C4DFF),  // Deep Purple
    tile2 = Color(0xFF3D5AFE),  // Indigo
    tile4 = Color(0xFFF50057),  // Pink-Red
    tile8 = Color(0xFF651FFF),  // Bright Purple
    tile16 = Color(0xFF00E5FF)  // Cyan
)

class SunsetColors : Colors(
    darkBlue = Color(0xFF2E1A22),
    deepBlack = Color(0xFF1F0D14),
    tile1 = Color(0xFFFF8A80),  // Light Coral
    tile2 = Color(0xFFFF1744),  // Bright Red
    tile4 = Color(0xFFFFAB40),  // Orange
    tile8 = Color(0xFFFF5252),  // Soft Red
    tile16 = Color(0xFFFFD54F)  // Yellow
)

class MintyColors : Colors(
    darkBlue = Color(0xFF11221C),
    deepBlack = Color(0xFF091410),
    tile1 = Color(0xFF69F0AE),  // Light Green
    tile2 = Color(0xFF00BFA5),  // Teal
    tile4 = Color(0xFF00E676),  // Bright Green
    tile8 = Color(0xFF1DE9B6),  // Aqua
    tile16 = Color(0xFF00C853)  // Deep Green
)

class PastelColors : Colors(
    darkBlue = Color(0xFF222229),
    deepBlack = Color(0xFF16161B),
    tile1 = Color(0xFFFFCDD2),  // Soft Pink
    tile2 = Color(0xFFD1C4E9),  // Soft Purple
    tile4 = Color(0xFFFFF9C4),  // Soft Yellow
    tile8 = Color(0xFFC8E6C9),  // Soft Green
    tile16 = Color(0xFFB3E5FC)  // Soft Blue
)

class CyberColors : Colors(
    darkBlue = Color(0xFF0D1B2A),
    deepBlack = Color(0xFF060B12),
    tile1 = Color(0xFF00FFCC),  // Cyan
    tile2 = Color(0xFFFF00FF),  // Magenta
    tile4 = Color(0xFF00FF99),  // Neon Green
    tile8 = Color(0xFFFF0066),  // Hot Pink
    tile16 = Color(0xFF00CCFF)  // Bright Blue
)

class BerryColors : Colors(
    darkBlue = Color(0xFF290E16),
    deepBlack = Color(0xFF1A070D),
    tile1 = Color(0xFFE91E63),  // Pink
    tile2 = Color(0xFF7B1FA2),  // Deep Purple
    tile4 = Color(0xFFF50057),  // Vibrant Pink
    tile8 = Color(0xFF9C27B0),  // Purple
    tile16 = Color(0xFFFF4081)  // Hot Pink
)

internal val DefaultColors = staticCompositionLocalOf<Colors> { NeonGlowColors() }
