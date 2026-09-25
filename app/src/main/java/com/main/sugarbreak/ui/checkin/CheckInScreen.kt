package com.main.sugarbreak.ui.checkin

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckInScreen(
    onNavigateBack: () -> Unit,
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    val surfaceTint = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val background = MaterialTheme.colorScheme.background

    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        // Ambient Atmosphere Blobs
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
        ) {
            if (isDark) {
                drawCircle(
                    color = Color(0xFF064E3B).copy(alpha = 0.25f),
                    radius = 320f,
                    center = androidx.compose.ui.geometry.Offset(-80f, -40f)
                )
                drawCircle(
                    color = Color(0xFF0369A1).copy(alpha = 0.18f),
                    radius = 280f,
                    center = androidx.compose.ui.geometry.Offset(size.width + 80f, 150f)
                )
            } else {
                drawCircle(
                    color = Color(0xFFC4E7FF).copy(alpha = 0.35f),
                    radius = 300f,
                    center = androidx.compose.ui.geometry.Offset(-100f, -50f)
                )
                drawCircle(
                    color = Color(0xFF6FFBBE).copy(alpha = 0.3f),
                    radius = 300f,
                    center = androidx.compose.ui.geometry.Offset(size.width + 100f, 200f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GlassBox(
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = if (isDark) Color.White else surfaceTint
                        )
                    }
                }

                Text(
                    text = "Daily Check-In",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurface
                )

                GlassBox(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = surfaceTint,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Today, $today",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceVariant
                        )
                    }
                }
            }

            // Main Content Flow
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // Header Section
                Row(
                    modifier = Modifier
                        .background(
                            if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else surfaceTint.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF10B981).copy(alpha = 0.35f) else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF6EE7B7) else surfaceTint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Safe & Non-Judgmental Space",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFF6EE7B7) else surfaceTint
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "How was today?",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Be honest with yourself—every log brings greater mindful awareness.",
                    fontSize = 14.sp,
                    color = onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Core Decision Targets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isTracked = uiState.hasHadSugar == false

                    // Stayed on track Card
                    val trackedBg = if (isTracked) {
                        if (isDark) Color(0xFF064E3B).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.92f)
                    } else {
                        if (isDark) Color(0xFF121C24).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.72f)
                    }
                    val trackedBorder = if (isTracked) {
                        if (isDark) Color(0xFF10B981) else Color(0xFF10B981).copy(alpha = 0.7f)
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFBBCABF).copy(alpha = 0.45f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(trackedBg)
                            .border(
                                width = if (isTracked) 2.dp else 1.dp,
                                color = trackedBorder,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { viewModel.onTrackSelection(true) }
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
                                        .size(46.dp)
                                        .background(
                                            if (isDark) Color(0xFF10B981).copy(alpha = 0.2f) else surfaceTint.copy(alpha = 0.15f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF34D399) else surfaceTint
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (isTracked) Color(0xFF10B981) else Color.Transparent,
                                            CircleShape
                                        )
                                        .border(
                                            1.dp,
                                            if (isTracked) Color.Transparent else outlineVariant.copy(alpha = 0.6f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isTracked) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFF022C1E) else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Stayed on track",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF6EE7B7) else surfaceTint
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No added sugar today! Feeling proud.",
                                fontSize = 12.sp,
                                color = onSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    val isSugar = uiState.hasHadSugar == true

                    // Had sugar Card
                    val sugarBg = if (isSugar) {
                        if (isDark) Color(0xFF78350F).copy(alpha = 0.40f) else Color(0xFFFFEDD5).copy(alpha = 0.85f)
                    } else {
                        if (isDark) Color(0xFF121C24).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.72f)
                    }
                    val sugarBorder = if (isSugar) {
                        if (isDark) Color(0xFFF59E0B) else Color(0xFFFDBA74)
                    } else {
                        if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFBBCABF).copy(alpha = 0.45f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(sugarBg)
                            .border(
                                width = if (isSugar) 2.dp else 1.dp,
                                color = sugarBorder,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { viewModel.onTrackSelection(false) }
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
                                        .size(46.dp)
                                        .background(
                                            if (isDark) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color(0xFFFFEDD5),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFFFCD34D) else Color(0xFFC2410C)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            if (isSugar) (if (isDark) Color(0xFFF59E0B) else Color(0xFFC2410C)) else Color.Transparent,
                                            CircleShape
                                        )
                                        .border(
                                            1.dp,
                                            if (isSugar) Color.Transparent else outlineVariant.copy(alpha = 0.6f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSugar) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFF451A03) else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Had sugar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFFCD34D) else Color(0xFFC2410C)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Enjoyed some sugar. Learning and moving forward.",
                                fontSize = 12.sp,
                                color = onSurfaceVariant,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = uiState.hasHadSugar == true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    GlassCard(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "What sweet treat did you enjoy?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Naming what we taste without blame helps build dietary awareness.",
                                fontSize = 13.sp,
                                color = onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Chips for SlipReason
                            val reasons = SlipReason.values()
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                reasons.forEach { reason ->
                                    val isSelected = uiState.selectedReason == reason
                                    val chipBg = if (isSelected) {
                                        if (isDark) Color(0xFF064E3B).copy(alpha = 0.55f) else surfaceTint.copy(alpha = 0.12f)
                                    } else {
                                        if (isDark) Color(0xFF16232D) else Color(0xFFF0F4F8)
                                    }
                                    val chipBorder = if (isSelected) {
                                        Color(0xFF10B981)
                                    } else {
                                        if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFBBCABF).copy(alpha = 0.5f)
                                    }
                                    val chipText = if (isSelected) {
                                        if (isDark) Color(0xFF6EE7B7) else surfaceTint
                                    } else {
                                        onSurfaceVariant
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(chipBg, RoundedCornerShape(24.dp))
                                            .border(1.dp, chipBorder, RoundedCornerShape(24.dp))
                                            .clip(RoundedCornerShape(24.dp))
                                            .clickable { viewModel.onReasonSelection(reason) }
                                            .padding(horizontal = 14.dp, vertical = 7.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isSelected) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp),
                                                    tint = chipText
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = reason.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = chipText
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(
                                color = if (isDark) Color.White.copy(alpha = 0.08f) else outlineVariant.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Gentle Reminder Banner
                            val reminderBg = if (isDark) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFFEDD5)
                            val reminderBorder = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.35f) else Color(0xFFFDBA74).copy(alpha = 0.6f)
                            val reminderTitle = if (isDark) Color(0xFFFDE68A) else Color(0xFFC2410C)
                            val reminderText = if (isDark) Color(0xFFFCD34D) else Color(0xFF7C2D12)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(reminderBg, RoundedCornerShape(12.dp))
                                    .border(1.dp, reminderBorder, RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(reminderTitle.copy(alpha = 0.15f), CircleShape)
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = reminderTitle,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Gentle Reminder",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = reminderTitle
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Acknowledge it, breathe, and trust your journey. Progress is built on self-compassion, never deprivation.",
                                        fontSize = 13.sp,
                                        color = reminderText,
                                        lineHeight = 18.sp
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
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(if (isDark) Color(0xFF0A0F14).copy(alpha = 0.95f) else background.copy(alpha = 0.92f))
                .border(
                    BorderStroke(
                        1.dp,
                        if (isDark) Color.White.copy(alpha = 0.08f) else outlineVariant.copy(alpha = 0.25f)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = { viewModel.submitCheckIn() },
                enabled = uiState.hasHadSugar != null && (uiState.hasHadSugar == false || uiState.selectedReason != null),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.4f),
                    contentColor = if (isDark) Color(0xFF022C1E) else Color.White,
                    disabledContentColor = if (isDark) Color(0xFF022C1E).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Today", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Congratulatory Supportive Popup Banner
        if (uiState.isSubmitted) {
            Dialog(
                onDismissRequest = onNavigateBack,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            onNavigateBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val dialogBg = if (isDark) Color(0xFF121C24) else Color.White
                    val dialogBorder = if (isDark) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFFBBCABF).copy(alpha = 0.5f)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(dialogBg, RoundedCornerShape(24.dp))
                            .border(1.dp, dialogBorder, RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    if (isDark) Color(0xFF064E3B) else Color(0xFF6FFBBE),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF34D399) else Color(0xFF002113),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "✨ Daily Check-In Saved",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Logged! Compassion over perfection. See you tomorrow!",
                            fontSize = 15.sp,
                            color = onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = if (isDark) Color(0xFF022C1E) else Color.White
                            )
                        ) {
                            Text("Return to Dashboard", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
