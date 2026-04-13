package com.borderrun.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Background gradient stops ───────────────────────────────────────────────
/** Mint — first stop of the app background gradient. */
val GradientMint = Color(0xFFDDFCE6)

/** Teal — second stop of the app background gradient. */
val GradientTeal = Color(0xFFCCFBF1)

/** Cyan — third stop of the app background gradient. */
val GradientCyan = Color(0xFFCFFAFE)

/** Sky — fourth (final) stop of the app background gradient. */
val GradientSky = Color(0xFFE0F2FE)

// ── Brand colours ────────────────────────────────────────────────────────────
/** Primary emerald green used for key actions and the CTA gradient start. */
val PrimaryGreen = Color(0xFF10B981)

/** Secondary teal used for accents and the CTA gradient end. */
val SecondaryTeal = Color(0xFF0891B2)

// ── Text hierarchy ───────────────────────────────────────────────────────────
/** Deep emerald for page titles and headings. */
val TextHeading = Color(0xFF064E3B)

/** Mid grey for body copy. */
val TextBody = Color(0xFF6B7280)

/** Light grey for captions and muted labels. */
val TextMuted = Color(0xFF9CA3AF)

// ── Card surface ─────────────────────────────────────────────────────────────
/** Frosted-glass white for card backgrounds (55 % opacity). */
val CardSurface = Color(0x8CFFFFFF)

/** Semi-opaque white for card borders (80 % opacity). */
val CardBorder = Color(0xCCFFFFFF)

// ── Feedback ─────────────────────────────────────────────────────────────────
/** Success green for correct answers and positive states. */
val SuccessGreen = Color(0xFF059669)

/** Error red for wrong answers and destructive actions. */
val ErrorRed = Color(0xFFE11D48)

// ── Region accent colours ────────────────────────────────────────────────────
/** Asia primary accent (pink). */
val RegionAsiaPrimary = Color(0xFFEC4899)

/** Asia gradient start (light pink). */
val RegionAsiaGradientStart = Color(0xFFF472B6)

/** Europe primary accent (purple). */
val RegionEuropePrimary = Color(0xFF8B5CF6)

/** Europe gradient start (light purple). */
val RegionEuropeGradientStart = Color(0xFFA78BFA)

/** Africa primary accent (cyan). */
val RegionAfricaPrimary = Color(0xFF06B6D4)

/** Africa gradient start (light cyan). */
val RegionAfricaGradientStart = Color(0xFF22D3EE)

/** Americas primary accent (amber). */
val RegionAmericasPrimary = Color(0xFFF59E0B)

/** Americas gradient start (light amber). */
val RegionAmericasGradientStart = Color(0xFFFBBF24)

/** Oceania primary accent (green). */
val RegionOceaniaPrimary = Color(0xFF10B981)

/** Oceania gradient start (light green). */
val RegionOceaniaGradientStart = Color(0xFF34D399)

// ── CTA button gradient ───────────────────────────────────────────────────────
/** Start colour of the primary call-to-action button gradient. */
val CtaGradientStart = PrimaryGreen

/** End colour of the primary call-to-action button gradient. */
val CtaGradientEnd = SecondaryTeal
