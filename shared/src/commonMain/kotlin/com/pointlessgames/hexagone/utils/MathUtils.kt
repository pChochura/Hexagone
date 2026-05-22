package com.pointlessgames.hexagone.utils

import kotlin.math.PI

internal fun Float.toDegrees() = (this * 180 / PI + 360) % 360
