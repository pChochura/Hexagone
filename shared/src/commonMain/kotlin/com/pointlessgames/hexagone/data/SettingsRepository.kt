package com.pointlessgames.hexagone.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface SettingsRepository {
    suspend fun getBestScore(): Int
    suspend fun setBestScore(score: Int): Preferences
    suspend fun getPlayerId(): String?
    suspend fun setPlayerId(id: String?): Preferences
    suspend fun getPlayerName(): String?
    suspend fun setPlayerName(name: String?): Preferences
    suspend fun getPlayerRegion(): String?
    suspend fun setPlayerRegion(region: String?): Preferences
    fun getSoundEnabledFlow(): kotlinx.coroutines.flow.Flow<Boolean>
    suspend fun getSoundEnabled(): Boolean
    suspend fun setSoundEnabled(enabled: Boolean): Preferences
    fun getBgMusicEnabledFlow(): kotlinx.coroutines.flow.Flow<Boolean>
    suspend fun getBgMusicEnabled(): Boolean
    suspend fun setBgMusicEnabled(enabled: Boolean): Preferences
    suspend fun getMergeHintsEnabled(): Boolean
    suspend fun setMergeHintsEnabled(enabled: Boolean): Preferences
    suspend fun getGameState(): String?
    suspend fun setGameState(state: String?): Preferences
    suspend fun getTotalMergesLifetime(): Long
    suspend fun incrementTotalMergesLifetime(): Preferences
    suspend fun getPerksUsedLifetime(): Set<String>
    suspend fun addPerkToLifetime(perkName: String): Preferences
    suspend fun getUnlockedAchievements(): Set<String>
    suspend fun setAchievementUnlocked(achievementId: String): Preferences
    suspend fun getPerksCollectedLifetime(): Int
    suspend fun incrementPerksCollectedLifetime(): Preferences
    suspend fun getGamesFinishedLifetime(): Int
    suspend fun incrementGamesFinishedLifetime(): Preferences
    suspend fun getRerollsLifetime(): Int
    suspend fun incrementRerollsLifetime(): Preferences
    suspend fun getMaxComboLifetime(): Int
    suspend fun updateMaxComboLifetime(value: Int): Preferences
    suspend fun getHighestLevelLifetime(): Int
    suspend fun updateHighestLevelLifetime(value: Int): Preferences
    suspend fun getMaxConsecutiveMergesLifetime(): Int
    suspend fun updateMaxConsecutiveMergesLifetime(value: Int): Preferences
    suspend fun getMaxTacticalMergesLifetime(): Int
    suspend fun updateMaxTacticalMergesLifetime(value: Int): Preferences
    suspend fun getMaxCollectedPerksLifetime(): Int
    suspend fun updateMaxCollectedPerksLifetime(value: Int): Preferences
    suspend fun getMaxConsecutiveUndosLifetime(): Int
    suspend fun updateMaxConsecutiveUndosLifetime(value: Int): Preferences
    suspend fun getMaxTacticalGhostsLifetime(): Int
    suspend fun updateMaxTacticalGhostsLifetime(value: Int): Preferences
    suspend fun getMaxBarRaisedLifetime(): Int
    suspend fun updateMaxBarRaisedLifetime(value: Int): Preferences
    suspend fun getHighestTileValueLifetime(): Int
    suspend fun updateHighestTileValueLifetime(value: Int): Preferences
    suspend fun getPendingScores(): Set<String>
    suspend fun addPendingScore(serializedScore: String): Preferences
    suspend fun removePendingScore(serializedScore: String): Preferences
    suspend fun getLastCompletedChallengeDate(): Long
    suspend fun setLastCompletedChallengeDate(date: Long): Preferences
    suspend fun getCompletedChallengeDates(): Set<String>
    suspend fun addCompletedChallengeDate(dateSeed: String): Preferences
    suspend fun getChallengeStreak(): Int
    suspend fun setChallengeStreak(streak: Int): Preferences
    suspend fun getPersistentCompletedMissionIds(): Set<String>
    suspend fun addPersistentCompletedMissionId(missionId: String): Preferences
    suspend fun clearPersistentCompletedMissionIds(): Preferences
    suspend fun getDailyMissionDate(): Long
    suspend fun setDailyMissionDate(date: Long): Preferences
    suspend fun getHasShownMergeTip(): Boolean
    suspend fun setHasShownMergeTip(shown: Boolean): Preferences
    suspend fun getHasShownPerkTip(): Boolean
    suspend fun setHasShownPerkTip(shown: Boolean): Preferences
    suspend fun getHasShownPostGameTip(): Boolean
    suspend fun setHasShownPostGameTip(shown: Boolean): Preferences
    suspend fun getHasShownDailyChallengeTip(): Boolean
    suspend fun setHasShownDailyChallengeTip(shown: Boolean): Preferences
}

class DataStoreSettingsRepository(
    private val appSettings: DataStore<Preferences>,
) : SettingsRepository {
    private val bestScoreKey = intPreferencesKey("best_score")
    private val soundEnabledKey = booleanPreferencesKey("sound_enabled")
    private val bgMusicEnabledKey = booleanPreferencesKey("bg_music_enabled")
    private val mergeHintsEnabledKey = booleanPreferencesKey("merge_hints_enabled")
    private val gameStateKey = stringPreferencesKey("game_state")
    private val playerIdKey = stringPreferencesKey("player_id")
    private val playerNameKey = stringPreferencesKey("player_name")
    private val playerRegionKey = stringPreferencesKey("player_region")
    private val totalMergesLifetimeKey = longPreferencesKey("total_merges_lifetime")
    private val perksUsedLifetimeKey = stringSetPreferencesKey("perks_used_lifetime")
    private val unlockedAchievementsKey = stringSetPreferencesKey("unlocked_achievements")
    private val perksCollectedLifetimeKey = intPreferencesKey("perks_collected_lifetime")
    private val gamesFinishedLifetimeKey = intPreferencesKey("games_finished_lifetime")
    private val rerollsLifetimeKey = intPreferencesKey("rerolls_lifetime")
    private val maxComboLifetimeKey = intPreferencesKey("max_combo_lifetime")
    private val highestLevelLifetimeKey = intPreferencesKey("highest_level_lifetime")
    private val maxConsecutiveMergesLifetimeKey =
        intPreferencesKey("max_consecutive_merges_lifetime")
    private val maxTacticalMergesLifetimeKey = intPreferencesKey("max_tactical_merges_lifetime")
    private val maxCollectedPerksLifetimeKey = intPreferencesKey("max_collected_perks_lifetime")
    private val maxConsecutiveUndosLifetimeKey = intPreferencesKey("max_consecutive_undos_lifetime")
    private val maxTacticalGhostsLifetimeKey = intPreferencesKey("max_tactical_ghosts_lifetime")
    private val maxBarRaisedLifetimeKey = intPreferencesKey("max_bar_raised_lifetime")
    private val highestTileValueLifetimeKey = intPreferencesKey("highest_tile_value_lifetime")
    private val pendingScoresKey = stringSetPreferencesKey("pending_scores")
    private val lastCompletedChallengeDateKey = longPreferencesKey("last_completed_challenge_date")
    private val completedChallengeDatesKey = stringSetPreferencesKey("completed_challenge_dates")
    private val challengeStreakKey = intPreferencesKey("challenge_streak")
    private val persistentCompletedMissionIdsKey = stringSetPreferencesKey("persistent_completed_mission_ids")
    private val dailyMissionDateKey = longPreferencesKey("daily_mission_date")
    private val hasShownMergeTipKey = booleanPreferencesKey("has_shown_merge_tip")
    private val hasShownPerkTipKey = booleanPreferencesKey("has_shown_perk_tip")
    private val hasShownPostGameTipKey = booleanPreferencesKey("has_shown_post_game_tip")
    private val hasShownDailyChallengeTipKey =
        booleanPreferencesKey("has_shown_daily_challenge_tip")

    override suspend fun getBestScore(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[bestScoreKey] ?: 0
    }

    override suspend fun setBestScore(score: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[bestScoreKey] = score
            }
        }
    }

    override suspend fun getPlayerId(): String? = withContext(Dispatchers.IO) {
        appSettings.data.first()[playerIdKey]
    }

    override suspend fun setPlayerId(id: String?) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                if (id == null) {
                    prefs.remove(playerIdKey)
                } else {
                    prefs[playerIdKey] = id
                }
            }
        }
    }

    override suspend fun getPlayerName(): String? = withContext(Dispatchers.IO) {
        appSettings.data.first()[playerNameKey]
    }

    override suspend fun setPlayerName(name: String?) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                if (name == null) {
                    prefs.remove(playerNameKey)
                } else {
                    prefs[playerNameKey] = name
                }
            }
        }
    }

    override suspend fun getPlayerRegion(): String? = withContext(Dispatchers.IO) {
        appSettings.data.first()[playerRegionKey]
    }

    override suspend fun setPlayerRegion(region: String?) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                if (region == null) {
                    prefs.remove(playerRegionKey)
                } else {
                    prefs[playerRegionKey] = region
                }
            }
        }
    }

    override fun getSoundEnabledFlow(): kotlinx.coroutines.flow.Flow<Boolean> {
        return appSettings.data.map { preferences ->
            preferences[soundEnabledKey] ?: true
        }
    }

    override suspend fun getSoundEnabled(): Boolean = withContext(Dispatchers.IO) {
        appSettings.data.first()[soundEnabledKey] ?: true
    }

    override suspend fun setSoundEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[soundEnabledKey] = enabled
            }
        }
    }

    override fun getBgMusicEnabledFlow(): kotlinx.coroutines.flow.Flow<Boolean> {
        return appSettings.data.map { preferences ->
            preferences[bgMusicEnabledKey] ?: true
        }
    }

    override suspend fun getBgMusicEnabled(): Boolean = withContext(Dispatchers.IO) {
        appSettings.data.first()[bgMusicEnabledKey] ?: true
    }

    override suspend fun setBgMusicEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[bgMusicEnabledKey] = enabled
            }
        }
    }

    override suspend fun getMergeHintsEnabled(): Boolean = withContext(Dispatchers.IO) {
        appSettings.data.first()[mergeHintsEnabledKey] ?: true
    }

    override suspend fun setMergeHintsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[mergeHintsEnabledKey] = enabled
            }
        }
    }

    override suspend fun getGameState(): String? = withContext(Dispatchers.IO) {
        appSettings.data.first()[gameStateKey]
    }

    override suspend fun setGameState(state: String?) = withContext(Dispatchers.IO) {
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

    override suspend fun getTotalMergesLifetime(): Long = withContext(Dispatchers.IO) {
        appSettings.data.first()[totalMergesLifetimeKey] ?: 0L
    }

    override suspend fun incrementTotalMergesLifetime() = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[totalMergesLifetimeKey] ?: 0L
                prefs[totalMergesLifetimeKey] = current + 1
            }
        }
    }

    override suspend fun getPerksUsedLifetime(): Set<String> = withContext(Dispatchers.IO) {
        appSettings.data.first()[perksUsedLifetimeKey] ?: emptySet()
    }

    override suspend fun addPerkToLifetime(perkName: String) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[perksUsedLifetimeKey] ?: emptySet()
                prefs[perksUsedLifetimeKey] = current + perkName
            }
        }
    }

    override suspend fun getUnlockedAchievements(): Set<String> = withContext(Dispatchers.IO) {
        appSettings.data.first()[unlockedAchievementsKey] ?: emptySet()
    }

    override suspend fun setAchievementUnlocked(achievementId: String) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[unlockedAchievementsKey] ?: emptySet()
                prefs[unlockedAchievementsKey] = current + achievementId
            }
        }
    }

    override suspend fun getPerksCollectedLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[perksCollectedLifetimeKey] ?: 0
    }

    override suspend fun incrementPerksCollectedLifetime() = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[perksCollectedLifetimeKey] ?: 0
                prefs[perksCollectedLifetimeKey] = current + 1
            }
        }
    }

    override suspend fun getGamesFinishedLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[gamesFinishedLifetimeKey] ?: 0
    }

    override suspend fun incrementGamesFinishedLifetime() = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[gamesFinishedLifetimeKey] ?: 0
                prefs[gamesFinishedLifetimeKey] = current + 1
            }
        }
    }

    override suspend fun getRerollsLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[rerollsLifetimeKey] ?: 0
    }

    override suspend fun incrementRerollsLifetime() = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[rerollsLifetimeKey] ?: 0
                prefs[rerollsLifetimeKey] = current + 1
            }
        }
    }

    override suspend fun getMaxComboLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[maxComboLifetimeKey] ?: 0
    }

    override suspend fun updateMaxComboLifetime(value: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[maxComboLifetimeKey] ?: 0
                if (value > current) prefs[maxComboLifetimeKey] = value
            }
        }
    }

    override suspend fun getHighestLevelLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[highestLevelLifetimeKey] ?: 1
    }

    override suspend fun updateHighestLevelLifetime(value: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[highestLevelLifetimeKey] ?: 1
                if (value > current) prefs[highestLevelLifetimeKey] = value
            }
        }
    }

    override suspend fun getMaxConsecutiveMergesLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[maxConsecutiveMergesLifetimeKey] ?: 0
    }

    override suspend fun updateMaxConsecutiveMergesLifetime(value: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[maxConsecutiveMergesLifetimeKey] ?: 0
                if (value > current) prefs[maxConsecutiveMergesLifetimeKey] = value
            }
        }
    }

    override suspend fun getMaxTacticalMergesLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[maxTacticalMergesLifetimeKey] ?: 0
    }

    override suspend fun updateMaxTacticalMergesLifetime(value: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[maxTacticalMergesLifetimeKey] ?: 0
                if (value > current) prefs[maxTacticalMergesLifetimeKey] = value
            }
        }
    }

    override suspend fun getMaxCollectedPerksLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[maxCollectedPerksLifetimeKey] ?: 0
    }

    override suspend fun updateMaxCollectedPerksLifetime(value: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[maxCollectedPerksLifetimeKey] ?: 0
                if (value > current) prefs[maxCollectedPerksLifetimeKey] = value
            }
        }
    }

    override suspend fun getMaxConsecutiveUndosLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[maxConsecutiveUndosLifetimeKey] ?: 0
    }

    override suspend fun updateMaxConsecutiveUndosLifetime(value: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[maxConsecutiveUndosLifetimeKey] ?: 0
                if (value > current) prefs[maxConsecutiveUndosLifetimeKey] = value
            }
        }
    }

    override suspend fun getMaxTacticalGhostsLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[maxTacticalGhostsLifetimeKey] ?: 0
    }

    override suspend fun updateMaxTacticalGhostsLifetime(value: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[maxTacticalGhostsLifetimeKey] ?: 0
                if (value > current) prefs[maxTacticalGhostsLifetimeKey] = value
            }
        }
    }

    override suspend fun getMaxBarRaisedLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[maxBarRaisedLifetimeKey] ?: 0
    }

    override suspend fun updateMaxBarRaisedLifetime(value: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[maxBarRaisedLifetimeKey] ?: 0
                if (value > current) prefs[maxBarRaisedLifetimeKey] = value
            }
        }
    }

    override suspend fun getHighestTileValueLifetime(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[highestTileValueLifetimeKey] ?: 1
    }

    override suspend fun updateHighestTileValueLifetime(value: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[highestTileValueLifetimeKey] ?: 1
                if (value > current) prefs[highestTileValueLifetimeKey] = value
            }
        }
    }

    override suspend fun getPendingScores(): Set<String> = withContext(Dispatchers.IO) {
        appSettings.data.first()[pendingScoresKey] ?: emptySet()
    }

    override suspend fun addPendingScore(serializedScore: String) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[pendingScoresKey] ?: emptySet()
                prefs[pendingScoresKey] = current + serializedScore
            }
        }
    }

    override suspend fun removePendingScore(serializedScore: String) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[pendingScoresKey] ?: emptySet()
                prefs[pendingScoresKey] = current - serializedScore
            }
        }
    }

    override suspend fun getLastCompletedChallengeDate(): Long = withContext(Dispatchers.IO) {
        appSettings.data.first()[lastCompletedChallengeDateKey] ?: 0L
    }

    override suspend fun setLastCompletedChallengeDate(date: Long) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[lastCompletedChallengeDateKey] = date
            }
        }
    }

    override suspend fun getCompletedChallengeDates(): Set<String> = withContext(Dispatchers.IO) {
        appSettings.data.first()[completedChallengeDatesKey] ?: emptySet()
    }

    override suspend fun addCompletedChallengeDate(dateSeed: String) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[completedChallengeDatesKey] ?: emptySet()
                // Keep only last 30 entries to prevent bloat
                val next = (current + dateSeed).toList().takeLast(30).toSet()
                prefs[completedChallengeDatesKey] = next
            }
        }
    }

    override suspend fun getChallengeStreak(): Int = withContext(Dispatchers.IO) {
        appSettings.data.first()[challengeStreakKey] ?: 0
    }

    override suspend fun setChallengeStreak(streak: Int) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[challengeStreakKey] = streak
            }
        }
    }

    override suspend fun getPersistentCompletedMissionIds(): Set<String> = withContext(Dispatchers.IO) {
        appSettings.data.first()[persistentCompletedMissionIdsKey] ?: emptySet()
    }

    override suspend fun addPersistentCompletedMissionId(missionId: String) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                val current = prefs[persistentCompletedMissionIdsKey] ?: emptySet()
                prefs[persistentCompletedMissionIdsKey] = current + missionId
            }
        }
    }

    override suspend fun clearPersistentCompletedMissionIds() = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs.remove(persistentCompletedMissionIdsKey)
            }
        }
    }

    override suspend fun getDailyMissionDate(): Long = withContext(Dispatchers.IO) {
        appSettings.data.first()[dailyMissionDateKey] ?: 0L
    }

    override suspend fun setDailyMissionDate(date: Long) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[dailyMissionDateKey] = date
            }
        }
    }

    override suspend fun getHasShownMergeTip(): Boolean = withContext(Dispatchers.IO) {
        appSettings.data.first()[hasShownMergeTipKey] ?: false
    }

    override suspend fun setHasShownMergeTip(shown: Boolean) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[hasShownMergeTipKey] = shown
            }
        }
    }

    override suspend fun getHasShownPerkTip(): Boolean = withContext(Dispatchers.IO) {
        appSettings.data.first()[hasShownPerkTipKey] ?: false
    }

    override suspend fun setHasShownPerkTip(shown: Boolean) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[hasShownPerkTipKey] = shown
            }
        }
    }

    override suspend fun getHasShownPostGameTip(): Boolean = withContext(Dispatchers.IO) {
        appSettings.data.first()[hasShownPostGameTipKey] ?: false
    }

    override suspend fun setHasShownPostGameTip(shown: Boolean) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[hasShownPostGameTipKey] = shown
            }
        }
    }

    override suspend fun getHasShownDailyChallengeTip(): Boolean = withContext(Dispatchers.IO) {
        appSettings.data.first()[hasShownDailyChallengeTipKey] ?: false
    }

    override suspend fun setHasShownDailyChallengeTip(shown: Boolean) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[hasShownDailyChallengeTipKey] = shown
            }
        }
    }
}
