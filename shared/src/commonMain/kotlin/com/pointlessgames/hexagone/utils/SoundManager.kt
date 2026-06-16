package com.pointlessgames.hexagone.utils

import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import hexagone.shared.generated.resources.Res
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

object SoundManager {
    private val cache = mutableMapOf<String, String>()
    
    fun playSound(player: GadulkaPlayer, fileName: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            try {
                val cachedPath = cache[fileName] ?: run {
                    val bytes = Res.readBytes("files/sounds/$fileName")
                    val path = writeToTempFile(fileName, bytes)
                    cache[fileName] = path
                    path
                }
                // Gadulka needs a URL format for local files on some platforms, 
                // but absolute path usually works. For iOS we might need file:// 
                // Let's ensure file:// prefix if not present, though Gadulka might handle it.
                val playUrl = if (cachedPath.startsWith("http") || cachedPath.startsWith("file://")) {
                    cachedPath
                } else {
                    "file://$cachedPath"
                }
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    player.play(playUrl)
                }
            } catch (e: Exception) {
                println("Failed to play sound: $fileName - ${e.message}")
            }
        }
    }
}

expect fun writeToTempFile(fileName: String, bytes: ByteArray): String

@androidx.compose.runtime.Composable
fun rememberPlayButtonSound(): () -> Unit {
    val player = com.pointlessgames.hexagone.LocalMediaPlayer.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val settingsRepository = org.koin.compose.koinInject<com.pointlessgames.hexagone.data.SettingsRepository>()
    val isSoundEnabled by androidx.compose.runtime.remember(settingsRepository) { settingsRepository.getSoundEnabledFlow() }.collectAsState(true, context = kotlin.coroutines.EmptyCoroutineContext)

    return androidx.compose.runtime.remember(player, coroutineScope, isSoundEnabled) {
        {
            if (isSoundEnabled) {
                SoundManager.playSound(player, "button.wav", coroutineScope)
            }
        }
    }
}
