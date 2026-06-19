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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.launch

internal class LeaderboardViewModel(
    private val leaderboardRepository: LeaderboardRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    data class UiState(
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

    fun getRankingsFlow(targetRank: Int?): Flow<PagingData<com.pointlessgames.hexagone.data.RankedGameResult>> {
        val initialPage = if (targetRank != null && targetRank > 0) {
            (targetRank - 1) / 20
        } else {
            0
        }
        
        // Sync pending scores, then fetch the pager
        viewModelScope.launch {
            try {
                leaderboardRepository.syncPendingScores()
            } catch (e: Exception) {
                // Ignore sync errors here
            }
        }
        
        return leaderboardRepository.getLeaderboardPager(pageSize = 20, initialPage = initialPage)
            .flow
            .cachedIn(viewModelScope)
    }
}
