package com.pointlessgames.hexagone.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.pointlessgames.hexagone.billing.BillingManager
import com.pointlessgames.hexagone.billing.RevenueCatBillingManager
import com.pointlessgames.hexagone.data.createDataStore
import com.pointlessgames.hexagone.haptic.AndroidHapticManager
import com.pointlessgames.hexagone.haptic.HapticManager
import com.pointlessgames.hexagone.share.AndroidShareManager
import com.pointlessgames.hexagone.share.ShareManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<DataStore<Preferences>> { createDataStore(androidContext()) }
    single<BillingManager> { RevenueCatBillingManager() }
    single<ShareManager> { AndroidShareManager(androidContext()) }
    single<HapticManager> { AndroidHapticManager(androidContext()) }
}
