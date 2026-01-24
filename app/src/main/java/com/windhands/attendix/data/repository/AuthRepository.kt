package com.windhands.attendix.data.repository

import com.windhands.attendix.data.remote.AuthApiService
import com.windhands.attendix.data.remote.dto.LoginRequest
import com.windhands.attendix.data.remote.dto.LoginResponse
import com.windhands.attendix.data.remote.dto.ProfileResponse
import com.windhands.attendix.data.remote.dto.ApiResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService
) {

    suspend fun login(employeeId: String, password: String): Result<LoginResponse> {
        return try {
            val response = authApiService.login(LoginRequest(employeeId, password))
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: response.error ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> {
        return try {
            val response = authApiService.logout()
            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Logout failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<ApiResponse<Unit>> {
        return try {
            val response = authApiService.forgotPassword(mapOf("email" to email))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(token: String, newPassword: String): Result<ApiResponse<Unit>> {
        return try {
            val response = authApiService.resetPassword(
                mapOf("token" to token, "newPassword" to newPassword)
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            val response = authApiService.getProfile()
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        phone: String
    ): Result<ProfileResponse> {
        return try {
            val response = authApiService.updateProfile(
                mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "phone" to phone
                )
            )
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<ApiResponse<Unit>> {
        return try {
            val response = authApiService.changePassword(
                mapOf(
                    "currentPassword" to currentPassword,
                    "newPassword" to newPassword
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
