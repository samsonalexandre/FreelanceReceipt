package com.alexandresamson.freelancereceipt.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alexandresamson.freelancereceipt.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

private val Context.dataStore by preferencesDataStore(name = "freelancereceipt_prefs")

/**
 * Single source of truth for non-DB user state:
 *  - premium entitlement flag
 *  - monthly scan counter (for free-tier 10/month limit)
 *  - welcome-seen flag
 */
class PreferencesManager(private val context: Context) {

    private val KEY_PREMIUM       = booleanPreferencesKey("is_premium")
    private val KEY_SCAN_COUNT    = intPreferencesKey("scan_count_current_month")
    private val KEY_SCAN_MONTH    = stringPreferencesKey("scan_count_month_key")
    private val KEY_WELCOME_SEEN  = booleanPreferencesKey("welcome_seen")

    val isPremium: Flow<Boolean> =
        if (BuildConfig.DEBUG) flowOf(true)
        else context.dataStore.data.map { it[KEY_PREMIUM] ?: false }

    val scanCount: Flow<Int> =
        context.dataStore.data.map { prefs ->
            val storedMonth = prefs[KEY_SCAN_MONTH]
            val currentMonth = monthKey()
            if (storedMonth != currentMonth) 0 else prefs[KEY_SCAN_COUNT] ?: 0
        }

    val welcomeSeen: Flow<Boolean> =
        context.dataStore.data.map { it[KEY_WELCOME_SEEN] ?: false }

    suspend fun setPremium(value: Boolean) {
        context.dataStore.edit { it[KEY_PREMIUM] = value }
    }

    suspend fun incrementScanCount() {
        context.dataStore.edit { prefs ->
            val currentMonth = monthKey()
            val storedMonth = prefs[KEY_SCAN_MONTH]
            val current = if (storedMonth != currentMonth) 0 else prefs[KEY_SCAN_COUNT] ?: 0
            prefs[KEY_SCAN_COUNT] = current + 1
            prefs[KEY_SCAN_MONTH] = currentMonth
        }
    }

    suspend fun currentScanCountSync(): Int = scanCount.first()
    suspend fun isPremiumSync(): Boolean =
        if (BuildConfig.DEBUG) true else isPremium.first()

    suspend fun setWelcomeSeen() {
        context.dataStore.edit { it[KEY_WELCOME_SEEN] = true }
    }

    private fun monthKey(): String =
        SimpleDateFormat("yyyy-MM", Locale.US).format(Date())

    companion object {
        const val FREE_MONTHLY_LIMIT = 10
    }
}
