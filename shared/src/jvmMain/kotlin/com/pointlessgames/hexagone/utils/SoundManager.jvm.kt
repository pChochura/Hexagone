package com.pointlessgames.hexagone.utils

import java.io.File

actual fun writeToTempFile(fileName: String, bytes: ByteArray): String {
    val tempFile = File.createTempFile("snd_", "_$fileName")
    tempFile.writeBytes(bytes)
    tempFile.deleteOnExit()
    return tempFile.absolutePath
}
