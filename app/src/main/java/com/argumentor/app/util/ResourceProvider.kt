package com.argumentor.app.util

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides access to Android string resources in a testable way.
 * This class can be injected into ViewModels and other classes that need
 * to access string resources without directly depending on Context.
 */
@Singleton
class ResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Gets a string from resources by its resource ID.
     */
    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    /**
     * Gets a formatted string from resources with arguments.
     */
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}
