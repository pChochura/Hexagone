package com.pointlessgames.hexagone.utils

import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.di.initKoin
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object IosBackgroundSync : KoinComponent {
    private val leaderboardRepository: LeaderboardRepository by inject()
    private val scope = MainScope()

    private var initialized = false

    fun initializeKoin() {
        if (!initialized) {
            try {
                initKoin()
            } catch (e: Exception) {
                // Already initialized or failed
            }
            initialized = true
        }
    }

    fun sync(completion: (Boolean) -> Unit) {
        initializeKoin()
        scope.launch {
            try {
                leaderboardRepository.syncPendingScores()
                completion(true)
            } catch (e: Exception) {
                completion(false)
            }
        }
    }
}
