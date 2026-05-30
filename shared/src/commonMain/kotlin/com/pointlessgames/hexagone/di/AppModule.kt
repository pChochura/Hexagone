package com.pointlessgames.hexagone.di

import com.pointlessgames.hexagone.data.di.dataModule
import com.pointlessgames.hexagone.game.di.gameModule
import com.pointlessgames.hexagone.leaderboard.di.leaderboardModule
import com.pointlessgames.hexagone.onboarding.di.onboardingModule
import com.pointlessgames.hexagone.start.di.startModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

internal val appModule = module {
    includes(platformModule)
    includes(dataModule)

    includes(onboardingModule)
    includes(leaderboardModule)
    includes(startModule)
    includes(gameModule)
}

fun initKoin(config: KoinAppDeclaration = {}) = startKoin {
    config()
    modules(appModule)
}
