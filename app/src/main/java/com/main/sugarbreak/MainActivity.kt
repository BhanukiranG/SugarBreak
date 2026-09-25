package com.main.sugarbreak

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
import com.main.sugarbreak.ui.navigation.AppNavGraph
import com.main.sugarbreak.ui.theme.SugarBreakTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val startDestination by mainViewModel.startDestination.collectAsState()
            SugarBreakTheme {
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