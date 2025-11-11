package com.argumentor.app.util

import com.argumentor.app.data.model.Claim
import com.argumentor.app.data.model.Rebuttal
import com.argumentor.app.data.model.Source
import com.argumentor.app.data.model.Topic
import java.net.URL
import java.security.MessageDigest
import java.text.Normalizer

/**
 * Utilities for generating fingerprints for duplicate detection.
 * Uses SHA-256 hashing of normalized text.
 */
object FingerprintUtils {

    /**
     * Maximum text length for similarity comparison (in characters).
     * Prevents DoS attacks with very long strings that would cause O(m*n) memory/time complexity.
     */
    private const val MAX_TEXT_LENGTH = 5000

    /**
     * Fingerprint hash length (in hex characters).
     * Uses first 16 characters of SHA-256 hex digest (64 bits) for compactness
     * while maintaining low collision probability for typical dataset sizes.
     */
    private const val FINGERPRINT_HASH_LENGTH = 16

    /**
     * Normalize text for fingerprinting:
     * - Convert to lowercase
     * - Remove Unicode accents (NFD decomposition + strip)
     * - Remove all punctuation
     * - Collapse whitespace to single spaces
     * - Trim leading/trailing whitespace
     */
    fun normalizeText(text: String): String {
        // NFD decomposition to separate base characters from accents
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)

        // Remove diacritical marks (accents)
        val withoutAccents = normalized.replace("\\p{M}".toRegex(), "")

        // Convert to lowercase
        val lowercase = withoutAccents.lowercase()

        // Remove all punctuation
        val noPunctuation = lowercase.replace("\\p{P}".toRegex(), "")

        // Collapse multiple whitespace to single space and trim
        return noPunctuation.replace("\\s+".toRegex(), " ").trim()
    }

    /**
     * Generate SHA-256 hash of normalized text.
     * Returns first FINGERPRINT_HASH_LENGTH characters of hex digest for compactness.
     */
    fun generateTextFingerprint(text: String): String {
        val normalized = normalizeText(text)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(normalized.toByteArray(Charsets.UTF_8))
        val hexString = hashBytes.joinToString("") { "%02x".format(it) }
        return hexString.take(FINGERPRINT_HASH_LENGTH)
    }

    /**
     * Generate fingerprint for a Claim based on normalized text.
     */
    fun generateClaimFingerprint(claim: Claim): String {
        return generateTextFingerprint(claim.text)
    }

    /**
     * Generate fingerprint for a Rebuttal based on normalized text.
     */
    fun generateRebuttalFingerprint(rebuttal: Rebuttal): String {
        return generateTextFingerprint(rebuttal.text)
    }

    /**
     * Generate fingerprint for a Source based on multiple fields.
     * Uses: title | publisher | date | url_host+path
     */
    fun generateSourceFingerprint(source: Source): String {
        val parts = mutableListOf<String>()

        parts.add(normalizeText(source.title))

        if (!source.publisher.isNullOrBlank()) {
            parts.add(normalizeText(source.publisher))
        }

        if (!source.date.isNullOrBlank()) {
            parts.add(normalizeText(source.date))
        }

        if (!source.url.isNullOrBlank()) {
            try {
                val url = URL(source.url)
                val hostPath = "${url.host}${url.path}"
                parts.add(normalizeText(hostPath))
            } catch (e: Exception) {
                // If URL parsing fails, just use the raw URL
                parts.add(normalizeText(source.url))
            }
        }

        val combined = parts.joinToString("|")
        return generateTextFingerprint(combined)
    }

    /**
     * Generate fingerprint for a Topic based on title and tags.
     * Uses: title | sorted_tags
     */
    fun generateTopicFingerprint(topic: Topic): String {
        val sortedTags = topic.tags.sorted().joinToString(",")
        val combined = "${normalizeText(topic.title)}|$sortedTags"
        return generateTextFingerprint(combined)
    }

    /**
     * Calculate Levenshtein distance with optimized space complexity.
     *
     * Time complexity: O(m × n)
     * Space complexity: O(min(m, n)) instead of O(m × n)
     *
     * @param s1 First string (max MAX_TEXT_LENGTH chars)
     * @param s2 Second string (max MAX_TEXT_LENGTH chars)
     * @return Edit distance between strings
     * @throws IllegalArgumentException if strings exceed max length
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        // Prevent DoS attacks with very long strings
        require(s1.length <= MAX_TEXT_LENGTH && s2.length <= MAX_TEXT_LENGTH) {
            "Text too long for similarity comparison (max: $MAX_TEXT_LENGTH characters)"
        }

        // BUG-009: Prevent integer overflow in array allocation
        // Check that the product of lengths doesn't exceed Int.MAX_VALUE
        require(s1.length.toLong() * s2.length.toLong() <= Int.MAX_VALUE) {
            "Text product too large for similarity comparison"
        }

        // Early exit for identical strings
        if (s1 == s2) return 0
        if (s1.isEmpty()) return s2.length
        if (s2.isEmpty()) return s1.length

        // Use shorter string for inner loop to optimize space
        val shorter = if (s1.length <= s2.length) s1 else s2
        val longer = if (s1.length > s2.length) s1 else s2

        // Only need two rows instead of full matrix
        var previous = IntArray(shorter.length + 1) { it }
        var current = IntArray(shorter.length + 1)

        for (i in 1..longer.length) {
            current[0] = i
            for (j in 1..shorter.length) {
                val cost = if (longer[i - 1] == shorter[j - 1]) 0 else 1
                current[j] = minOf(
                    current[j - 1] + 1,      // insertion
                    previous[j] + 1,          // deletion
                    previous[j - 1] + cost    // substitution
                )
            }
            // Swap arrays instead of copying
            val temp = previous
            previous = current
            current = temp
        }

        return previous[shorter.length]
    }

    /**
     * Calculate similarity ratio between two strings (0.0 to 1.0).
     * 1.0 means identical, 0.0 means completely different.
     *
     * @throws IllegalArgumentException if strings exceed max length for comparison
     */
    fun similarityRatio(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() && s2.isEmpty()) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val distance = levenshteinDistance(s1, s2)
        val maxLen = maxOf(s1.length, s2.length)
        return 1.0 - (distance.toDouble() / maxLen.toDouble())
    }

    /**
     * Check if two texts are similar based on normalized comparison.
     * @param threshold Similarity threshold (0.0-1.0), default 0.90
     * @return true if similarity >= threshold, false if texts too long or not similar
     */
    fun areSimilar(text1: String, text2: String, threshold: Double = 0.90): Boolean {
        return try {
            val normalized1 = normalizeText(text1)
            val normalized2 = normalizeText(text2)
            similarityRatio(normalized1, normalized2) >= threshold
        } catch (e: IllegalArgumentException) {
            // Text too long for comparison - consider not similar
            false
        }
    }
}
