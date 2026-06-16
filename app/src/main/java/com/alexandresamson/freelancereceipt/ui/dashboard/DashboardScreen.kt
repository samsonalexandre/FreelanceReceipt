package com.alexandresamson.freelancereceipt.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexandresamson.freelancereceipt.R
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ReceiptViewModel = koinViewModel(),
    onAddClick: () -> Unit = {},
    onExportClick: () -> Unit,
    onLogout: () -> Unit = {}
) {
    // collectAsStateWithLifecycle beachtet automatisch den Lifecycle der App (schont Akku)
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()

    DashboardContent(
        receipts = receipts,
        onAddClick = onAddClick,
        onExportClick = onExportClick,
        onLogout = onLogout,
        onDeleteClick = { viewModel.deleteReceipt(it) }
    )
}

// Reines Compose ohne ViewModel – Perfekt für saubere Previews und Tests
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    receipts: List<ReceiptEntity>,
    onAddClick: () -> Unit,
    onExportClick: () -> Unit,
    onLogout: () -> Unit,
    onDeleteClick: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onExportClick) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = stringResource(R.string.action_export)
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = stringResource(R.string.action_logout)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.fab_scan_content_desc)
                )
            }
        }
    ) { paddingValues ->
        if (receipts.isEmpty()) {
            EmptyState(modifier = Modifier.padding(paddingValues))
        } else {
            ReceiptList(
                receipts = receipts,
                onDeleteClick = onDeleteClick,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.dashboard_empty_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.dashboard_empty_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReceiptList(
    receipts: List<ReceiptEntity>,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = receipts,
            key = { it.id } // Stabile Keys -> effiziente Recomposition bei Listen-Änderungen
        ) { receipt ->
            ReceiptItem(
                receipt = receipt,
                onDeleteClick = { onDeleteClick(receipt.id) }
            )
        }
    }
}

@Composable
private fun ReceiptItem(
    receipt: ReceiptEntity,
    onDeleteClick: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = receipt.merchant,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(Date(receipt.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = receipt.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    // Cent -> Euro/Währung für die Anzeige
                    text = currencyFormat.format(receipt.amountInCents / 100.0),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    // Hier greifen wir auf den %.1f Platzhalter in der XML zu (ohne .toInt())
                    text = stringResource(R.string.receipt_tax_rate, receipt.taxRate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.receipt_delete_content_desc),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// --- Previews (kein ViewModel nötig, sofort sichtbar im Studio) ---

@Preview(showBackground = true, showSystemUi = true, locale = "de")
@Composable
private fun DashboardEmptyPreviewDe() {
    MaterialTheme {
        DashboardContent(
            receipts = emptyList(),
            onAddClick = {},
            onExportClick = {},
            onLogout = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, locale = "de")
@Composable
private fun DashboardWithDataPreviewDe() {
    MaterialTheme {
        DashboardContent(
            receipts = listOf(
                ReceiptEntity(
                    id = 1,
                    timestamp = System.currentTimeMillis(),
                    merchant = "REWE City",
                    amountInCents = 4799,
                    taxRate = 19.0,
                    category = "Lebensmittel"
                ),
                ReceiptEntity(
                    id = 2,
                    timestamp = System.currentTimeMillis() - 86_400_000, // minus 1 Tag
                    merchant = "Tankstelle Shell",
                    amountInCents = 8950,
                    taxRate = 19.0,
                    category = "Fahrtkosten"
                )
            ),
            onAddClick = {},
            onExportClick = {},
            onLogout = {},
            onDeleteClick = {}
        )
    }
}