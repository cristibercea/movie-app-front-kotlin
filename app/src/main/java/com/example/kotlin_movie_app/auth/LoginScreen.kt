package com.example.kotlin_movie_app.auth

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlin_movie_app.R
import com.example.kotlin_movie_app.comp.ui.movies.PulsingStatusTitle
import com.example.kotlin_movie_app.core.utils.observeConnectivityAsFlow

const val TAG = "LoginScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onClose: () -> Unit) {
    val loginViewModel = viewModel<LoginViewModel>(factory = LoginViewModel.Factory)
    val loginUiState = loginViewModel.uiState

    val context = LocalContext.current
    val isOnline by remember { observeConnectivityAsFlow(context) }.collectAsState(initial = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    PulsingStatusTitle(text = stringResource(id = R.string.login), isOnline = isOnline)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                var username by remember { mutableStateOf("") }
                TextField(
                    label = { Text(text = "Username") },
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                var password by remember { mutableStateOf("") }
                TextField(
                    label = { Text(text = "Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = { Log.d(TAG, "login..."); loginViewModel.login(username, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isOnline && !loginUiState.isAuthenticating
                ) { Text("Login") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (!isOnline) {
                Text(
                    text = "You are offline. Connect to internet.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (loginUiState.isAuthenticating) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 16.dp)
                )
            }
            if (loginUiState.authenticationError != null) {
                Text(
                    text = "Login failed: ${loginUiState.authenticationError.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

    LaunchedEffect(loginUiState.authenticationCompleted) {
        if (loginUiState.authenticationCompleted) onClose()
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() { LoginScreen {} }