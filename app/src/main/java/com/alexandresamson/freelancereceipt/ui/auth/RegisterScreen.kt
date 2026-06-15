package com.alexandresamson.freelancereceipt.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alexandresamson.freelancereceipt.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) onRegisterSuccess()
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    val passwordsMatch = password == passwordConfirm && password.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.register_title),
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
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it },
            label = { Text(stringResource(R.string.label_password_confirm)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordConfirm.isNotBlank() && !passwordsMatch,
            supportingText = {
                if (passwordConfirm.isNotBlank() && !passwordsMatch)
                    Text(stringResource(R.string.error_passwords_no_match))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        authState.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.registerWithEmail(email, password) },
            enabled = !authState.isLoading && email.isNotBlank() && passwordsMatch,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (authState.isLoading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(stringResource(R.string.action_register))
        }
        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text(stringResource(R.string.action_go_to_login))
        }
    }
}