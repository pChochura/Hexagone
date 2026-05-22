package com.pointlessgames.hexagone.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

internal fun createDataStore(
    context: Context,
): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    produceFile = {
        context.filesDir.resolve("data_store.preferences_pb").absolutePath.toPath()
    },
)
