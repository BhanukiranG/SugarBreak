package com.main.sugarbreak.ui.history

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.domain.model.DailyCheckIn
import com.main.sugarbreak.ui.components.GlassBox
import com.main.sugarbreak.ui.components.GlassCard
import com.main.sugarbreak.ui.components.SugarBackground
import com.main.sugarbreak.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    // Pulsing animation for today's active ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

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
                                "SugarBreak",
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
                val recordsMap = uiState.records.associateBy { it.date }
                val totalLogged = recordsMap.size
                val successCount = recordsMap.count { it.value.status == CheckInStatus.SUCCESS }
                val slipCount = recordsMap.count { it.value.status == CheckInStatus.SLIP }
                val restCount = recordsMap.count { it.value.status == CheckInStatus.SKIPPED }
                val consistency = if (totalLogged > 0) (successCount * 100) / totalLogged else 0

                val daysInMonth = currentMonth.lengthOfMonth()
                val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value // 1 = Monday, 7 = Sunday
                val today = LocalDate.now()

                val activeSelectedDate = selectedDate ?: today
                val selectedRecord = recordsMap[activeSelectedDate]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Month Navigation & Title Section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "History",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Month Navigator Pill
                            GlassBox(shape = RoundedCornerShape(50)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { currentMonth = currentMonth.minusMonths(1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronLeft,
                                            contentDescription = "Previous month",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Text(
                                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { currentMonth = currentMonth.plusMonths(1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Next month",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(primaryContainer)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Day ${today.dayOfMonth} of $daysInMonth days · $consistency% mindful habit consistency",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Summary Strip (Horizontal Mini Stats Chips)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Mint Chip: On Track
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(primaryColor.copy(alpha = if (isDark) 0.20f else 0.10f))
                                .border(1.dp, primaryContainer.copy(alpha = 0.35f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(primaryContainer)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "$successCount On Track",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = primaryColor
                                    )
                                    Text(
                                        text = "Mint days",
                                        fontSize = 9.sp,
                                        color = primaryColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Peach Chip: Gentle Slips
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isDark) Color(0xFF28180E).copy(alpha = 0.6f)
                                    else calendarSlipContainerLight.copy(alpha = 0.6f)
                                )
                                .border(1.dp, tertiaryContainer.copy(alpha = 0.35f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(tertiaryColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "$slipCount Gentle Slips",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = tertiaryColor
                                    )
                                    Text(
                                        text = "Peach days",
                                        fontSize = 9.sp,
                                        color = tertiaryColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Grey Chip: Rest Days
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.outline)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "$restCount Rest Days",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Grey days",
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    // Calendar Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Weekday Headers (Mon - Sun)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.width(36.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Days Grid
                            val totalSlots = 35 // 5 rows x 7 columns
                            val offset = firstDayOfWeek - 1 // 0 for Monday

                            for (row in 0 until 5) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    for (col in 0 until 7) {
                                        val dayIndex = (row * 7 + col) - offset + 1
                                        if (dayIndex in 1..daysInMonth) {
                                            val date = currentMonth.atDay(dayIndex)
                                            val record = recordsMap[date]
                                            val isToday = date == today
                                            val isSelected = date == activeSelectedDate
                                            val isFuture = date.isAfter(today)

                                            val status = record?.status

                                            // Determine Day Cell Styling
                                            val cellBg = when {
                                                status == CheckInStatus.SUCCESS -> primaryContainer
                                                status == CheckInStatus.SLIP -> if (isDark) calendarSlipContainerDark else calendarSlipContainerLight
                                                status == CheckInStatus.SKIPPED -> if (isDark) calendarRestContainerDark else calendarRestContainerLight
                                                else -> Color.Transparent
                                            }

                                            val cellTextColor = when {
                                                status == CheckInStatus.SUCCESS -> Color.White
                                                status == CheckInStatus.SLIP -> if (isDark) calendarSlipContentDark else calendarSlipContentLight
                                                status == CheckInStatus.SKIPPED -> if (isDark) calendarRestContentDark else calendarRestContentLight
                                                else -> if (isFuture) MaterialTheme.colorScheme.outline.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                            }

                                            val borderModifier = when {
                                                isSelected -> Modifier.border(2.dp, primaryColor, CircleShape)
                                                isToday -> Modifier.border(2.dp, primaryColor.copy(alpha = pulseAlpha), CircleShape)
                                                isFuture -> Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), CircleShape)
                                                status == CheckInStatus.SLIP -> Modifier.border(1.dp, tertiaryContainer.copy(alpha = 0.4f), CircleShape)
                                                else -> Modifier
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(cellBg)
                                                    .then(borderModifier)
                                                    .clickable { selectedDate = date },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = "$dayIndex",
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold,
                                                        color = cellTextColor,
                                                        lineHeight = 12.sp
                                                    )
                                                    if (status == CheckInStatus.SUCCESS) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    } else if (status == CheckInStatus.SLIP) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(cellTextColor)
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            // Empty cell outside month bounds
                                            Box(
                                                modifier = Modifier.size(38.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val pastOrFutureNum = if (dayIndex < 1) {
                                                    val prevMonthLen = currentMonth.minusMonths(1).lengthOfMonth()
                                                    prevMonthLen + dayIndex
                                                } else {
                                                    dayIndex - daysInMonth
                                                }
                                                Text(
                                                    text = "$pastOrFutureNum",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Legend Ribbon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Success
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(primaryContainer)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Success", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                // Gentle Slip
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDark) calendarSlipContainerDark
                                                else calendarSlipContainerLight
                                            )
                                            .border(1.dp, tertiaryContainer.copy(alpha = 0.5f), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gentle slip", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                // Rest
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Rest", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                // Upcoming
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upcoming", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // Selected Day Detail Card (Level 2 Elevated Glass Card)
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        isElevated = true
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Accent Top Edge Gradient Strip
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                primaryColor,
                                                primaryContainer,
                                                MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                val isLogSuccess = selectedRecord?.status == CheckInStatus.SUCCESS
                                val isLogSlip = selectedRecord?.status == CheckInStatus.SLIP

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "SELECTED DAILY LOG",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = primaryColor
                                        )
                                        Text(
                                            text = "${activeSelectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${activeSelectedDate.dayOfMonth}, ${activeSelectedDate.year}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Status Pill Badge
                                    val statusBg = if (isLogSuccess) primaryColor.copy(alpha = 0.12f)
                                    else if (isLogSlip) tertiaryColor.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

                                    val statusColor = if (isLogSuccess) primaryColor
                                    else if (isLogSlip) tertiaryColor
                                    else MaterialTheme.colorScheme.onSurfaceVariant

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(statusBg)
                                            .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(50))
                                            .padding(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isLogSuccess) Icons.Default.Eco else if (isLogSlip) Icons.Default.Favorite else Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = statusColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isLogSuccess) "Stayed on Track" else if (isLogSlip) "Gentle Slip" else "Rest Day",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = statusColor
                                            )
                                        }
                                    }
                                }

                                // Metric Highlight Bento Mini Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Sugar Avoided Card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(primaryColor.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.WaterDrop,
                                                    contentDescription = null,
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Sugar Avoided",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                                Text(
                                                    text = if (isLogSuccess) "~36g" else "0g",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = primaryColor
                                                )
                                            }
                                        }
                                    }

                                    // Craving Peak Card
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Bolt,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Craving Peak",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                                Text(
                                                    text = if (isLogSuccess) "Mild (2/10)" else "Moderate (5/10)",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                }

                                // Mindful Reflection Quotation Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isDark) Color(0xFF131C26).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.EditNote,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.outline,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Mindful Reflection",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            Text(
                                                text = "8:45 PM",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        Text(
                                            text = if (isLogSuccess) "“Drank lemon sparkling water instead of soda at dinner!”"
                                            else if (isLogSlip) "“Had a slice of birthday cake with friends. Acknowledged and moving forward.”"
                                            else "“Taking time to rest and reset awareness.”",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Mood indication & Action Footer
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.SentimentSatisfied,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isLogSuccess) "Felt energized & clear-headed" else "Practicing self-compassion",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { }
                                    ) {
                                        Text(
                                            text = "Logged",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = primaryColor
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
