package com.argumentor.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.math.pow

/**
 * WCAG 2.1 Contrast Tests for ArguMentor color palette.
 *
 * WCAG Guidelines:
 * - Level AA: Minimum contrast ratio of 4.5:1 for normal text, 3:1 for large text
 * - Level AAA: Minimum contrast ratio of 7:1 for normal text, 4.5:1 for large text
 *
 * This test file validates that all color combinations used in the app
 * meet accessibility requirements.
 */
class ColorContrastTest {

    /**
     * Calculate relative luminance of a color according to WCAG 2.1
     * https://www.w3.org/TR/WCAG21/#dfn-relative-luminance
     */
    private fun relativeLuminance(color: Color): Double {
        fun adjust(component: Float): Double {
            return if (component <= 0.03928) {
                component / 12.92
            } else {
                ((component + 0.055) / 1.055).pow(2.4)
            }
        }

        val r = adjust(color.red)
        val g = adjust(color.green)
        val b = adjust(color.blue)

        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }

    /**
     * Calculate contrast ratio between two colors according to WCAG 2.1
     * https://www.w3.org/TR/WCAG21/#dfn-contrast-ratio
     */
    private fun contrastRatio(foreground: Color, background: Color): Double {
        val l1 = relativeLuminance(foreground)
        val l2 = relativeLuminance(background)

        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)

        return (lighter + 0.05) / (darker + 0.05)
    }

    // ============================================
    // LIGHT THEME TESTS
    // ============================================

    @Test
    fun `light theme - OnPrimary on Primary meets AA large text`() {
        val ratio = contrastRatio(OnPrimary, Primary)
        assertThat(ratio).isAtLeast(3.0)
        println("OnPrimary/Primary ratio: ${"%.2f".format(ratio)}:1")
    }

    @Test
    fun `light theme - OnBackground on Background meets AAA`() {
        val ratio = contrastRatio(OnBackground, Background)
        assertThat(ratio).isAtLeast(7.0)
        println("OnBackground/Background ratio: ${"%.2f".format(ratio)}:1")
    }

    @Test
    fun `light theme - OnSurface on Surface meets AAA`() {
        val ratio = contrastRatio(OnSurface, Surface)
        assertThat(ratio).isAtLeast(7.0)
        println("OnSurface/Surface ratio: ${"%.2f".format(ratio)}:1")
    }

    @Test
    fun `light theme - OnError on Error meets AA large text`() {
        val ratio = contrastRatio(OnError, Error)
        assertThat(ratio).isAtLeast(3.0)
        println("OnError/Error ratio: ${"%.2f".format(ratio)}:1")
    }

    @Test
    fun `light theme - Primary on Surface meets AA large text`() {
        // Primary color is used for interactive elements (buttons, links)
        // which are typically large text or have visual affordances
        val ratio = contrastRatio(Primary, Surface)
        assertThat(ratio).isAtLeast(3.0) // AA large text requirement
        println("Primary/Surface ratio: ${"%.2f".format(ratio)}:1 (AA large text)")
    }

    // ============================================
    // DARK THEME TESTS
    // ============================================

    @Test
    fun `dark theme - OnBackgroundDark on BackgroundDark meets AA`() {
        val ratio = contrastRatio(OnBackgroundDark, BackgroundDark)
        assertThat(ratio).isAtLeast(4.5)
        println("OnBackgroundDark/BackgroundDark ratio: ${"%.2f".format(ratio)}:1")
    }

    @Test
    fun `dark theme - OnSurfaceDark on SurfaceDark meets AA`() {
        val ratio = contrastRatio(OnSurfaceDark, SurfaceDark)
        assertThat(ratio).isAtLeast(4.5)
        println("OnSurfaceDark/SurfaceDark ratio: ${"%.2f".format(ratio)}:1")
    }

    @Test
    fun `dark theme - OnSurfaceVariantDark on SurfaceDark meets AA`() {
        val ratio = contrastRatio(OnSurfaceVariantDark, SurfaceDark)
        assertThat(ratio).isAtLeast(4.5)
        println("OnSurfaceVariantDark/SurfaceDark ratio: ${"%.2f".format(ratio)}:1")
    }

    @Test
    fun `dark theme - PrimaryDark on BackgroundDark meets AA`() {
        val ratio = contrastRatio(PrimaryDark, BackgroundDark)
        assertThat(ratio).isAtLeast(4.5)
        println("PrimaryDark/BackgroundDark ratio: ${"%.2f".format(ratio)}:1")
    }

    // ============================================
    // SEMANTIC COLOR TESTS (Stance badges)
    // ============================================

    @Test
    fun `stance colors - StancePro with white text meets AA`() {
        val ratio = contrastRatio(Color.White, StancePro)
        assertThat(ratio).isAtLeast(4.5)
        println("White/StancePro ratio: ${"%.2f".format(ratio)}:1")
    }

    @Test
    fun `stance colors - StanceCon with white text meets AA`() {
        val ratio = contrastRatio(Color.White, StanceCon)
        assertThat(ratio).isAtLeast(4.5)
        println("White/StanceCon ratio: ${"%.2f".format(ratio)}:1")
    }

    @Test
    fun `stance colors - StanceNeutral with white text meets AAA`() {
        val ratio = contrastRatio(Color.White, StanceNeutral)
        // Comment in Color.kt says 7.8:1 (AAA)
        assertThat(ratio).isAtLeast(7.0)
        println("White/StanceNeutral ratio: ${"%.2f".format(ratio)}:1")
    }

    // ============================================
    // QUALITY COLOR TESTS
    // ============================================

    @Test
    fun `quality colors - QualityHigh with white text meets AA`() {
        val ratio = contrastRatio(Color.White, QualityHigh)
        assertThat(ratio).isAtLeast(4.5)
        println("White/QualityHigh ratio: ${"%.2f".format(ratio)}:1")
    }

    @Test
    fun `quality colors - QualityMedium with dark text meets AA`() {
        // Orange typically needs dark text for contrast
        val ratio = contrastRatio(Color.Black, QualityMedium)
        assertThat(ratio).isAtLeast(4.5)
        println("Black/QualityMedium ratio: ${"%.2f".format(ratio)}:1")
    }

    @Test
    fun `quality colors - QualityLow with white text meets AA`() {
        val ratio = contrastRatio(Color.White, QualityLow)
        assertThat(ratio).isAtLeast(4.5)
        println("White/QualityLow ratio: ${"%.2f".format(ratio)}:1")
    }

    // ============================================
    // COMPREHENSIVE REPORT
    // ============================================

    @Test
    fun `print all contrast ratios for documentation`() {
        println("\n========== WCAG CONTRAST AUDIT REPORT ==========\n")

        println("LIGHT THEME:")
        println("  OnPrimary/Primary:       ${"%.2f".format(contrastRatio(OnPrimary, Primary))}:1")
        println("  OnBackground/Background: ${"%.2f".format(contrastRatio(OnBackground, Background))}:1")
        println("  OnSurface/Surface:       ${"%.2f".format(contrastRatio(OnSurface, Surface))}:1")
        println("  OnError/Error:           ${"%.2f".format(contrastRatio(OnError, Error))}:1")
        println("  Primary/Surface:         ${"%.2f".format(contrastRatio(Primary, Surface))}:1")

        println("\nDARK THEME:")
        println("  OnBackgroundDark/BackgroundDark:     ${"%.2f".format(contrastRatio(OnBackgroundDark, BackgroundDark))}:1")
        println("  OnSurfaceDark/SurfaceDark:           ${"%.2f".format(contrastRatio(OnSurfaceDark, SurfaceDark))}:1")
        println("  OnSurfaceVariantDark/SurfaceDark:    ${"%.2f".format(contrastRatio(OnSurfaceVariantDark, SurfaceDark))}:1")
        println("  PrimaryDark/BackgroundDark:          ${"%.2f".format(contrastRatio(PrimaryDark, BackgroundDark))}:1")

        println("\nSEMANTIC COLORS (Stance badges with white text):")
        println("  White/StancePro:     ${"%.2f".format(contrastRatio(Color.White, StancePro))}:1")
        println("  White/StanceCon:     ${"%.2f".format(contrastRatio(Color.White, StanceCon))}:1")
        println("  White/StanceNeutral: ${"%.2f".format(contrastRatio(Color.White, StanceNeutral))}:1")

        println("\nQUALITY COLORS:")
        println("  White/QualityHigh:   ${"%.2f".format(contrastRatio(Color.White, QualityHigh))}:1")
        println("  Black/QualityMedium: ${"%.2f".format(contrastRatio(Color.Black, QualityMedium))}:1")
        println("  White/QualityLow:    ${"%.2f".format(contrastRatio(Color.White, QualityLow))}:1")

        println("\nWCAG REQUIREMENTS:")
        println("  AA Normal text:  4.5:1")
        println("  AA Large text:   3.0:1")
        println("  AAA Normal text: 7.0:1")
        println("  AAA Large text:  4.5:1")

        println("\n===============================================\n")

        // This test always passes - it's for documentation
        assertThat(true).isTrue()
    }
}
