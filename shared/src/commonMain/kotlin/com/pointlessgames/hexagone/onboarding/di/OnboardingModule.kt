package com.pointlessgames.hexagone.onboarding.di

import com.pointlessgames.hexagone.Route
import com.pointlessgames.hexagone.onboarding.OnboardingViewModel
import com.pointlessgames.hexagone.onboarding.ui.OnboardingScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
internal val onboardingModule = module {
    viewModelOf(::OnboardingViewModel)

    navigation<Route.Onboarding> {
        OnboardingScreen(
            viewModel = koinViewModel(),
        )
    }
}
