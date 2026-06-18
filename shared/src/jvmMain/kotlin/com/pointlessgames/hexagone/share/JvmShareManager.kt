package com.pointlessgames.hexagone.share

import androidx.compose.ui.graphics.ImageBitmap

class JvmShareManager : ShareManager {
    override fun shareImage(image: ImageBitmap, title: String, text: String) {
        // Desktop sharing not implemented
    }
}
