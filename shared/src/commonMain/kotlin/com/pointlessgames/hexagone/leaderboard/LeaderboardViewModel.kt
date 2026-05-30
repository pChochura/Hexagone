package com.pointlessgames.hexagone.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.data.SettingsRepository
import com.pointlessgames.hexagone.game.model.DetailedGameResult
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
    )

    enum class Filter {
        Global, Regional
    }

    init {
        loadPlayerRegion()
        loadRankings()
    }

    private fun loadPlayerRegion() {
        viewModelScope.launch {
            val region = settingsRepository.getPlayerRegion()
            _uiState.value = _uiState.value.copy(playerRegion = region)
        }
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
