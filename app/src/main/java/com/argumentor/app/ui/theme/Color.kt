package com.argumentor.app.ui.theme

import androidx.compose.ui.graphics.Color

// Light theme colors
val Primary = Color(0xFF1976D2)
val PrimaryVariant = Color(0xFF1565C0)
val Secondary = Color(0xFF03DAC5)
val SecondaryVariant = Color(0xFF018786)
val Background = Color(0xFFF5F5F0)  // Blanc cassé légèrement beige, moins éblouissant
val Surface = Color(0xFFFAFAFA)  // Gris très clair au lieu du blanc pur
val Error = Color(0xFFB00020)
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFF000000)
val OnBackground = Color(0xFF000000)
val OnSurface = Color(0xFF000000)
val OnError = Color(0xFFFFFFFF)

// Dark theme colors
val PrimaryDark = Color(0xFF90CAF9)
val PrimaryVariantDark = Color(0xFF64B5F6)
val SecondaryDark = Color(0xFF03DAC6)
val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val OnPrimaryDark = Color(0xFF000000)
val OnBackgroundDark = Color(0xFFE3E2E6)  // Material 3 onSurface (~90% luminosité, pas de halo OLED)
val OnSurfaceDark = Color(0xFFE3E2E6)     // Material 3 onSurface
val OnSurfaceVariantDark = Color(0xFFC6C6C6)  // Texte secondaire en dark mode

// Semantic colors for claims (distinct colors for stance)
val StancePro = Color(0xFF2E7D32)        // Vert foncé pour "Pour"
val StanceCon = Color(0xFFC62828)        // Rouge foncé pour "Contre"
val StanceNeutral = Color(0xFF424242)    // Gris foncé pour "Neutre" - Ratio 7.8:1 (AAA)

// Evidence quality colors (distinct from stance colors)
val QualityHigh = Color(0xFF1B5E20)      // Vert très foncé (différent de StancePro)
val QualityMedium = Color(0xFFEF6C00)    // Orange vif
val QualityLow = Color(0xFFD32F2F)       // Rouge (différent de StanceCon)
