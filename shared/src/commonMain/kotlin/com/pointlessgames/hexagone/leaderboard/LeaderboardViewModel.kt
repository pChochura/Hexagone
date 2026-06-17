package com.pointlessgames.hexagone.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.game.model.DetailedGameResult
import com.pointlessgames.hexagone.game.model.RankingInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class LeaderboardViewModel(
    private val leaderboardRepository: LeaderboardRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    data class UiState(
        val rankings: List<DetailedGameResult> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val playerName: String? = null,
        val onboardingName: String = "",
        val isCreatingProfile: Boolean = false,
        val currentRank: RankingInfo? = null,
    )

    init {
        loadPlayerInfo()
        syncPendingScores()
        loadRankings()
    }

    private fun syncPendingScores() {
        viewModelScope.launch {
            leaderboardRepository.syncPendingScores()
        }
    }

    private fun loadPlayerInfo() {
        viewModelScope.launch {
            val name = settingsRepository.getPlayerName()
            _uiState.value = _uiState.value.copy(playerName = name)
        }
    }

    fun loadRankings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Try to sync any pending scores first
                leaderboardRepository.syncPendingScores()

                val scores = leaderboardRepository.getTopScores()
                _uiState.value = _uiState.value.copy(rankings = scores, isLoading = false)
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }
}
