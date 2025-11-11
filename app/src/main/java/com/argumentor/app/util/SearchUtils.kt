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
     *
     * Enhanced security checks:
     * - Validates quote nesting and balance
     * - Checks for NEAR operator with distance syntax (NEAR/n)
     * - Validates parentheses balance
     * - Rejects complex FTS expressions
     *
     * @return true if the query is likely safe for FTS, false if it should use fallback LIKE
     */
    fun isSafeFtsQuery(query: String): Boolean {
        val trimmed = query.trim()

        // Empty queries are safe
        if (trimmed.isEmpty()) return true

        // Reject queries with any standalone FTS special characters. They tend to
        // trigger syntax errors unless the caller knows how to escape them.
        if (trimmed.any { it in FTS_SPECIAL_CHARS }) {
            return false
        }

        // Check for NEAR operator with distance (e.g., NEAR/5)
        // This pattern: NEAR followed by optional slash and digit
        if (Regex("\\bNEAR(/\\d+)?\\b", RegexOption.IGNORE_CASE).containsMatchIn(trimmed)) {
            return false
        }

        // Reject queries that rely on explicit FTS operators. This keeps the public
        // search box consistent and falls back to LIKE when users attempt to craft
        // advanced expressions.
        val tokens = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokens.any { token -> FTS_OPERATORS.contains(token.uppercase()) }) {
            return false
        }

        // Enhanced quote validation: Check for nested quotes and proper balance
        if (!areQuotesValid(trimmed)) {
            return false
        }

        // Check for unbalanced brackets
        if (!areBracketsBalanced(trimmed, '[', ']')) {
            return false
        }

        // Check for unbalanced parentheses
        if (!areBracketsBalanced(trimmed, '(', ')')) {
            return false
        }

        // Reject queries with multiple consecutive operators or special chars
        if (Regex("[*\\-]{2,}").containsMatchIn(trimmed)) {
            return false
        }

        return true
    }

    /**
     * Validates that quotes are properly balanced and not nested incorrectly.
     * Also checks for escaped quotes within quoted strings.
     */
    private fun areQuotesValid(text: String): Boolean {
        var inQuote = false
        var escapeNext = false
        var quoteCount = 0

        for (i in text.indices) {
            val char = text[i]

            if (escapeNext) {
                escapeNext = false
                continue
            }

            when (char) {
                '\\' -> escapeNext = true
                '"' -> {
                    inQuote = !inQuote
                    quoteCount++
                }
            }
        }

        // Quotes must be balanced (even count) and not end while still in a quote
        return quoteCount % 2 == 0 && !inQuote && !escapeNext
    }

    /**
     * Validates that brackets (parentheses, square brackets, etc.) are properly balanced.
     */
    private fun areBracketsBalanced(text: String, open: Char, close: Char): Boolean {
        var balance = 0
        for (char in text) {
            when (char) {
                open -> balance++
                close -> balance--
            }
            // Closing bracket without matching opening bracket
            if (balance < 0) return false
        }
        // All brackets must be closed
        return balance == 0
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
     * Escape SQL LIKE wildcards in user input to prevent wildcard injection.
     *
     * SECURITY FIX (SEC-004): Escapes % and _ characters with backslash instead
     * of removing them, allowing users to search for these characters while
     * preventing wildcard injection attacks. Use with ESCAPE '\' clause in SQL.
     *
     * @param query The user input query
     * @return The query with SQL wildcards properly escaped
     */
    fun sanitizeLikeQuery(query: String): String {
        // Escape backslash first to avoid double-escaping
        var escaped = query.replace("\\", "\\\\")
        // Escape LIKE wildcards: % (matches any sequence) and _ (matches single char)
        escaped = escaped.replace("%", "\\%")
        escaped = escaped.replace("_", "\\_")
        return escaped
    }
}
