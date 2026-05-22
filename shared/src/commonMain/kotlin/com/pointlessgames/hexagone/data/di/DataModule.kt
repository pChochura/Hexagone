package com.pointlessgames.hexagone.data.di

import com.pointlessgames.hexagone.data.SettingsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val dataModule = module {
    singleOf(::SettingsRepository)
}
