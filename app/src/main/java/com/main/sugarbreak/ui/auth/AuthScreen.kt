package com.main.sugarbreak.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthenticated: () -> Unit = {}, viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val googleClientIdId = remember(context) {
        context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    }
    androidx.compose.runtime.LaunchedEffect(state.authenticated) {
        if (state.authenticated) onAuthenticated()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sugar Break", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            text = when (state.mode) {
                AuthMode.SIGN_IN -> "Sign in to keep your progress synced."
                AuthMode.REGISTER -> "Create an account to save your progress."
                AuthMode.RESET_PASSWORD -> "We’ll email you a password reset link."
                AuthMode.VERIFY_EMAIL -> "Verify your email address to continue."
            },
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(24.dp))

        if (state.mode != AuthMode.VERIFY_EMAIL) {
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::setEmail,
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.mode == AuthMode.SIGN_IN || state.mode == AuthMode.REGISTER) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::setPassword,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        when (state.mode) {
            AuthMode.VERIFY_EMAIL -> {
                Button(onClick = viewModel::submit, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                    Text("I’ve verified my email")
                }
                TextButton(onClick = viewModel::resendVerification, enabled = !state.busy) {
                    Text("Resend verification email")
                }
                TextButton(onClick = viewModel::useDifferentAccount, enabled = !state.busy) {
                    Text("Use a different account")
                }
            }
            else -> {
                Button(onClick = viewModel::submit, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                    if (state.busy) CircularProgressIndicator()
                    else Text(when (state.mode) {
                        AuthMode.SIGN_IN -> "Sign in"
                        AuthMode.REGISTER -> "Create account"
                        AuthMode.RESET_PASSWORD -> "Send reset link"
                        AuthMode.VERIFY_EMAIL -> "Continue"
                    })
                }
                if (state.mode == AuthMode.SIGN_IN || state.mode == AuthMode.REGISTER) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            if (googleClientIdId == 0) {
                                viewModel.setMode(AuthMode.SIGN_IN)
                                viewModel.setEmail("")
                            } else {
                                scope.launch {
                                    runCatching {
                                        val googleOption = GetGoogleIdOption.Builder()
                                            .setServerClientId(context.getString(googleClientIdId))
                                            .setFilterByAuthorizedAccounts(false)
                                            .build()
                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleOption)
                                            .build()
                                        val result = credentialManager.getCredential(context, request)
                                        val credential = result.credential
                                        if (credential is CustomCredential &&
                                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                        ) {
                                            val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                            viewModel.signInWithGoogleToken(token)
                                        } else error("Google did not return a valid sign-in credential")
                                    }.onFailure { error ->
                                        viewModel.showError(error.message ?: "Google sign-in failed")
                                    }
                                }
                            }
                        },
                        enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Continue with Google") }
                    if (googleClientIdId == 0) {
                        Text("Google sign-in needs the Firebase app config file.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        state.message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(12.dp))
        when (state.mode) {
            AuthMode.SIGN_IN -> {
                TextButton(onClick = { viewModel.setMode(AuthMode.REGISTER) }) { Text("Create an account") }
                TextButton(onClick = { viewModel.setMode(AuthMode.RESET_PASSWORD) }) { Text("Forgot password?") }
            }
            AuthMode.REGISTER, AuthMode.RESET_PASSWORD ->
                TextButton(onClick = { viewModel.setMode(AuthMode.SIGN_IN) }) { Text("Back to sign in") }
            AuthMode.VERIFY_EMAIL -> Unit
        }
    }
}
