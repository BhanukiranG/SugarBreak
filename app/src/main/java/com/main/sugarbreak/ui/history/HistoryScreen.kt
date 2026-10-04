package com.main.sugarbreak.ui.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.main.sugarbreak.R
import com.main.sugarbreak.domain.model.CheckInStatus
import com.main.sugarbreak.ui.components.GlassBox
import com.main.sugarbreak.ui.components.GlassCard
import com.main.sugarbreak.ui.components.SugarBackground
import com.main.sugarbreak.ui.theme.calendarRestContainerDark
import com.main.sugarbreak.ui.theme.calendarRestContainerLight
import com.main.sugarbreak.ui.theme.calendarRestContentDark
import com.main.sugarbreak.ui.theme.calendarRestContentLight
import com.main.sugarbreak.ui.theme.calendarSlipContainerDark
import com.main.sugarbreak.ui.theme.calendarSlipContainerLight
import com.main.sugarbreak.ui.theme.calendarSlipContentDark
import com.main.sugarbreak.ui.theme.calendarSlipContentLight
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateToUserDetails: () -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = com.main.sugarbreak.ui.theme.LocalIsDarkTheme.current

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

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
                            Image(
                                painter = painterResource(id = R.drawable.logo_app),
                                contentDescription = "App Logo",
                                modifier = Modifier.size(36.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "History",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = if (isDark) Color.White else primaryColor
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToUserDetails) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.15f))
                                    .border(1.dp, primaryColor.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Profile",
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
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
                val recordsMap = uiState.records.associateBy { it.date }
                val totalLogged = recordsMap.size
                val successCount = recordsMap.count { it.value.status == CheckInStatus.SUCCESS }
                val slipCount = recordsMap.count { it.value.status == CheckInStatus.SLIP }
                val restCount = recordsMap.count { it.value.status == CheckInStatus.SKIPPED }
                val consistency = if (totalLogged > 0) (successCount * 100) / totalLogged else 0

                val daysInMonth = currentMonth.lengthOfMonth()
                val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value // 1 = Monday, 7 = Sunday
                val today = LocalDate.now()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
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
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(primaryColor.copy(alpha = if (isDark) 0.20f else 0.10f))
                                .border(1.dp, primaryContainer.copy(alpha = 0.35f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$successCount On Track",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = primaryColor,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Mint days",
                                    fontSize = 9.sp,
                                    color = primaryColor.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Peach Chip: Gentle Slips
                        Box(
                            contentAlignment = Alignment.Center,
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$slipCount Gentle Slips",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = tertiaryColor,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Peach days",
                                    fontSize = 9.sp,
                                    color = tertiaryColor.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Grey Chip: Rest Days
                        Box(
                            contentAlignment = Alignment.Center,
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$restCount Rest Days",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Grey days",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center
                                )
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
                                                    .then(borderModifier),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = "$dayIndex",
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
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

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}





