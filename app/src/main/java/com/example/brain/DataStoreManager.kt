package com.example.brain

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("brain_data")

class DataStoreManager(private val context: Context) {

    companion object {
        val HIGH_LEVEL = intPreferencesKey("high_level")
        val HIGH_COMBO = intPreferencesKey("high_combo")
    }

    val highLevelFlow = context.dataStore.data.map {
        it[HIGH_LEVEL] ?: 1
    }

    val highComboFlow = context.dataStore.data.map {
        it[HIGH_COMBO] ?: 0
    }

    suspend fun saveHighScore(
        level: Int,
        combo: Int
    ) {
        context.dataStore.edit {
            it[HIGH_LEVEL] = level
            it[HIGH_COMBO] = combo
        }
    }
}