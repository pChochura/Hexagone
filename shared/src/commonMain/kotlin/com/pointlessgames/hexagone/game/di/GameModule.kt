package com.pointlessgames.hexagone.game.di

import com.pointlessgames.hexagone.Route
import com.pointlessgames.hexagone.game.GameViewModel
import com.pointlessgames.hexagone.game.ui.GameScreen
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
}
