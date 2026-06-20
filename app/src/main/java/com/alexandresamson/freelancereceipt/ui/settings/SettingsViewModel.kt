package com.alexandresamson.freelancereceipt.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexandresamson.freelancereceipt.data.billing.BillingManager
import com.alexandresamson.freelancereceipt.data.prefs.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class SettingsUiState(
    val isPremium: Boolean = false,
    val scanCount: Int = 0,
    val freeLimit: Int = PreferencesManager.FREE_MONTHLY_LIMIT
)

class SettingsViewModel(
    private val prefs: PreferencesManager,
    private val billing: BillingManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        combine(prefs.isPremium, prefs.scanCount) { premium, count ->
            SettingsUiState(isPremium = premium, scanCount = count)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun restorePurchases() = billing.restorePurchases()

    /**
     * Hidden gift-code activator. The plaintext password is NEVER stored in
     * the APK — only its SHA-256 hash. If someone decompiles the app, they
     * see a 64-char hex string they can't reverse without brute-forcing.
     *
     * To change the code:
     *   1. Pick a new password.
     *   2. Generate its SHA-256 hash (see README or use PowerShell:
     *      [System.Security.Cryptography.SHA256]::Create().ComputeHash(...))
     *   3. Replace [GIFT_CODE_HASH] below with the new hex string.
     *
     * Current code: "GESCHENK2026"
     */
    fun activateGiftCode(input: String): Boolean {
        val inputHash = sha256(input.trim())
        val isValid = inputHash.equals(GIFT_CODE_HASH, ignoreCase = true)
        if (isValid) {
            viewModelScope.launch { prefs.setPremium(true) }
        }
        return isValid
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
        // Bytes → hex string (lowercase)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        // SHA-256("GESCHENK2026") — change this hash to change the gift code.
        const val GIFT_CODE_HASH =
            "274fa9f747388403ba6caf39feef56d442d1f2c1f65a2ce99e4392b5749d5a5d"
    }
}
