package com.argumentor.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Shape tokens for ArguMentor
 * Defines corner radius for different component types
 */
val Shapes = Shapes(
    /** Extra small shapes (4dp) - Chips, small buttons */
    extraSmall = RoundedCornerShape(4.dp),

    /** Small shapes (8dp) - Badges, small cards */
    small = RoundedCornerShape(8.dp),

    /** Medium shapes (12dp) - Standard cards, buttons */
    medium = RoundedCornerShape(12.dp),

    /** Large shapes (16dp) - Large cards, dialogs */
    large = RoundedCornerShape(16.dp),

    /** Extra large shapes (28dp) - FAB, special components */
    extraLarge = RoundedCornerShape(28.dp)
)
