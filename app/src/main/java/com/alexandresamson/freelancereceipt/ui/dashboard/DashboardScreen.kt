package com.alexandresamson.freelancereceipt.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexandresamson.freelancereceipt.R
import com.alexandresamson.freelancereceipt.data.local.entity.ReceiptEntity
import com.alexandresamson.freelancereceipt.domain.Category
import com.alexandresamson.freelancereceipt.ui.theme.AccentGold
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
    onLogout: () -> Unit = {},
    onItemClick: (Long) -> Unit = {},
    onStatsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onUpgradeClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardContent(
        state          = state,
        onAddClick     = onAddClick,
        onExportClick  = onExportClick,
        onSettingsClick = onSettingsClick,
        onUpgradeClick = onUpgradeClick,
        onDeleteClick  = { viewModel.deleteReceipt(it) },
        onItemClick    = onItemClick,
        onStatsClick   = onStatsClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    state: DashboardUiState,
    onAddClick: () -> Unit,
    onExportClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    onDeleteClick: (Long) -> Unit,
    onItemClick: (Long) -> Unit,
    onStatsClick: () -> Unit
) {
    var receiptToDelete by remember { mutableStateOf<ReceiptEntity?>(null) }

    receiptToDelete?.let { receipt ->
        AlertDialog(
            onDismissRequest = { receiptToDelete = null },
            title = { Text(stringResource(R.string.dashboard_delete_title)) },
            text  = { Text(stringResource(R.string.dashboard_delete_message, receipt.merchant)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(receipt.id)
                        receiptToDelete = null
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_delete_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { receiptToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.app_name),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            softWrap = false
                        )
                        Spacer(Modifier.width(8.dp))
                        if (state.isPremium) PremiumBadge() else FreeBadge()
                    }
                },
                actions = {
                    IconButton(onClick = onStatsClick) {
                        Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.action_stats))
                    }
                    IconButton(onClick = onExportClick) {
                        Icon(Icons.Default.FileDownload, contentDescription = stringResource(R.string.action_export))
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.fab_scan_content_desc),
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            // Free tier scan counter banner
            if (!state.isPremium) {
                ScanCounterBanner(
                    count = state.scanCount,
                    limit = state.freeLimit,
                    onUpgradeClick = onUpgradeClick
                )
            }

            if (state.receipts.isEmpty()) {
                EmptyState()
            } else {
                ReceiptList(
                    receipts      = state.receipts,
                    onDeleteClick = { receiptToDelete = it },
                    onItemClick   = onItemClick
                )
            }
        }
    }
}

@Composable
private fun PremiumBadge() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AccentGold
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "PRO",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun FreeBadge() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.25f)
    ) {
        Text(
            text = "FREE",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ScanCounterBanner(count: Int, limit: Int, onUpgradeClick: () -> Unit) {
    val remaining = (limit - count).coerceAtLeast(0)
    val limitReached = count >= limit

    Card(
        onClick = onUpgradeClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (limitReached)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (limitReached)
                        stringResource(R.string.dashboard_limit_reached_title)
                    else
                        stringResource(R.string.dashboard_scans_left, remaining, limit),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.dashboard_upgrade_hint),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = AccentGold
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
    onDeleteClick: (ReceiptEntity) -> Unit,
    onItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = receipts, key = { it.id }) { receipt ->
            ReceiptItem(
                receipt       = receipt,
                onDeleteClick = { onDeleteClick(receipt) },
                onItemClick   = { onItemClick(receipt.id) }
            )
        }
    }
}

@Composable
private fun ReceiptItem(
    receipt: ReceiptEntity,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit
) {
    val context = LocalContext.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val categoryDisplay = remember(receipt.category) {
        Category.dbKeyToDisplayName(context, receipt.category)
    }

    Card(
        onClick = onItemClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = receipt.merchant,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateFormat.format(Date(receipt.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = categoryDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormat.format(receipt.amountInCents / 100.0),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.receipt_tax_rate, receipt.taxRate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.receipt_delete_content_desc),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
