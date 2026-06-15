package com.alexandresamson.freelancereceipt.ui.addreceipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import com.alexandresamson.freelancereceipt.data.repository.ReceiptRepository
import com.alexandresamson.freelancereceipt.domain.ParsedReceipt
import com.alexandresamson.freelancereceipt.domain.ReceiptParser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddReceiptUiState(
    val merchant: String        = "",
    val amountEuro: String      = "",   // Anzeige in Euro, z.B. "12,99"
    val taxRate: String         = "19",
    val category: String        = "Sonstiges",
    val isSaving: Boolean       = false,
    val isSaved: Boolean        = false,
    val error: String?          = null
)

val CATEGORIES = listOf(
    "Lebensmittel", "Fahrtkosten", "Büro",
    "Restaurant", "Software", "Sonstiges"
)

class AddReceiptViewModel(
    private val repository: ReceiptRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddReceiptUiState())
    val uiState: StateFlow<AddReceiptUiState> = _uiState.asStateFlow()

    // Wird vom CameraScreen aufgerufen — parsed den rohen OCR-Text
    fun onRawTextReceived(rawText: String) {
        val parsed = ReceiptParser.parse(rawText)
        applyParsedReceipt(parsed)
    }

    private fun applyParsedReceipt(parsed: ParsedReceipt) {
        _uiState.update {
            it.copy(
                merchant   = parsed.merchant,
                amountEuro = "%.2f".format(parsed.amountInCents / 100.0).replace(".", ","),
                taxRate    = parsed.taxRate.toInt().toString(),
                category   = parsed.category
            )
        }
    }

    fun onMerchantChange(value: String)  = _uiState.update { it.copy(merchant = value) }
    fun onAmountChange(value: String)    = _uiState.update { it.copy(amountEuro = value) }
    fun onTaxRateChange(value: String)   = _uiState.update { it.copy(taxRate = value) }
    fun onCategoryChange(value: String)  = _uiState.update { it.copy(category = value) }

    fun saveReceipt(timestampMs: Long) {
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
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                repository.saveReceipt(
                    ReceiptEntity(
                        timestamp      = timestampMs,
                        merchant       = state.merchant.ifBlank { "Unbekannt" },
                        amountInCents  = amountCents,
                        taxRate        = state.taxRate.toDoubleOrNull() ?: 19.0,
                        category       = state.category,
                        // Firebase UID anhängen — jeder Nutzer sieht nur seine Belege
                        isPremiumFeature = auth.currentUser != null
                    )
                )
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