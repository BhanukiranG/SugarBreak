package com.main.sugarbreak.ui.settings

import com.main.sugarbreak.R

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.ui.components.GlassCard
import com.main.sugarbreak.ui.components.SugarBackground
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToUserDetails: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = com.main.sugarbreak.ui.theme.LocalIsDarkTheme.current
    val context = LocalContext.current

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    var showTimePicker by remember { mutableStateOf(false) }
    var showRuleDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.toggleReminder(isGranted)
    }

    SugarBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_app),
                                contentDescription = "App Logo",
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Settings",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = if (isDark) Color.White else primaryColor
                            )
                        }
                    },
                    
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDark) Color(0xFF0B0F17).copy(alpha = 0.85f)
                        else Color.White.copy(alpha = 0.82f)
                    )
                )
            }
        ) { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Profile Preview Banner Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        onClick = onNavigateToUserDetails
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(54.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.15f))
                                        .border(2.dp, primaryContainer.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clip(CircleShape)
                                        .background(primaryContainer)
                                        .border(2.dp, if (isDark) Color(0xFF131C26) else Color.White, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = uiState.userName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    )

                                }
                            }
                        }
                    }
                    // Section: Appearance
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "APPEARANCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column {
                                SettingsToggleRow(
                                    icon = Icons.Default.Nightlight,
                                    iconTint = primaryColor,
                                    iconBg = primaryColor.copy(alpha = 0.12f),
                                    title = "Dark Mode",
                                    subtitle = "Toggle dark theme across the app",
                                    checked = uiState.isDarkMode ?: isDark,
                                    onCheckedChange = { isChecked ->
                                        viewModel.setIsDarkMode(isChecked)
                                    }
                                )
                            }
                        }
                    }

                    // Section 1: Notifications & Reminders
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "NOTIFICATIONS & REMINDERS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column {
                                SettingsToggleRow(
                                    icon = Icons.Default.Alarm,
                                    iconTint = primaryColor,
                                    iconBg = primaryColor.copy(alpha = 0.12f),
                                    title = "Daily Check In Reminder",
                                    subtitle = "Evening nudge to log.",
                                    checked = uiState.reminderEnabled,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                val hasPermission = ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.POST_NOTIFICATIONS
                                                ) == PackageManager.PERMISSION_GRANTED

                                                if (hasPermission) {
                                                    viewModel.toggleReminder(true)
                                                } else {
                                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                }
                                            } else {
                                                viewModel.toggleReminder(true)
                                            }
                                        } else {
                                            viewModel.toggleReminder(false)
                                        }
                                    }
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                                val timeFormatted = remember(uiState.reminderTime) {
                                    uiState.reminderTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
                                }
                                 SettingsActionRow(
                                    icon = Icons.Default.Schedule,
                                    iconTint = MaterialTheme.colorScheme.secondary,
                                    iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                    title = "Reminder Time",
                                    badgeText = timeFormatted,
                                    onClick = { showTimePicker = true }
                                )
                            }
                        }
                    }

                    // Section 2: Tracking & Challenge Rules
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "TRACKING & CHALLENGE RULES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column {
                                val behaviorText = when (uiState.challengeBehavior) {
                                    ChallengeBehavior.CONTINUE -> "Continue tracking normally"
                                    ChallengeBehavior.ADD_RECOVERY_DAY -> "Add recovery day"
                                    ChallengeBehavior.RESET_STREAK -> "Reset streak"
                                }
                                SettingsActionRow(
                                    icon = Icons.Default.Sync,
                                    iconTint = primaryColor,
                                    iconBg = primaryColor.copy(alpha = 0.12f),
                                    title = "Rule on Slip ups",
                                    subtitle = "Non punitive mindset",
                                    valueText = behaviorText,
                                    onClick = { showRuleDialog = true }
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                                SettingsActionRow(
                                    icon = Icons.Default.Flag,
                                    iconTint = MaterialTheme.colorScheme.secondary,
                                    iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                    title = "Challenge Goal",
                                    valueText = "${uiState.challengeGoalDays} Days Challenge",
                                    onClick = { showGoalDialog = true }
                                )
                            }
                        }
                    }

                    // Section 3: Data & Philosophy
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "DATA & PHILOSOPHY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column {

                                SettingsActionRow(
                                    icon = Icons.Default.Download,
                                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    iconBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    title = "Export Tracking Data (CSV)",
                                    onClick = { viewModel.exportDataCsv(context) }
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                                SettingsActionRow(
                                    icon = Icons.Default.DeleteOutline,
                                    iconTint = MaterialTheme.colorScheme.error,
                                    iconBg = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                    title = "Reset All Data",
                                    badgeText = "Caution",
                                    badgeColor = MaterialTheme.colorScheme.error,
                                    onClick = { showResetDialog = true }
                                )
                            }
                        }
                    }
                    // App Version Footer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sugar Break V1.0",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Time Picker Modal Dialog
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = uiState.reminderTime.hour,
                initialMinute = uiState.reminderTime.minute,
                is24Hour = false
            )

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                title = {
                    Text(
                        "Set Reminder Time",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(state = timePickerState)
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateReminderTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                            showTimePicker = false
                        }
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.outline)
                    }
                }
            )
        }

        // Challenge Behavior Rule Selection Dialog
        if (showRuleDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showRuleDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                SugarBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                            TopAppBar(
                                title = { },
                                navigationIcon = {
                                    IconButton(onClick = { showRuleDialog = false }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                            )
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 24.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(Modifier.height(16.dp))
                            com.main.sugarbreak.ui.onboarding.Step3Rule(
                                selectedRule = uiState.challengeBehavior,
                                onRuleSelected = { 
                                    viewModel.updateChallengeBehavior(it)
                                    showRuleDialog = false 
                                }
                            )
                        }
                    }
                }
            }
        }

        // Challenge Goal Selection Dialog
        if (showGoalDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showGoalDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                SugarBackground {
                    Scaffold(
                        containerColor = Color.Transparent,
                        topBar = {
                            TopAppBar(
                                title = { },
                                navigationIcon = {
                                    IconButton(onClick = { showGoalDialog = false }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                            )
                        }
                    ) { paddingValues ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 24.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Spacer(Modifier.height(16.dp))
                            com.main.sugarbreak.ui.onboarding.Step4TargetDays(
                                selectedDays = uiState.challengeGoalDays,
                                onDaysSelected = { 
                                    viewModel.updateChallengeGoal(it)
                                    showGoalDialog = false 
                                }
                            )
                        }
                    }
                }
            }
        }

        // Reset Data Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset All Data", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                text = {
                    Text("Are you sure you want to reset your logs? This will clear past check in records for a fresh start.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetAllData {
                                showResetDialog = false
                                Toast.makeText(context, "All tracking data has been reset", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("Reset", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String? = null,
    valueText: String? = null,
    badgeText: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor
                )
            }
        }

        if (valueText != null) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
        )
    }
}
















