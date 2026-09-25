package com.main.sugarbreak.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable Glassmorphic Card that automatically adapts between
 * Light Mode (Luminous Frosted Glass) and Dark Mode (Obsidian Emerald Glass).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    isElevated: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val containerColor = if (isDark) {
        if (isElevated) Color(0xFF131F29).copy(alpha = 0.95f)
        else Color(0xFF121C24).copy(alpha = 0.90f)
    } else {
        if (isElevated) Color.White.copy(alpha = 0.92f)
        else Color.White.copy(alpha = 0.82f)
    }

    val borderColor = if (isDark) {
        if (isElevated) Color(0xFF10B981).copy(alpha = 0.22f)
        else Color(0xFF10B981).copy(alpha = 0.16f)
    } else {
        if (isElevated) Color(0xFFBBCABF).copy(alpha = 0.60f)
        else Color(0xFFBBCABF).copy(alpha = 0.40f)
    }

    val elevation: Dp = if (isElevated) 6.dp else 2.dp

    Card(
        modifier = modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0xFF10B981).copy(alpha = 0.08f),
            spotColor = if (isDark) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFF111C2D).copy(alpha = 0.06f)
        ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        content()
    }
}

/**
 * Reusable Glassmorphic Box container for pills, buttons, and custom layout slots.
 */
@Composable
fun GlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50),
    isElevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val containerColor = if (isDark) {
        if (isElevated) Color(0xFF16232D).copy(alpha = 0.92f)
        else Color(0xFF121C24).copy(alpha = 0.85f)
    } else {
        if (isElevated) Color.White.copy(alpha = 0.92f)
        else Color.White.copy(alpha = 0.78f)
    }

    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color(0xFFBBCABF).copy(alpha = 0.45f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape),
        content = content
    )
}
