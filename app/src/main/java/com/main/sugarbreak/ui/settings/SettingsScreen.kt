package com.main.sugarbreak.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.ui.components.GlassBox
import com.main.sugarbreak.ui.components.GlassCard
import com.main.sugarbreak.ui.components.SugarBackground
import com.main.sugarbreak.ui.theme.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToUserDetails: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    var showTimePicker by remember { mutableStateOf(false) }
    var showRuleDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var quotesEnabled by remember { mutableStateOf(true) }

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
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = if (isDark) 0.20f else 0.12f))
                                    .border(1.dp, primaryColor.copy(alpha = if (isDark) 0.35f else 0.20f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
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
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Profile Preview Banner Card (Level 1 Elevation)
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
                            // Avatar with online status pip
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
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(primaryColor.copy(alpha = 0.12f))
                                            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(50))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Active Member",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = primaryColor
                                        )
                                    }
                                }

                                Text(
                                    text = "Mindful Tracker since Sep 2026",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFDEE8FF))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.80f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(50))
                                                .background(primaryContainer)
                                        )
                                    }
                                    Text(
                                        text = "Day 24",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = primaryColor
                                    )
                                }
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
                                // Row 1: Daily Check-In Reminder
                                SettingsToggleRow(
                                    icon = Icons.Default.Alarm,
                                    iconTint = primaryColor,
                                    iconBg = primaryColor.copy(alpha = 0.12f),
                                    title = "Daily Check-In Reminder",
                                    subtitle = "Evening reflection prompt",
                                    checked = uiState.reminderEnabled,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            viewModel.toggleReminder(isChecked)
                                        }
                                    }
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                                // Row 2: Reminder Time
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

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                                // Row 3: Gentle Motivational Quotes
                                SettingsToggleRow(
                                    icon = Icons.Default.FormatQuote,
                                    iconTint = tertiaryColor,
                                    iconBg = tertiaryColor.copy(alpha = 0.12f),
                                    title = "Gentle Motivational Quotes",
                                    subtitle = "Calm & compassion-first notes",
                                    checked = quotesEnabled,
                                    onCheckedChange = { quotesEnabled = it }
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
                                // Rule on Slip-ups
                                val behaviorText = when (uiState.challengeBehavior) {
                                    ChallengeBehavior.CONTINUE -> "Continue tracking normally"
                                    ChallengeBehavior.ADD_RECOVERY_DAY -> "Add recovery day"
                                    ChallengeBehavior.RESET_STREAK -> "Reset streak"
                                }
                                SettingsActionRow(
                                    icon = Icons.Default.Sync,
                                    iconTint = primaryColor,
                                    iconBg = primaryColor.copy(alpha = 0.12f),
                                    title = "Rule on Slip-ups",
                                    subtitle = "Non-punitive mindset",
                                    valueText = behaviorText,
                                    onClick = { showRuleDialog = true }
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                                // Challenge Goal
                                SettingsActionRow(
                                    icon = Icons.Default.Flag,
                                    iconTint = MaterialTheme.colorScheme.secondary,
                                    iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                    title = "Challenge Goal",
                                    valueText = "30 Days Challenge",
                                    onClick = { }
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                                // Excluded Sugars
                                SettingsActionRow(
                                    icon = Icons.Default.Egg,
                                    iconTint = tertiaryColor,
                                    iconBg = tertiaryColor.copy(alpha = 0.12f),
                                    title = "Excluded Sugars",
                                    valueText = "Added/Free sugars only",
                                    onClick = { }
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
                                // Philosophy
                                SettingsActionRow(
                                    icon = Icons.Default.Favorite,
                                    iconTint = primaryColor,
                                    iconBg = primaryColor.copy(alpha = 0.12f),
                                    title = "Our Non-Judgmental Philosophy",
                                    subtitle = "Guilt-free habits & awareness",
                                    onClick = { }
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                                // Export
                                SettingsActionRow(
                                    icon = Icons.Default.Download,
                                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    iconBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    title = "Export Tracking Data (CSV)",
                                    onClick = { }
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

                                // Reset All Data
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
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "SugarBreak v2.4 — Designed with care 🌿",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Build 142 • Calm vitality for healthy minds",
                            style = MaterialTheme.typography.labelSmall,
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
            AlertDialog(
                onDismissRequest = { showRuleDialog = false },
                title = {
                    Text(
                        "Rule on Slip-ups",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChallengeBehavior.entries.forEach { behavior ->
                            val isSelected = uiState.challengeBehavior == behavior
                            val label = when (behavior) {
                                ChallengeBehavior.CONTINUE -> "Continue tracking normally (Recommended)"
                                ChallengeBehavior.ADD_RECOVERY_DAY -> "Add recovery day"
                                ChallengeBehavior.RESET_STREAK -> "Reset streak"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) primaryColor.copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        viewModel.updateChallengeBehavior(behavior)
                                        showRuleDialog = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updateChallengeBehavior(behavior)
                                        showRuleDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRuleDialog = false }) {
                        Text("Close", color = primaryColor)
                    }
                }
            )
        }

        // Reset Data Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset All Data", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                text = {
                    Text("Are you sure you want to reset your logs? This action is intended to give you a fresh mindful start.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showResetDialog = false
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
