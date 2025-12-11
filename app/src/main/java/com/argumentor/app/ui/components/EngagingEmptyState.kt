package com.argumentor.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Engaging empty state component with icon, title, description and CTA button.
 * Provides a more user-friendly experience than just showing "No items".
 *
 * @param icon The icon to display (large, with transparency)
 * @param title Main heading text
 * @param description Supporting text that explains what the user can do
 * @param actionText Text for the primary action button (optional)
 * @param onAction Callback when the action button is clicked (optional)
 * @param modifier Optional modifier for the container
 */
@Composable
fun EngagingEmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large icon with transparency for visual hierarchy
        // contentDescription is null because the title provides the semantic meaning
        Icon(
            imageVector = icon,
            contentDescription = null, // Decorative: title provides meaning
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title - attention grabbing
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description - explains what to do
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 400.dp)
        )

        // Primary action button (only show if actionText and onAction are provided)
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null, // Part of button, text provides meaning
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = actionText)
            }
        }
    }
}
