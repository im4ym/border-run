package com.borderrun.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal that carries the current dark-theme flag down the tree.
 *
 * Screens that build gradient backgrounds or otherwise need to branch on
 * light vs dark can read this rather than querying [MaterialTheme].
 *
 * Usage: `val isDark = LocalIsDarkTheme.current`
 */
val LocalIsDarkTheme = compositionLocalOf { false }

/**
 * Material 3 light colour scheme derived from the Border Run design system.
 *
 * Dynamic colour (Monet) is intentionally disabled so the app always renders
 * with the spec's mint-to-sky gradient palette regardless of device wallpaper.
 */
private val BorderRunLightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextHeading,
    secondary = SecondaryTeal,
    onSecondary = TextHeading,
    background = GradientMint,
    surface = CardSurface,
    onBackground = TextHeading,
    onSurface = TextBody,
    onSurfaceVariant = TextMuted,
    outline = CardBorder,
    error = ErrorRed,
    onError = TextHeading,
)

/**
 * Material 3 dark colour scheme for Border Run.
 *
 * Near-black background (#121212) with dark-glassmorphism cards (80% opaque
 * #1E1E1E) and high-contrast light-grey text (#E0E0E0) for maximum legibility.
 * Primary emerald and accent teal are unchanged.
 */
private val BorderRunDarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    onPrimary = Color(0xFF003322),
    secondary = SecondaryTeal,
    onSecondary = Color(0xFF001F2A),
    background = Color(0xFF121212),
    surface = Color(0xCC1E1E1E),            // 80 % opaque dark glassmorphism
    onBackground = Color(0xFFE0E0E0),       // high-contrast light text
    onSurface = Color(0xFFB0B0B0),          // body text
    onSurfaceVariant = Color(0xFF707070),   // muted / caption text
    outline = Color(0xFF333333),            // card borders / dividers
    error = ErrorRed,
    onError = Color(0xFF2D0012),
)

/**
 * Shape tokens matching the Border Run spec corner-radius values.
 *
 * | Token       | dp  | Usage                        |
 * |-------------|-----|------------------------------|
 * | extraSmall  | 12  | icon containers, progress    |
 * | small       | 16  | buttons, region / stat cards |
 * | medium      | 22  | main content cards           |
 */
private val BorderRunShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
)

/**
 * Root Material 3 theme for Border Run.
 *
 * Selects between [BorderRunLightColorScheme] and [BorderRunDarkColorScheme]
 * based on [darkTheme], and provides [LocalIsDarkTheme] so screens can switch
 * gradients and other non-Material-token colours accordingly. Dynamic colour
 * (Monet) is intentionally disabled.
 *
 * @param darkTheme When `true` the dark colour scheme is applied.
 * @param content The composable content to apply the theme to.
 */
@Composable
fun BorderRunTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) BorderRunDarkColorScheme else BorderRunLightColorScheme
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BorderRunTypography,
            shapes = BorderRunShapes,
            content = content,
        )
    }
}
