package com.pointlessgames.hexagone.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pointlessgames.hexagone.data.LeaderboardRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LeaderboardSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val leaderboardRepository: LeaderboardRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            leaderboardRepository.syncPendingScores()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
