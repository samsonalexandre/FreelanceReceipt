package com.alexandresamson.freelancereceipt.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import com.alexandresamson.freelancereceipt.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReceiptViewModel(private val repository: ReceiptRepository) : ViewModel() {

    // StateFlow hält immer den letzten Wert – ideal für Compose
    val receipts: StateFlow<List<ReceiptEntity>> = repository
        .getAllReceipts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteReceipt(id: Long) {
        viewModelScope.launch {
            repository.deleteReceipt(id)
        }
    }
}