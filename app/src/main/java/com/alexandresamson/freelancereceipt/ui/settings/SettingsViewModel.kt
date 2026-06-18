package com.alexandresamson.freelancereceipt.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexandresamson.freelancereceipt.data.billing.BillingManager
import com.alexandresamson.freelancereceipt.data.prefs.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
}
