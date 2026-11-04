package com.naturewalls.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "naturewalls_prefs")

/** Small, non-content preferences. No wallpaper data lives here — that is always fetched live. */
class UserPreferences(private val context: Context) {

    private object Keys {
        val LAST_LIVE_FETCH = longPreferencesKey("last_live_fetch")
        val DAILY_ENABLED = booleanPreferencesKey("daily_enabled")
        val LAST_DAILY_DAY = stringPreferencesKey("last_daily_day")
        val SAVED_COUNT = longPreferencesKey("saved_count")
    }

    val lastLiveFetch: Flow<Long> = context.dataStore.data.map { it[Keys.LAST_LIVE_FETCH] ?: 0L }

    suspend fun setLastLiveFetch(timestamp: Long) {
        context.dataStore.edit { it[Keys.LAST_LIVE_FETCH] = timestamp }
    }

    val dailyEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.DAILY_ENABLED] ?: true }

    suspend fun setDailyEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DAILY_ENABLED] = enabled }
    }

    val lastDailyDay: Flow<String?> = context.dataStore.data.map { it[Keys.LAST_DAILY_DAY] }

    suspend fun setLastDailyDay(day: String) {
        context.dataStore.edit { it[Keys.LAST_DAILY_DAY] = day }
    }

    val savedCount: Flow<Long> = context.dataStore.data.map { it[Keys.SAVED_COUNT] ?: 0L }

    suspend fun incrementSavedCount() {
        context.dataStore.edit { it[Keys.SAVED_COUNT] = (it[Keys.SAVED_COUNT] ?: 0L) + 1L }
    }
}
