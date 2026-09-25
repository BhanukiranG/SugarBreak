package com.main.sugarbreak.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.main.sugarbreak.ui.theme.*

/**
 * Stitch Level 0 Canvas Atmosphere Wrapper.
 * Provides the signature organic radial ambient lighting glows for both
 * Light Mode ("Luminous Equilibrium") and Dark Mode ("Obsidian Emerald").
 */
@Composable
fun SugarBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Atmospheric Ambient Lighting Blobs (Layered behind content)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(72.dp)
        ) {
            val width = size.width
            val height = size.height

            if (isDark) {
                // Top-Left Cyan Glow
                drawCircle(
                    color = ambientCyanDark.copy(alpha = 0.22f),
                    radius = width * 0.42f,
                    center = Offset(width * 0.05f, height * 0.02f)
                )
                // Top-Right Emerald Vitality Glow
                drawCircle(
                    color = ambientEmeraldDark.copy(alpha = 0.18f),
                    radius = width * 0.45f,
                    center = Offset(width * 0.95f, height * 0.12f)
                )
                // Mid-Lower Warm Compassion Aura
                drawCircle(
                    color = ambientAmberDark.copy(alpha = 0.14f),
                    radius = width * 0.40f,
                    center = Offset(width * 0.15f, height * 0.70f)
                )
            } else {
                // Top-Left Sky Restorative Wash
                drawCircle(
                    color = ambientSkyLight.copy(alpha = 0.60f),
                    radius = width * 0.48f,
                    center = Offset(width * 0.05f, height * 0.02f)
                )
                // Top-Right Mint Vitality Glow
                drawCircle(
                    color = ambientMintLight.copy(alpha = 0.55f),
                    radius = width * 0.50f,
                    center = Offset(width * 0.95f, height * 0.10f)
                )
                // Mid-Lower Gentle Peach Compassion Glow
                drawCircle(
                    color = ambientPeachLight.copy(alpha = 0.45f),
                    radius = width * 0.45f,
                    center = Offset(width * 0.15f, height * 0.70f)
                )
            }
        }

        // Screen Content
        content()
    }
}
