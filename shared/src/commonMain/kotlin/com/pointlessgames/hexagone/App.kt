package com.pointlessgames.hexagone

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.pointlessgames.hexagone.ui.LocalInnerPadding
import com.pointlessgames.hexagone.ui.theme.DefaultColors
import com.pointlessgames.hexagone.ui.theme.DefaultCornerRadius
import com.pointlessgames.hexagone.ui.theme.DefaultSpacing
import com.pointlessgames.hexagone.utils.LocalResultEventBus
import com.pointlessgames.hexagone.utils.ResultEventBus
import com.pointlessgames.hexagone.utils.plus
import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import eu.iamkonstantin.kotlin.gadulka.rememberGadulkaState
import hexagone.shared.generated.resources.Poppins_Bold
import hexagone.shared.generated.resources.Poppins_Medium
import hexagone.shared.generated.resources.Poppins_Regular
import hexagone.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
@Preview
fun App() {
    val fontFamily = FontFamily(
        Font(Res.font.Poppins_Bold, FontWeight.Bold),
        Font(Res.font.Poppins_Regular, FontWeight.Normal),
        Font(Res.font.Poppins_Medium, FontWeight.Medium),
    )

    val colors = DefaultColors.current
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = colors.ochre,
            onPrimary = colors.beige,
            primaryContainer = colors.rose,
            onPrimaryContainer = colors.brown,

            secondary = colors.rose,
            onSecondary = colors.brown,

            tertiary = colors.tan,
            onTertiary = colors.brown,

            background = colors.beige,
            onBackground = colors.brown,

            surface = colors.beige,
            onSurface = colors.brown,

            surfaceVariant = colors.tan,
            surfaceContainerLow = colors.softCream,
            surfaceContainer = colors.tan,
            surfaceContainerHigh = colors.honeyOak,
            surfaceContainerHighest = colors.rose,

            onSurfaceVariant = colors.brown,

            inverseSurface = colors.brown,
            inverseOnSurface = colors.beige,
            inversePrimary = colors.rose,

            outline = colors.ochre,
        ),
        shapes = MaterialTheme.shapes.copy(
            small = RoundedCornerShape(DefaultCornerRadius.current.small),
            medium = RoundedCornerShape(DefaultCornerRadius.current.medium),
            large = RoundedCornerShape(DefaultCornerRadius.current.large),
        ),
        typography = MaterialTheme.typography.copy(
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
        ),
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.systemBars,
        ) { innerPadding ->
            val spacing = DefaultSpacing.current
            CompositionLocalProvider(
                LocalInnerPadding provides remember {
                    PaddingValues(spacing.extraLarge) + innerPadding
                },
                LocalMediaPlayer provides rememberGadulkaState(),
                LocalResultEventBus provides remember { ResultEventBus() },
            ) {
                Navigator(Route.Game)
            }
        }
    }
}

val LocalMediaPlayer: ProvidableCompositionLocal<GadulkaPlayer> = staticCompositionLocalOf {
    error("LocalMediaPlayer not provided")
}
