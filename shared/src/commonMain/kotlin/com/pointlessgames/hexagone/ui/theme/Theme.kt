package com.pointlessgames.hexagone.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import hexagone.shared.generated.resources.Poppins_Bold
import hexagone.shared.generated.resources.Poppins_Medium
import hexagone.shared.generated.resources.Poppins_Regular
import hexagone.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun HexagoneTheme(
    content: @Composable () -> Unit
) {
    val fontFamily = FontFamily(
        Font(Res.font.Poppins_Bold, FontWeight.Bold),
        Font(Res.font.Poppins_Regular, FontWeight.Normal),
        Font(Res.font.Poppins_Medium, FontWeight.Medium),
    )

    val colors = DefaultColors.current
    val colorScheme = MaterialTheme.colorScheme.copy(
        primary = colors.pink,
        secondary = colors.purple,
        tertiary = colors.yellow,
        background = colors.deepBlack,
        surface = colors.darkBlue,
        outline = colors.greyBlue,
        outlineVariant = colors.lightBlueGrey,
        onSurfaceVariant = colors.lightPurple,
        secondaryContainer = colors.skyBlue,
        surfaceContainerLowest = colors.tile1,
        surfaceContainerLow = colors.tile2,
        surfaceContainer = colors.tile4,
        surfaceContainerHigh = colors.tile8,
        surfaceContainerHighest = colors.tile16,
        inversePrimary = colors.perkMove,
        error = colors.perkRemove,
        tertiaryContainer = colors.perkFusion,
        primaryContainer = colors.perkChain,
        onTertiaryContainer = colors.perkAdvance,
        scrim = colors.surge,
        inverseSurface = colors.overdrive,
        surfaceBright = colors.zenith,
        surfaceDim = colors.gold,
        errorContainer = colors.orangeRed,
        onPrimaryContainer = colors.deepPurple,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
    )

    val shapes = MaterialTheme.shapes.copy(
        small = RoundedCornerShape(MaterialTheme.cornerRadius.small),
        medium = RoundedCornerShape(MaterialTheme.cornerRadius.medium),
        large = RoundedCornerShape(MaterialTheme.cornerRadius.large),
    )

    val typography = MaterialTheme.typography.copy(
        headlineLarge = TextStyle(
            fontSize = 32.sp,
            lineHeight = 39.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = fontFamily,
        ),
        labelMedium = TextStyle(
            fontSize = 16.sp,
            lineHeight = 19.5f.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = fontFamily,
            letterSpacing = 2.sp,
        ),
        bodySmall = TextStyle(
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Light,
            fontFamily = fontFamily,
        ),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = typography,
        content = content
    )
}
