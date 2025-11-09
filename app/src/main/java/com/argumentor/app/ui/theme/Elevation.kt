package com.argumentor.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Elevation tokens for ArguMentor
 * Provides consistent elevation/shadow depths
 */
object Elevation {
    /** Level 0 - No elevation (flat surfaces) */
    val level0: Dp = 0.dp

    /** Level 1 - Minimal elevation (1dp) - Used for cards at rest */
    val level1: Dp = 1.dp

    /** Level 2 - Low elevation (3dp) - Elevated cards, buttons */
    val level2: Dp = 3.dp

    /** Level 3 - Medium elevation (6dp) - FAB, dialogs */
    val level3: Dp = 6.dp

    /** Level 4 - High elevation (8dp) - Navigation drawer */
    val level4: Dp = 8.dp

    /** Level 5 - Highest elevation (12dp) - Modal bottom sheets */
    val level5: Dp = 12.dp
}
