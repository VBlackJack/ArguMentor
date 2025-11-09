package com.argumentor.app.ui.common

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Modifier that adds a spring-based scale animation when pressed
 * Makes interactions feel more natural and responsive
 *
 * @param pressedScale The scale to animate to when pressed (default 0.95f)
 * @param onClick Lambda to execute on click
 */
fun Modifier.springClickable(
    pressedScale: Float = 0.95f,
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "spring_scale"
    )

    this
        .scale(scale)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    val released = try {
                        tryAwaitRelease()
                    } catch (_: Exception) {
                        false
                    }
                    isPressed = false
                    released
                },
                onTap = { onClick() }
            )
        }
}

/**
 * Common spring animation specs for consistency across the app
 */
object SpringAnimationSpecs {
    /**
     * Bouncy spring for playful interactions (cards, buttons)
     */
    val Bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    /**
     * Low bouncy spring for subtle interactions (list items, chips)
     */
    val LowBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * No bounce spring for smooth transitions (navigation, fades)
     */
    val NoBounce = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /**
     * High stiffness spring for quick, responsive interactions
     */
    val Quick = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
}
