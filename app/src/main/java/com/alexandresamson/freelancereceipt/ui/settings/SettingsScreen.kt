package com.alexandresamson.freelancereceipt.ui.settings

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexandresamson.freelancereceipt.BuildConfig
import com.alexandresamson.freelancereceipt.R
import com.alexandresamson.freelancereceipt.ui.theme.AccentGold
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onUpgradeClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subscription status card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (state.isPremium)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (state.isPremium) AccentGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (state.isPremium)
                                stringResource(R.string.settings_status_premium)
                            else
                                stringResource(R.string.settings_status_free),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (state.isPremium)
                            stringResource(R.string.settings_premium_desc)
                        else
                            stringResource(R.string.settings_free_desc, state.scanCount, state.freeLimit),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!state.isPremium) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onUpgradeClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                        ) {
                            Text(stringResource(R.string.settings_upgrade), color = Color.White)
                        }
                    }
                }
            }

            // Restore purchases
            SettingsRow(
                title = stringResource(R.string.settings_restore_purchases),
                subtitle = stringResource(R.string.settings_restore_purchases_desc),
                onClick = { viewModel.restorePurchases() }
            )

            // Privacy Policy
            SettingsRow(
                title = stringResource(R.string.settings_privacy),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, BuildConfig.PRIVACY_POLICY_URL.toUri())
                    context.startActivity(intent)
                }
            )

            // Terms
            SettingsRow(
                title = stringResource(R.string.settings_terms),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, BuildConfig.TERMS_URL.toUri())
                    context.startActivity(intent)
                }
            )

            // Contact
            SettingsRow(
                title = stringResource(R.string.settings_contact),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, "mailto:${BuildConfig.SUPPORT_EMAIL}".toUri())
                    context.startActivity(intent)
                }
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String? = null, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
