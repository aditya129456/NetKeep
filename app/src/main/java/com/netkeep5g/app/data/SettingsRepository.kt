package com.netkeep5g.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.netkeep5g.app.engine.TrafficEngine
import com.netkeep5g.app.model.TrafficMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "netkeep_settings")

class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val DEFAULT_MODE = stringPreferencesKey("default_mode")
        val DEFAULT_SPEED = intPreferencesKey("default_speed_kbps")
        val KEEP_ALIVE_INTERVAL = intPreferencesKey("keep_alive_interval_sec")
        val DATA_USAGE_WARNING = intPreferencesKey("data_usage_warning_mb")
        val CONTINUOUS_URL = stringPreferencesKey("custom_continuous_url")
        val KEEP_ALIVE_URL = stringPreferencesKey("custom_keep_alive_url")
        val CUMULATIVE_BYTES = longPreferencesKey("cumulative_bytes_all_time")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        val modeStr = preferences[PreferencesKeys.DEFAULT_MODE] ?: TrafficMode.KEEP_ALIVE.name
        val speed = preferences[PreferencesKeys.DEFAULT_SPEED] ?: 5
        val interval = preferences[PreferencesKeys.KEEP_ALIVE_INTERVAL] ?: 30
        val warning = preferences[PreferencesKeys.DATA_USAGE_WARNING] ?: 1000
        val continuousUrl = preferences[PreferencesKeys.CONTINUOUS_URL] ?: TrafficEngine.DEFAULT_CONTINUOUS_URL
        val keepAliveUrl = preferences[PreferencesKeys.KEEP_ALIVE_URL] ?: TrafficEngine.DEFAULT_KEEP_ALIVE_URL
        val cumulativeBytes = preferences[PreferencesKeys.CUMULATIVE_BYTES] ?: 0L

        UserSettings(
            defaultMode = TrafficMode.fromName(modeStr),
            defaultSpeedKBps = speed,
            keepAliveIntervalSec = interval,
            dataUsageWarningMB = warning,
            customContinuousUrl = continuousUrl,
            customKeepAliveUrl = keepAliveUrl,
            cumulativeBytesAllTime = cumulativeBytes
        )
    }

    suspend fun updateDefaultMode(mode: TrafficMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_MODE] = mode.name
        }
    }

    suspend fun updateDefaultSpeed(speedKBps: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_SPEED] = speedKBps
        }
    }

    suspend fun updateKeepAliveInterval(intervalSec: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_ALIVE_INTERVAL] = intervalSec
        }
    }

    suspend fun updateDataUsageWarningMB(warningMB: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATA_USAGE_WARNING] = warningMB
        }
    }

    suspend fun updateContinuousUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CONTINUOUS_URL] = url
        }
    }

    suspend fun updateKeepAliveUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.KEEP_ALIVE_URL] = url
        }
    }

    suspend fun addCumulativeBytes(bytes: Long) {
        if (bytes <= 0) return
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CUMULATIVE_BYTES] ?: 0L
            preferences[PreferencesKeys.CUMULATIVE_BYTES] = current + bytes
        }
    }

    suspend fun resetCumulativeBytes() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUMULATIVE_BYTES] = 0L
        }
    }
}
