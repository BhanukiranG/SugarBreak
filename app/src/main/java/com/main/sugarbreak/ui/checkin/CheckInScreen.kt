package com.main.sugarbreak.ui.checkin

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.main.sugarbreak.domain.model.SlipReason
import com.main.sugarbreak.ui.components.GlassBox
import com.main.sugarbreak.ui.components.GlassCard
import com.main.sugarbreak.ui.components.SugarBackground
import com.main.sugarbreak.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CheckInScreen(
    onNavigateBack: () -> Unit,
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer

    val todayDateFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    }

    var selectedTreatIndex by remember { mutableStateOf<Int?>(null) }
    var selectedTrigger by remember { mutableStateOf("stress") }

    val treatOptions = listOf(
        Pair("Pastry / Bakery", Icons.Default.BakeryDining),
        Pair("Soda / Sweet Drink", Icons.Default.LocalCafe),
        Pair("Dessert / Ice Cream", Icons.Default.Icecream),
        Pair("Snack / Candy", Icons.Default.Cookie),
        Pair("Hidden Sugar in Sauce", Icons.Default.SoupKitchen)
    )

    val triggerOptions = listOf(
        Pair("Social gathering", "social"),
        Pair("Stress relief", "stress"),
        Pair("Late night craving", "night"),
        Pair("Just felt like it", "felt_like")
    )

    SugarBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 84.dp)
            ) {
                // Top Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF131C26).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f))
                            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.90f), CircleShape)
                            .clickable(onClick = onNavigateBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = if (isDark) Color.White else primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Screen Title
                    Text(
                        text = "Daily Check-In",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Date Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (isDark) Color(0xFF1A2738).copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Today, $todayDateFormatted",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Main Content Flow
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    // Mindfulness Hook Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(primaryColor.copy(alpha = if (isDark) 0.20f else 0.10f))
                            .border(1.dp, primaryColor.copy(alpha = if (isDark) 0.35f else 0.20f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Safe & Non-Judgmental Space",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "How was today?",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Be honest with yourself—every log brings greater mindful awareness.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Decision Targets (Side-by-Side Bento Touchpoints)
                    val isStayedOnTrack = uiState.hasHadSugar == false
                    val isHadSugar = uiState.hasHadSugar == true

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Choice 1: Stayed on Track
                        val trackBorder = if (isStayedOnTrack) primaryColor else if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        val trackBg = if (isStayedOnTrack) {
                            if (isDark) Color(0xFF0C241B) else Color.White.copy(alpha = 0.95f)
                        } else {
                            if (isDark) Color(0xFF131C26).copy(alpha = 0.70f) else Color.White.copy(alpha = 0.76f)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(trackBg)
                                .border(if (isStayedOnTrack) 2.dp else 1.dp, trackBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.onTrackSelection(true)
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Selection Circle
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isStayedOnTrack) primaryColor else Color.Transparent)
                                            .border(
                                                1.dp,
                                                if (isStayedOnTrack) primaryColor else MaterialTheme.colorScheme.outlineVariant,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isStayedOnTrack) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Stayed on track",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isStayedOnTrack) primaryColor else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No added sugar today! Feeling proud.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Choice 2: Had Sugar (Warm peach, compassionate non-punitive styling)
                        val sugarBorder = if (isHadSugar) tertiaryColor else if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        val sugarBg = if (isHadSugar) {
                            if (isDark) Color(0xFF28180E) else Color.White.copy(alpha = 0.95f)
                        } else {
                            if (isDark) Color(0xFF131C26).copy(alpha = 0.70f) else Color.White.copy(alpha = 0.76f)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(sugarBg)
                                .border(if (isHadSugar) 2.dp else 1.dp, sugarBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    viewModel.onTrackSelection(false)
                                }
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDark) tertiaryContainer.copy(alpha = 0.35f)
                                                else calendarSlipContainerLight
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = tertiaryColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Selection Circle
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isHadSugar) tertiaryColor else Color.Transparent)
                                            .border(
                                                1.dp,
                                                if (isHadSugar) tertiaryColor else MaterialTheme.colorScheme.outlineVariant,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isHadSugar) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Had sugar",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isHadSugar) tertiaryColor else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Enjoyed some sugar. Learning and moving forward.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Expandable Accordion under "Had sugar"
                    AnimatedVisibility(
                        visible = isHadSugar,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDark) Color(0xFF131C26).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f))
                                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.90f), RoundedCornerShape(20.dp))
                                .padding(20.dp)
                        ) {
                            // Section: What sweet treat did you enjoy?
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "What sweet treat did you enjoy?",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Select item",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Text(
                                text = "Naming what we taste without blame helps build dietary awareness.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                            )

                            // Sweet Treat Chips
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                treatOptions.forEachIndexed { index, pair ->
                                    val isSelected = selectedTreatIndex == index
                                    val chipBg = if (isSelected) primaryColor.copy(alpha = if (isDark) 0.25f else 0.12f)
                                    else if (isDark) Color(0xFF1A2738).copy(alpha = 0.70f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
                                    val chipBorder = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f)
                                    val chipText = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(chipBg)
                                            .border(1.dp, chipBorder, RoundedCornerShape(50))
                                            .clickable {
                                                selectedTreatIndex = if (isSelected) null else index
                                                val reason = when (index) {
                                                    0 -> SlipReason.SWEET
                                                    1 -> SlipReason.SUGARY_DRINK
                                                    2 -> SlipReason.DESSERT
                                                    3 -> SlipReason.SWEET
                                                    else -> SlipReason.OTHER
                                                }
                                                viewModel.onReasonSelection(reason)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = pair.second,
                                                contentDescription = null,
                                                tint = chipText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = pair.first,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                                color = chipText
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Section: Emotional trigger
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "What prompted it?",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Emotional trigger",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                triggerOptions.forEach { pair ->
                                    val isChosen = selectedTrigger == pair.second
                                    val trigBg = if (isChosen) primaryColor.copy(alpha = if (isDark) 0.15f else 0.08f)
                                    else if (isDark) Color(0xFF16202C).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f)
                                    val trigBorder = if (isChosen) primaryColor.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(trigBg)
                                            .border(1.dp, trigBorder, RoundedCornerShape(12.dp))
                                            .clickable { selectedTrigger = pair.second }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isChosen,
                                            onClick = { selectedTrigger = pair.second },
                                            colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = pair.first,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isChosen) FontWeight.SemiBold else FontWeight.Normal
                                            ),
                                            color = if (isChosen) primaryColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Gentle Reminder Card (Warm Compassionate Tone)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isDark) Color(0xFF28180E).copy(alpha = 0.6f)
                                        else calendarSlipContainerLight.copy(alpha = 0.5f)
                                    )
                                    .border(1.dp, tertiaryColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(tertiaryColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EnergySavingsLeaf,
                                            contentDescription = null,
                                            tint = tertiaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Gentle Reminder",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = tertiaryColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Acknowledge it, breathe, and trust your journey. Progress is built on self-compassion, never deprivation.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Fixed Bottom Action Bar
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .shadow(16.dp, ambientColor = primaryColor.copy(alpha = 0.1f)),
                color = if (isDark) Color(0xFF0F1722).copy(alpha = 0.94f) else Color.White.copy(alpha = 0.92f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFBBCABF).copy(alpha = 0.35f)
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    val canSubmit = uiState.hasHadSugar != null
                    Button(
                        onClick = { viewModel.submitCheckIn() },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(
                                elevation = if (canSubmit) 8.dp else 0.dp,
                                shape = RoundedCornerShape(50),
                                spotColor = primaryColor.copy(alpha = 0.40f)
                            ),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryContainer,
                            disabledContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = if (canSubmit) Color.White else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Today",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canSubmit) Color.White else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Congratulatory Supportive Popup Banner / Success Overlay Dialog
            if (uiState.isSubmitted) {
                Dialog(
                    onDismissRequest = onNavigateBack,
                    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
                ) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        isElevated = true
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "✨ Daily Check-In Saved",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (uiState.isSuccess) {
                                    "✨ Awesome job staying mindful today! Keep up the gentle momentum."
                                } else {
                                    "✨ Logged! Compassion over perfection. See you tomorrow!"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Awareness Score XP Chip
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Awareness Score",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = "+15 Mindful XP added",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Text("Return to Dashboard", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
