package com.alexandresamson.freelancereceipt.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alexandresamson.freelancereceipt.data.repository.ReceiptRepository
import com.alexandresamson.freelancereceipt.domain.ReceiptStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatsUiState(
    val stats: ReceiptStats? = null,
    val isLoading: Boolean   = true,
    val error: String?       = null
)

class StatsViewModel(
    private val repository: ReceiptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val stats = repository.getStats()
                _uiState.update { it.copy(stats = stats, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            }
        }
    }

    // Ermöglicht manuelles Neuladen (z.B. Pull-to-Refresh später)
    fun refresh() = loadStats()
}