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
        val filter: Filter = Filter.Global,
        val playerRegion: String? = null,
        val error: String? = null,
        val playerName: String? = null,
        val onboardingName: String = "",
        val isCreatingProfile: Boolean = false,
        val pendingResult: DetailedGameResult? = null,
        val currentRank: RankingInfo? = null,
    )

    enum class Filter {
        Global, Regional
    }

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
            val region = settingsRepository.getPlayerRegion()
            val name = settingsRepository.getPlayerName()
            _uiState.value = _uiState.value.copy(playerRegion = region, playerName = name)
        }
    }

    fun onOnboardingNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(onboardingName = name, error = null)
    }

    fun onCreateProfile() {
        val name = _uiState.value.onboardingName.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Username cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingProfile = true, error = null)
            try {
                val profile = leaderboardRepository.createProfile(name, "Global")
                val pendingResult = _uiState.value.pendingResult
                var rank: RankingInfo? = null
                if (pendingResult != null) {
                    rank = leaderboardRepository.submitResult(pendingResult)
                }

                _uiState.value = _uiState.value.copy(
                    isCreatingProfile = false,
                    playerName = profile.username,
                    playerRegion = profile.region,
                    pendingResult = null,
                    currentRank = rank
                )
                loadRankings()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isCreatingProfile = false, error = e.message ?: "Unknown error")
            }
        }
    }

    fun setPendingResult(result: DetailedGameResult?) {
        _uiState.value = _uiState.value.copy(pendingResult = result)
    }

    fun loadRankings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val region = if (_uiState.value.filter == Filter.Regional) {
                    _uiState.value.playerRegion
                } else {
                    null
                }
                val scores = leaderboardRepository.getTopScores(region = region)
                _uiState.value = _uiState.value.copy(rankings = scores, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }

    fun onFilterChanged(filter: Filter) {
        if (_uiState.value.filter != filter) {
            _uiState.value = _uiState.value.copy(filter = filter)
            loadRankings()
        }
    }
}
