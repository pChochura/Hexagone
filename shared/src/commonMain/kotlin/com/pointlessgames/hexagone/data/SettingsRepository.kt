package com.pointlessgames.hexagone.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class SettingsRepository(
    private val appSettings: DataStore<Preferences>,
) {
    private val bestScoreKey = intPreferencesKey("best_score")
    private val mergeHintsEnabledKey = booleanPreferencesKey("merge_hints_enabled")
    private val gameStateKey = androidx.datastore.preferences.core.stringPreferencesKey("game_state")
    private val playerIdKey = androidx.datastore.preferences.core.stringPreferencesKey("player_id")
    private val playerNameKey = androidx.datastore.preferences.core.stringPreferencesKey("player_name")
    private val playerRegionKey = androidx.datastore.preferences.core.stringPreferencesKey("player_region")

    suspend fun getBestScore(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[bestScoreKey] ?: 0
    }

    suspend fun setBestScore(score: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[bestScoreKey] = score
            }
        }
    }

    suspend fun getPlayerId(): String? = withContext(Dispatchers.IO) {
        appSettings.data.first()[playerIdKey]
    }

    suspend fun setPlayerId(id: String) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[playerIdKey] = id
            }
        }
    }

    suspend fun getPlayerName(): String? = withContext(Dispatchers.IO) {
        appSettings.data.first()[playerNameKey]
    }

    suspend fun setPlayerName(name: String) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[playerNameKey] = name
            }
        }
    }

    suspend fun getPlayerRegion(): String? = withContext(Dispatchers.IO) {
        appSettings.data.first()[playerRegionKey]
    }

    suspend fun setPlayerRegion(region: String) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[playerRegionKey] = region
            }
        }
    }

    suspend fun getMergeHintsEnabled(): Boolean = withContext(Dispatchers.IO) {
        appSettings.data.first()[mergeHintsEnabledKey] ?: true
    }

    suspend fun setMergeHintsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[mergeHintsEnabledKey] = enabled
            }
        }
    }

    suspend fun getGameState(): String? = withContext(Dispatchers.IO) {
        appSettings.data.first()[gameStateKey]
    }

    suspend fun setGameState(state: String?) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                if (state == null) {
                    prefs.remove(gameStateKey)
                } else {
                    prefs[gameStateKey] = state
                }
            }
        }
    }
}
