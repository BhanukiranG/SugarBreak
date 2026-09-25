package com.main.sugarbreak.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.main.sugarbreak.ui.theme.*

/**
 * Stitch Glassmorphic Card (Level 1 & Level 2 Depth).
 * Accurately replicates Stitch's frosted glass substrate, specular rim stroke,
 * and ambient diffuse shadow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    isElevated: Boolean = false,
    borderColorOverride: Color? = null,
    backgroundColorOverride: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val containerColor = backgroundColorOverride ?: if (isDark) {
        if (isElevated) glassElevatedBgDark else glassCardBgDark
    } else {
        if (isElevated) glassElevatedBgLight else glassCardBgLight
    }

    val defaultBorderColor = if (isDark) {
        if (isElevated) Color.White.copy(alpha = 0.14f) else glassBorderDark
    } else {
        if (isElevated) Color.White.copy(alpha = 0.95f) else glassBorderLight
    }

    val finalBorderColor = borderColorOverride ?: defaultBorderColor
    val elevationDp: Dp = if (isElevated) 8.dp else 3.dp

    val baseModifier = modifier.shadow(
        elevation = elevationDp,
        shape = shape,
        ambientColor = if (isDark) Color.Black.copy(alpha = 0.6f) else Color(0xFF10B981).copy(alpha = 0.06f),
        spotColor = if (isDark) Color(0xFF10B981).copy(alpha = 0.10f) else Color(0xFF111C2D).copy(alpha = 0.04f)
    )

    val clickableModifier = if (onClick != null) {
        baseModifier.clip(shape).clickable(onClick = onClick)
    } else {
        baseModifier
    }

    Card(
        modifier = clickableModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, finalBorderColor)
    ) {
        content()
    }
}

/**
 * Stitch Reusable Glassmorphic Box container for pills, buttons, and custom layout slots.
 */
@Composable
fun GlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50),
    isElevated: Boolean = false,
    borderColorOverride: Color? = null,
    backgroundColorOverride: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    val containerColor = backgroundColorOverride ?: if (isDark) {
        if (isElevated) Color(0xFF1A2738).copy(alpha = 0.92f)
        else Color(0xFF131C26).copy(alpha = 0.85f)
    } else {
        if (isElevated) Color.White.copy(alpha = 0.92f)
        else Color.White.copy(alpha = 0.76f)
    }

    val defaultBorderColor = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.80f)
    }

    val finalBorderColor = borderColorOverride ?: defaultBorderColor

    val boxModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .background(containerColor)
            .border(1.dp, finalBorderColor, shape)
    } else {
        modifier
            .clip(shape)
            .background(containerColor)
            .border(1.dp, finalBorderColor, shape)
    }

    Box(
        modifier = boxModifier,
        content = content
    )
}

/**
 * Stitch-styled Chip / Filter Pill.
 */
@Composable
fun GlassChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary

    val bg = if (selected) {
        primaryColor.copy(alpha = if (isDark) 0.25f else 0.12f)
    } else {
        if (isDark) Color(0xFF1A2738).copy(alpha = 0.60f)
        else Color.White.copy(alpha = 0.70f)
    }

    val border = if (selected) {
        primaryColor.copy(alpha = if (isDark) 0.80f else 0.70f)
    } else {
        if (isDark) Color(0xFF334155).copy(alpha = 0.50f)
        else Color(0xFFBBCABF).copy(alpha = 0.40f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50)),
        content = content
    )
}
