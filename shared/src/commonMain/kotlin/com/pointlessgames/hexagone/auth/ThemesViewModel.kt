package com.pointlessgames.hexagone.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.achievements.GameAchievement
import com.pointlessgames.hexagone.billing.BillingManager
import com.pointlessgames.hexagone.data.MonetizationRepository
import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.ui.theme.ThemeId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

data class ThemesUiState(
    val activeTheme: ThemeId = ThemeId.NEON_GLOW,
    val unlockedThemes: Set<String> = emptySet(),
    val diamondsBalance: Int = 0,
    val isPurchasing: Boolean = false,
    val purchaseError: String? = null,
    val pendingPurchaseTheme: ThemeId? = null,
    val highestLevel: Int = 1,
    val hasAllAchievements: Boolean = false,
    val dailyMissionStreak: Int = 0,
    val newlyUnlockedTheme: ThemeId? = null,
)


class ThemesViewModel(
    private val settingsRepository: SettingsRepository,
    private val monetizationRepository: MonetizationRepository,
    private val billingManager: BillingManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemesUiState())
    val uiState: StateFlow<ThemesUiState> = combine(
        _uiState,
        settingsRepository.getActiveThemeFlow(),
        settingsRepository.getUnlockedThemesFlow(),
        billingManager.currencyBalances,
    ) { state, activeThemeStr, unlocked, balances ->
        val activeTheme = try {
            ThemeId.valueOf(activeThemeStr)
        } catch (e: Exception) {
            ThemeId.NEON_GLOW
        }
        val diamonds = balances?.get("diamonds") ?: state.diamondsBalance
        state.copy(
            activeTheme = activeTheme,
            unlockedThemes = unlocked,
            diamondsBalance = diamonds,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemesUiState(),
    )

    init {
        viewModelScope.launch {
            val highestLevel = settingsRepository.getHighestLevelLifetime()
            val achievements = settingsRepository.getUnlockedAchievements()
            val completedDates =
                settingsRepository.getCompletedChallengeDates().mapNotNull { it.toLongOrNull() }
                    .toSet()
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val streak = com.pointlessgames.hexagone.game.logic.DailyMissionUtils.calculateStreak(
                completedDates,
                today,
            )

            _uiState.update {
                it.copy(
                    highestLevel = highestLevel,
                    hasAllAchievements = achievements.size >= GameAchievement.entries.size,
                    dailyMissionStreak = streak,
                )
            }
        }
    }

    fun onThemeClicked(themeId: ThemeId, isPremium: Boolean, cost: Int) {
        val unlocked = uiState.value.unlockedThemes.contains(themeId.name)
        if (unlocked || (!isPremium && !hasUnlockCondition(themeId))) {
            viewModelScope.launch {
                settingsRepository.setActiveTheme(themeId.name)
                settingsRepository.unlockTheme(themeId.name) // Ensure it's in the unlocked set
            }
        } else if (hasUnlockCondition(themeId)) {
            val canUnlock = canUnlockTheme(themeId, uiState.value)
            if (canUnlock) {
                viewModelScope.launch {
                    settingsRepository.unlockTheme(themeId.name)
                    settingsRepository.setActiveTheme(themeId.name)
                    _uiState.update { it.copy(newlyUnlockedTheme = themeId) }
                }
            }
        } else {
            _uiState.update { it.copy(pendingPurchaseTheme = themeId) }
        }
    }

    private fun hasUnlockCondition(themeId: ThemeId): Boolean {
        return themeId in listOf(ThemeId.MIDNIGHT, ThemeId.CYBER, ThemeId.BERRY)
    }

    private fun canUnlockTheme(themeId: ThemeId, state: ThemesUiState): Boolean {
        return when (themeId) {
            ThemeId.MIDNIGHT -> state.highestLevel >= 50
            ThemeId.CYBER -> state.hasAllAchievements
            ThemeId.BERRY -> state.dailyMissionStreak >= 10
            else -> false
        }
    }

    fun confirmPurchase(themeId: ThemeId, cost: Int) {
        val diamonds = uiState.value.diamondsBalance
        if (diamonds >= cost) {
            viewModelScope.launch {
                _uiState.update { it.copy(isPurchasing = true, purchaseError = null) }
                val success = monetizationRepository.buyTheme(themeId.name, cost)
                if (success) {
                    settingsRepository.unlockTheme(themeId.name)
                    settingsRepository.setActiveTheme(themeId.name)
                } else {
                    _uiState.update { it.copy(purchaseError = "Purchase failed") }
                }
                _uiState.update { it.copy(isPurchasing = false, pendingPurchaseTheme = null) }
            }
        } else {
            _uiState.update { it.copy(pendingPurchaseTheme = null) } // Can't afford
        }
    }

    fun dismissPurchaseDialog() {
        _uiState.update { it.copy(pendingPurchaseTheme = null, purchaseError = null) }
    }

    fun dismissUnlockedOverlay() {
        _uiState.update { it.copy(newlyUnlockedTheme = null) }
    }

    fun refreshBalance() {
        viewModelScope.launch {
            billingManager.refreshBalance()
        }
    }
}
