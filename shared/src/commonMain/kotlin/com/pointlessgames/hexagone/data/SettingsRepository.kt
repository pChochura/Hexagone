package com.pointlessgames.hexagone.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Clock

class SettingsRepository(
    private val appSettings: DataStore<Preferences>,
) {
    private val lastHintsUsedKey = stringSetPreferencesKey("last_hints_used")
    private val tutorialFinishedKey = booleanPreferencesKey("tutorial_finished")
    private val bestScoreKey = androidx.datastore.preferences.core.intPreferencesKey("best_score")
    private val dailyChallengeLevelsFinishedKey =
        stringSetPreferencesKey("daily_challenge_levels_finished")

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

    suspend fun addDailyChallengeLevelFinished(id: Long) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[dailyChallengeLevelsFinishedKey] =
                    prefs[dailyChallengeLevelsFinishedKey].orEmpty() + id.toString()
            }
        }
    }

    suspend fun areDailyChallengeLevelsFinished(ids: List<Long>): Map<Long, Boolean> =
        withContext(Dispatchers.IO) {
            val finishedIds = appSettings.data.first()[dailyChallengeLevelsFinishedKey]
                ?: return@withContext emptyMap()

            ids.associateWith { it.toString() in finishedIds }
        }

    suspend fun setTutorialFinished() = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[tutorialFinishedKey] = true
            }
        }
    }

    suspend fun isTutorialFinished(): Boolean = withContext(Dispatchers.IO) {
        appSettings.data.first()[tutorialFinishedKey] ?: false
    }

    suspend fun addLastHintUsed(
        timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    ) = withContext(Dispatchers.IO) {
        appSettings.updateData {
            it.toMutablePreferences().also { prefs ->
                prefs[lastHintsUsedKey] =
                    (prefs[lastHintsUsedKey].orEmpty() + timestamp.toString()).trimOldest(10)
            }
        }
    }

    suspend fun getCooldownUntilNextHint(): Long = withContext(Dispatchers.IO) {
        val currentTimestamp = Clock.System.now().toEpochMilliseconds()
        val windowDuration = (currentTimestamp - MAX_COOLDOWN)..currentTimestamp
        val timestamps = appSettings.data.first()[lastHintsUsedKey]
            .orEmpty()
            .map { it.toLong() }
            .filter { it in windowDuration }
            .sorted()

        if (timestamps.isEmpty()) {
            return@withContext 0L
        }

        val cooldownDuration = (BASE_COOLDOWN * 2.0.pow(timestamps.size - 1)).toLong()
        return@withContext max(0L, timestamps.last() + cooldownDuration - currentTimestamp)
    }

    private fun Set<String>.trimOldest(limit: Int = 10) =
        this.sortedDescending().take(limit).toSet()

    private companion object {
        const val BASE_COOLDOWN = 5 * 1000L // 5 seconds
        const val MAX_COOLDOWN = 5 * 60 * 1000L // 5 minutes
    }
}
