package com.main.sugarbreak.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.main.sugarbreak.domain.model.ChallengeBehavior
import com.main.sugarbreak.ui.components.GlassBox
import com.main.sugarbreak.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(state.onboardingCompleted) {
        if (state.onboardingCompleted) {
            onFinish()
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    // Subtle atmospheric ambient gradient tailored for dark and light palettes
    val bgBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color(0xFF072319),
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.background
            )
        } else {
            listOf(
                Color(0xFFE8F5E9).copy(alpha = 0.65f),
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.background
            )
        }
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = primaryColor.copy(alpha = 0.12f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Spa,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.padding(7.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "SugarBreak",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.92f else 0.88f),
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.height(48.dp),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go back",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Back",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                    } else {
                        Spacer(Modifier.weight(0.01f))
                    }

                    Button(
                        onClick = {
                            if (state.currentStep < 5) viewModel.nextStep() else viewModel.finishOnboarding()
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        val nextText = when (state.currentStep) {
                            1 -> "Next: Profile"
                            2 -> "Next: Rules"
                            3 -> "Next: Target Days"
                            4 -> "Next: Reminder"
                            5 -> "Next: Summary"
                            else -> "Start Challenge"
                        }
                        Text(text = nextText, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        if (state.currentStep < 6) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Step Indicator Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stepTitle = when (state.currentStep) {
                        1 -> "Introduction"
                        2 -> "Profile Details"
                        3 -> "Tracking Rules"
                        4 -> "Target Days"
                        5 -> "Reminder"
                        else -> "Summary"
                    }
                    GlassBox(shape = RoundedCornerShape(50)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(primaryColor, CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "${state.currentStep} of 6: $stepTitle",
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(
                        text = "Step ${state.currentStep} / 6",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Step Progress Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 1..6) {
                        val isActive = i == state.currentStep
                        val isCompleted = i < state.currentStep
                        val color = if (isActive || isCompleted) {
                            primaryColor
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.45f)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                // Dynamic Step Content
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    when (state.currentStep) {
                        1 -> Step1Intro()
                        2 -> Step2Profile(state.userName, viewModel::setUserName)
                        3 -> Step3Rule(state.selectedRule, viewModel::setRule, state.targetDays, state.reminderHour, state.reminderMinute)
                        4 -> Step4TargetDays(state.targetDays, viewModel::setTargetDays)
                        5 -> Step5Reminder(state.reminderHour, state.reminderMinute, viewModel::setReminderTime)
                        6 -> Step6Summary(state)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun Step1Intro() {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text(
                text = "Welcome to SugarBreak",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "A supportive, science-informed habit tracker designed to help you reduce free sugar intake naturally — with zero guilt.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Three Feature Highlight GlassCards
        GlassCard(shape = RoundedCornerShape(16.dp), isElevated = true) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                IntroFeatureRow(
                    icon = Icons.Filled.Spa,
                    title = "Non-Judgmental Tracking",
                    desc = "Slips happen. Choose your own recovery rules and keep your momentum intact.",
                    iconTint = primaryColor
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                IntroFeatureRow(
                    icon = Icons.Filled.Timer,
                    title = "Personalized Target",
                    desc = "Set a 7, 21, 30, or 90-day challenge that fits your lifestyle and pace.",
                    iconTint = MaterialTheme.colorScheme.secondary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                IntroFeatureRow(
                    icon = Icons.Filled.NotificationsActive,
                    title = "Gentle Daily Reminders",
                    desc = "Timely daily notifications to pause, reflect, and check in peacefully.",
                    iconTint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun IntroFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    iconTint: Color
) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = CircleShape,
            color = iconTint.copy(alpha = 0.14f),
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun Step2Profile(name: String, onNameChange: (String) -> Unit) {
    val isDark = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary

    Column {
        Text(
            text = "Tell us your name",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "We like to keep things personal! What should we call you on your daily dashboard?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your Name") },
            placeholder = { Text("e.g. Alex") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.65f else 0.85f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.45f else 0.70f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedLabelColor = primaryColor,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = primaryColor
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        GlassBox(
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Your profile is stored locally and securely on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun Step3Rule(
    selectedRule: ChallengeBehavior,
    onRuleSelected: (ChallengeBehavior) -> Unit,
    targetDays: Int,
    reminderHour: Int,
    reminderMinute: Int
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column {
        GlassBox(shape = RoundedCornerShape(50)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Gentle & Sustainable",
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Every journey is personal. What happens if you slip?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 30.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Slip-ups are a natural part of lasting habit change. Choose what feels most supportive for your mental peace.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(20.dp))

        val rules = listOf(
            ChallengeBehavior.CONTINUE to ("Continue tracking normally" to "Forgive and keep going without breaking stride. Best for reducing guilt and fostering natural resilience."),
            ChallengeBehavior.ADD_RECOVERY_DAY to ("Add a recovery day" to "Gives you an extra day to re-center before counting streaks. Pause without judgment."),
            ChallengeBehavior.RESET_STREAK to ("Reset my streak" to "Start fresh with day 1. Best for strict reset rules and clean slate purists.")
        )

        rules.forEach { (rule, texts) ->
            val isSelected = selectedRule == rule
            val (title, description) = texts

            GlassCard(
                shape = RoundedCornerShape(16.dp),
                isElevated = isSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .selectable(selected = isSelected, onClick = { onRuleSelected(rule) })
                    .then(
                        if (isSelected) Modifier.border(1.5.dp, primaryColor, RoundedCornerShape(16.dp))
                        else Modifier
                    )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(24.dp)
                            .background(if (isSelected) primaryColor else Color.Transparent, CircleShape)
                            .border(
                                width = if (isSelected) 0.dp else 1.5.dp,
                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (rule == ChallengeBehavior.CONTINUE) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = primaryColor.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "Recommended",
                                        fontSize = 10.sp,
                                        color = primaryColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Target Preview Chip with semantic tokens
        GlassBox(
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Selected Goal",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Target: $targetDays Days",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reminder Preview Card with semantic tokens
        GlassCard(
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(7.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    val timeString = "${reminderHour.toString().padStart(2, '0')}:${reminderMinute.toString().padStart(2, '0')}"
                    Row {
                        Text("Daily reminder at ", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(timeString, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    }
                    Text("(Configured in next step)", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                }
                Icon(
                    imageVector = Icons.Outlined.LockOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun Step4TargetDays(selectedDays: Int, onDaysSelected: (Int) -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column {
        Text(
            text = "Target Challenge Length",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Choose a milestone duration that motivates you. You can always extend or create a new challenge later.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(28.dp))

        val daysOptions = listOf(7, 14, 21, 30, 60, 90)

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            daysOptions.chunked(2).forEach { rowDays ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowDays.forEach { days ->
                        val isSelected = selectedDays == days
                        GlassCard(
                            shape = RoundedCornerShape(18.dp),
                            isElevated = isSelected,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onDaysSelected(days) }
                                .then(
                                    if (isSelected) Modifier.border(1.5.dp, primaryColor, RoundedCornerShape(18.dp))
                                    else Modifier
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 24.dp, horizontal = 16.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$days",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Days",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step5Reminder(hour: Int, minute: Int, onTimeSelected: (Int, Int) -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Daily Reminder",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pick a consistent time in the evening to log your sugar intake peacefully.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(28.dp))

        var permissionRequested by remember { mutableStateOf(false) }
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            permissionRequested = true
        }

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val timePickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute)

        GlassCard(
            shape = RoundedCornerShape(24.dp),
            isElevated = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        selectorColor = primaryColor,
                        periodSelectorBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        periodSelectorSelectedContainerColor = primaryColor.copy(alpha = 0.2f),
                        periodSelectorSelectedContentColor = primaryColor,
                        periodSelectorUnselectedContainerColor = Color.Transparent,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        timeSelectorSelectedContainerColor = primaryColor.copy(alpha = 0.2f),
                        timeSelectorSelectedContentColor = primaryColor,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        LaunchedEffect(timePickerState.hour, timePickerState.minute) {
            onTimeSelected(timePickerState.hour, timePickerState.minute)
        }
    }
}

@Composable
fun Step6Summary(state: OnboardingState) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column {
        Text(
            text = "Ready to start?",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Review your plan below. You can change these preferences at any time in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(28.dp))

        GlassCard(
            shape = RoundedCornerShape(24.dp),
            isElevated = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryRow(label = "Name", value = if (state.userName.isNotBlank()) state.userName else "Local Profile")
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                SummaryRow(label = "Target", value = "${state.targetDays} days", highlight = true)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                SummaryRow(label = "Rule on slip", value = state.selectedRule.name.replace("_", " "))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                val timeString = "${state.reminderHour.toString().padStart(2, '0')}:${state.reminderMinute.toString().padStart(2, '0')}"
                SummaryRow(label = "Daily Reminder", value = timeString)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}
