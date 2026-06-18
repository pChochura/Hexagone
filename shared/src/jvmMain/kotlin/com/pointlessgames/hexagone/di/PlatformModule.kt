package com.pointlessgames.hexagone.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.pointlessgames.hexagone.billing.BillingManager
import com.pointlessgames.hexagone.billing.RevenueCatBillingManager
import com.pointlessgames.hexagone.data.createDataStore
import com.pointlessgames.hexagone.share.JvmShareManager
import com.pointlessgames.hexagone.share.ShareManager
import org.koin.dsl.module

actual val platformModule = module {
    single<DataStore<Preferences>> { createDataStore() }
    single<BillingManager> { RevenueCatBillingManager() }
    single<ShareManager> { JvmShareManager() }
}
