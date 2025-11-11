package com.argumentor.app.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Common error handling utilities for ViewModels.
 * Provides consistent patterns for async operations with proper error logging and state management.
 */

/**
 * Execute an operation with standardized error handling.
 * Automatically logs errors and updates error/loading state flows.
 *
 * @param errorState MutableStateFlow to update with error messages
 * @param loadingState Optional MutableStateFlow to track loading state
 * @param errorPrefix Prefix for the error log message (e.g., "Failed to save claim")
 * @param onSuccess Callback invoked on successful operation
 * @param operation The suspending operation to execute
 */
fun ViewModel.launchWithErrorHandling(
    errorState: MutableStateFlow<String?>,
    loadingState: MutableStateFlow<Boolean>? = null,
    errorPrefix: String,
    onSuccess: () -> Unit = {},
    operation: suspend () -> Unit
) {
    viewModelScope.launch {
        loadingState?.value = true
        errorState.value = null

        runCatching {
            operation()
        }.onSuccess {
            onSuccess()
        }.onFailure { error ->
            Timber.e(error, errorPrefix)
            errorState.value = error.message ?: "Unknown error"
        }

        loadingState?.value = false
    }
}

/**
 * Execute an operation with standardized error handling and custom error message formatting.
 * Useful when you need to format the error message with a ResourceProvider.
 *
 * @param errorState MutableStateFlow to update with error messages
 * @param loadingState Optional MutableStateFlow to track loading state
 * @param errorPrefix Prefix for the error log message (e.g., "Failed to save claim")
 * @param errorMessageFormatter Function to format error message from exception
 * @param onSuccess Callback invoked on successful operation
 * @param operation The suspending operation to execute
 */
fun ViewModel.launchWithErrorHandling(
    errorState: MutableStateFlow<String?>,
    loadingState: MutableStateFlow<Boolean>? = null,
    errorPrefix: String,
    errorMessageFormatter: (Throwable) -> String,
    onSuccess: () -> Unit = {},
    operation: suspend () -> Unit
) {
    viewModelScope.launch {
        loadingState?.value = true
        errorState.value = null

        runCatching {
            operation()
        }.onSuccess {
            onSuccess()
        }.onFailure { error ->
            Timber.e(error, errorPrefix)
            errorState.value = errorMessageFormatter(error)
        }

        loadingState?.value = false
    }
}

/**
 * Execute an operation with standardized error handling that returns a result.
 * Automatically logs errors and updates error/loading state flows.
 *
 * @param T The type of result returned by the operation
 * @param errorState MutableStateFlow to update with error messages
 * @param loadingState Optional MutableStateFlow to track loading state
 * @param errorPrefix Prefix for the error log message (e.g., "Failed to load data")
 * @param onSuccess Callback invoked with the result on successful operation
 * @param onError Optional callback invoked on error (after state update)
 * @param operation The suspending operation to execute that returns a result
 */
fun <T> ViewModel.launchWithErrorHandling(
    errorState: MutableStateFlow<String?>,
    loadingState: MutableStateFlow<Boolean>? = null,
    errorPrefix: String,
    onSuccess: (T) -> Unit,
    onError: ((Throwable) -> Unit)? = null,
    operation: suspend () -> T
) {
    viewModelScope.launch {
        loadingState?.value = true
        errorState.value = null

        runCatching {
            operation()
        }.onSuccess { result ->
            onSuccess(result)
        }.onFailure { error ->
            Timber.e(error, errorPrefix)
            errorState.value = error.message ?: "Unknown error"
            onError?.invoke(error)
        }

        loadingState?.value = false
    }
}

/**
 * Extension to launch a coroutine in viewModelScope with automatic exception logging.
 * Use this for fire-and-forget operations where you don't need explicit error state management.
 *
 * @param errorPrefix Prefix for the error log message
 * @param block The suspending block to execute
 */
fun ViewModel.launchWithLogging(
    errorPrefix: String,
    block: suspend CoroutineScope.() -> Unit
) {
    viewModelScope.launch {
        try {
            block()
        } catch (e: Exception) {
            Timber.e(e, errorPrefix)
        }
    }
}
