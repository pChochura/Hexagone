package com.pointlessgames.hexagone.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class)
actual fun writeToTempFile(fileName: String, bytes: ByteArray): String {
    val tempDir = NSTemporaryDirectory()
    val path = tempDir + fileName
    
    val nsData = bytes.usePinned {
        NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong())
    }
    
    NSFileManager.defaultManager.createFileAtPath(path, nsData, null)
    return path
}
