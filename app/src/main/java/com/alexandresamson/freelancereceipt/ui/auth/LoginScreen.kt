package com.alexandresamson.freelancereceipt.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexandresamson.freelancereceipt.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val webClientId = stringResource(id = R.string.default_web_client_id)

    // Navigiere bei Erfolg
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) onLoginSuccess()
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.label_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.label_password)) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) stringResource(R.string.action_hide)
                    else stringResource(R.string.action_show))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        // Fehlermeldung
        authState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.signInWithEmail(email, password) },
            enabled = !authState.isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (authState.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(stringResource(R.string.action_login))
        }
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { viewModel.signInWithGoogle(context, webClientId) },
            enabled = !authState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.action_google_signin))
        }
        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text(stringResource(R.string.action_go_to_register))
        }
    }
}