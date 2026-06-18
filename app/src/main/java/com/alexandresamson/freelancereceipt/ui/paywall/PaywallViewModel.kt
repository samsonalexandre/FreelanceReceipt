package com.alexandresamson.freelancereceipt.ui.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexandresamson.freelancereceipt.data.billing.BillingManager
import com.alexandresamson.freelancereceipt.data.prefs.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PaywallUiState(
    val priceLabel: String? = null,
    val isPremium: Boolean = false,
    val error: String? = null
)

class PaywallViewModel(
    private val billing: BillingManager,
    private val prefs: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<PaywallUiState> =
        combine(
            billing.premiumPriceLabel,
            prefs.isPremium,
            billing.billingError
        ) { price, premium, err ->
            PaywallUiState(priceLabel = price, isPremium = premium, error = err)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PaywallUiState())

    init {
        billing.connect()
    }

    fun purchase(activity: Activity) {
        billing.launchPurchase(activity)
    }

    fun restore() {
        billing.restorePurchases()
    }

    fun consumeError() {
        billing.consumeError()
    }
}
