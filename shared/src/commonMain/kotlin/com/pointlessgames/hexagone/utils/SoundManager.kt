package com.pointlessgames.hexagone.utils

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.pointlessgames.hexagone.LocalMediaPlayer
import com.pointlessgames.hexagone.data.SettingsRepository
import eu.iamkonstantin.kotlin.gadulka.GadulkaPlayer
import hexagone.shared.generated.resources.Res
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

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
    val player = LocalMediaPlayer.current
    val coroutineScope = rememberCoroutineScope()
    val settingsRepository = koinInject<SettingsRepository>()
    val isSoundEnabled by remember(settingsRepository) {
        settingsRepository.getSoundEnabledFlow()
    }.collectAsState(true)

    return remember(player, coroutineScope, isSoundEnabled) {
        {
            if (isSoundEnabled) {
                SoundManager.playSound(player, "button.wav", coroutineScope)
            }
        }
    }
}
