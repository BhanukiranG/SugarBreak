package com.main.sugarbreak.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.main.sugarbreak.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem("home", "Today", Icons.Default.Today)
    data object History : BottomNavItem("history", "History", Icons.Default.CalendarMonth)
    data object Statistics : BottomNavItem("statistics", "Insights", Icons.Default.Analytics)
    data object Settings : BottomNavItem("settings", "Settings", Icons.Default.Tune)

    companion object {
        val items = listOf(Home, History, Statistics, Settings)
    }
}

@Composable
fun AppBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary

    val barBgColor = if (isDark) {
        Color(0xFF0F1722).copy(alpha = 0.94f)
    } else {
        Color.White.copy(alpha = 0.90f)
    }

    val barBorderColor = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color(0xFFBBCABF).copy(alpha = 0.35f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.5f) else primaryColor.copy(alpha = 0.08f),
                spotColor = if (isDark) primaryColor.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.04f)
            ),
        color = barBgColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = barBorderColor,
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                )
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem.items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val animatedIconColor by animateColorAsState(
                        targetValue = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        label = "iconColor"
                    )

                    val pillBg = if (isSelected) {
                        primaryColor.copy(alpha = if (isDark) 0.20f else 0.12f)
                    } else {
                        Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(pillBg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (currentRoute != item.route) {
                                    onNavigateToRoute(item.route)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = animatedIconColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = item.title,
                                fontSize = if (isSelected) 12.sp else 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = animatedIconColor
                            )
                        }
                    }
                }
            }
        }
    }
}
