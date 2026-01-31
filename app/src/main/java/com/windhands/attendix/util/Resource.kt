package com.windhands.attendix.util

/**
 * Unified sealed class for representing operation results.
 * Combines Resource and NetworkResult patterns for consistency across the app.
 * Supports both data loading states and network call results.
 */
sealed class Resource<out T> {
    /**
     * Success state with data
     * @param data The successfully loaded data
     */
    data class Success<out T>(val data: T) : Resource<T>()
    
    /**
     * Error state with message and optional error details
     * @param message Error message to display
     * @param code Optional HTTP status code
     * @param errorCode Optional error code from API (e.g., "IP_RESTRICTED")
     */
    data class Error(
        val message: String,
        val code: Int? = null,
        val errorCode: String? = null
    ) : Resource<Nothing>()
    
    /**
     * Loading state
     */
    data object Loading : Resource<Nothing>()
}
