package com.windhands.attendix.util

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object ErrorMessage {
    fun toException(throwable: Throwable): Exception {
        return Exception(toMessage(throwable))
    }

    fun toMessage(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                val body = throwable.response()?.errorBody()?.string()
                val apiMessage = extractMessage(body)
                apiMessage ?: httpStatusMessage(throwable.code())
            }
            is UnknownHostException -> "No internet connection. Please check your network."
            is SocketTimeoutException -> "Request timed out. Please try again."
            is IOException -> "Network error. Please try again."
            else -> throwable.message?.takeIf { it.isNotBlank() }
                ?: "Something went wrong. Please try again."
        }
    }

    private fun extractMessage(body: String?): String? {
        if (body.isNullOrBlank()) return null
        return try {
            val json = JSONObject(body)
            json.optString("message", null)
                ?: json.optString("error", null)
        } catch (e: Exception) {
            null
        }
    }

    private fun httpStatusMessage(code: Int): String {
        return when (code) {
            400 -> "Invalid request. Please check your input."
            401 -> "Unauthorized. Please log in again."
            403 -> "Access denied."
            404 -> "Requested resource was not found."
            409 -> "Conflict detected. Please try again."
            422 -> "Validation failed. Please check your input."
            500 -> "Server error. Please try again later."
            502, 503, 504 -> "Service unavailable. Please try again later."
            else -> "Unexpected error occurred. Please try again."
        }
    }
}
