package com.semenenko.healthdiary.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

// Own palette (teal to blue) instead of the wallpaper colours: the app looks the same on every phone.
private val LightColors = lightColorScheme(
    primary = Color(0xFF0B7A75),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEDEA),
    onPrimaryContainer = Color(0xFF00201E),
    secondary = Color(0xFF2F6FB0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E6F7),
    onSecondaryContainer = Color(0xFF0A2540),
    background = Color(0xFFF2F7F7),
    onBackground = Color(0xFF161D1D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF161D1D),
    surfaceVariant = Color(0xFFE2EBEB),
    onSurfaceVariant = Color(0xFF3F4948),
    outline = Color(0xFF6F7978),
    outlineVariant = Color(0xFFC3CECD),
    error = Color(0xFFC62F2F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4FD1C5),
    onPrimary = Color(0xFF003733),
    primaryContainer = Color(0xFF00504B),
    onPrimaryContainer = Color(0xFFCDEDEA),
    secondary = Color(0xFF8DB9EA),
    onSecondary = Color(0xFF0A2540),
    secondaryContainer = Color(0xFF1F4E7A),
    onSecondaryContainer = Color(0xFFD6E6F7),
    background = Color(0xFF0E1514),
    onBackground = Color(0xFFDDE5E4),
    surface = Color(0xFF162120),
    onSurface = Color(0xFFDDE5E4),
    surfaceVariant = Color(0xFF26332F),
    onSurfaceVariant = Color(0xFFBFC9C7),
    outline = Color(0xFF899392),
    outlineVariant = Color(0xFF3F4948),
    error = Color(0xFFFF8A80)
)

// Large rounded corners everywhere: cards, dialogs, menus, fields.
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

/** Colours of the measurement status stripe on cards (informational, not a diagnosis). */
object LevelColors {
    val normal = Color(0xFF2E9E5B)
    val elevated = Color(0xFFE0A100)
    val high = Color(0xFFD64545)
}

/** Gradient of the top bar. */
@Composable
fun headerBrush(): Brush {
    val c = MaterialTheme.colorScheme
    val dark = c.background.luminance() < 0.5f
    return Brush.horizontalGradient(
        if (dark) listOf(c.primaryContainer, c.secondaryContainer)
        else listOf(c.primary, c.secondary)
    )
}

/** [mode]: 0 = follow the system, 1 = light, 2 = dark. */
@Composable
fun HealthDiaryTheme(mode: Int, content: @Composable () -> Unit) {
    val dark = when (mode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        shapes = AppShapes,
        content = content
    )
}
