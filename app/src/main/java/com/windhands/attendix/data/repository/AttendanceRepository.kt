package com.windhands.attendix.data.repository

import com.windhands.attendix.data.remote.AttendanceApiService
import com.windhands.attendix.data.remote.dto.ApiResponse
import com.windhands.attendix.data.remote.dto.AttendanceDto
import com.windhands.attendix.data.remote.dto.AttendanceListResponse
import com.windhands.attendix.data.remote.dto.AttendanceStatsResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val attendanceApiService: AttendanceApiService
) {

    suspend fun getTodayStatus(): Result<ApiResponse<AttendanceDto>> {
        return try {
            val response = attendanceApiService.getTodayStatus()
            Result.success(
                ApiResponse(
                    success = response.success,
                    message = response.message,
                    data = response.data?.attendance
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkIn(): Result<ApiResponse<AttendanceDto>> {
        return try {
            val response = attendanceApiService.checkIn()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkOut(): Result<ApiResponse<AttendanceDto>> {
        return try {
            val response = attendanceApiService.checkOut()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAttendanceHistory(
        startDate: String? = null,
        endDate: String? = null,
        status: String? = null,
        page: Int = 1,
        limit: Int = 30
    ): Result<ApiResponse<AttendanceListResponse>> {
        return try {
            val response = attendanceApiService.getAttendanceHistory(
                startDate, endDate, status, page, limit
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAttendanceStats(
        month: Int? = null,
        year: Int? = null
    ): Result<ApiResponse<AttendanceStatsResponse>> {
        return try {
            val response = attendanceApiService.getAttendanceStats(month, year)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Admin methods
    suspend fun getAllAttendance(
        date: String? = null,
        department: String? = null,
        status: String? = null,
        page: Int = 1,
        limit: Int = 30
    ): Result<ApiResponse<AttendanceListResponse>> {
        return try {
            val response = attendanceApiService.getAllAttendance(
                date, department, status, page, limit
            )
            val wrappedData = AttendanceListResponse(
                attendance = null,
                records = response.data,
                pagination = null
            )
            Result.success(ApiResponse(
                success = response.success,
                message = response.message,
                data = wrappedData
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
