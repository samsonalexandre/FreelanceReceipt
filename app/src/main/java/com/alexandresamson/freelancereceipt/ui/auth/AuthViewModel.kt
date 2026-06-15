package com.alexandresamson.freelancereceipt.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(private val auth: FirebaseAuth) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState(isLoggedIn = auth.currentUser != null))
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Firebase Auth-Listener — reagiert auf Token-Ablauf, Logout von außen etc.
    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _authState.update { it.copy(isLoggedIn = firebaseAuth.currentUser != null) }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                // AuthStateListener aktualisiert isLoggedIn automatisch
            } catch (e: Exception) {
                _authState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            } finally {
                _authState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun registerWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            try {
                auth.createUserWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _authState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            } finally {
                _authState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signInWithGoogle(context: Context, webClientId: String) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val googleIdToken = GoogleIdTokenCredential
                    .createFrom(result.credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(firebaseCredential).await()
            } catch (e: Exception) {
                _authState.update { it.copy(error = e.localizedMessage, isLoading = false) }
            } finally {
                _authState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signOut() = auth.signOut()

    fun clearError() = _authState.update { it.copy(error = null) }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }
}