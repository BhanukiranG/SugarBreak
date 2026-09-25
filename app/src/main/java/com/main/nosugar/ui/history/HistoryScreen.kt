package com.main.nosugar.ui.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.main.nosugar.domain.model.CheckInStatus
import com.main.nosugar.domain.model.DailyCheckIn
import com.main.nosugar.ui.components.GlassBox
import com.main.nosugar.ui.components.GlassCard
import com.main.nosugar.ui.theme.*
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

    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val primaryContainerColor = colorScheme.primaryContainer

    val secondaryColor = colorScheme.secondary
    val secondaryFixedColor = colorScheme.secondaryContainer

    val surfaceContainerHighColor = colorScheme.surfaceVariant
    val outlineVariantColor = colorScheme.outlineVariant

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isDark) primaryColor.copy(alpha = 0.2f) else primaryColor.copy(alpha = 0.1f),
                                    CircleShape
                                )
                                .border(
                                    1.dp,
                                    if (isDark) primaryColor.copy(alpha = 0.4f) else Color.Transparent,
                                    CircleShape
                                ),
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
                            text = "SugarBreak",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else primaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF0A0F14).copy(alpha = 0.88f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                ),
                modifier = Modifier.shadow(if (isDark) 0.dp else 1.dp)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Ambient Background Blobs
            Canvas(modifier = Modifier.fillMaxSize().blur(80.dp)) {
                if (isDark) {
                    drawCircle(
                        color = Color(0xFF064E3B).copy(alpha = 0.22f),
                        radius = 320f,
                        center = androidx.compose.ui.geometry.Offset(-80f, -40f)
                    )
                    drawCircle(
                        color = Color(0xFF0369A1).copy(alpha = 0.15f),
                        radius = 300f,
                        center = androidx.compose.ui.geometry.Offset(size.width + 80f, 180f)
                    )
                } else {
                    drawCircle(
                        color = secondaryFixedColor.copy(alpha = 0.3f),
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

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                val recordsMap = uiState.records.associateBy { it.date }
                val currentMonth = YearMonth.now()
                val totalLogged = recordsMap.size
                val successCount = recordsMap.count { it.value.status == CheckInStatus.SUCCESS }
                val slipCount = recordsMap.count { it.value.status == CheckInStatus.SLIP }
                val skipCount = recordsMap.count { it.value.status == CheckInStatus.SKIPPED }
                val consistency = if (totalLogged > 0) (successCount * 100) / totalLogged else 0

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            GlassBox(shape = RoundedCornerShape(50)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(primaryColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalLogged days logged \u00B7 $consistency% mindful habit consistency",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Summary Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isDark) {
                            SummaryChip(
                                titleColor = Color(0xFF6EE7B7),
                                subtitleColor = Color(0xFF34D399).copy(alpha = 0.8f),
                                containerColor = Color(0xFF064E3B).copy(alpha = 0.6f),
                                borderColor = Color(0xFF10B981).copy(alpha = 0.35f),
                                indicatorColor = Color(0xFF34D399),
                                title = "$successCount On Track",
                                subtitle = "Mint days"
                            )
                            SummaryChip(
                                titleColor = Color(0xFFFDE68A),
                                subtitleColor = Color(0xFFFCD34D).copy(alpha = 0.8f),
                                containerColor = Color(0xFF78350F).copy(alpha = 0.5f),
                                borderColor = Color(0xFFF59E0B).copy(alpha = 0.35f),
                                indicatorColor = Color(0xFFF59E0B),
                                title = "$slipCount Gentle Slips",
                                subtitle = "Peach days"
                            )
                            SummaryChip(
                                titleColor = Color(0xFFCBD5E1),
                                subtitleColor = Color(0xFF94A3B8),
                                containerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                                borderColor = Color(0xFF334155).copy(alpha = 0.5f),
                                indicatorColor = Color(0xFF94A3B8),
                                title = "$skipCount Rest Days",
                                subtitle = "Grey days"
                            )
                        } else {
                            SummaryChip(
                                titleColor = Color(0xFF006C49),
                                subtitleColor = Color(0xFF006C49).copy(alpha = 0.8f),
                                containerColor = Color(0xFF006C49).copy(alpha = 0.1f),
                                borderColor = Color(0xFF10B981).copy(alpha = 0.3f),
                                indicatorColor = Color(0xFF10B981),
                                title = "$successCount On Track",
                                subtitle = "Mint days"
                            )
                            SummaryChip(
                                titleColor = Color(0xFFC2410C),
                                subtitleColor = Color(0xFFC2410C).copy(alpha = 0.8f),
                                containerColor = Color(0xFFFFEDD5),
                                borderColor = Color(0xFFFDBA74).copy(alpha = 0.6f),
                                indicatorColor = Color(0xFFEA580C),
                                title = "$slipCount Gentle Slips",
                                subtitle = "Peach days"
                            )
                            SummaryChip(
                                titleColor = Color(0xFF475569),
                                subtitleColor = Color(0xFF64748B),
                                containerColor = Color(0xFFF1F5F9),
                                borderColor = Color(0xFFCBD5E1),
                                indicatorColor = Color(0xFF94A3B8),
                                title = "$skipCount Rest Days",
                                subtitle = "Grey days"
                            )
                        }
                    }

                    // Calendar Card with Adaptive Glassmorphism
                    GlassCard(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Weekday headers
                            val headerTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
                                    Text(
                                        text = day,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = headerTextColor,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = if (isDark) Color.White.copy(alpha = 0.08f) else outlineVariantColor.copy(alpha = 0.3f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Calendar Grid
                            CalendarGrid(month = currentMonth, recordsMap = recordsMap)

                            HorizontalDivider(
                                color = if (isDark) Color.White.copy(alpha = 0.08f) else outlineVariantColor.copy(alpha = 0.3f),
                                modifier = Modifier.padding(top = 12.dp, bottom = 10.dp)
                            )

                            // Legend Ribbon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                LegendItem(
                                    color = if (isDark) calendarSuccessDark else calendarSuccessLight,
                                    label = "Success",
                                    borderColor = null
                                )
                                LegendItem(
                                    color = if (isDark) calendarSlipContentDark else calendarSlipContainerLight,
                                    label = "Gentle slip",
                                    borderColor = if (isDark) calendarSlipBorderDark else calendarSlipBorderLight
                                )
                                LegendItem(
                                    color = if (isDark) Color(0xFF1E293B) else calendarRestContainerLight,
                                    label = "Rest",
                                    borderColor = if (isDark) calendarRestBorderDark else calendarRestBorderLight
                                )
                                LegendItem(
                                    color = Color.Transparent,
                                    label = "Upcoming",
                                    borderColor = if (isDark) calendarPendingBorderDark else calendarPendingBorderLight,
                                    isDashed = true
                                )
                            }
                        }
                    }

                    // Selected Day Detail Card with Elevated Glass
                    GlassCard(
                        shape = RoundedCornerShape(24.dp),
                        isElevated = true,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Gradient Top Edge
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(primaryColor, primaryContainerColor, secondaryColor)
                                        )
                                    )
                            )

                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            "SELECTED DAILY LOG",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            "Today",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .background(
                                                if (isDark) Color(0xFF064E3B).copy(alpha = 0.6f) else primaryColor.copy(alpha = 0.1f),
                                                RoundedCornerShape(50)
                                            )
                                            .border(
                                                1.dp,
                                                if (isDark) Color(0xFF10B981).copy(alpha = 0.4f) else primaryContainerColor.copy(alpha = 0.3f),
                                                RoundedCornerShape(50)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Eco,
                                            contentDescription = null,
                                            tint = if (isDark) Color(0xFF6EE7B7) else primaryColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "On Track",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color(0xFF6EE7B7) else primaryColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Bento Mini Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    BentoBox(
                                        icon = Icons.Default.WaterDrop,
                                        iconColor = primaryColor,
                                        iconBgColor = if (isDark) primaryColor.copy(alpha = 0.2f) else primaryColor.copy(alpha = 0.15f),
                                        label = "Sugar Avoided",
                                        value = "--g",
                                        valueColor = primaryColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    BentoBox(
                                        icon = Icons.Default.Bolt,
                                        iconColor = secondaryColor,
                                        iconBgColor = if (isDark) secondaryColor.copy(alpha = 0.2f) else secondaryFixedColor.copy(alpha = 0.5f),
                                        label = "Craving Peak",
                                        value = "--",
                                        valueColor = secondaryColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Reflection / Notes
                                val reflectionBg = if (isDark) Color(0xFF0C161F) else Color.White
                                val reflectionBorder = if (isDark) Color.White.copy(alpha = 0.08f) else outlineVariantColor.copy(alpha = 0.4f)
                                val reflectionLabel = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(reflectionBg, RoundedCornerShape(14.dp))
                                        .border(1.dp, reflectionBorder, RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.EditNote,
                                                contentDescription = null,
                                                tint = reflectionLabel,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Mindful Reflection",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = reflectionLabel
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "No notes added for this day.",
                                        fontSize = 14.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Footer
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
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Felt good",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
fun SummaryChip(
    titleColor: Color,
    subtitleColor: Color,
    containerColor: Color,
    borderColor: Color,
    indicatorColor: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(50))
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(indicatorColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Text(text = subtitle, fontSize = 10.sp, color = subtitleColor)
        }
    }
}

@Composable
fun BentoBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0C161F) else Color(0xFFF0F4F8)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color(0xFFBBCABF).copy(alpha = 0.4f)
    val labelColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = label, fontSize = 11.sp, color = labelColor)
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, borderColor: Color?, isDashed: Boolean = false) {
    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurfaceVariant

    Row(verticalAlignment = Alignment.CenterVertically) {
        val baseSize = Modifier.size(10.dp)

        val finalModifier = if (isDashed) {
            baseSize.border(
                width = 1.dp,
                brush = SolidColor(borderColor ?: Color.Gray),
                shape = CircleShape
            )
        } else {
            val m = baseSize.background(color, CircleShape)
            if (borderColor != null) m.border(1.dp, borderColor, CircleShape) else m
        }

        Box(modifier = finalModifier)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = textColor)
    }
}

@Composable
fun CalendarGrid(month: YearMonth, recordsMap: Map<LocalDate, DailyCheckIn>) {
    val isDark = isSystemInDarkTheme()
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = month.atDay(1).dayOfWeek.value - 1
    val totalCells = daysInMonth + firstDayOfWeek

    val offMonthColor = if (isDark) Color(0xFF64748B).copy(alpha = 0.35f) else Color(0xFF94A3B8).copy(alpha = 0.5f)

    Column(modifier = Modifier.fillMaxWidth()) {
        val rows = (totalCells + 6) / 7
        for (i in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (j in 0 until 7) {
                    val index = i * 7 + j
                    if (index < firstDayOfWeek) {
                        // Previous month days placeholder
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (month.minusMonths(1).lengthOfMonth() - firstDayOfWeek + index + 1).toString(),
                                fontSize = 12.sp,
                                color = offMonthColor
                            )
                        }
                    } else if (index < totalCells) {
                        val day = index - firstDayOfWeek + 1
                        val date = month.atDay(day)
                        val record = recordsMap[date]
                        val status = record?.status ?: CheckInStatus.PENDING
                        val isToday = date == LocalDate.now()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            DayCell(day = day, status = status, isToday = isToday)
                        }
                    } else {
                        // Next month days placeholder
                        val day = index - totalCells + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = 12.sp,
                                color = offMonthColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(day: Int, status: CheckInStatus, isToday: Boolean) {
    val isDark = isSystemInDarkTheme()
    val baseModifier = Modifier.size(36.dp).clip(CircleShape)

    when (status) {
        CheckInStatus.SUCCESS -> {
            val bgColor = if (isDark) calendarSuccessDark else calendarSuccessLight
            val contentColor = if (isDark) onCalendarSuccessDark else onCalendarSuccessLight

            var mod = baseModifier.background(bgColor)
            if (isToday) {
                val ringColor = if (isDark) Color(0xFF34D399) else Color(0xFF004D34)
                mod = mod.border(2.dp, ringColor, CircleShape)
            }

            Box(modifier = mod, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        CheckInStatus.SLIP -> {
            val bgColor = if (isDark) calendarSlipContainerDark else calendarSlipContainerLight
            val contentColor = if (isDark) calendarSlipContentDark else calendarSlipContentLight
            val borderColor = if (isDark) calendarSlipBorderDark else calendarSlipBorderLight

            var mod = baseModifier
                .background(bgColor)
                .border(1.dp, borderColor, CircleShape)

            if (isToday) {
                val ringColor = if (isDark) Color(0xFFFCD34D) else Color(0xFFEA580C)
                mod = mod.border(2.dp, ringColor, CircleShape)
            }

            Box(modifier = mod, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(contentColor, CircleShape)
                    )
                }
            }
        }

        CheckInStatus.SKIPPED -> {
            val bgColor = if (isDark) calendarRestContainerDark else calendarRestContainerLight
            val contentColor = if (isDark) calendarRestContentDark else calendarRestContentLight
            val borderColor = if (isDark) calendarRestBorderDark else calendarRestBorderLight

            var mod = baseModifier
                .background(bgColor)
                .border(1.dp, borderColor, CircleShape)

            if (isToday) {
                mod = mod.border(2.dp, Color(0xFF94A3B8), CircleShape)
            }

            Box(modifier = mod, contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = day.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(contentColor, CircleShape)
                    )
                }
            }
        }

        CheckInStatus.PENDING -> {
            val borderColor = if (isDark) calendarPendingBorderDark else calendarPendingBorderLight
            val contentColor = if (isDark) calendarPendingContentDark else calendarPendingContentLight

            var mod = baseModifier.border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape
            )

            if (isToday) {
                val ringColor = if (isDark) Color(0xFF10B981) else Color(0xFF006C49)
                mod = mod.border(2.dp, ringColor, CircleShape)
            }

            Box(
                modifier = mod,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
            }
        }
    }
}
