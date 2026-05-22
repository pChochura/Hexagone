package com.pointlessgames.hexagone

import android.app.Application
import com.pointlessgames.hexagone.di.initKoin
import org.koin.android.ext.koin.androidContext

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin { androidContext(applicationContext) }
    }
}
