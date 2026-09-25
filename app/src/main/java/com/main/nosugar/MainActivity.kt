package com.main.nosugar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.main.nosugar.ui.navigation.AppNavGraph
import com.main.nosugar.ui.theme.NoSugarTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val startDestination by mainViewModel.startDestination.collectAsState()
            NoSugarTheme {
                if (startDestination != null) {
                    AppNavGraph(
                        modifier = Modifier.fillMaxSize(),
                        startDestination = startDestination!!
                    )
                }
            }
        }
    }
}