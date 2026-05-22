package com.pointlessgames.hexagone.utils

import kotlin.time.Duration.Companion.seconds

internal fun Long.readableDuration(): String =
    this.seconds.toComponents { _, _, minutes, seconds, _ ->
        if (minutes == 0) return "${seconds}s"

        return "${minutes}m ${seconds}s"
    }
