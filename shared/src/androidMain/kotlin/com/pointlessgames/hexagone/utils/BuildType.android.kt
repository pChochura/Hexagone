package com.pointlessgames.hexagone.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import org.koin.core.context.GlobalContext

actual val isDebug: Boolean
    get() {
        return try {
            val context = GlobalContext.get().get<Context>()
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } catch (e: Exception) {
            false
        }
    }
