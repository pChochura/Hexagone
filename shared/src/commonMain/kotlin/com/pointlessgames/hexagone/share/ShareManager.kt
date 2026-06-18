package com.pointlessgames.hexagone.share

import androidx.compose.ui.graphics.ImageBitmap

interface ShareManager {
    fun shareImage(image: ImageBitmap, title: String, text: String)
}
