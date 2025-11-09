package com.argumentor.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size classes following Material 3 adaptive design guidelines
 * https://m3.material.io/foundations/layout/applying-layout/window-size-classes
 */
enum class WindowWidthSizeClass {
    /** Width < 600dp - Phones in portrait */
    COMPACT,
    /** 600dp <= Width < 840dp - Tablets in portrait, phones in landscape */
    MEDIUM,
    /** Width >= 840dp - Tablets in landscape, desktops */
    EXPANDED
}

enum class WindowHeightSizeClass {
    /** Height < 480dp */
    COMPACT,
    /** 480dp <= Height < 900dp */
    MEDIUM,
    /** Height >= 900dp */
    EXPANDED
}

/**
 * Contains the width and height size classes for the current window
 */
data class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowHeightSizeClass
) {
    /**
     * Whether the current window is in compact width mode (phone portrait)
     */
    val isCompact: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.COMPACT

    /**
     * Whether the current window is in medium width mode (tablet portrait, phone landscape)
     */
    val isMedium: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.MEDIUM

    /**
     * Whether the current window is in expanded width mode (tablet landscape, desktop)
     */
    val isExpanded: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.EXPANDED

    /**
     * Whether the window is large enough for dual-pane layouts
     */
    val supportsDualPane: Boolean
        get() = widthSizeClass >= WindowWidthSizeClass.MEDIUM

    /**
     * Whether the window is large enough for tri-pane layouts
     */
    val supportsTriPane: Boolean
        get() = widthSizeClass == WindowWidthSizeClass.EXPANDED
}

/**
 * Calculates the window size class based on current configuration
 */
@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    return remember(screenWidth, screenHeight) {
        WindowSizeClass(
            widthSizeClass = getWidthSizeClass(screenWidth),
            heightSizeClass = getHeightSizeClass(screenHeight)
        )
    }
}

private fun getWidthSizeClass(width: Dp): WindowWidthSizeClass {
    return when {
        width < 600.dp -> WindowWidthSizeClass.COMPACT
        width < 840.dp -> WindowWidthSizeClass.MEDIUM
        else -> WindowWidthSizeClass.EXPANDED
    }
}

private fun getHeightSizeClass(height: Dp): WindowHeightSizeClass {
    return when {
        height < 480.dp -> WindowHeightSizeClass.COMPACT
        height < 900.dp -> WindowHeightSizeClass.MEDIUM
        else -> WindowHeightSizeClass.EXPANDED
    }
}

/**
 * Recommended content padding based on window size class
 */
fun WindowSizeClass.getContentPadding(): Dp {
    return when (widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> 16.dp
        WindowWidthSizeClass.MEDIUM -> 24.dp
        WindowWidthSizeClass.EXPANDED -> 32.dp
    }
}

/**
 * Recommended content max width based on window size class
 */
fun WindowSizeClass.getContentMaxWidth(): Dp {
    return when (widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> Dp.Infinity
        WindowWidthSizeClass.MEDIUM -> 840.dp
        WindowWidthSizeClass.EXPANDED -> 1200.dp
    }
}

/**
 * Number of columns for grid layouts based on window size class
 */
fun WindowSizeClass.getGridColumns(): Int {
    return when (widthSizeClass) {
        WindowWidthSizeClass.COMPACT -> 1
        WindowWidthSizeClass.MEDIUM -> 2
        WindowWidthSizeClass.EXPANDED -> 3
    }
}
