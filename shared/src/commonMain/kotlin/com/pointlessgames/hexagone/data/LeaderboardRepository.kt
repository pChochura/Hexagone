package com.pointlessgames.hexagone.data

import com.pointlessgames.hexagone.game.model.DetailedGameResult
import com.pointlessgames.hexagone.game.model.PlayerProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class LeaderboardRepository(
    private val supabase: SupabaseClient,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun submitResult(result: DetailedGameResult) = withContext(Dispatchers.IO) {
        val playerId = settingsRepository.getPlayerId() ?: return@withContext
        val username = settingsRepository.getPlayerName() ?: "Unknown"
        val region = settingsRepository.getPlayerRegion() ?: "Global"
        
        val finalResult = result.copy(
            profileId = playerId,
            username = username,
            region = region
        )
        
        supabase.from("game_results").insert(finalResult)
        
        // Update profile best score if needed
        val currentBest = settingsRepository.getBestScore()
        if (result.score >= currentBest) {
            supabase.from("profiles").update(mapOf("best_score" to result.score)) {
                filter {
                    eq("id", playerId)
                }
            }
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
        val playerId = settingsRepository.getPlayerId() ?: com.pointlessgames.hexagone.utils.generateUUID().also {
            settingsRepository.setPlayerId(it)
        }
        
        val profile = PlayerProfile(id = playerId, username = username, region = region)
        supabase.from("profiles").upsert(profile)
        settingsRepository.setPlayerName(username)
        settingsRepository.setPlayerRegion(region)
        profile
    }
}
