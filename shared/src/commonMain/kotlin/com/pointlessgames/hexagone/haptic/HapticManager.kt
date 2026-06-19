package com.pointlessgames.hexagone.haptic

enum class HapticIntensity {
    LIGHT, MEDIUM, HEAVY
}

interface HapticManager {
    fun playHaptic(intensity: HapticIntensity)
}
