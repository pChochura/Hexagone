package com.pointlessgames.hexagone

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.ui.LocalInnerPadding
import com.pointlessgames.hexagone.ui.theme.HexagoneTheme
import com.pointlessgames.hexagone.ui.theme.LocalCornerRadius
import com.pointlessgames.hexagone.ui.theme.LocalIconsSize
import com.pointlessgames.hexagone.ui.theme.LocalSpacing
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.iconsSize
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.utils.LocalResultEventBus
import com.pointlessgames.hexagone.utils.ResultEventBus
import com.pointlessgames.hexagone.utils.plus
import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import eu.iamkonstantin.kotlin.gadulka.rememberGadulkaState
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    KoinContext {
        HexagoneTheme {
            Scaffold(
                modifier = modifier,
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.systemBars,
            ) { innerPadding ->
                val spacing = MaterialTheme.spacing
                val cornerRadius = MaterialTheme.cornerRadius
                val iconsSize = MaterialTheme.iconsSize

                val settingsRepository: SettingsRepository = koinInject()
                var startingRoute by remember { mutableStateOf<Route?>(null) }

                LaunchedEffect(Unit) {
                    val name = settingsRepository.getPlayerName()
                    startingRoute = if (name == null) Route.Onboarding else Route.Start
                }

                CompositionLocalProvider(
                    LocalSpacing provides spacing,
                    LocalCornerRadius provides cornerRadius,
                    LocalIconsSize provides iconsSize,
                    LocalInnerPadding provides remember {
                        PaddingValues(spacing.extraLarge) + innerPadding
                    },
                    LocalMediaPlayer provides rememberGadulkaState(),
                    LocalResultEventBus provides remember { ResultEventBus() },
                ) {
                    startingRoute?.let {
                        Navigator(it)
                    }
                }
            }
        }
    }
}

val LocalMediaPlayer: ProvidableCompositionLocal<GadulkaPlayer> = staticCompositionLocalOf {
    error("LocalMediaPlayer not provided")
}
