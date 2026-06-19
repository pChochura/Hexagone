package com.pointlessgames.hexagone.game.di

import com.pointlessgames.hexagone.Route
import com.pointlessgames.hexagone.auth.SettingsViewModel
import com.pointlessgames.hexagone.auth.ThemesViewModel
import com.pointlessgames.hexagone.auth.ui.SettingsScreen
import com.pointlessgames.hexagone.auth.ui.ThemesScreen
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.ui.AchievementsScreen
import com.pointlessgames.hexagone.game.ui.DailyMissionsScreen
import com.pointlessgames.hexagone.game.ui.GameScreen
import com.pointlessgames.hexagone.game.ui.ShopScreen
import com.pointlessgames.hexagone.leaderboard.ui.LeaderboardScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val gameModule = module {
    viewModelOf(::GameViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ThemesViewModel)

    navigation<Route.Settings> {
        SettingsScreen(
            viewModel = koinViewModel(),
        )
    }

    navigation<Route.Themes> {
        ThemesScreen(
            viewModel = koinViewModel(),
        )
    }

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

    navigation<Route.Leaderboard> { route ->
        LeaderboardScreen(
            viewModel = koinViewModel(),
            targetRank = route.targetRank,
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
