package com.main.sugarbreak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import com.main.sugarbreak.ui.navigation.AppNavGraph
import com.main.sugarbreak.ui.theme.SugarBreakTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onRestart() {
        super.onRestart()
        mainViewModel.refreshProgress()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val startDestination by mainViewModel.startDestination.collectAsState()
            val isDarkMode by mainViewModel.isDarkMode.collectAsState()
            val appTheme by mainViewModel.appTheme.collectAsState()
            SugarBreakTheme(
                darkTheme = isDarkMode ?: androidx.compose.foundation.isSystemInDarkTheme(),
                appTheme = appTheme
            ) {
                if (startDestination != null) {
                    key(startDestination) {
                        AppNavGraph(
                            modifier = Modifier.fillMaxSize(),
                            startDestination = startDestination!!,
                            onAuthenticated = mainViewModel::refreshAuthenticationRoute
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

