package com.main.sugarbreak.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.main.sugarbreak.R
import com.main.sugarbreak.ui.components.GlassCard
import com.main.sugarbreak.ui.components.SugarBackground
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
    LaunchedEffect(state.authenticated) {
        if (state.authenticated) onAuthenticated()
    }

    SugarBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo_app),
                contentDescription = "Sugar Break",
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = when (state.mode) {
                    AuthMode.SIGN_IN -> "WELCOME BACK"
                    AuthMode.REGISTER -> "A FRESH START"
                    AuthMode.RESET_PASSWORD -> "ACCOUNT RECOVERY"
                    AuthMode.VERIFY_EMAIL -> "ONE LAST STEP"
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Sugar Break",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = when (state.mode) {
                    AuthMode.SIGN_IN -> "Sign in to keep your progress synced across devices."
                    AuthMode.REGISTER -> "Create an account and start building a healthier routine."
                    AuthMode.RESET_PASSWORD -> "We’ll email you a secure password reset link."
                    AuthMode.VERIFY_EMAIL -> "Verify your email address to continue to your progress."
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                isElevated = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.mode == AuthMode.VERIFY_EMAIL) {
                        Text(
                            text = state.email,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = viewModel::setEmail,
                            label = { Text("Email") },
                            singleLine = true,
                            enabled = !state.busy,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = if (state.mode == AuthMode.RESET_PASSWORD) ImeAction.Done else ImeAction.Next
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (state.mode == AuthMode.SIGN_IN || state.mode == AuthMode.REGISTER) {
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = state.password,
                                onValueChange = viewModel::setPassword,
                                label = { Text("Password") },
                                singleLine = true,
                                enabled = !state.busy,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    if (state.mode == AuthMode.VERIFY_EMAIL) {
                        Button(
                            onClick = viewModel::submit,
                            enabled = !state.busy,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("I’ve verified my email")
                        }
                        TextButton(onClick = viewModel::resendVerification, enabled = !state.busy) {
                            Text("Resend verification email")
                        }
                        TextButton(onClick = viewModel::useDifferentAccount, enabled = !state.busy) {
                            Text("Use a different account")
                        }
                    } else {
                        Button(
                            onClick = viewModel::submit,
                            enabled = !state.busy,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.busy) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    when (state.mode) {
                                        AuthMode.SIGN_IN -> "Sign in"
                                        AuthMode.REGISTER -> "Create account"
                                        AuthMode.RESET_PASSWORD -> "Send reset link"
                                        AuthMode.VERIFY_EMAIL -> "Continue"
                                    }
                                )
                            }
                        }
                        if (state.mode == AuthMode.SIGN_IN || state.mode == AuthMode.REGISTER) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    if (googleClientIdId == 0) {
                                        viewModel.showError("Google sign-in needs the Firebase app config file.")
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
                                                } else {
                                                    error("Google did not return a valid sign-in credential")
                                                }
                                            }.onFailure { failure ->
                                                viewModel.showError(failure.message ?: "Google sign-in failed")
                                            }
                                        }
                                    }
                                },
                                enabled = !state.busy,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continue with Google")
                            }
                        }
                    }

                    state.error?.let { message ->
                        Spacer(Modifier.height(12.dp))
                        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                    state.message?.let { message ->
                        Spacer(Modifier.height(12.dp))
                        Text(message, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                    }
                    Spacer(Modifier.height(8.dp))
                    when (state.mode) {
                        AuthMode.SIGN_IN -> {
                            TextButton(onClick = { viewModel.setMode(AuthMode.REGISTER) }) {
                                Text("Create an account")
                            }
                            TextButton(onClick = { viewModel.setMode(AuthMode.RESET_PASSWORD) }) {
                                Text("Forgot password?")
                            }
                        }
                        AuthMode.REGISTER, AuthMode.RESET_PASSWORD ->
                            TextButton(onClick = { viewModel.setMode(AuthMode.SIGN_IN) }) {
                                Text("Back to sign in")
                            }
                        AuthMode.VERIFY_EMAIL -> Unit
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Your progress syncs securely with your account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
