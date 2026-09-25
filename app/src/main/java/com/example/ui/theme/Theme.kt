package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// =============================================================================
// Material 3 Shapes Palette for Healthcare Components
// =============================================================================
val MedTimeShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // Micro badges, pill status indicators
    small = RoundedCornerShape(10.dp),       // Text fields, dosage chips, dropdowns
    medium = RoundedCornerShape(16.dp),      // Medicine cards, appointment cards, dialogs
    large = RoundedCornerShape(24.dp),       // Bottom sheets, hero banners, action cards
    extraLarge = RoundedCornerShape(32.dp)   // Floating buttons, modal headers
)

// =============================================================================
// Light Color Scheme - Anchored by MedTime Primary Blue (#1565C0)
// =============================================================================
private val LightColorScheme = lightColorScheme(
    primary = MedBluePrimary,
    onPrimary = MedOnPrimary,
    primaryContainer = MedBlueContainer,
    onPrimaryContainer = MedBlueDark,
    inversePrimary = Color(0xFF90CAF9),

    secondary = MedInfo,
    onSecondary = Color.White,
    secondaryContainer = MedInfoLight,
    onSecondaryContainer = MedInfoDark,

    tertiary = MedWarning,
    onTertiary = Color.White,
    tertiaryContainer = MedWarningLight,
    onTertiaryContainer = MedWarningDark,

    background = MedBackground,
    onBackground = MedTextPrimary,
    surface = MedSurface,
    onSurface = MedTextPrimary,
    surfaceVariant = MedSurfaceVariant,
    onSurfaceVariant = MedTextSecondary,
    surfaceTint = MedBluePrimary,
    inverseSurface = MedDarkSurface,
    inverseOnSurface = MedDarkTextPrimary,

    outline = MedBorder,
    outlineVariant = MedDivider,

    error = MedError,
    onError = Color.White,
    errorContainer = MedErrorLight,
    onErrorContainer = MedErrorDark,

    scrim = Color(0x99000000)
)

// =============================================================================
// Dark Color Scheme - High Contrast Clinical Night Mode
// =============================================================================
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = MedBlueDark,
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color.White,
    inversePrimary = MedBluePrimary,

    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF00332C),
    secondaryContainer = MedInfo,
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF4E2600),
    tertiaryContainer = Color(0xFFE65100),
    onTertiaryContainer = Color(0xFFFFE0B2),

    background = MedDarkBackground,
    onBackground = MedDarkTextPrimary,
    surface = MedDarkSurface,
    onSurface = MedDarkTextPrimary,
    surfaceVariant = MedDarkSurfaceVariant,
    onSurfaceVariant = MedDarkTextSecondary,
    surfaceTint = Color(0xFF90CAF9),
    inverseSurface = MedSurface,
    inverseOnSurface = MedTextPrimary,

    outline = MedDarkBorder,
    outlineVariant = Color(0xFF242E3C),

    error = Color(0xFFEF9A9A),
    onError = Color(0xFF640000),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFFFDAD6),

    scrim = Color.Black
)

<<<<<<< HEAD
/**
 * ThemeMode options for MedTime:
 * - SYSTEM: Automatically follow system theme
 * - LIGHT: Force Light clinical theme
 * - DARK: Force Dark clinical night theme
 */
enum class ThemeMode(val title: String, val description: String) {
    SYSTEM("System Default", "Follows system-wide display settings"),
    LIGHT("Light Mode", "High-visibility clean clinical theme"),
    DARK("Dark Mode", "High-contrast clinical night theme")
}

=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
// =============================================================================
// MaterialTheme Wrappers
// =============================================================================

/**
 * MedTimeTheme: Main design system wrapper composing colors, typography,
 * and shapes to ensure consistent UI across all patient, doctor, caretaker,
 * and admin screens.
 */
@Composable
fun MedTimeTheme(
<<<<<<< HEAD
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
=======
    darkTheme: Boolean = isSystemInDarkTheme(),
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    dynamicColor: Boolean = false, // Healthcare branding mandates fixed cohesive palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = MedTimeShapes,
        content = content
    )
}

/**
 * Backward-compatible alias for application entry points and tests.
 */
@Composable
fun MyApplicationTheme(
<<<<<<< HEAD
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
=======
    darkTheme: Boolean = isSystemInDarkTheme(),
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MedTimeTheme(
<<<<<<< HEAD
        themeMode = themeMode,
=======
>>>>>>> 4f93c3ba4af1622bb07741d40563cd12f81cc465
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}
