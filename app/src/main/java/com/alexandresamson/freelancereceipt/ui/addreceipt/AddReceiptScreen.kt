package com.alexandresamson.freelancereceipt.ui.addreceipt

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexandresamson.freelancereceipt.R
import com.alexandresamson.freelancereceipt.domain.Category
import com.alexandresamson.freelancereceipt.ui.common.TaxBreakdownCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReceiptScreen(
    rawOcrText: String,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddReceiptViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(rawOcrText) {
        if (rawOcrText.isNotBlank()) viewModel.onRawTextReceived(rawOcrText)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_receipt_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

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
                taxRate = uiState.taxRate
            )

            CategoryDropdown(
                selectedDbKey = uiState.categoryDbKey,
                onSelected = viewModel::onCategoryChange
            )

            uiState.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = { viewModel.saveReceipt(System.currentTimeMillis()) },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.action_save_receipt))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(selectedDbKey: String, onSelected: (String) -> Unit) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val displayName = Category.dbKeyToDisplayName(context, selectedDbKey)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.label_category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Category.entries.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.displayName(context)) },
                    onClick = {
                        onSelected(category.dbKey)
                        expanded = false
                    }
                )
            }
        }
    }
}
