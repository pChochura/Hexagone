package com.pointlessgames.hexagone.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.pointlessgames.hexagone.achievements.AchievementManager
import com.pointlessgames.hexagone.achievements.LocalAchievementManager
import com.pointlessgames.hexagone.data.createDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<DataStore<Preferences>> { createDataStore(androidContext()) }
    single<AchievementManager> { LocalAchievementManager(get()) }
}
