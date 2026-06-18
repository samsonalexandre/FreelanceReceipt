package com.alexandresamson.freelancereceipt.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import com.alexandresamson.freelancereceipt.data.prefs.PreferencesManager
import com.alexandresamson.freelancereceipt.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val receipts: List<ReceiptEntity> = emptyList(),
    val isPremium: Boolean = false,
    val scanCount: Int = 0,
    val freeLimit: Int = PreferencesManager.FREE_MONTHLY_LIMIT
) {
    val scansRemaining: Int get() = (freeLimit - scanCount).coerceAtLeast(0)
    val limitReached: Boolean get() = !isPremium && scanCount >= freeLimit
}

class ReceiptViewModel(
    private val repository: ReceiptRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        combine(
            repository.getAllReceipts(),
            prefs.isPremium,
            prefs.scanCount
        ) { receipts, premium, count ->
            DashboardUiState(receipts = receipts, isPremium = premium, scanCount = count)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState()
        )

    fun deleteReceipt(id: Long) {
        viewModelScope.launch { repository.deleteReceipt(id) }
    }
}
