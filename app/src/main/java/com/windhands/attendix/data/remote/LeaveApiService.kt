package com.windhands.attendix.data.remote

import com.windhands.attendix.data.remote.dto.ApiResponse
import com.windhands.attendix.data.remote.dto.ApplyLeaveRequest
import com.windhands.attendix.data.remote.dto.LeaveBalanceResponse
import com.windhands.attendix.data.remote.dto.LeaveRequestDto
import com.windhands.attendix.data.remote.dto.LeaveRequestListResponse
import com.windhands.attendix.data.remote.dto.LeaveRequestResponse
import com.windhands.attendix.data.remote.dto.LeaveReviewRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface LeaveApiService {

    @GET("leave/balance")
    suspend fun getLeaveBalance(): ApiResponse<LeaveBalanceResponse>

    @GET("leave/my-requests")
    suspend fun getLeaveRequests(
        @Query("status") status: String? = null
    ): ApiResponse<List<LeaveRequestDto>>

    @POST("leave/apply")
    suspend fun applyLeave(
        @Body request: ApplyLeaveRequest
    ): ApiResponse<LeaveRequestDto>

    @DELETE("leave/{id}")
    suspend fun cancelLeaveRequest(@Path("id") requestId: Int): ApiResponse<Unit>

    // Admin endpoints
    @GET("leave/all-requests")
    suspend fun getPendingLeaveRequests(
        @Query("status") status: String? = null
    ): ApiResponse<List<LeaveRequestDto>>

    @PUT("leave/{id}/review")
    suspend fun approveLeaveRequest(
        @Path("id") requestId: Int,
        @Body request: LeaveReviewRequest
    ): ApiResponse<Unit>

    @PUT("leave/{id}/review")
    suspend fun rejectLeaveRequest(
        @Path("id") requestId: Int,
        @Body request: LeaveReviewRequest
    ): ApiResponse<Unit>

    @GET("leave/all-requests")
    suspend fun getAllLeaveRequests(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<LeaveRequestDto>>
}
