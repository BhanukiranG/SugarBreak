package com.main.sugarbreak.ui.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.main.sugarbreak.ui.components.GlassCard
import com.main.sugarbreak.ui.components.SugarBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateToUserDetails: () -> Unit = {},
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = com.main.sugarbreak.ui.theme.LocalIsDarkTheme.current

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer

    var selectedFilter by remember { mutableStateOf("This Month") }

    SugarBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = com.main.sugarbreak.R.drawable.logo_app),
                                contentDescription = "App Logo",
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Insights",
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = paddingValues.calculateTopPadding())
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Timeframe Selector Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("This Month", "All Time").forEach { filter ->
                            val isSelected = selectedFilter == filter
                            val bg = if (isSelected) primaryColor.copy(alpha = if (isDark) 0.22f else 0.12f)
                            else if (isDark) Color(0xFF131C26).copy(alpha = 0.70f) else Color.White.copy(alpha = 0.80f)
                            val border = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f)
                            val textCol = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(bg)
                                    .border(1.dp, border, RoundedCornerShape(50))
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 16.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = filter,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = textCol
                                )
                            }
                        }
                    }

                    // 1. Core Hero Metrics (3 Minimal Cards Row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Current Streak Card
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "${uiState.currentStreak} Days",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Current Streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Consistency Rate Card
                        val ratePercent = (uiState.successRate * 100).toInt()
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "$ratePercent%",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Consistency",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Target Goal Card
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "${uiState.targetGoalDays} Days",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Target Goal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 2. Streamlined Challenge Progress Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${uiState.targetGoalDays} Day Challenge",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                val progressPercent = (uiState.challengeProgress * 100).toInt()
                                Text(
                                    "$progressPercent%",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = primaryColor
                                )
                            }

                            // Minimal Progress Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFDEE8FF))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(uiState.challengeProgress.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(primaryColor)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${uiState.currentStreak} of ${uiState.targetGoalDays} days",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${(uiState.targetGoalDays - uiState.currentStreak).coerceAtLeast(0)} days left",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // 3. Minimal Days Breakdown (Successful vs Slip)
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Habit Breakdown",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val total = (uiState.successfulDays + uiState.slipDays).coerceAtLeast(1)
                            val successRatio = uiState.successfulDays.toFloat() / total

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                            ) {
                                if (uiState.successfulDays > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(successRatio)
                                            .fillMaxHeight()
                                            .background(primaryContainer)
                                    )
                                }
                                if (uiState.slipDays > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f - successRatio)
                                            .fillMaxHeight()
                                            .background(tertiaryContainer)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(primaryContainer)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${uiState.successfulDays} On Track",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(tertiaryContainer)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${uiState.slipDays} Slips",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}





