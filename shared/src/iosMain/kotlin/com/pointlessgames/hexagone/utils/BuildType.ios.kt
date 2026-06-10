package com.pointlessgames.hexagone.utils

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

@OptIn(ExperimentalNativeApi::class)
actual val isDebug: Boolean = com.pointlessgames.hexagone.BuildKonfig.IS_DEBUG || Platform.isDebugBinary
