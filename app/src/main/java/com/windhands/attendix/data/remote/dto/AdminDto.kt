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
    // Backend uses different field names
    @Json(name = "onLeave") val _onLeave: Int? = null,
    @Json(name = "onLeaveToday") val _onLeaveToday: Int? = null,
    @Json(name = "lateArrivals") val _lateArrivals: Int? = null,
    @Json(name = "lateToday") val _lateToday: Int? = null,
    @Json(name = "absentToday") val absentToday: Int = 0,
    @Json(name = "pendingLeaveRequests") val pendingLeaveRequests: Int = 0
) {
    // Computed properties for compatibility
    val onLeave: Int get() = _onLeave ?: _onLeaveToday ?: 0
    val lateArrivals: Int get() = _lateArrivals ?: _lateToday ?: 0
}

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
    @Json(name = "status") val _status: String? = null,
    @Json(name = "is_active") val isActive: Boolean? = null,
    @Json(name = "joining_date") val joiningDate: String? = null,
    @Json(name = "date_of_joining") val _dateOfJoining: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
) {
    // Computed status: use _status if present, otherwise derive from isActive
    val status: String get() = _status ?: if (isActive == true) "active" else "inactive"
    
    // Computed joining date: backend uses different field names
    val dateOfJoining: String? get() = joiningDate ?: _dateOfJoining
}

@JsonClass(generateAdapter = true)
data class PaginationResponse(
    @Json(name = "page") val page: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "totalItems") val totalItems: Int
)

@JsonClass(generateAdapter = true)
data class LeaveBalanceResponse(
    // Direct balance fields from backend
    @Json(name = "paidLeavesBalance") val paidLeavesBalance: Int = 0,
    @Json(name = "paidLeavesTotal") val paidLeavesTotal: Int = 0,
    @Json(name = "unpaidLeavesTaken") val unpaidLeavesTaken: Int = 0,
    @Json(name = "pendingRequests") val pendingRequests: Int = 0,
    @Json(name = "approvedRequests") val approvedRequests: Int = 0,
    // Legacy fields for backward compatibility
    @Json(name = "balance") val balance: List<LeaveBalanceDto>? = null,
    @Json(name = "annual") val annual: LeaveBalanceDto? = null,
    @Json(name = "sick") val sick: LeaveBalanceDto? = null,
    @Json(name = "personal") val personal: LeaveBalanceDto? = null,
    @Json(name = "unpaid") val unpaid: LeaveBalanceDto? = null
) {
    /**
     * Convert backend response to a list of LeaveBalanceItem-compatible data
     * since backend only tracks paid vs unpaid leaves
     */
    fun toBalanceItems(): List<LeaveBalanceDto> {
        // If legacy balance list is provided, use it
        if (!balance.isNullOrEmpty()) return balance
        
        // Otherwise, build from direct fields
        val paidUsed = paidLeavesTotal - paidLeavesBalance
        return listOf(
            LeaveBalanceDto(
                type = "paid",
                total = paidLeavesTotal,
                used = paidUsed,
                remaining = paidLeavesBalance,
                pending = pendingRequests
            ),
            LeaveBalanceDto(
                type = "unpaid",
                total = 0, // Unpaid has no limit
                used = unpaidLeavesTaken,
                remaining = 0,
                pending = 0
            )
        )
    }
}

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
    @Json(name = "employee_id") val _employeeIdString: String? = null,
    // Employee name fields - backend uses different names in different endpoints
    @Json(name = "employee_first_name") val _employeeFirstName: String? = null,
    @Json(name = "employee_last_name") val _employeeLastName: String? = null,
    @Json(name = "first_name") val _firstName: String? = null,
    @Json(name = "last_name") val _lastName: String? = null,
    @Json(name = "employee_name") val _employeeName: String? = null,
    @Json(name = "department") val department: String? = null,
    @Json(name = "designation") val designation: String? = null,
    @Json(name = "leave_type") val leaveType: String,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    @Json(name = "total_days") val totalDays: Int = 0,
    @Json(name = "reason") val reason: String? = null,
    @Json(name = "status") val status: String,
    // Reviewer fields - backend uses different names
    @Json(name = "reviewer_id") val _reviewerId: Int? = null,
    @Json(name = "reviewed_by") val _reviewedBy: Int? = null,
    @Json(name = "reviewed_by_name") val reviewedByName: String? = null,
    @Json(name = "reviewed_at") val reviewedAt: String? = null,
    @Json(name = "reviewer_remarks") val _reviewerRemarks: String? = null,
    @Json(name = "review_remarks") val _reviewRemarks: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
) {
    // Computed properties for compatibility
    val employeeId: Int get() = _employeeIdString?.toIntOrNull() ?: 0
    val employeeFirstName: String get() = _employeeFirstName ?: _firstName ?: ""
    val employeeLastName: String get() = _employeeLastName ?: _lastName ?: ""
    val employeeName: String get() = _employeeName ?: "$employeeFirstName $employeeLastName".trim()
    val reviewerId: Int? get() = _reviewerId ?: _reviewedBy
    val reviewerRemarks: String? get() = _reviewerRemarks ?: _reviewRemarks
}
