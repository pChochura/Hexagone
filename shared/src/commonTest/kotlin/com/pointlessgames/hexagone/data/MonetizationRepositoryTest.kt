package com.pointlessgames.hexagone.data

import androidx.datastore.preferences.core.Preferences
import com.pointlessgames.hexagone.billing.BillingManager
import com.pointlessgames.hexagone.billing.BillingProduct
import com.pointlessgames.hexagone.billing.PurchaseResult
import com.pointlessgames.hexagone.game.logic.PerkCategory
import com.pointlessgames.hexagone.game.model.Perk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

        override suspend fun initialize() {}
        override suspend fun purchase(product: BillingProduct) {}
        
        suspend fun emitSuccess(productId: String) {
            purchaseEvents.emit(PurchaseResult.Success(productId, "token"))
        }
    }

    private class FakeSettingsRepository : SettingsRepository {
        var diamonds = 0
        var bankedPerksJson: String? = null

        override suspend fun getBestScore(): Int = 0
        override suspend fun setBestScore(score: Int): Preferences = error("")
        override suspend fun getPlayerId(): String? = null
        override suspend fun setPlayerId(id: String): Preferences = error("")
        override suspend fun getPlayerName(): String? = null
        override suspend fun setPlayerName(name: String): Preferences = error("")
        override suspend fun getPlayerRegion(): String? = null
        override suspend fun setPlayerRegion(region: String): Preferences = error("")
        override suspend fun getMergeHintsEnabled(): Boolean = true
        override suspend fun setMergeHintsEnabled(enabled: Boolean): Preferences = error("")
        override suspend fun getGameState(): String? = null
        override suspend fun setGameState(state: String?): Preferences = error("")
        override suspend fun getTotalMergesLifetime(): Long = 0
        override suspend fun incrementTotalMergesLifetime(): Preferences = error("")
        override suspend fun getPerksUsedLifetime(): Set<String> = emptySet()
        override suspend fun addPerkToLifetime(perkName: String): Preferences = error("")
        override suspend fun getUnlockedAchievements(): Set<String> = emptySet()
        override suspend fun setAchievementUnlocked(achievementId: String): Preferences = error("")
        override suspend fun getPerksCollectedLifetime(): Int = 0
        override suspend fun incrementPerksCollectedLifetime(): Preferences = error("")
        override suspend fun getGamesFinishedLifetime(): Int = 0
        override suspend fun incrementGamesFinishedLifetime(): Preferences = error("")
        override suspend fun getRerollsLifetime(): Int = 0
        override suspend fun incrementRerollsLifetime(): Preferences = error("")
        override suspend fun getMaxComboLifetime(): Int = 0
        override suspend fun updateMaxComboLifetime(value: Int): Preferences = error("")
        override suspend fun getHighestLevelLifetime(): Int = 1
        override suspend fun updateHighestLevelLifetime(value: Int): Preferences = error("")
        override suspend fun getMaxConsecutiveMergesLifetime(): Int = 0
        override suspend fun updateMaxConsecutiveMergesLifetime(value: Int): Preferences = error("")
        override suspend fun getMaxTacticalMergesLifetime(): Int = 0
        override suspend fun updateMaxTacticalMergesLifetime(value: Int): Preferences = error("")
        override suspend fun getMaxCollectedPerksLifetime(): Int = 0
        override suspend fun updateMaxCollectedPerksLifetime(value: Int): Preferences = error("")
        override suspend fun getMaxConsecutiveUndosLifetime(): Int = 0
        override suspend fun updateMaxConsecutiveUndosLifetime(value: Int): Preferences = error("")
        override suspend fun getMaxTacticalGhostsLifetime(): Int = 0
        override suspend fun updateMaxTacticalGhostsLifetime(value: Int): Preferences = error("")
        override suspend fun getMaxBarRaisedLifetime(): Int = 0
        override suspend fun updateMaxBarRaisedLifetime(value: Int): Preferences = error("")
        override suspend fun getHighestTileValueLifetime(): Int = 1
        override suspend fun updateHighestTileValueLifetime(value: Int): Preferences = error("")
        override suspend fun getPendingScores(): Set<String> = emptySet()
        override suspend fun addPendingScore(serializedScore: String): Preferences = error("")
        override suspend fun removePendingScore(serializedScore: String): Preferences = error("")
        override suspend fun getLastCompletedChallengeDate(): Long = 0
        override suspend fun setLastCompletedChallengeDate(date: Long): Preferences = error("")
        override suspend fun getCompletedChallengeDates(): Set<String> = emptySet()
        override suspend fun addCompletedChallengeDate(dateSeed: String): Preferences = error("")
        override suspend fun getChallengeStreak(): Int = 0
        override suspend fun setChallengeStreak(streak: Int): Preferences = error("")

        override suspend fun getDiamonds(): Int = diamonds
        override suspend fun setDiamonds(diamonds: Int) {
            this.diamonds = diamonds
        }

        override suspend fun getBankedPerks(): String? = bankedPerksJson
        override suspend fun setBankedPerks(perksJson: String) {
            this.bankedPerksJson = perksJson
        }

        override suspend fun getHasShownMergeTip(): Boolean = true
        override suspend fun setHasShownMergeTip(shown: Boolean): Preferences = error("")
        override suspend fun getHasShownPerkTip(): Boolean = true
        override suspend fun setHasShownPerkTip(shown: Boolean): Preferences = error("")
        override suspend fun getHasShownPostGameTip(): Boolean = true
        override suspend fun setHasShownPostGameTip(shown: Boolean): Preferences = error("")
        override suspend fun getHasShownDailyChallengeTip(): Boolean = true
        override suspend fun setHasShownDailyChallengeTip(shown: Boolean): Preferences = error("")
    }

    @Test
    fun testHandleDiamondPurchase() = runTest {
        val billingManager = FakeBillingManager()
        val settingsRepository = FakeSettingsRepository()
        val repository = MonetizationRepository(billingManager, settingsRepository)

        billingManager.emitSuccess("diamonds_100")
        assertEquals(100, settingsRepository.diamonds)

        billingManager.emitSuccess("diamonds_500")
        assertEquals(600, settingsRepository.diamonds)
    }

    @Test
    fun testHandlePerkBundlePurchase() = runTest {
        val billingManager = FakeBillingManager()
        val settingsRepository = FakeSettingsRepository()
        val repository = MonetizationRepository(billingManager, settingsRepository)

        billingManager.emitSuccess("perk_legendary_1")
        
        val bankedPerks = settingsRepository.bankedPerksJson?.let {
            Json.decodeFromString<Map<Perk, Int>>(it)
        } ?: emptyMap()

        assertEquals(1, bankedPerks.values.sum())
        assertTrue(bankedPerks.keys.any { it.isLegendary })
    }

    @Test
    fun testBuyRandomPerk() = runTest {
        val billingManager = FakeBillingManager()
        val settingsRepository = FakeSettingsRepository()
        val repository = MonetizationRepository(billingManager, settingsRepository)

        settingsRepository.diamonds = 100
        val perk = repository.buyRandomPerk(PerkCategory.COMMON)

        assertEquals(50, settingsRepository.diamonds)
        val bankedPerks = settingsRepository.bankedPerksJson?.let {
            Json.decodeFromString<Map<Perk, Int>>(it)
        } ?: emptyMap()
        assertEquals(1, bankedPerks[perk])
    }
}
