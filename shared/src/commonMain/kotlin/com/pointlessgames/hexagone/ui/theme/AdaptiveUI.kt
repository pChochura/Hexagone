package com.pointlessgames.hexagone.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A scaling factor based on the screen width to provide adaptive UI for smaller devices.
 */
data class AdaptiveScale(
    val factor: Float = 1f,
    val isSmallDevice: Boolean = false
)

val LocalAdaptiveScale = staticCompositionLocalOf { AdaptiveScale() }

val AdaptiveScaleFactor: Float
    @Composable
    @ReadOnlyComposable
    get() = LocalAdaptiveScale.current.factor

val IsSmallDevice: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalAdaptiveScale.current.isSmallDevice

/**
 * Extension to scale Dp values based on the adaptive scale factor.
 */
val Dp.scaled: Dp
    @Composable
    @ReadOnlyComposable
    get() = (this.value * AdaptiveScaleFactor).dp

/**
 * Extension to scale TextUnit (sp) values based on the adaptive scale factor.
 */
val TextUnit.scaled: TextUnit
    @Composable
    @ReadOnlyComposable
    get() = (this.value * AdaptiveScaleFactor).sp

/**
 * Baseline width used for scaling calculations.
 */
const val BASELINE_WIDTH_DP = 360f
const val SMALL_DEVICE_THRESHOLD_DP = 340f
