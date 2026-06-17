package com.pointlessgames.hexagone

import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.Lifecycle
import com.pointlessgames.hexagone.data.SettingsRepository
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.ui.theme.AdaptiveScale
import com.pointlessgames.hexagone.ui.theme.BASELINE_WIDTH_DP
import com.pointlessgames.hexagone.ui.theme.HexagoneTheme
import com.pointlessgames.hexagone.ui.theme.LocalCornerRadius
import com.pointlessgames.hexagone.ui.theme.LocalIconsSize
import com.pointlessgames.hexagone.ui.theme.LocalSpacing
import com.pointlessgames.hexagone.ui.theme.SMALL_DEVICE_THRESHOLD_DP
import com.pointlessgames.hexagone.ui.theme.cornerRadius
import com.pointlessgames.hexagone.ui.theme.iconsSize
import com.pointlessgames.hexagone.ui.theme.spacing
import com.pointlessgames.hexagone.utils.SoundManager
import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayerState
import eu.iamkonstantin.kotlin.gadulka.rememberGadulkaState
import eu.iamkonstantin.kotlin.gadulka.rememberGadulkaLiveState
import org.koin.compose.koinInject

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    BoxWithConstraints {
        val scaleFactor = (maxWidth.value / BASELINE_WIDTH_DP).coerceIn(0.8f, 1.2f)
        val isSmallDevice = maxWidth < SMALL_DEVICE_THRESHOLD_DP.dp
        val adaptiveScale = AdaptiveScale(scaleFactor, isSmallDevice)

        HexagoneTheme(adaptiveScale = adaptiveScale) {
            Scaffold(
                modifier = modifier,
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.systemBars,
            ) {
                val spacing = MaterialTheme.spacing
                val cornerRadius = MaterialTheme.cornerRadius
                val iconsSize = MaterialTheme.iconsSize

                val leaderboardRepository = koinInject<LeaderboardRepository>()
                var startingRoute by remember { mutableStateOf<Route?>(null) }

                val bgMusicState = rememberGadulkaLiveState()
                val settingsRepository = koinInject<SettingsRepository>()
                val isBgMusicEnabled by remember(settingsRepository) {
                    settingsRepository.getBgMusicEnabledFlow()
                }.collectAsState(true)

                val lifecycleOwner = LocalLifecycleOwner.current
                val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
                val isAppForeground = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

                LaunchedEffect(isBgMusicEnabled, bgMusicState.state, isAppForeground) {
                    if (isBgMusicEnabled && isAppForeground) {
                        if (bgMusicState.state == GadulkaPlayerState.IDLE) {
                            val url = SoundManager.getFileUrl("bg_music.wav")
                            url?.let { bgMusicState.player.play(it) }
                        } else if (bgMusicState.state == GadulkaPlayerState.PAUSED) {
                            bgMusicState.player.play()
                        }
                    } else if (bgMusicState.state != GadulkaPlayerState.IDLE) {
                        if (isBgMusicEnabled && !isAppForeground) {
                            if (bgMusicState.state == GadulkaPlayerState.PLAYING || bgMusicState.state == GadulkaPlayerState.BUFFERING) {
                                bgMusicState.player.pause()
                            }
                        } else if (!isBgMusicEnabled) {
                            bgMusicState.player.stop()
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    leaderboardRepository.syncPendingScores()
                    startingRoute = Route.Game
                }

                CompositionLocalProvider(
                    LocalSpacing provides spacing,
                    LocalCornerRadius provides cornerRadius,
                    LocalIconsSize provides iconsSize,
                    LocalMediaPlayer provides rememberGadulkaState(),
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
