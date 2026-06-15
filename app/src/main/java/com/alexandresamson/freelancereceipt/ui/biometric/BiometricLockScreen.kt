package com.alexandresamson.freelancereceipt.ui.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.alexandresamson.freelancereceipt.R

@Composable
fun BiometricLockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Biometrie direkt beim Öffnen des Screens auslösen
    LaunchedEffect(Unit) {
        triggerBiometric(
            activity = context as FragmentActivity,
            onSuccess = onUnlocked,
            onError = { errorMessage = it }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.biometric_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.biometric_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        errorMessage?.let {
            Spacer(Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            // Manueller Retry-Button, falls Biometrie fehlschlägt
            OutlinedButton(onClick = {
                errorMessage = null
                triggerBiometric(
                    activity = context as FragmentActivity,
                    onSuccess = onUnlocked,
                    onError = { errorMessage = it }
                )
            }) {
                Text(stringResource(R.string.biometric_retry))
            }
        }
    }
}

private fun triggerBiometric(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val biometricManager = BiometricManager.from(activity)

    // Prüfe ob Hardware & Enrollments vorhanden — falle auf PIN zurück
    val canAuthenticate = biometricManager.canAuthenticate(
        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    )
    if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
        // Kein Biometrie-Hardware oder nicht eingerichtet → direkt durchlassen
        // In einer echten App: Nutzer auffordern, Biometrie einzurichten
        onSuccess()
        return
    }

    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Nutzer hat abgebrochen oder zu viele Fehlversuche
                onError(errString.toString())
            }
            override fun onAuthenticationFailed() {
                // Einzelner fehlgeschlagener Versuch – Prompt bleibt offen, kein Callback nötig
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(activity.getString(R.string.biometric_prompt_title))
        .setSubtitle(activity.getString(R.string.biometric_prompt_subtitle))
        .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        .build()

    prompt.authenticate(promptInfo)
}