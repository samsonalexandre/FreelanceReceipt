package com.alexandresamson.freelancereceipt.ui.addreceipt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alexandresamson.freelancereceipt.R
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import com.alexandresamson.freelancereceipt.data.prefs.PreferencesManager
import com.alexandresamson.freelancereceipt.data.repository.ReceiptRepository
import com.alexandresamson.freelancereceipt.domain.Category
import com.alexandresamson.freelancereceipt.domain.ParsedReceipt
import com.alexandresamson.freelancereceipt.domain.ReceiptParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddReceiptUiState(
    val merchant: String        = "",
    val amountEuro: String      = "",
    val taxRate: String         = "19",
    val categoryDbKey: String   = Category.OTHER.dbKey,
    val isSaving: Boolean       = false,
    val isSaved: Boolean        = false,
    val error: String?          = null
)

class AddReceiptViewModel(
    private val repository: ReceiptRepository,
    private val prefs: PreferencesManager,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddReceiptUiState())
    val uiState: StateFlow<AddReceiptUiState> = _uiState.asStateFlow()

    fun onRawTextReceived(rawText: String) {
        val parsed = ReceiptParser.parse(rawText)
        applyParsedReceipt(parsed)
    }

    private fun applyParsedReceipt(parsed: ParsedReceipt) {
        _uiState.update {
            it.copy(
                merchant      = parsed.merchant,
                amountEuro    = "%.2f".format(parsed.amountInCents / 100.0).replace(".", ","),
                taxRate       = parsed.taxRate.toInt().toString(),
                categoryDbKey = parsed.category
            )
        }
    }

    fun onMerchantChange(value: String)     = _uiState.update { it.copy(merchant = value) }
    fun onAmountChange(value: String)       = _uiState.update { it.copy(amountEuro = value) }
    fun onTaxRateChange(value: String)      = _uiState.update { it.copy(taxRate = value) }
    fun onCategoryChange(dbKey: String)     = _uiState.update { it.copy(categoryDbKey = dbKey) }

    fun saveReceipt(timestampMs: Long) {
        val state = _uiState.value
        val amountCents = state.amountEuro
            .replace(",", ".")
            .toDoubleOrNull()
            ?.let { (it * 100).toLong() }
            ?: run {
                _uiState.update {
                    it.copy(error = getApplication<Application>().getString(R.string.error_invalid_amount))
                }
                return
            }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                repository.saveReceipt(
                    ReceiptEntity(
                        timestamp      = timestampMs,
                        merchant       = state.merchant.ifBlank { "Unbekannt" },
                        amountInCents  = amountCents,
                        taxRate        = state.taxRate.toDoubleOrNull() ?: 19.0,
                        category       = state.categoryDbKey
                    )
                )
                // Count this as a successful scan toward the free-tier monthly cap.
                // Premium users still get incremented; the limit only applies if !isPremium.
                prefs.incrementScanCount()
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
