package com.pointlessgames.hexagone.data

import com.pointlessgames.hexagone.billing.BillingManager
import com.pointlessgames.hexagone.billing.BillingProduct
import com.pointlessgames.hexagone.billing.PurchaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MonetizationRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeBillingManager : BillingManager {
        override val products = MutableStateFlow<List<BillingProduct>>(emptyList())
        override val purchaseEvents = MutableSharedFlow<PurchaseResult>()
        override val currencyBalances = MutableStateFlow<Map<String, Int>>(emptyMap())
        override val isInitializing = MutableStateFlow(false)
        override var appUserId: String? = "test_user"

        override suspend fun initialize() {}
        override suspend fun purchase(product: BillingProduct) {}
        override suspend fun refreshBalance() {}
        
        fun setBalance(key: String, amount: Int) {
            val current = currencyBalances.value.toMutableMap()
            current[key] = amount
            currencyBalances.value = current
        }
    }

    @Test
    fun testHandleDiamondPurchase() = runTest {
        val billingManager = FakeBillingManager()
        val repository = MonetizationRepository(billingManager, createFakeSupabase())

        billingManager.setBalance("diamonds", 100)
        assertEquals(100, billingManager.currencyBalances.value["diamonds"])
    }

    private fun createFakeSupabase(): io.github.jan.supabase.SupabaseClient {
        return io.github.jan.supabase.createSupabaseClient("https://fake.url", "fake_key") {}
    }
}
