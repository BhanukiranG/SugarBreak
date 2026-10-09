package com.main.sugarbreak.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.main.sugarbreak.domain.model.AppTheme

// OCEAN
private val oceanPrimaryLight = Color(0xFF006493)
private val oceanOnPrimaryLight = Color(0xFFFFFFFF)
private val oceanPrimaryContainerLight = Color(0xFFCAE6FF)
private val oceanOnPrimaryContainerLight = Color(0xFF001E30)
private val oceanSecondaryLight = Color(0xFF50606E)
private val oceanOnSecondaryLight = Color(0xFFFFFFFF)
private val oceanSecondaryContainerLight = Color(0xFFD3E5F5)
private val oceanOnSecondaryContainerLight = Color(0xFF0C1D29)

private val oceanPrimaryDark = Color(0xFF8DCDFF)
private val oceanOnPrimaryDark = Color(0xFF00344F)
private val oceanPrimaryContainerDark = Color(0xFF004B70)
private val oceanOnPrimaryContainerDark = Color(0xFFCAE6FF)
private val oceanSecondaryDark = Color(0xFFB7C9D9)
private val oceanOnSecondaryDark = Color(0xFF22323F)
private val oceanSecondaryContainerDark = Color(0xFF394856)
private val oceanOnSecondaryContainerDark = Color(0xFFD3E5F5)

val OceanLightColorScheme = lightColorScheme(
    primary = oceanPrimaryLight, onPrimary = oceanOnPrimaryLight, primaryContainer = oceanPrimaryContainerLight, onPrimaryContainer = oceanOnPrimaryContainerLight,
    secondary = oceanSecondaryLight, onSecondary = oceanOnSecondaryLight, secondaryContainer = oceanSecondaryContainerLight, onSecondaryContainer = oceanOnSecondaryContainerLight,
    background = Color(0xFFF0F8FF), onBackground = Color(0xFF001E30), surface = Color(0xFFF0F8FF), onSurface = Color(0xFF001E30),
    surfaceVariant = Color(0xFFE2EFF9), onSurfaceVariant = Color(0xFF40484C), surfaceContainer = Color(0xFFE2EFF9)
)
val OceanDarkColorScheme = darkColorScheme(
    primary = oceanPrimaryDark, onPrimary = oceanOnPrimaryDark, primaryContainer = oceanPrimaryContainerDark, onPrimaryContainer = oceanOnPrimaryContainerDark,
    secondary = oceanSecondaryDark, onSecondary = oceanOnSecondaryDark, secondaryContainer = oceanSecondaryContainerDark, onSecondaryContainer = oceanOnSecondaryContainerDark,
    background = Color(0xFF001E30), onBackground = Color(0xFFC6E7FF), surface = Color(0xFF001E30), onSurface = Color(0xFFC6E7FF),
    surfaceVariant = Color(0xFF00344F), onSurfaceVariant = Color(0xFFC6E7FF), surfaceContainer = Color(0xFF00344F)
)

// LAVENDER
private val lavPrimaryLight = Color(0xFF7D4E7F)
private val lavOnPrimaryLight = Color(0xFFFFFFFF)
private val lavPrimaryContainerLight = Color(0xFFFFD6F9)
private val lavOnPrimaryContainerLight = Color(0xFF320A38)
private val lavSecondaryLight = Color(0xFF6B586A)
private val lavOnSecondaryLight = Color(0xFFFFFFFF)
private val lavSecondaryContainerLight = Color(0xFFF3DAEF)
private val lavOnSecondaryContainerLight = Color(0xFF251625)

private val lavPrimaryDark = Color(0xFFE9B3E8)
private val lavOnPrimaryDark = Color(0xFF4A204E)
private val lavPrimaryContainerDark = Color(0xFF633766)
private val lavOnPrimaryContainerDark = Color(0xFFFFD6F9)
private val lavSecondaryDark = Color(0xFFD6BED3)
private val lavOnSecondaryDark = Color(0xFF3B2B3B)
private val lavSecondaryContainerDark = Color(0xFF534152)
private val lavOnSecondaryContainerDark = Color(0xFFF3DAEF)

val LavenderLightColorScheme = lightColorScheme(
    primary = lavPrimaryLight, onPrimary = lavOnPrimaryLight, primaryContainer = lavPrimaryContainerLight, onPrimaryContainer = lavOnPrimaryContainerLight,
    secondary = lavSecondaryLight, onSecondary = lavOnSecondaryLight, secondaryContainer = lavSecondaryContainerLight, onSecondaryContainer = lavOnSecondaryContainerLight,
    background = Color(0xFFFFF7FB), onBackground = Color(0xFF320A38), surface = Color(0xFFFFF7FB), onSurface = Color(0xFF320A38),
    surfaceVariant = Color(0xFFF8E7F5), onSurfaceVariant = Color(0xFF4C3F4D), surfaceContainer = Color(0xFFF8E7F5)
)
val LavenderDarkColorScheme = darkColorScheme(
    primary = lavPrimaryDark, onPrimary = lavOnPrimaryDark, primaryContainer = lavPrimaryContainerDark, onPrimaryContainer = lavOnPrimaryContainerDark,
    secondary = lavSecondaryDark, onSecondary = lavOnSecondaryDark, secondaryContainer = lavSecondaryContainerDark, onSecondaryContainer = lavOnSecondaryContainerDark,
    background = Color(0xFF320A38), onBackground = Color(0xFFFFD6F9), surface = Color(0xFF320A38), onSurface = Color(0xFFFFD6F9),
    surfaceVariant = Color(0xFF4A204E), onSurfaceVariant = Color(0xFFFFD6F9), surfaceContainer = Color(0xFF4A204E)
)

// SUNSET
private val sunPrimaryLight = Color(0xFF9E4200)
private val sunOnPrimaryLight = Color(0xFFFFFFFF)
private val sunPrimaryContainerLight = Color(0xFFFFDBCB)
private val sunOnPrimaryContainerLight = Color(0xFF341100)
private val sunSecondaryLight = Color(0xFF775749)
private val sunOnSecondaryLight = Color(0xFFFFFFFF)
private val sunSecondaryContainerLight = Color(0xFFFFDBCB)
private val sunOnSecondaryContainerLight = Color(0xFF2C160B)

private val sunPrimaryDark = Color(0xFFFFB692)
private val sunOnPrimaryDark = Color(0xFF552000)
private val sunPrimaryContainerDark = Color(0xFF793100)
private val sunOnPrimaryContainerDark = Color(0xFFFFDBCB)
private val sunSecondaryDark = Color(0xFFE7BDAA)
private val sunOnSecondaryDark = Color(0xFF442A1E)
private val sunSecondaryContainerDark = Color(0xFF5D4033)
private val sunOnSecondaryContainerDark = Color(0xFFFFDBCB)

val SunsetLightColorScheme = lightColorScheme(
    primary = sunPrimaryLight, onPrimary = sunOnPrimaryLight, primaryContainer = sunPrimaryContainerLight, onPrimaryContainer = sunOnPrimaryContainerLight,
    secondary = sunSecondaryLight, onSecondary = sunOnSecondaryLight, secondaryContainer = sunSecondaryContainerLight, onSecondaryContainer = sunOnSecondaryContainerLight,
    background = Color(0xFFFFF8F6), onBackground = Color(0xFF341100), surface = Color(0xFFFFF8F6), onSurface = Color(0xFF341100),
    surfaceVariant = Color(0xFFFBECE6), onSurfaceVariant = Color(0xFF52443F), surfaceContainer = Color(0xFFFBECE6)
)
val SunsetDarkColorScheme = darkColorScheme(
    primary = sunPrimaryDark, onPrimary = sunOnPrimaryDark, primaryContainer = sunPrimaryContainerDark, onPrimaryContainer = sunOnPrimaryContainerDark,
    secondary = sunSecondaryDark, onSecondary = sunOnSecondaryDark, secondaryContainer = sunSecondaryContainerDark, onSecondaryContainer = sunOnSecondaryContainerDark,
    background = Color(0xFF341100), onBackground = Color(0xFFFFDBCB), surface = Color(0xFF341100), onSurface = Color(0xFFFFDBCB),
    surfaceVariant = Color(0xFF552000), onSurfaceVariant = Color(0xFFFFDBCB), surfaceContainer = Color(0xFF552000)
)

fun getColorSchemeForTheme(theme: AppTheme, isDark: Boolean) = when(theme) {
    AppTheme.OCEAN -> if (isDark) OceanDarkColorScheme else OceanLightColorScheme
    AppTheme.LAVENDER -> if (isDark) LavenderDarkColorScheme else LavenderLightColorScheme
    AppTheme.SUNSET -> if (isDark) SunsetDarkColorScheme else SunsetLightColorScheme
    else -> null // will fallback to default
}
