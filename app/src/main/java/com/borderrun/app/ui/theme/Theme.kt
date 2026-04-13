package com.borderrun.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Material 3 colour scheme derived from the Border Run design system.
 *
 * Dynamic colour (Monet) is intentionally disabled so the app always renders
 * with the spec's mint-to-sky gradient palette regardless of device wallpaper.
 */
private val BorderRunColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextHeading,
    secondary = SecondaryTeal,
    onSecondary = TextHeading,
    background = GradientMint,         // first gradient stop used as base background
    surface = CardSurface,
    onBackground = TextHeading,
    onSurface = TextBody,
    error = ErrorRed,
    onError = TextHeading,
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
 * Always uses the light colour scheme with Nunito typography and spec-aligned
 * shape tokens. Dark mode and dynamic colour are not supported in v1.
 *
 * @param content The composable content to apply the theme to.
 */
@Composable
fun BorderRunTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BorderRunColorScheme,
        typography = BorderRunTypography,
        shapes = BorderRunShapes,
        content = content,
    )
}
