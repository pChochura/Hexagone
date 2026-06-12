package com.pointlessgames.hexagone.billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration

object IosBillingInitializer {
    fun configure() {
        Purchases.configure(
            PurchasesConfiguration(
                apiKey = BillingConfiguration.revenueCatIosKey
            )
        )
    }
}
