package com.alexandresamson.freelancereceipt.ui.settings

import android.content.Intent
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexandresamson.freelancereceipt.BuildConfig
import com.alexandresamson.freelancereceipt.R
import com.alexandresamson.freelancereceipt.ui.theme.AccentGold
import org.koin.androidx.compose.koinViewModel

// Hidden gift-code trigger: tap the version line this many times in
// quick succession to reveal the password dialog.
private const val GIFT_TAP_THRESHOLD = 7
// Window (ms) within which all 7 taps must happen. Slow tapping resets.
private const val GIFT_TAP_WINDOW_MS = 3_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onUpgradeClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Hidden gift-code state
    var tapCount        by remember { mutableIntStateOf(0) }
    var firstTapTime    by remember { mutableLongStateOf(0L) }
    var showGiftDialog  by remember { mutableStateOf(false) }

    if (showGiftDialog) {
        GiftCodeDialog(
            isAlreadyPremium = state.isPremium,
            onDismiss = { showGiftDialog = false },
            onSubmit = { code ->
                val success = viewModel.activateGiftCode(code)
                val toastMsgId = if (success)
                    R.string.gift_success
                else
                    R.string.gift_invalid
                Toast.makeText(context, context.getString(toastMsgId), Toast.LENGTH_LONG).show()
                if (success) showGiftDialog = false
                success
            }
        )
    }

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

            SettingsRow(
                title = stringResource(R.string.settings_restore_purchases),
                subtitle = stringResource(R.string.settings_restore_purchases_desc),
                onClick = { viewModel.restorePurchases() }
            )

            SettingsRow(
                title = stringResource(R.string.settings_privacy),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, BuildConfig.PRIVACY_POLICY_URL.toUri())
                    context.startActivity(intent)
                }
            )

            SettingsRow(
                title = stringResource(R.string.settings_terms),
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, BuildConfig.TERMS_URL.toUri())
                    context.startActivity(intent)
                }
            )

            SettingsRow(
                title = stringResource(R.string.settings_contact),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, "mailto:${BuildConfig.SUPPORT_EMAIL}".toUri())
                    context.startActivity(intent)
                }
            )

            Spacer(Modifier.height(8.dp))

            // Hidden tap target: the version line.
            // 7 taps within GIFT_TAP_WINDOW_MS open the gift-code dialog.
            // Skipped entirely if user is already premium.
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(
                        enabled = !state.isPremium,
                        onClick = {
                            val now = SystemClock.elapsedRealtime()
                            // Reset counter if too slow OR if this is the very first tap
                            if (tapCount == 0 || now - firstTapTime > GIFT_TAP_WINDOW_MS) {
                                tapCount = 1
                                firstTapTime = now
                            } else {
                                tapCount++
                                if (tapCount >= GIFT_TAP_THRESHOLD) {
                                    tapCount = 0
                                    showGiftDialog = true
                                }
                            }
                        }
                    )
                    .padding(8.dp) // larger tap target without changing visual position
            )
        }
    }
}

@Composable
private fun GiftCodeDialog(
    isAlreadyPremium: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Boolean
) {
    var code by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CardGiftcard,
                contentDescription = null,
                tint = AccentGold
            )
        },
        title = { Text(stringResource(R.string.gift_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isAlreadyPremium)
                        stringResource(R.string.gift_already_premium)
                    else
                        stringResource(R.string.gift_dialog_body),
                    fontSize = 14.sp
                )
                if (!isAlreadyPremium) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            code = it
                            hasError = false
                        },
                        label = { Text(stringResource(R.string.gift_code_label)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        isError = hasError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (hasError) {
                        Text(
                            stringResource(R.string.gift_invalid),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isAlreadyPremium) {
                TextButton(
                    onClick = {
                        val success = onSubmit(code)
                        if (!success) hasError = true
                    },
                    enabled = code.isNotBlank()
                ) {
                    Text(stringResource(R.string.gift_activate))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        },
        dismissButton = {
            if (!isAlreadyPremium) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    )
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
