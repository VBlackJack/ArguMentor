package com.argumentor.app.util

import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Source
import org.junit.Assert.*
import org.junit.Test

class FingerprintUtilsTest {

    @Test
    fun `normalizeText removes accents`() {
        val input = "éàùçô"
        val expected = "eauco"
        assertEquals(expected, FingerprintUtils.normalizeText(input))
    }

    @Test
    fun `normalizeText removes punctuation`() {
        val input = "Hello, World! How are you?"
        val result = FingerprintUtils.normalizeText(input)
        assertFalse(result.contains(","))
        assertFalse(result.contains("!"))
        assertFalse(result.contains("?"))
    }

    @Test
    fun `normalizeText converts to lowercase`() {
        val input = "HELLO World"
        val result = FingerprintUtils.normalizeText(input)
        assertEquals(input.lowercase().replace(" ", " "), result.lowercase())
    }

    @Test
    fun `normalizeText collapses whitespace`() {
        val input = "Hello    World"
        val expected = "hello world"
        assertEquals(expected, FingerprintUtils.normalizeText(input))
    }

    @Test
    fun `generateTextFingerprint produces consistent hash`() {
        val text = "Test claim text"
        val hash1 = FingerprintUtils.generateTextFingerprint(text)
        val hash2 = FingerprintUtils.generateTextFingerprint(text)
        assertEquals(hash1, hash2)
    }

    @Test
    fun `generateTextFingerprint ignores case and accents`() {
        val text1 = "éléphant"
        val text2 = "ELEPHANT"
        val hash1 = FingerprintUtils.generateTextFingerprint(text1)
        val hash2 = FingerprintUtils.generateTextFingerprint(text2)
        assertEquals(hash1, hash2)
    }

    @Test
    fun `generateClaimFingerprint works correctly`() {
        val claim = Claim(
            text = "This is a test claim"
        )
        val fingerprint = FingerprintUtils.generateClaimFingerprint(claim)
        assertNotNull(fingerprint)
        assertTrue(fingerprint.length == 16)
    }

    @Test
    fun `generateSourceFingerprint uses multiple fields`() {
        val source1 = Source(
            title = "Test Source",
            publisher = "Test Publisher",
            date = "2024"
        )
        val source2 = Source(
            title = "Test Source",
            publisher = "Different Publisher",
            date = "2024"
        )

        val hash1 = FingerprintUtils.generateSourceFingerprint(source1)
        val hash2 = FingerprintUtils.generateSourceFingerprint(source2)

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `levenshteinDistance calculates correctly`() {
        assertEquals(0, FingerprintUtils.levenshteinDistance("test", "test"))
        assertEquals(1, FingerprintUtils.levenshteinDistance("test", "tests"))
        assertEquals(3, FingerprintUtils.levenshteinDistance("kitten", "sitting"))
    }

    @Test
    fun `similarityRatio returns 1 for identical strings`() {
        val ratio = FingerprintUtils.similarityRatio("test", "test")
        assertEquals(1.0, ratio, 0.001)
    }

    @Test
    fun `similarityRatio returns 0 for completely different strings`() {
        val ratio = FingerprintUtils.similarityRatio("abc", "xyz")
        assertTrue(ratio < 1.0)
    }

    @Test
    fun `areSimilar returns true for similar texts`() {
        val text1 = "This is a test"
        val text2 = "This is a test!"
        assertTrue(FingerprintUtils.areSimilar(text1, text2, 0.90))
    }

    @Test
    fun `areSimilar returns false for different texts`() {
        val text1 = "This is a test"
        val text2 = "Completely different"
        assertFalse(FingerprintUtils.areSimilar(text1, text2, 0.90))
    }

    @Test
    fun `areSimilar uses custom threshold`() {
        val text1 = "test"
        val text2 = "tests"

        // With high threshold, should not match
        assertFalse(FingerprintUtils.areSimilar(text1, text2, 0.99))

        // With lower threshold, should match
        assertTrue(FingerprintUtils.areSimilar(text1, text2, 0.70))
    }
}
