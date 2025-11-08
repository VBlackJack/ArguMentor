package com.argumentor.app.util

/**
 * Utilities for handling search queries, especially FTS (Full-Text Search) queries.
 */
object SearchUtils {

    /**
     * Characters that are FTS operators and need to be handled carefully.
     * Common FTS special characters: " * - ( ) AND OR NOT
     */
    private val FTS_SPECIAL_CHARS = setOf('"', '*', '-', '(', ')', '[', ']')

    /**
     * FTS operators that might cause issues if used incorrectly.
     */
    private val FTS_OPERATORS = setOf("AND", "OR", "NOT", "NEAR")

    /**
     * Check if a query contains FTS operators or special characters that might cause errors.
     * @return true if the query is likely safe for FTS, false if it should use fallback LIKE
     */
    fun isSafeFtsQuery(query: String): Boolean {
        // Check for unbalanced quotes
        val quoteCount = query.count { it == '"' }
        if (quoteCount % 2 != 0) return false

        // Check for unbalanced parentheses
        var parenBalance = 0
        for (char in query) {
            when (char) {
                '(' -> parenBalance++
                ')' -> parenBalance--
            }
            if (parenBalance < 0) return false
        }
        if (parenBalance != 0) return false

        // Query is safe for FTS
        return true
    }

    /**
     * Sanitize a query for FTS by escaping special characters.
     * This wraps the query in quotes and escapes internal quotes.
     */
    fun sanitizeFtsQuery(query: String): String {
        // If the query is already quoted or contains operators, return as-is
        if (query.startsWith('"') && query.endsWith('"')) {
            return query
        }

        // If query contains FTS operators (AND, OR, NOT), return as-is
        val upperQuery = query.uppercase()
        if (FTS_OPERATORS.any { upperQuery.contains(" $it ") }) {
            return query
        }

        // Escape internal quotes and wrap in quotes for phrase search
        return "\"${query.replace("\"", "\"\"")}\""
    }

    /**
     * Clean a query for LIKE search by removing wildcards that users shouldn't control.
     * This prevents SQL injection-like patterns in LIKE queries.
     */
    fun sanitizeLikeQuery(query: String): String {
        // Remove or escape % and _ which are LIKE wildcards
        // We allow them to be used by the system (in the query template), not by the user
        return query.replace("%", "").replace("_", "")
    }
}
