package com.windhands.attendix.util

/**
 * Sealed class to represent network call results
 */
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val errorCode: String? = null
    ) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}
