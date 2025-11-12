package com.argumentor.app.util

import android.content.Context
import com.argumentor.app.R
import java.time.Instant

/**
 * Validation utilities for input validation across the application.
 * Provides consistent validation rules for text fields, URLs, and other inputs.
 *
 * INTERNATIONALIZATION: All validation functions now require Context parameter to use
 * string resources instead of hardcoded English error messages.
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
     * INTERNATIONALIZATION: Now uses string resources for error messages.
     *
     * @param context Context to access string resources
     * @param text Text to validate
     * @param fieldName Name of the field for error messages
     * @param minLength Minimum allowed length (default: MIN_TEXT_LENGTH)
     * @param maxLength Maximum allowed length (default: MAX_LONG_TEXT_LENGTH)
     * @return ValidationResult indicating if valid or containing error message
     */
    fun validateText(
        context: Context,
        text: String,
        fieldName: String,
        minLength: Int = MIN_TEXT_LENGTH,
        maxLength: Int = MAX_LONG_TEXT_LENGTH
    ): ValidationResult {
        // ISSUE-002 FIX: Trim once at the beginning for consistency
        // Use trimmed text for all checks (blank, min length, max length)
        val trimmedText = text.trim()
        return when {
            trimmedText.isBlank() -> ValidationResult.Invalid(context.getString(R.string.validation_field_empty, fieldName))
            trimmedText.length < minLength -> ValidationResult.Invalid(context.getString(R.string.validation_field_min_length, fieldName, minLength))
            trimmedText.length > maxLength -> ValidationResult.Invalid(context.getString(R.string.validation_field_max_length, fieldName, maxLength))
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates a short text field (e.g., title, label).
     *
     * @param context Context to access string resources
     * @param text Text to validate
     * @param fieldName Name of the field for error messages
     * @return ValidationResult indicating if valid or containing error message
     */
    fun validateShortText(context: Context, text: String, fieldName: String): ValidationResult {
        return validateText(context, text, fieldName, MIN_TEXT_LENGTH, MAX_SHORT_TEXT_LENGTH)
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
     * INTERNATIONALIZATION: Now uses string resources for error messages.
     *
     * @param context Context to access string resources
     * @param url URL string to validate
     * @return ValidationResult indicating if valid or containing error message
     */
    fun validateUrl(context: Context, url: String): ValidationResult {
        if (url.isBlank()) {
            return ValidationResult.Valid // URLs are often optional
        }

        val trimmedUrl = url.trim()

        // Check for dangerous protocols first
        val lowercaseUrl = trimmedUrl.lowercase()
        val dangerousProtocols = listOf("javascript:", "data:", "file:", "vbscript:", "about:", "blob:")
        if (dangerousProtocols.any { lowercaseUrl.startsWith(it) }) {
            return ValidationResult.Invalid(context.getString(R.string.validation_url_invalid_protocol))
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
                return ValidationResult.Invalid(context.getString(R.string.validation_url_http_only))
            }

            // Validate port is in valid range (if specified)
            val port = url.port
            if (port != -1 && (port < 1 || port > 65535)) {
                return ValidationResult.Invalid(context.getString(R.string.validation_url_invalid_port))
            }

            // Validate host is not empty and has reasonable format
            if (url.host.isBlank() || url.host.length > 253) {
                return ValidationResult.Invalid(context.getString(R.string.validation_url_invalid_domain))
            }

            // Additional checks for suspicious patterns
            if (url.host.contains("..") || url.host.startsWith(".") || url.host.endsWith(".")) {
                return ValidationResult.Invalid(context.getString(R.string.validation_url_invalid_domain))
            }

            ValidationResult.Valid
        } catch (e: java.net.MalformedURLException) {
            ValidationResult.Invalid(context.getString(R.string.validation_url_invalid_format))
        } catch (e: Exception) {
            ValidationResult.Invalid(context.getString(R.string.validation_url_invalid_format))
        }
    }

    /**
     * Validates reliability score is within 0.0-1.0 range.
     *
     * INTERNATIONALIZATION: Now uses string resources for error messages.
     *
     * @param context Context to access string resources
     * @param score Score to validate
     * @return ValidationResult indicating if valid or containing error message
     */
    fun validateReliabilityScore(context: Context, score: Double?): ValidationResult {
        if (score == null) {
            return ValidationResult.Valid // Score is optional
        }

        return when {
            score < 0.0 || score > 1.0 -> ValidationResult.Invalid(context.getString(R.string.validation_score_invalid))
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates an ISO 8601 timestamp format.
     * Accepts timestamps in the format: "2025-11-08T13:00:00Z"
     *
     * INTERNATIONALIZATION: Now uses string resources for error messages.
     *
     * @param context Context to access string resources
     * @param timestamp Timestamp string to validate
     * @param fieldName Name of the field for error messages
     * @return ValidationResult indicating if valid or containing error message
     */
    fun validateIsoTimestamp(context: Context, timestamp: String, fieldName: String): ValidationResult {
        if (timestamp.isBlank()) {
            return ValidationResult.Invalid(context.getString(R.string.validation_field_empty, fieldName))
        }

        return try {
            Instant.parse(timestamp)
            ValidationResult.Valid
        } catch (e: Exception) {
            // ERROR MESSAGE FIX: Use timestamp-specific error message instead of URL error
            ValidationResult.Invalid(context.getString(R.string.validation_timestamp_invalid_format))
        }
    }

    /**
     * Validates that a list is not empty.
     *
     * INTERNATIONALIZATION: Now uses string resources for error messages.
     *
     * @param context Context to access string resources
     * @param list List to validate
     * @param fieldName Name of the field for error messages
     * @return ValidationResult indicating if valid or containing error message
     */
    fun <T> validateNotEmpty(context: Context, list: List<T>, fieldName: String): ValidationResult {
        return if (list.isEmpty()) {
            ValidationResult.Invalid(context.getString(R.string.validation_field_empty, fieldName))
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
