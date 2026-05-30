package com.pointlessgames.hexagone.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.data.LeaderboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class OnboardingViewModel(
    private val leaderboardRepository: LeaderboardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    data class UiState(
        val username: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val isProfileCreated: Boolean = false,
    )

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username, error = null)
    }

    fun onCreateProfile() {
        val username = _uiState.value.username.trim()
        if (username.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Username cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Using "Global" as a placeholder region for now.
                leaderboardRepository.createProfile(username, "Global")
                _uiState.value = _uiState.value.copy(isLoading = false, isProfileCreated = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }
}
