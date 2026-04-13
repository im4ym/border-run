package com.borderrun.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Border Run Material 3 typography scale.
 *
 * Size mapping:
 * - **23sp** — page titles (`displaySmall`, `headlineMedium`)
 * - **17sp** — section headers (`titleLarge`)
 * - **15sp** — body copy (`bodyLarge`)
 * - **13sp** — subtitles / labels (`labelLarge`, `bodyMedium`)
 * - **11sp** — captions (`labelSmall`)
 */
val BorderRunTypography = Typography(
    // 23sp — page title, stat numbers
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 23.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    // 23sp — card / section headings
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 23.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    // 17sp — section headers
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    // 15sp — body copy
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp,
    ),
    // 13sp — subtitles, answer options
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    // 13sp — chip labels, form labels
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // 11sp — captions, metadata
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
