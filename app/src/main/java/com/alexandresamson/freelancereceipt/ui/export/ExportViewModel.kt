package com.alexandresamson.freelancereceipt.ui.export

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import com.alexandresamson.freelancereceipt.data.repository.ExportRepository
import com.alexandresamson.freelancereceipt.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

data class ExportUiState(
    val receipts: List<ReceiptEntity> = emptyList(),
    val isLoading: Boolean            = false,
    val shareIntent: Intent?          = null,
    val error: String?                = null
) {
    // Berechnete Zusammenfassung für die UI
    val totalBrutto: Double get() = receipts.sumOf { it.amountInCents } / 100.0
    val totalNetto: Double  get() = receipts.sumOf { r ->
        r.amountInCents / (1 + r.taxRate / 100.0)
    } / 100.0
    val totalTax: Double    get() = totalBrutto - totalNetto
    val currencyFormat: NumberFormat get() = NumberFormat.getCurrencyInstance(Locale.GERMANY)
}

class ExportViewModel(
    private val receiptRepository: ReceiptRepository,
    private val exportRepository: ExportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState(isLoading = true))
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        loadReceipts()
    }

    private fun loadReceipts() {
        viewModelScope.launch {
            val receipts = receiptRepository.getAllReceipts().first()
            _uiState.update { it.copy(receipts = receipts, isLoading = false) }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val intent = exportRepository.exportCsv(_uiState.value.receipts)
                _uiState.update { it.copy(shareIntent = intent) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val intent = exportRepository.exportPdf(_uiState.value.receipts)
                _uiState.update { it.copy(shareIntent = intent) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Nach dem Teilen zurücksetzen
    fun onShareHandled() = _uiState.update { it.copy(shareIntent = null) }
    fun clearError()     = _uiState.update { it.copy(error = null) }
}