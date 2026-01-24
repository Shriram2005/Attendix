package com.windhands.attendix.data.repository

import com.windhands.attendix.data.remote.LeaveApiService
import com.windhands.attendix.data.remote.dto.ApiResponse
import com.windhands.attendix.data.remote.dto.LeaveBalanceResponse
import com.windhands.attendix.data.remote.dto.LeaveRequestDto
import com.windhands.attendix.data.remote.dto.LeaveRequestListResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaveRepository @Inject constructor(
    private val leaveApiService: LeaveApiService
) {

    suspend fun getLeaveBalance(): Result<ApiResponse<LeaveBalanceResponse>> {
        return try {
            val response = leaveApiService.getLeaveBalance()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLeaveRequests(status: String? = null): Result<ApiResponse<LeaveRequestListResponse>> {
        return try {
            val response = leaveApiService.getLeaveRequests(status)
            // Wrap the array response in LeaveRequestListResponse
            val wrappedData = LeaveRequestListResponse(
                requests = response.data,
                leaves = null,
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

    suspend fun applyLeave(
        leaveType: String,
        startDate: String,
        endDate: String,
        reason: String?
    ): Result<ApiResponse<LeaveRequestDto>> {
        return try {
            val request = mutableMapOf<String, Any?>(
                "leaveType" to leaveType,
                "startDate" to startDate,
                "endDate" to endDate
            )
            reason?.let { request["reason"] = it }

            val response = leaveApiService.applyLeave(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelLeaveRequest(requestId: Int): Result<ApiResponse<Unit>> {
        return try {
            val response = leaveApiService.cancelLeaveRequest(requestId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Admin methods
    suspend fun getPendingLeaveRequests(): Result<ApiResponse<LeaveRequestListResponse>> {
        return try {
            val response = leaveApiService.getPendingLeaveRequests(status = "pending")
            val wrappedData = LeaveRequestListResponse(
                requests = response.data,
                leaves = null,
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

    suspend fun approveLeaveRequest(requestId: Int, remarks: String?): Result<ApiResponse<Unit>> {
        return try {
            val request = mutableMapOf<String, Any?>("status" to "approved")
            if (remarks != null) {
                request["remarks"] = remarks
            }
            val response = leaveApiService.approveLeaveRequest(requestId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectLeaveRequest(requestId: Int, remarks: String?): Result<ApiResponse<Unit>> {
        return try {
            val request = mapOf(
                "status" to "rejected",
                "remarks" to (remarks ?: "Request rejected")
            )
            val response = leaveApiService.rejectLeaveRequest(requestId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllLeaveRequests(
        status: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<ApiResponse<LeaveRequestListResponse>> {
        return try {
            val response = leaveApiService.getAllLeaveRequests(status, page, limit)
            val wrappedData = LeaveRequestListResponse(
                requests = response.data,
                leaves = null,
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
