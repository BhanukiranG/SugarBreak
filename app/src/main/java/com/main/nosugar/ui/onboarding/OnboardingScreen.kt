package com.main.nosugar.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.main.nosugar.domain.model.ChallengeBehavior

val PrimaryMint = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.onboardingCompleted) {
        if (state.onboardingCompleted) {
            onFinish()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryMint.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Spa, contentDescription = null, tint = PrimaryMint, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("SugarBreak", fontWeight = FontWeight.Bold, color = PrimaryMint, fontSize = 20.sp)
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC).copy(alpha = 0.8f)
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.height(48.dp),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                    } else {
                        Spacer(Modifier.weight(0.1f))
                    }

                    Button(
                        onClick = {
                            if (state.currentStep < 5) viewModel.nextStep() else viewModel.finishOnboarding()
                        },
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMint)
                    ) {
                        val nextText = when (state.currentStep) {
                            1 -> "Next: Your Profile"
                            2 -> "Next: Tracking Rules"
                            3 -> "Next: Set Challenge Length"
                            4 -> "Next: Daily Reminder"
                            5 -> "Next: Summary"
                            else -> "Start Challenge"
                        }
                        Text(text = nextText, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        if (state.currentStep < 6) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                // Step Indicator
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(PrimaryMint, CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text("${state.currentStep} of 6: $stepTitle", fontWeight = FontWeight.SemiBold, color = PrimaryMint, fontSize = 13.sp)
                    }
                    Text("Step ${state.currentStep} / 6", color = MaterialTheme.colorScheme.outline, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in 1..6) {
                        val isActive = i == state.currentStep
                        val isCompleted = i < state.currentStep
                        val color = if (isActive || isCompleted) PrimaryMint else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

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
    Column {
        Text(
            text = "Welcome to SugarBreak",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Let's set up your challenge to reduce sugar intake and build healthier habits. We're taking a supportive, non-judgmental approach.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun Step2Profile(name: String, onNameChange: (String) -> Unit) {
    Column {
        Text(
            text = "Tell us your name",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "We like to keep things personal! What should we call you?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your Name") },
            placeholder = { Text("e.g. Alex") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
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
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(PrimaryMint.copy(alpha = 0.1f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = PrimaryMint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Gentle & Sustainable", color = PrimaryMint, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Every journey is personal. What happens if you slip?",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, lineHeight = 28.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Slip-ups are a natural part of lasting habit change. Choose what feels most supportive for your mental peace.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        val rules = listOf(
            ChallengeBehavior.CONTINUE to ("Continue tracking normally" to "Forgive and keep going without breaking stride. Best for reducing guilt and fostering natural resilience."),
            ChallengeBehavior.ADD_RECOVERY_DAY to ("Add a recovery day" to "Gives you an extra day to re-center before counting streaks. Pause without judgment."),
            ChallengeBehavior.RESET_STREAK to ("Reset my streak" to "Start fresh with day 1. Best for strict reset rules and clean slate purists.")
        )
        
        rules.forEach { (rule, texts) ->
            val isSelected = selectedRule == rule
            val (title, description) = texts
            
            val containerColor = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.9f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            val borderColor = if (isSelected) PrimaryMint else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            val elevation = if (isSelected) 8.dp else 2.dp
            
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .selectable(selected = isSelected, onClick = { onRuleSelected(rule) })
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(24.dp)
                            .background(if (isSelected) PrimaryMint else MaterialTheme.colorScheme.surface, CircleShape)
                            .border(if (isSelected) 0.dp else 2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            if (rule == ChallengeBehavior.CONTINUE) {
                                Text(
                                    "Recommended", 
                                    fontSize = 11.sp, 
                                    color = PrimaryMint, 
                                    modifier = Modifier
                                        .background(PrimaryMint.copy(alpha = 0.1f), CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp), 
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Target Preview Chip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(50))
                .border(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(24.dp).background(Color(0xFF6FFBBE), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Flag, contentDescription = null, tint = Color(0xFF002113), modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Selected Goal", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                "Target: $targetDays Days",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryMint,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                    .border(1.dp, PrimaryMint.copy(alpha = 0.1f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Notification Hint Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(32.dp).background(Color(0xFFC4E7FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Color(0xFF00668A), modifier = Modifier.size(18.dp))
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
            Icon(Icons.Outlined.LockOpen, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun Step4TargetDays(selectedDays: Int, onDaysSelected: (Int) -> Unit) {
    Column {
        Text(
            text = "Target Days",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        val daysOptions = listOf(7, 14, 21, 30, 60, 90)
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            daysOptions.chunked(2).forEach { rowDays ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowDays.forEach { days ->
                        val isSelected = selectedDays == days
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) PrimaryMint.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) PrimaryMint else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onDaysSelected(days) }
                        ) {
                            Box(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$days Days",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) PrimaryMint else MaterialTheme.colorScheme.onSurface
                                )
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Daily Reminder",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(32.dp))
        
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
        
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                TimePicker(state = timePickerState)
            }
        }
        
        LaunchedEffect(timePickerState.hour, timePickerState.minute) {
            onTimeSelected(timePickerState.hour, timePickerState.minute)
        }
    }
}

@Composable
fun Step6Summary(state: OnboardingState) {
    Column {
        Text(
            text = "Ready to start?",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryRow(label = "Name", value = if (state.userName.isNotBlank()) state.userName else "Local Profile")
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                SummaryRow(label = "Target", value = "${state.targetDays} days")
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                SummaryRow(label = "Rule on slip", value = state.selectedRule.name.replace("_", " "))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                SummaryRow(label = "Reminder", value = "${state.reminderHour.toString().padStart(2, '0')}:${state.reminderMinute.toString().padStart(2, '0')}")
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
    }
}

