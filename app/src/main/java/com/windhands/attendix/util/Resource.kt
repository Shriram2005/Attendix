package com.windhands.attendix.util

/**
 * A sealed class that encapsulates successful outcome with a value of type [T]
 * or a failure with message and optional data.
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val errorCode: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, errorCode: String? = null, data: T? = null) : Resource<T>(data, message, errorCode)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
