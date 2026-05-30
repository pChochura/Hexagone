package com.pointlessgames.hexagone.leaderboard.di

import com.pointlessgames.hexagone.leaderboard.LeaderboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val leaderboardModule = module {
    viewModelOf(::LeaderboardViewModel)
}
