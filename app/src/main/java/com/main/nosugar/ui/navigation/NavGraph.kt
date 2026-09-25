package com.main.nosugar.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.main.nosugar.ui.checkin.CheckInScreen
import com.main.nosugar.ui.history.HistoryScreen
import com.main.nosugar.ui.home.HomeScreen
import com.main.nosugar.ui.onboarding.OnboardingScreen
import com.main.nosugar.ui.settings.SettingsScreen
import com.main.nosugar.ui.settings.UserDetailsScreen
import com.main.nosugar.ui.statistics.StatisticsScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "home"
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainTabRoutes = setOf("home", "history", "statistics", "settings")
    val showBottomBar = currentRoute in mainTabRoutes

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    onFinish = {
                        navController.navigate("home") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen(
                    onNavigateToCheckIn = {
                        navController.navigate("checkin")
                    },
                    onNavigateToSettings = {
                        navController.navigate("settings")
                    }
                )
            }
            composable("checkin") {
                CheckInScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("history") {
                HistoryScreen()
            }
            composable("statistics") {
                StatisticsScreen()
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateToUserDetails = {
                        navController.navigate("userdetails")
                    }
                )
            }
            composable("userdetails") {
                UserDetailsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
