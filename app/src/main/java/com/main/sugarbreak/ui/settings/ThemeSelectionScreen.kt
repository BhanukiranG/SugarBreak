package com.main.sugarbreak.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.main.sugarbreak.domain.model.AppTheme

import com.main.sugarbreak.ui.components.SugarBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    if (uiState.isLoading) {
        return
    }

    var localIsDarkMode by rememberSaveable(uiState.isDarkMode) { mutableStateOf(uiState.isDarkMode) }
    var localAppTheme by rememberSaveable(uiState.appTheme) { mutableStateOf(uiState.appTheme) }
    
    val isDark = localIsDarkMode ?: androidx.compose.foundation.isSystemInDarkTheme()
    val actualIsDark = uiState.isDarkMode ?: androidx.compose.foundation.isSystemInDarkTheme()
    val view = androidx.compose.ui.platform.LocalView.current

    DisposableEffect(actualIsDark) {
        onDispose {
            var context = view.context
            while (context is android.content.ContextWrapper) {
                if (context is android.app.Activity) break
                context = context.baseContext
            }
            val window = (context as? android.app.Activity)?.window
            if (window != null) {
                androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !actualIsDark
                androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !actualIsDark
            }
        }
    }

    com.main.sugarbreak.ui.theme.SugarBreakTheme(
        darkTheme = isDark,
        appTheme = localAppTheme
    ) {
        SugarBackground {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Theme & Appearance", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                },
                bottomBar = {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                viewModel.setIsDarkMode(localIsDarkMode)
                                viewModel.setAppTheme(localAppTheme)
                                onNavigateBack()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Apply & Save Theme", fontSize = MaterialTheme.typography.titleMedium.fontSize, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))

                    // System / Light / Dark Mode Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val modes = listOf(null to "System", false to "Light", true to "Dark")
                        modes.forEach { (modeIsDark, label) ->
                            val selected = localIsDarkMode == modeIsDark
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { localIsDarkMode = modeIsDark }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("App Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(AppTheme.values()) { theme ->
                            ThemeCard(
                                theme = theme,
                                isSelected = localAppTheme == theme,
                                onClick = { localAppTheme = theme }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeCard(theme: AppTheme, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(theme.displayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Pallete preview based on theme
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val colors = getPreviewColors(theme)
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                }
            }
        }
    }
}

fun getPreviewColors(theme: AppTheme): List<Color> {
    return when(theme) {
        AppTheme.MINT -> listOf(Color(0xFF10b981), Color(0xFFe7eeff), Color(0xFF006c49))
        AppTheme.OCEAN -> listOf(Color(0xFF006493), Color(0xFFCAE6FF), Color(0xFF50606E))
        AppTheme.LAVENDER -> listOf(Color(0xFF7D4E7F), Color(0xFFFFD6F9), Color(0xFF6B586A))
        AppTheme.ROSE -> listOf(Color(0xFFE11D48), Color(0xFFFFE4E6), Color(0xFFBE123C))
    }
}
