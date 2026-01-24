package com.windhands.attendix.data.remote

import com.windhands.attendix.data.remote.dto.ApiResponse
import com.windhands.attendix.data.remote.dto.AttendanceDto
import com.windhands.attendix.data.remote.dto.AttendanceListResponse
import com.windhands.attendix.data.remote.dto.AttendanceResponse
import com.windhands.attendix.data.remote.dto.AttendanceStatsResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AttendanceApiService {

    @GET("attendance/today")
    suspend fun getTodayStatus(): ApiResponse<AttendanceResponse>

    @POST("attendance/check-in")
    suspend fun checkIn(): ApiResponse<AttendanceDto>

    @POST("attendance/check-out")
    suspend fun checkOut(): ApiResponse<AttendanceDto>

    @GET("attendance/history")
    suspend fun getAttendanceHistory(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30
    ): ApiResponse<AttendanceListResponse>

    @GET("attendance/stats")
    suspend fun getAttendanceStats(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): ApiResponse<AttendanceStatsResponse>

    // Admin endpoints
    @GET("attendance/all")
    suspend fun getAllAttendance(
        @Query("date") date: String? = null,
        @Query("department") department: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30
    ): ApiResponse<List<AttendanceDto>>
}
