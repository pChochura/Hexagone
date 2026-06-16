package com.pointlessgames.hexagone.billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class RevenueCatBillingManager : BillingManager {

    private val _products = MutableStateFlow<List<BillingProduct>>(emptyList())
    override val products = _products.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>()
    override val purchaseEvents = _purchaseEvents.asSharedFlow()

    private val _currencyBalances = MutableStateFlow<Map<String, Int>>(emptyMap())
    override val currencyBalances = _currencyBalances.asStateFlow()

    private val _isInitializing = MutableStateFlow(false)
    override val isInitializing = _isInitializing.asStateFlow()

    override val appUserId: String?
        get() = if (Purchases.isConfigured) Purchases.sharedInstance.appUserID else null

    private val packageMap = mutableMapOf<String, Package>()

    private val delegate = object : PurchasesDelegate {
        override fun onPurchasePromoProduct(
            product: StoreProduct,
            startPurchase: (onError: (error: com.revenuecat.purchases.kmp.models.PurchasesError, userCancelled: Boolean) -> Unit, onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit) -> Unit
        ) {
        }

        override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
            updateBalance()
        }
    }

    override suspend fun initialize() {
        Purchases.sharedInstance.delegate = delegate
        _isInitializing.value = true
        withContext(Dispatchers.IO) {
            try {
                Purchases.sharedInstance.invalidateVirtualCurrenciesCache()
                updateBalance()

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
                e.printStackTrace()
                // Handle error
            } finally {
                _isInitializing.value = false
            }
        }
    }

    override suspend fun purchase(product: BillingProduct) {
        val pkg = packageMap[product.id] ?: return
        try {
            Purchases.sharedInstance.awaitPurchase(pkg)
            updateBalanceSuspended()
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

    override suspend fun refreshBalance() {
        Purchases.sharedInstance.invalidateVirtualCurrenciesCache()
        updateBalanceSuspended()
    }

    private suspend fun updateBalanceSuspended() = suspendCancellableCoroutine<Unit> { continuation ->
        Purchases.sharedInstance.getVirtualCurrencies(
            onError = { error ->
                continuation.resume(Unit)
            },
            onSuccess = { currencies ->
                _currencyBalances.value = currencies.all.mapValues { it.value.balance }
                continuation.resume(Unit)
            }
        )
    }

    private fun updateBalance() {
        Purchases.sharedInstance.getVirtualCurrencies(
            onError = { /* Log error */ },
            onSuccess = { currencies ->
                _currencyBalances.value = currencies.all.mapValues { it.value.balance }
            }
        )
    }
}
