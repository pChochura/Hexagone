package com.pointlessgames.hexagone.billing

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BillingManager {
    val products: StateFlow<List<BillingProduct>>
    val purchaseEvents: SharedFlow<PurchaseResult>

    suspend fun initialize()
    suspend fun purchase(product: BillingProduct)
}

data class BillingProduct(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val type: ProductType
)

enum class ProductType {
    CONSUMABLE,
    NON_CONSUMABLE
}

sealed interface PurchaseResult {
    data class Success(val productId: String, val purchaseToken: String) : PurchaseResult
    data class Error(val message: String) : PurchaseResult
    object Cancelled : PurchaseResult
}
