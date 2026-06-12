package com.pointlessgames.hexagone.billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class RevenueCatBillingManager : BillingManager {
    private val _products = MutableStateFlow<List<BillingProduct>>(emptyList())
    override val products = _products.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>()
    override val purchaseEvents = _purchaseEvents.asSharedFlow()

    private val packageMap = mutableMapOf<String, Package>()

    override suspend fun initialize() {
        try {
            val offerings = Purchases.sharedInstance.awaitOfferings()
            val currentOffering = offerings.current
            if (currentOffering != null) {
                val availablePackages = currentOffering.availablePackages
                _products.value = availablePackages.map { pkg ->
                    packageMap[pkg.identifier] = pkg
                    BillingProduct(
                        id = pkg.identifier,
                        name = pkg.storeProduct.title,
                        description = "",
                        price = pkg.storeProduct.price.formatted,
                        type = ProductType.CONSUMABLE
                    )
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    override suspend fun purchase(product: BillingProduct) {
        val pkg = packageMap[product.id] ?: return
        try {
            Purchases.sharedInstance.awaitPurchase(pkg)
            _purchaseEvents.emit(
                PurchaseResult.Success(
                    productId = product.id,
                    purchaseToken = ""
                )
            )
        } catch (e: Exception) {
            _purchaseEvents.emit(PurchaseResult.Error(e.message ?: "Unknown error"))
        }
    }
}
