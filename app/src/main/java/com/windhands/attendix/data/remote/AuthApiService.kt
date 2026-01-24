package com.windhands.attendix.data.remote

import com.windhands.attendix.data.remote.dto.ApiResponse
import com.windhands.attendix.data.remote.dto.LoginRequest
import com.windhands.attendix.data.remote.dto.LoginResponse
import com.windhands.attendix.data.remote.dto.ProfileResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<Unit>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: Map<String, String>): ApiResponse<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: Map<String, String>): ApiResponse<Unit>

    @GET("auth/me")
    suspend fun getProfile(): ApiResponse<ProfileResponse>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: Map<String, @JvmSuppressWildcards Any?>): ApiResponse<ProfileResponse>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: Map<String, String>): ApiResponse<Unit>
}
