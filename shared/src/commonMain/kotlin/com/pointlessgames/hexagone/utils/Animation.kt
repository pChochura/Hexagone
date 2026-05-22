package com.pointlessgames.hexagone.utils

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.geometry.Offset

internal fun <T> defaultAnimationSpec(): AnimationSpec<T> =
    spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow)

internal val defaultAnimationSpecFloat: AnimationSpec<Float> =
    spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow)

internal val defaultAnimationSpecOffset: AnimationSpec<Offset> =
    spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow)
