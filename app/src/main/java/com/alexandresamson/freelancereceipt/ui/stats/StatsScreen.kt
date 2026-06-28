package com.alexandresamson.freelancereceipt.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexandresamson.freelancereceipt.R
import com.alexandresamson.freelancereceipt.domain.Category
import com.alexandresamson.freelancereceipt.domain.ReceiptStats
import com.alexandresamson.freelancereceipt.ui.theme.ChartPalette
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

private val CHART_COLORS = ChartPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.stats == null || uiState.stats!!.receiptCount == 0 -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.stats_no_data))
                }
            }
            else -> {
                StatsContent(stats = uiState.stats!!, modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun StatsContent(stats: ReceiptStats, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val currency = NumberFormat.getCurrencyInstance(Locale.GERMANY)

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(stringResource(R.string.stats_summary_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryRow(label = stringResource(R.string.stats_receipt_count), value = "${stats.receiptCount}")
                SummaryRow(label = stringResource(R.string.stats_total_brutto), value = currency.format(stats.totalBruttoInCents / 100.0), bold = true)
                SummaryRow(label = stringResource(R.string.stats_total_netto), value = currency.format(stats.totalNettoInCents / 100.0))
                SummaryRow(label = stringResource(R.string.stats_total_tax), value = currency.format(stats.totalTaxInCents / 100.0))
            }
        }

        if (stats.byCategory.isNotEmpty()) {
            Text(stringResource(R.string.stats_by_category), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PieChart(data = stats.byCategory, total = stats.totalBruttoInCents, modifier = Modifier.fillMaxWidth().height(200.dp))
                    stats.byCategory.entries.forEachIndexed { index, (dbKey, cents) ->
                        val percent = if (stats.totalBruttoInCents > 0) (cents * 100.0 / stats.totalBruttoInCents) else 0.0
                        val displayName = Category.dbKeyToDisplayName(context, dbKey)
                        LegendItem(color = CHART_COLORS[index % CHART_COLORS.size], label = displayName, value = currency.format(cents / 100.0), percent = percent)
                    }
                }
            }
        }

        if (stats.byMonth.isNotEmpty()) {
            Text(stringResource(R.string.stats_by_month), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BarChartWithLabels(data = stats.byMonth)
                }
            }
        }
    }
}

@Composable
private fun PieChart(data: Map<String, Long>, total: Long, modifier: Modifier = Modifier) {
    if (total == 0L) return
    Canvas(modifier = modifier) {
        val diameter = minOf(size.width, size.height) * 0.85f
        val topLeft = Offset(x = (size.width - diameter) / 2f, y = (size.height - diameter) / 2f)
        var startAngle = -90f
        data.values.forEachIndexed { index, cents ->
            val sweep = (cents.toFloat() / total.toFloat()) * 360f
            drawArc(color = CHART_COLORS[index % CHART_COLORS.size], startAngle = startAngle, sweepAngle = sweep, useCenter = true, topLeft = topLeft, size = Size(diameter, diameter))
            startAngle += sweep
        }
    }
}

@Composable
private fun BarChartWithLabels(data: Map<String, Long>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return
    val maxValue = data.values.max().toFloat()
    val barColor = MaterialTheme.colorScheme.primary
    val entries = data.entries.toList()

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            val spacing = size.width / entries.size
            val barWidth = spacing * 0.6f
            entries.forEachIndexed { index, (_, cents) ->
                val barH = if (maxValue > 0) (cents / maxValue) * size.height else 0f
                val left = index * spacing + (spacing - barWidth) / 2f
                drawRect(color = barColor, topLeft = Offset(left, size.height - barH), size = Size(barWidth, barH))
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            entries.forEach { (monthKey, _) ->
                val parts = monthKey.split("-")
                val displayLabel = if (parts.size == 2) "${parts[1]}/${parts[0].takeLast(2)}" else monthKey
                Text(text = displayLabel, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: String, percent: Double) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.size(12.dp)) { drawCircle(color = color) }
        Text(text = label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(text = "%.1f%%".format(percent), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}