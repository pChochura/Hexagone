package com.pointlessgames.hexagone.utils

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op for JVM/Desktop for now, could be hooked up to Esc key if needed
}
