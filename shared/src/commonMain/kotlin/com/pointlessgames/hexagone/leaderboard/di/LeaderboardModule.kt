package com.pointlessgames.hexagone.leaderboard.di

import com.pointlessgames.hexagone.Route
import com.pointlessgames.hexagone.leaderboard.LeaderboardViewModel
import com.pointlessgames.hexagone.leaderboard.ui.LeaderboardScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val leaderboardModule = module {
    viewModelOf(::LeaderboardViewModel)

    navigation<Route.Leaderboard> {
        LeaderboardScreen(
            viewModel = koinViewModel(),
        )
    }
}
