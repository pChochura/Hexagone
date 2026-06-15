package com.pointlessgames.hexagone.data

import com.pointlessgames.hexagone.game.model.DetailedGameResult
import com.pointlessgames.hexagone.game.model.PlayerProfile
import com.pointlessgames.hexagone.game.model.RankingInfo
import com.pointlessgames.hexagone.utils.generateUUID
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
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

    suspend fun submitResult(result: DetailedGameResult): RankingInfo? = withContext(Dispatchers.IO) {
        val playerId = settingsRepository.getPlayerId() ?: return@withContext null
        val username = settingsRepository.getPlayerName() ?: "Unknown"
        val region = settingsRepository.getPlayerRegion() ?: "Global"
        
        val finalResult = result.copy(
            profileId = playerId,
            username = username,
            region = region
        )
        
        try {
            // Convert to JSON and remove fields that might not exist in the DB schema yet
            val jsonElement = json.encodeToJsonElement(finalResult).jsonObject.toMutableMap()
            jsonElement.remove("daily_challenges")
            
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

            getBestRank(result.score, region)
        } catch (e: Exception) {
            settingsRepository.addPendingScore(json.encodeToString(finalResult))
            null
        }
    }

    suspend fun syncPendingScores() = withContext(Dispatchers.IO) {
        val pending = settingsRepository.getPendingScores()
        if (pending.isEmpty()) return@withContext

        pending.forEach { serialized ->
            try {
                val result = json.decodeFromString<DetailedGameResult>(serialized)
                
                // Same logic as submitResult to ensure compatibility with DB schema
                val jsonElement = json.encodeToJsonElement(result).jsonObject.toMutableMap()
                jsonElement.remove("daily_challenges")
                
                supabase.from("game_results").insert(jsonElement)
                
                val playerId = result.profileId
                if (playerId != null) {
                    val currentBest = settingsRepository.getBestScore()
                    if (result.score >= currentBest) {
                        supabase.from("profiles").update(mapOf("best_score" to result.score)) {
                            filter {
                                eq("id", playerId)
                            }
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

    private suspend fun getBestRank(score: Int, region: String): RankingInfo = withContext(Dispatchers.IO) {
        val globalResult = supabase.from("game_results").select {
            filter {
                gt("score", score)
            }
        }
        // Fallback to list size if count is not available directly
        // In a real app we would use the 'count' parameter in select()
        val gRank = globalResult.decodeList<DetailedGameResult>().size + 1L

        val regionalResult = supabase.from("game_results").select {
            filter {
                eq("region", region)
                gt("score", score)
            }
        }
        val rRank = regionalResult.decodeList<DetailedGameResult>().size + 1L

        if (rRank < gRank && rRank <= 100L) {
            RankingInfo(rRank.toInt(), isRegional = true)
        } else {
            RankingInfo(gRank.toInt(), isRegional = false)
        }
    }

    suspend fun getTopScores(limit: Int = 50, region: String? = null): List<DetailedGameResult> = withContext(Dispatchers.IO) {
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

    suspend fun getPlayerProfile(): PlayerProfile? = withContext(Dispatchers.IO) {
        val playerId = settingsRepository.getPlayerId() ?: return@withContext null
        supabase.from("profiles").select {
            filter {
                eq("id", playerId)
            }
        }.decodeSingleOrNull<PlayerProfile>()
    }

    suspend fun createProfile(username: String, region: String): PlayerProfile = withContext(Dispatchers.IO) {
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
