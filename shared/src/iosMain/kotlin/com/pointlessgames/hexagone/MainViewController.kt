package com.pointlessgames.hexagone

import androidx.compose.ui.window.ComposeUIViewController
import com.pointlessgames.hexagone.di.initKoin
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        initKoin()
        App()
    }
}
