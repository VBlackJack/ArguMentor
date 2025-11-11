package com.argumentor.app.util

import java.time.Instant

/**
 * Validation utilities for input validation across the application.
 * Provides consistent validation rules for text fields, URLs, and other inputs.
 */
object ValidationUtils {

    /**
     * Minimum text length for most text fields.
     */
    const val MIN_TEXT_LENGTH = 3

    /**
     * Maximum text length for short fields (titles, labels).
     */
    const val MAX_SHORT_TEXT_LENGTH = 200

    /**
     * Maximum text length for long fields (content, descriptions).
     */
    const val MAX_LONG_TEXT_LENGTH = 10000

    /**
     * Result of a validation check.
     */
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()

        val isValid: Boolean get() = this is Valid
        val errorMessage: String? get() = (this as? Invalid)?.message
    }

    /**
     * Validates text with minimum and maximum length constraints.
     *
     * @param text Text to validate
     * @param fieldName Name of the field for error messages
     * @param minLength Minimum allowed length (default: MIN_TEXT_LENGTH)
     * @param maxLength Maximum allowed length (default: MAX_LONG_TEXT_LENGTH)
     * @return ValidationResult indicating if valid or containing error message
     */
    fun validateText(
        text: String,
        fieldName: String,
        minLength: Int = MIN_TEXT_LENGTH,
        maxLength: Int = MAX_LONG_TEXT_LENGTH
    ): ValidationResult {
        return when {
            text.isBlank() -> ValidationResult.Invalid("$fieldName cannot be empty")
            text.trim().length < minLength -> ValidationResult.Invalid("$fieldName must be at least $minLength characters")
            text.length > maxLength -> ValidationResult.Invalid("$fieldName cannot exceed $maxLength characters")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates a short text field (e.g., title, label).
     *
     * @param text Text to validate
     * @param fieldName Name of the field for error messages
     * @return ValidationResult indicating if valid or containing error message
     */
    fun validateShortText(text: String, fieldName: String): ValidationResult {
        return validateText(text, fieldName, MIN_TEXT_LENGTH, MAX_SHORT_TEXT_LENGTH)
    }

    /**
     * Validates a URL format with security checks.
     *
     * SECURITY FIX (SEC-005): Enhanced URL validation to prevent XSS and other attacks:
     * - Only allows HTTP and HTTPS protocols
     * - Validates port numbers are in valid range (1-65535)
     * - Blocks dangerous protocols (javascript:, data:, file:, vbscript:)
     * - Properly validates domain structure
     *
     * @param url URL string to validate
     * @return ValidationResult indicating if valid or containing error message
     */
    fun validateUrl(url: String): ValidationResult {
        if (url.isBlank()) {
            return ValidationResult.Valid // URLs are often optional
        }

        val trimmedUrl = url.trim()

        // Check for dangerous protocols first
        val lowercaseUrl = trimmedUrl.lowercase()
        val dangerousProtocols = listOf("javascript:", "data:", "file:", "vbscript:", "about:", "blob:")
        if (dangerousProtocols.any { lowercaseUrl.startsWith(it) }) {
            return ValidationResult.Invalid("Unsupported or dangerous URL protocol")
        }

        // Ensure HTTP/HTTPS protocol is present or can be assumed
        val urlWithProtocol = if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            "https://$trimmedUrl"
        } else {
            trimmedUrl
        }

        // Validate URL structure with proper constraints
        return try {
            val url = java.net.URL(urlWithProtocol)

            // Validate protocol is HTTP or HTTPS only
            if (url.protocol != "http" && url.protocol != "https") {
                return ValidationResult.Invalid("Only HTTP and HTTPS protocols are allowed")
            }

            // Validate port is in valid range (if specified)
            val port = url.port
            if (port != -1 && (port < 1 || port > 65535)) {
                return ValidationResult.Invalid("Invalid port number (must be 1-65535)")
            }

            // Validate host is not empty and has reasonable format
            if (url.host.isBlank() || url.host.length > 253) {
                return ValidationResult.Invalid("Invalid domain name")
            }

            // Additional checks for suspicious patterns
            if (url.host.contains("..") || url.host.startsWith(".") || url.host.endsWith(".")) {
                return ValidationResult.Invalid("Invalid domain format")
            }

            ValidationResult.Valid
        } catch (e: java.net.MalformedURLException) {
            ValidationResult.Invalid("Invalid URL format: ${e.message ?: "malformed URL"}")
        } catch (e: Exception) {
            ValidationResult.Invalid("Invalid URL format")
        }
    }

    /**
     * Validates reliability score is within 0.0-1.0 range.
     *
     * @param score Score to validate
     * @return ValidationResult indicating if valid or containing error message
     */
    fun validateReliabilityScore(score: Double?): ValidationResult {
        if (score == null) {
            return ValidationResult.Valid // Score is optional
        }

        return when {
            score < 0.0 -> ValidationResult.Invalid("Reliability score cannot be negative")
            score > 1.0 -> ValidationResult.Invalid("Reliability score cannot exceed 1.0")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates an ISO 8601 timestamp format.
     * Accepts timestamps in the format: "2025-11-08T13:00:00Z"
     *
     * @param timestamp Timestamp string to validate
     * @param fieldName Name of the field for error messages (default: "Timestamp")
     * @return ValidationResult indicating if valid or containing error message
     */
    fun validateIsoTimestamp(timestamp: String, fieldName: String = "Timestamp"): ValidationResult {
        if (timestamp.isBlank()) {
            return ValidationResult.Invalid("$fieldName cannot be empty")
        }

        return try {
            Instant.parse(timestamp)
            ValidationResult.Valid
        } catch (e: Exception) {
            ValidationResult.Invalid("$fieldName must be in ISO 8601 format (e.g., 2025-11-08T13:00:00Z)")
        }
    }

    /**
     * Validates that a list is not empty.
     *
     * @param list List to validate
     * @param fieldName Name of the field for error messages
     * @return ValidationResult indicating if valid or containing error message
     */
    fun <T> validateNotEmpty(list: List<T>, fieldName: String): ValidationResult {
        return if (list.isEmpty()) {
            ValidationResult.Invalid("$fieldName cannot be empty")
        } else {
            ValidationResult.Valid
        }
    }

    /**
     * Combines multiple validation results.
     * Returns the first invalid result, or Valid if all are valid.
     *
     * @param results Validation results to combine
     * @return First invalid result, or Valid if all valid
     */
    fun combineResults(vararg results: ValidationResult): ValidationResult {
        return results.firstOrNull { !it.isValid } ?: ValidationResult.Valid
    }
}
