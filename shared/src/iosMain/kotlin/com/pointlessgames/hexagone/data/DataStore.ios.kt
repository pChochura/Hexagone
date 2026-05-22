package com.pointlessgames.hexagone.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.pointlessgames.hexagone.utils.documentDirectory
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath

@OptIn(ExperimentalForeignApi::class)
internal fun createDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    produceFile = { (documentDirectory() + "/data_store.preferences_pb").toPath() },
)
