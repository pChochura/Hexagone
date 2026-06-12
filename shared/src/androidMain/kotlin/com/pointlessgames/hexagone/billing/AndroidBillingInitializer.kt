package com.pointlessgames.hexagone.billing

import android.content.Context
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

object AndroidBillingInitializer {
    fun configure(context: Context) {
        Purchases.configure(
            PurchasesConfiguration(BillingConfiguration.revenueCatAndroidKey)
        )
    }
}
