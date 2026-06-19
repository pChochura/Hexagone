package com.pointlessgames.hexagone.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.data.SettingsRepository
import hexagone.shared.generated.resources.Res
import hexagone.shared.generated.resources.onboarding_nickname_empty
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

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
        val showNicknamePopup: Boolean = false,
        val isSoundEnabled: Boolean = true,
        val isHapticsEnabled: Boolean = true,
        val isBgMusicEnabled: Boolean = true,
        val activeTheme: String = "",
    )

    init {
        loadAccountInfo()
        viewModelScope.launch {
            settingsRepository.getActiveThemeFlow().collect { themeId ->
                _uiState.value = _uiState.value.copy(activeTheme = themeId)
            }
        }
    }

    fun loadAccountInfo() {
        viewModelScope.launch {
            val name = settingsRepository.getPlayerName() ?: ""
            val user = supabaseClient.auth.currentUserOrNull()
            val soundEnabled = settingsRepository.getSoundEnabled()
            val hapticsEnabled = settingsRepository.getHapticsEnabled()
            val bgMusicEnabled = settingsRepository.getBgMusicEnabled()
            _uiState.value = _uiState.value.copy(
                nickname = name,
                originalNickname = name,
                isAnonymous = user?.isAnonymous ?: true,
                isLoggedOut = false,
                isSoundEnabled = soundEnabled,
                isHapticsEnabled = hapticsEnabled,
                isBgMusicEnabled = bgMusicEnabled,
            )
        }
    }

    fun consumeLoggedOut() {
        _uiState.value = _uiState.value.copy(isLoggedOut = false)
    }

    fun toggleSound() {
        viewModelScope.launch {
            val newState = !_uiState.value.isSoundEnabled
            settingsRepository.setSoundEnabled(newState)
            _uiState.value = _uiState.value.copy(isSoundEnabled = newState)
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            val newState = !_uiState.value.isHapticsEnabled
            settingsRepository.setHapticsEnabled(newState)
            _uiState.value = _uiState.value.copy(isHapticsEnabled = newState)
        }
    }

    fun toggleBgMusic() {
        viewModelScope.launch {
            val newState = !_uiState.value.isBgMusicEnabled
            settingsRepository.setBgMusicEnabled(newState)
            _uiState.value = _uiState.value.copy(isBgMusicEnabled = newState)
        }
    }

    fun onNicknameChanged(name: String) {
        _uiState.value = _uiState.value.copy(nickname = name, error = null)
    }

    fun onShowNicknamePopup() {
        _uiState.value = _uiState.value.copy(showNicknamePopup = true)
    }

    fun onDismissNicknamePopup() {
        _uiState.value = _uiState.value.copy(
            showNicknamePopup = false,
            nickname = _uiState.value.originalNickname,
            error = null,
        )
    }

    fun updateNickname() {
        val newName = _uiState.value.nickname.trim()
        if (newName.isEmpty()) {
            viewModelScope.launch {
                _uiState.value =
                    _uiState.value.copy(error = getString(Res.string.onboarding_nickname_empty))
            }
            return
        }
        if (newName == _uiState.value.originalNickname) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                leaderboardRepository.createProfile(newName)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    originalNickname = newName,
                    error = null,
                    showNicknamePopup = false,
                )
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
                settingsRepository.clear()
                _uiState.value = _uiState.value.copy(isLoading = false, isLoggedOut = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }
}
