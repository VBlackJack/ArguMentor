package com.argumentor.app.ui.common

/**
 * Represents UI events that should be handled by the UI layer.
 * Used for one-off events like showing snackbars, navigating back, etc.
 */
sealed class UiEvent {
    /**
     * Show a success message to the user
     */
    data class ShowSnackbar(val message: String) : UiEvent()

    /**
     * Show an error message to the user
     */
    data class ShowError(val error: String) : UiEvent()

    /**
     * Navigate back to the previous screen
     */
    object NavigateBack : UiEvent()
}
