package com.pointlessgames.hexagone.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.data.SettingsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val supabaseClient: SupabaseClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    data class UiState(
        val nickname: String = "",
        val originalNickname: String = "",
        val isAnonymous: Boolean = true,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isLoggedOut: Boolean = false,
    )

    init {
        loadAccountInfo()
    }

    private fun loadAccountInfo() {
        viewModelScope.launch {
            val name = settingsRepository.getPlayerName() ?: ""
            val user = supabaseClient.auth.currentUserOrNull()
            _uiState.value = _uiState.value.copy(
                nickname = name,
                originalNickname = name,
                isAnonymous = user?.isAnonymous ?: true
            )
        }
    }

    fun onNicknameChanged(name: String) {
        _uiState.value = _uiState.value.copy(nickname = name, error = null)
    }

    fun updateNickname() {
        val newName = _uiState.value.nickname.trim()
        if (newName.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Nickname cannot be empty")
            return
        }
        if (newName == _uiState.value.originalNickname) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                leaderboardRepository.createProfile(newName, settingsRepository.getPlayerRegion() ?: "Global")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    originalNickname = newName,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                supabaseClient.auth.signOut()
                settingsRepository.setPlayerId(null)
                settingsRepository.setPlayerName(null)
                _uiState.value = _uiState.value.copy(isLoading = false, isLoggedOut = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun removeAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // In a real app, you would call a Supabase Function or Auth Admin API to delete the user.
                // For this implementation, we clear local data and sign out.
                supabaseClient.auth.signOut()
                settingsRepository.setPlayerId(null)
                settingsRepository.setPlayerName(null)
                settingsRepository.setBestScore(0)
                // Additional cleanup could be done here (e.g. clearing game state)
                _uiState.value = _uiState.value.copy(isLoading = false, isLoggedOut = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
