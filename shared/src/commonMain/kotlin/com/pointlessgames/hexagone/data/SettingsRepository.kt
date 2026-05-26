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
}
