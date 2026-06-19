package com.pointlessgames.hexagone.haptic

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

class IosHapticManager : HapticManager {
    override fun playHaptic(intensity: HapticIntensity) {
        val style = when (intensity) {
            HapticIntensity.LIGHT -> UIImpactFeedbackStyle.UIImpactFeedbackStyleLight
            HapticIntensity.MEDIUM -> UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
            HapticIntensity.HEAVY -> UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy
        }
        val generator = UIImpactFeedbackGenerator(style)
        generator.prepare()
        generator.impactOccurred()
    }
}
