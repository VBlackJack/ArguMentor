package com.argumentor.app.ui.common

/**
 * Represents the different states of a UI screen
 * @param T the type of data to display
 */
sealed class UiState<out T> {
    /**
     * Initial state, nothing has happened yet
     */
    object Initial : UiState<Nothing>()

    /**
     * Loading state while data is being fetched
     */
    object Loading : UiState<Nothing>()

    /**
     * Success state with data
     * @param data the loaded data
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Error state when something went wrong
     * @param message the error message to display
     * @param exception the optional exception that caused the error
     */
    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : UiState<Nothing>()

    /**
     * Empty state when there is no data (but no error either)
     */
    object Empty : UiState<Nothing>()
}
