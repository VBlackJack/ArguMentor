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
     * Returns first 16 characters of hex digest for compactness.
     */
    fun generateTextFingerprint(text: String): String {
        val normalized = normalizeText(text)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(normalized.toByteArray(Charsets.UTF_8))
        val hexString = hashBytes.joinToString("") { "%02x".format(it) }
        return hexString.take(16) // First 16 characters (64 bits)
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
     * Calculate Levenshtein distance between two strings.
     * Used for fuzzy matching of potential duplicates.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }

        return dp[m][n]
    }

    /**
     * Calculate similarity ratio between two strings (0.0 to 1.0).
     * 1.0 means identical, 0.0 means completely different.
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
     */
    fun areSimilar(text1: String, text2: String, threshold: Double = 0.90): Boolean {
        val normalized1 = normalizeText(text1)
        val normalized2 = normalizeText(text2)
        return similarityRatio(normalized1, normalized2) >= threshold
    }
}
