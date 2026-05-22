package com.pointlessgames.hexagone

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pointlessgames.hexagone.di.initKoin

fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "Hexagone",
        content = { App() },
    )
}
