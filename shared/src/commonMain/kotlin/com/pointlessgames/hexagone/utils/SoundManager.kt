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
    
    suspend fun getFileUrl(fileName: String): String? {
        return try {
            val cachedPath = cache[fileName] ?: run {
                val bytes = Res.readBytes("files/sounds/$fileName")
                val path = writeToTempFile(fileName, bytes)
                cache[fileName] = path
                path
            }
            if (cachedPath.startsWith("http") || cachedPath.startsWith("file://")) {
                cachedPath
            } else {
                "file://$cachedPath"
            }
        } catch (e: Exception) {
            println("Failed to get sound url: $fileName - ${e.message}")
            null
        }
    }
    
    fun playSound(player: GadulkaPlayer, fileName: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            val playUrl = getFileUrl(fileName) ?: return@launch
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                player.play(playUrl)
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
