package com.argumentor.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * Text composable that highlights search query matches
 *
 * @param text The full text to display
 * @param query The search query to highlight (case-insensitive)
 * @param highlightColor The color to use for highlighting (default: yellow with alpha)
 * @param modifier Modifier for the Text composable
 * @param style TextStyle for the Text composable
 * @param maxLines Maximum number of lines
 */
@Composable
fun HighlightedText(
    text: String,
    query: String,
    highlightColor: Color = Color(0xFFFFEB3B).copy(alpha = 0.4f),
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = Int.MAX_VALUE
) {
    if (query.isBlank()) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            maxLines = maxLines
        )
        return
    }

    /**
     * PERFORMANCE FIX: Memoize annotated string calculation.
     * Without remember(), buildAnnotatedString would be recalculated on every recomposition,
     * causing unnecessary CPU usage and potential UI jank.
     */
    val annotatedString = remember(text, query, highlightColor) {
        buildAnnotatedString {
            var currentIndex = 0
            val lowerText = text.lowercase()
            val lowerQuery = query.lowercase()

            while (currentIndex < text.length) {
                val index = lowerText.indexOf(lowerQuery, currentIndex)

                if (index == -1) {
                    // No more matches, append rest of text
                    append(text.substring(currentIndex))
                    break
                }

                // Append text before match
                if (index > currentIndex) {
                    append(text.substring(currentIndex, index))
                }

                // Append highlighted match
                withStyle(SpanStyle(background = highlightColor)) {
                    append(text.substring(index, index + query.length))
                }

                currentIndex = index + query.length
            }
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        maxLines = maxLines
    )
}
