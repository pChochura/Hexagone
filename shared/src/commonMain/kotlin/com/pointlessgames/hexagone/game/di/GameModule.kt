package com.pointlessgames.hexagone.game.di

import com.pointlessgames.hexagone.Route
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.ui.GameScreen
import com.pointlessgames.hexagone.game.ui.AchievementsScreen
import com.pointlessgames.hexagone.game.ui.DailyMissionsScreen
import com.pointlessgames.hexagone.game.ui.LeaderboardScreen
import com.pointlessgames.hexagone.game.ui.ShopScreen
import com.pointlessgames.hexagone.leaderboard.LeaderboardViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val gameModule = module {
    viewModelOf(::GameViewModel)

    navigation<Route.Game> {
        GameScreen(
            viewModel = koinViewModel(),
        )
    }

    navigation<Route.Shop> {
        ShopScreen(
            viewModel = koinViewModel(),
        )
    }

    navigation<Route.Leaderboard> {
        LeaderboardScreen(
            viewModel = koinViewModel(),
        )
    }

    navigation<Route.DailyMissions> {
        DailyMissionsScreen(
            viewModel = koinViewModel(),
        )
    }

    navigation<Route.Achievements> { route ->
        val viewModel: GameViewModel = koinViewModel()
        AchievementsScreen(
            achievementManager = viewModel.getAchievementManager(),
            initialAchievementId = route.initialAchievementId,
        )
    }
}
