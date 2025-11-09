package com.argumentor.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Spacing tokens for ArguMentor
 * Provides consistent spacing across the app
 */
object Spacing {
    /** 0dp - No spacing */
    val none: Dp = 0.dp

    /** 2dp - Minimal spacing */
    val extraExtraSmall: Dp = 2.dp

    /** 4dp - Very small spacing, used for tight layouts */
    val extraSmall: Dp = 4.dp

    /** 8dp - Small spacing, used for chip gaps, icon padding */
    val small: Dp = 8.dp

    /** 12dp - Small-Medium spacing, used for card internal spacing */
    val smallMedium: Dp = 12.dp

    /** 16dp - Standard spacing, most common for padding/margins */
    val medium: Dp = 16.dp

    /** 24dp - Large spacing, section separators */
    val large: Dp = 24.dp

    /** 32dp - Extra large spacing, major section separators */
    val extraLarge: Dp = 32.dp

    /** 48dp - Extra extra large spacing, hero sections */
    val extraExtraLarge: Dp = 48.dp

    /** 64dp - Huge spacing, special use cases */
    val huge: Dp = 64.dp
}
