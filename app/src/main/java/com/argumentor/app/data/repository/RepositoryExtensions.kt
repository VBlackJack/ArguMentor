package com.argumentor.app.data.repository

import com.argumentor.app.util.SearchUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll

/**
 * Extension functions for repositories to reduce code duplication.
 */

/**
 * Performs a search with automatic FTS to LIKE fallback.
 *
 * If the query is safe for FTS, attempts FTS search first and falls back to LIKE on error.
 * If the query is unsafe for FTS, uses LIKE search directly.
 *
 * @param query The search query
 * @param ftsSearch Flow that performs FTS search
 * @param likeSearch Flow that performs LIKE search with sanitized query
 * @return Flow of search results
 */
fun <T> searchWithFtsFallback(
    query: String,
    ftsSearch: (String) -> Flow<List<T>>,
    likeSearch: (String) -> Flow<List<T>>
): Flow<List<T>> {
    val sanitizedQuery = SearchUtils.sanitizeLikeQuery(query)

    return if (SearchUtils.isSafeFtsQuery(query)) {
        // Try FTS first
        ftsSearch(query).catch { error ->
            // If FTS fails (e.g., invalid query syntax), fall back to LIKE
            emitAll(likeSearch(sanitizedQuery))
        }
    } else {
        // Query looks unsafe for FTS, use LIKE directly
        likeSearch(sanitizedQuery)
    }
}
