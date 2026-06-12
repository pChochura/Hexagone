package com.pointlessgames.hexagone.data.di

import com.pointlessgames.hexagone.BuildKonfig
import com.pointlessgames.hexagone.achievements.AchievementManager
import com.pointlessgames.hexagone.achievements.LocalAchievementManager
import com.pointlessgames.hexagone.data.DataStoreSettingsRepository
import com.pointlessgames.hexagone.data.LeaderboardRepository
import com.pointlessgames.hexagone.data.MonetizationRepository
import com.pointlessgames.hexagone.data.SettingsRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal val dataModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_KEY,
        ) {
            install(Postgrest)
            install(Auth)
        }
    }
    single<SettingsRepository> { DataStoreSettingsRepository(get()) }
    singleOf(::LeaderboardRepository)
    singleOf(::MonetizationRepository)
    single<AchievementManager> { LocalAchievementManager(get()) }
}
