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
    private val totalMergesLifetimeKey = androidx.datastore.preferences.core.longPreferencesKey("total_merges_lifetime")
    private val perksUsedLifetimeKey = androidx.datastore.preferences.core.stringSetPreferencesKey("perks_used_lifetime")
    private val unlockedAchievementsKey = androidx.datastore.preferences.core.stringSetPreferencesKey("unlocked_achievements")
    private val perksCollectedLifetimeKey = androidx.datastore.preferences.core.intPreferencesKey("perks_collected_lifetime")
    private val gamesFinishedLifetimeKey = androidx.datastore.preferences.core.intPreferencesKey("games_finished_lifetime")

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

    suspend fun getTotalMergesLifetime(): Long = withContext(Dispatchers.IO) {
        appSettings.data.first()[totalMergesLifetimeKey] ?: 0L
    }

    suspend fun incrementTotalMergesLifetime() = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[totalMergesLifetimeKey] ?: 0L
                prefs[totalMergesLifetimeKey] = current + 1
            }
        }
    }

    suspend fun getPerksUsedLifetime(): Set<String> = withContext(Dispatchers.IO) {
        appSettings.data.first()[perksUsedLifetimeKey] ?: emptySet()
    }

    suspend fun addPerkToLifetime(perkName: String) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[perksUsedLifetimeKey] ?: emptySet()
                prefs[perksUsedLifetimeKey] = current + perkName
            }
        }
    }

    suspend fun getUnlockedAchievements(): Set<String> = withContext(Dispatchers.IO) {
        appSettings.data.first()[unlockedAchievementsKey] ?: emptySet()
    }

    suspend fun setAchievementUnlocked(achievementId: String) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[unlockedAchievementsKey] ?: emptySet()
                prefs[unlockedAchievementsKey] = current + achievementId
            }
        }
    }

    suspend fun getPerksCollectedLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[perksCollectedLifetimeKey] ?: 0
    }

    suspend fun incrementPerksCollectedLifetime() = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[perksCollectedLifetimeKey] ?: 0
                prefs[perksCollectedLifetimeKey] = current + 1
            }
        }
    }

    suspend fun getGamesFinishedLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[gamesFinishedLifetimeKey] ?: 0
    }

    suspend fun incrementGamesFinishedLifetime() = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[gamesFinishedLifetimeKey] ?: 0
                prefs[gamesFinishedLifetimeKey] = current + 1
            }
        }
    }
}
