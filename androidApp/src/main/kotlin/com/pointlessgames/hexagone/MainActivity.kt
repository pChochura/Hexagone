package com.pointlessgames.hexagone

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import com.pointlessgames.hexagone.billing.BillingManager
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val billingManager: BillingManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent { App() }
    }
}
