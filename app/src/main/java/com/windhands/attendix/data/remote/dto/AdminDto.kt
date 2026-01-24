package com.windhands.attendix.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null,
    @Json(name = "data") val data: T? = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "code") val code: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminDashboardStatsResponse(
    @Json(name = "totalEmployees") val totalEmployees: Int = 0,
    @Json(name = "presentToday") val presentToday: Int = 0,
    @Json(name = "onLeave") val onLeave: Int = 0,
    @Json(name = "lateArrivals") val lateArrivals: Int = 0,
    @Json(name = "absentToday") val absentToday: Int = 0,
    @Json(name = "pendingLeaveRequests") val pendingLeaveRequests: Int = 0
)

@JsonClass(generateAdapter = true)
data class EmployeeListResponse(
    @Json(name = "employees") val employees: List<EmployeeDto>,
    @Json(name = "pagination") val pagination: PaginationResponse? = null
)

@JsonClass(generateAdapter = true)
data class EmployeeResponse(
    @Json(name = "employee") val employee: EmployeeDto
)

@JsonClass(generateAdapter = true)
data class EmployeeDto(
    @Json(name = "id") val id: Int,
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "department") val department: String? = null,
    @Json(name = "designation") val designation: String? = null,
    @Json(name = "role") val role: String,
    @Json(name = "status") val status: String = "active",
    @Json(name = "joining_date") val joiningDate: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class PaginationResponse(
    @Json(name = "page") val page: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "totalItems") val totalItems: Int
)

@JsonClass(generateAdapter = true)
data class LeaveBalanceResponse(
    @Json(name = "balance") val balance: List<LeaveBalanceDto>,
    @Json(name = "annual") val annual: LeaveBalanceDto? = null,
    @Json(name = "sick") val sick: LeaveBalanceDto? = null,
    @Json(name = "personal") val personal: LeaveBalanceDto? = null,
    @Json(name = "unpaid") val unpaid: LeaveBalanceDto? = null
)

@JsonClass(generateAdapter = true)
data class LeaveBalanceDto(
    @Json(name = "type") val type: String,
    @Json(name = "total") val total: Int,
    @Json(name = "used") val used: Int,
    @Json(name = "remaining") val remaining: Int,
    @Json(name = "pending") val pending: Int = 0
)

@JsonClass(generateAdapter = true)
data class LeaveRequestListResponse(
    @Json(name = "requests") val requests: List<LeaveRequestDto>? = null,
    @Json(name = "leaves") val leaves: List<LeaveRequestDto>? = null,
    @Json(name = "pagination") val pagination: PaginationResponse? = null
)

@JsonClass(generateAdapter = true)
data class LeaveRequestResponse(
    @Json(name = "request") val request: LeaveRequestDto? = null,
    @Json(name = "leave") val leave: LeaveRequestDto? = null
)

@JsonClass(generateAdapter = true)
data class LeaveRequestDto(
    @Json(name = "id") val id: Int,
    @Json(name = "employee_id") val employeeId: Int,
    @Json(name = "employee_first_name") val employeeFirstName: String = "",
    @Json(name = "employee_last_name") val employeeLastName: String = "",
    @Json(name = "leave_type") val leaveType: String,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "status") val status: String,
    @Json(name = "reviewer_id") val reviewerId: Int? = null,
    @Json(name = "reviewer_remarks") val reviewerRemarks: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)
