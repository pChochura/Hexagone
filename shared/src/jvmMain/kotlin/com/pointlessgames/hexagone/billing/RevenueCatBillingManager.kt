package com.pointlessgames.hexagone.billing

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class RevenueCatBillingManager : BillingManager {
    private val _products = MutableStateFlow<List<BillingProduct>>(emptyList())
    override val products = _products.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>()
    override val purchaseEvents = _purchaseEvents.asSharedFlow()

    override suspend fun initialize() {
        // No-op for JVM
    }

    override suspend fun purchase(product: BillingProduct) {
        // No-op for JVM
    }
}
