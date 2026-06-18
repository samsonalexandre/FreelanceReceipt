package com.alexandresamson.freelancereceipt.ui.detail

import com.alexandresamson.freelancereceipt.ui.common.TaxBreakdownCard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexandresamson.freelancereceipt.R
import com.alexandresamson.freelancereceipt.ui.addreceipt.CategoryDropdown
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailReceiptScreen(
    receiptId: Long,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: DetailReceiptViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(receiptId) {
        viewModel.loadReceipt(receiptId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onBack()
    }
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onDeleted()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text  = { Text(stringResource(R.string.detail_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteReceipt()
                    }
                ) {
                    Text(
                        text  = stringResource(R.string.action_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.receipt_delete_content_desc),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            if (uiState.timestampMs > 0L) {
                val dateStr = remember(uiState.timestampMs) {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        .format(Date(uiState.timestampMs))
                }
                Text(
                    text = stringResource(R.string.detail_date_label, dateStr),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = uiState.merchant,
                onValueChange = viewModel::onMerchantChange,
                label = { Text(stringResource(R.string.label_merchant)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.amountEuro,
                onValueChange = viewModel::onAmountChange,
                label = { Text(stringResource(R.string.label_amount)) },
                singleLine = true,
                suffix = { Text("€") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.taxRate,
                onValueChange = viewModel::onTaxRateChange,
                label = { Text(stringResource(R.string.label_tax_rate)) },
                singleLine = true,
                suffix = { Text("%") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            TaxBreakdownCard(
                amountEuro = uiState.amountEuro,
                taxRate    = uiState.taxRate
            )

            CategoryDropdown(
                selectedDbKey = uiState.categoryDbKey,
                onSelected = viewModel::onCategoryChange
            )

            uiState.error?.let {
                Text(
                    text  = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick  = viewModel::saveChanges,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = stringResource(R.string.action_save_changes),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
