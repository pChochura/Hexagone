package com.pointlessgames.hexagone.data

import com.pointlessgames.hexagone.game.model.DetailedGameResult
import com.pointlessgames.hexagone.game.model.PlayerProfile
import com.pointlessgames.hexagone.game.model.RankingInfo
import com.pointlessgames.hexagone.utils.generateUUID
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

class LeaderboardRepository(
    private val supabase: SupabaseClient,
    private val settingsRepository: SettingsRepository,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun submitResult(result: DetailedGameResult): RankingInfo? =
        withContext(Dispatchers.IO) {
            val playerId = settingsRepository.getPlayerId() ?: return@withContext null
            val username = settingsRepository.getPlayerName() ?: "Unknown"
            val region = settingsRepository.getPlayerRegion() ?: "Global"

            val finalResult = result.copy(
                profileId = playerId,
                username = username,
                region = region,
            )

            try {
                val jsonElement = json.encodeToJsonElement(finalResult).jsonObject.toMutableMap()

                supabase.from("game_results").insert(jsonElement)

                // Update profile best score if needed
                val currentBest = settingsRepository.getBestScore()
                if (result.score >= currentBest) {
                    supabase.from("profiles").update(mapOf("best_score" to result.score)) {
                        filter {
                            eq("id", playerId)
                        }
                    }
                }

                getBestRank(playerId, result.score)
            } catch (e: Exception) {
                println("Failed to submit result: ${e.message}")
                settingsRepository.addPendingScore(json.encodeToString(finalResult))
                null
            }
        }

    suspend fun syncPendingScores() = withContext(Dispatchers.IO) {
        val pending = settingsRepository.getPendingScores()
        if (pending.isEmpty()) return@withContext

        val currentPlayerId = settingsRepository.getPlayerId()
        val currentUsername = settingsRepository.getPlayerName()
        val currentRegion = settingsRepository.getPlayerRegion() ?: "Global"

        pending.forEach { serialized ->
            try {
                val result = json.decodeFromString<DetailedGameResult>(serialized)

                // Patch missing info from current settings if needed
                val finalResult = result.copy(
                    profileId = result.profileId ?: currentPlayerId,
                    username = result.username ?: currentUsername,
                    region = if (result.region == "Global" && currentRegion != "Global") currentRegion else result.region,
                )

                // Ensure we have at least an ID and username before uploading
                if (finalResult.profileId == null || finalResult.username == null) {
                    return@forEach
                }

                // Same logic as submitResult to ensure compatibility with DB schema
                val jsonElement = json.encodeToJsonElement(finalResult).jsonObject.toMutableMap()

                supabase.from("game_results").insert(jsonElement)

                val playerId = finalResult.profileId
                val currentBest = settingsRepository.getBestScore()
                if (finalResult.score >= currentBest) {
                    supabase.from("profiles").update(mapOf("best_score" to finalResult.score)) {
                        filter {
                            eq("id", playerId)
                        }
                    }
                }

                settingsRepository.removePendingScore(serialized)
            } catch (e: Exception) {
                // If it's a serialization error, we might want to remove it to avoid blocking the queue
                if (e is kotlinx.serialization.SerializationException) {
                    settingsRepository.removePendingScore(serialized)
                }
                // Otherwise (network error), keep it for next time
            }
        }
    }

    private suspend fun getBestRank(profileId: String, score: Int): RankingInfo =
        withContext(Dispatchers.IO) {
            val row = supabase
                .from("game_results_score_positions")
                .select(Columns.raw("score_position")) {
                    filter {
                        eq("profile_id", profileId)
                        eq("score", score)
                    }
                    order("score_position", Order.ASCENDING)
                    limit(1)
                }
                .decodeList<RankingInfo.RankRow>()
                .first()

            RankingInfo(rank = row.scorePosition, isRegional = false)
        }

    suspend fun getTopScores(limit: Int = 50, region: String? = null): List<DetailedGameResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val query = supabase.from("game_results").select() {
                    if (region != null) {
                        filter {
                            eq("region", region)
                        }
                    }
                    order("score", Order.DESCENDING)
                    limit(limit.toLong())
                }
                query.decodeList<DetailedGameResult>()
            }
                .onFailure {
                    it.printStackTrace()
                }
                .getOrNull()!!
        }

    suspend fun createProfile(username: String, region: String): PlayerProfile =
        withContext(Dispatchers.IO) {
            // For KMP, we might use a simple device ID or Supabase Auth.
            // For now, let's assume we use a generated UUID if not using Auth.
            val playerId = settingsRepository.getPlayerId() ?: generateUUID().also {
                settingsRepository.setPlayerId(it)
            }

            val profile = PlayerProfile(id = playerId, username = username, region = region)
            supabase.from("profiles").upsert(profile)
            settingsRepository.setPlayerName(username)
            settingsRepository.setPlayerRegion(region)
            profile
        }
}
