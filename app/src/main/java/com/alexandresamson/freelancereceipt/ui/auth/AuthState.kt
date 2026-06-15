package com.alexandresamson.freelancereceipt.ui.auth

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)