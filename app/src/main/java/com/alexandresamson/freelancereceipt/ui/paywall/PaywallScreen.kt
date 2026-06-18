package com.alexandresamson.freelancereceipt.ui.paywall

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexandresamson.freelancereceipt.R
import com.alexandresamson.freelancereceipt.ui.theme.AccentGold
import com.alexandresamson.freelancereceipt.ui.theme.BrandBlue
import com.alexandresamson.freelancereceipt.ui.theme.BrandBlueDark
import org.koin.androidx.compose.koinViewModel

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    onPurchased: () -> Unit,
    viewModel: PaywallViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    // Auto-dismiss after successful purchase
    LaunchedEffect(state.isPremium) {
        if (state.isPremium) onPurchased()
    }

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.consumeError() },
            confirmButton = {
                TextButton(onClick = { viewModel.consumeError() }) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            title = { Text(stringResource(R.string.paywall_error_title)) },
            text = { Text(state.error!!) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.paywall_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(
                    Brush.verticalGradient(listOf(BrandBlue, BrandBlueDark))
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(AccentGold, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Text(
                stringResource(R.string.paywall_headline),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                textAlign = TextAlign.Center
            )

            Text(
                stringResource(R.string.paywall_subhead),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Feature(stringResource(R.string.paywall_feature_unlimited))
                    Feature(stringResource(R.string.paywall_feature_no_ads))
                    Feature(stringResource(R.string.paywall_feature_export))
                    Feature(stringResource(R.string.paywall_feature_priority))
                    Feature(stringResource(R.string.paywall_feature_lifetime))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Price + CTA
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(R.string.paywall_one_time_label),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        state.priceLabel ?: stringResource(R.string.paywall_price_fallback),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlueDark
                    )
                    Text(
                        stringResource(R.string.paywall_no_subscription),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { activity?.let(viewModel::purchase) },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                    ) {
                        Text(
                            stringResource(R.string.paywall_cta_purchase),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    TextButton(
                        onClick = { viewModel.restore() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.paywall_restore))
                    }
                }
            }

            Text(
                stringResource(R.string.paywall_legal),
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Feature(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = BrandBlue
        )
        Text(text, fontSize = 15.sp)
    }
}
