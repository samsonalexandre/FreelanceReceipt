package com.alexandresamson.freelancereceipt.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import com.alexandresamson.freelancereceipt.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val merchant: String     = "",
    val amountEuro: String   = "",
    val taxRate: String      = "19",
    val category: String     = "Sonstiges",
    val timestampMs: Long    = 0L,
    val isLoading: Boolean   = true,
    val isSaved: Boolean     = false,
    val isDeleted: Boolean   = false,
    val error: String?       = null
)

class DetailReceiptViewModel(
    private val repository: ReceiptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Original-Entity merken für den Fall dass nichts geändert wurde
    private var originalEntity: ReceiptEntity? = null

    fun loadReceipt(id: Long) {
        viewModelScope.launch {
            val receipt = repository.getReceiptById(id)
            if (receipt == null) {
                _uiState.update { it.copy(isLoading = false, error = "Beleg nicht gefunden") }
                return@launch
            }
            originalEntity = receipt
            _uiState.update {
                it.copy(
                    merchant   = receipt.merchant,
                    amountEuro = "%.2f".format(receipt.amountInCents / 100.0).replace(".", ","),
                    taxRate    = receipt.taxRate.toInt().toString(),
                    category   = receipt.category,
                    timestampMs = receipt.timestamp,
                    isLoading  = false
                )
            }
        }
    }

    fun onMerchantChange(value: String) = _uiState.update { it.copy(merchant = value) }
    fun onAmountChange(value: String)   = _uiState.update { it.copy(amountEuro = value) }
    fun onTaxRateChange(value: String)  = _uiState.update { it.copy(taxRate = value) }
    fun onCategoryChange(value: String) = _uiState.update { it.copy(category = value) }

    fun saveChanges() {
        val entity = originalEntity ?: return
        val state = _uiState.value

        val amountCents = state.amountEuro
            .replace(",", ".")
            .toDoubleOrNull()
            ?.let { (it * 100).toLong() }
            ?: run {
                _uiState.update { it.copy(error = "Ungültiger Betrag") }
                return
            }

        viewModelScope.launch {
            try {
                repository.saveReceipt(
                    entity.copy(
                        merchant      = state.merchant.ifBlank { "Unbekannt" },
                        amountInCents = amountCents,
                        taxRate       = state.taxRate.toDoubleOrNull() ?: 19.0,
                        category      = state.category
                        // timestamp bleibt original — wann der Beleg ausgestellt wurde
                    )
                )
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage) }
            }
        }
    }

    fun deleteReceipt() {
        val entity = originalEntity ?: return
        viewModelScope.launch {
            try {
                repository.deleteReceipt(entity.id)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}