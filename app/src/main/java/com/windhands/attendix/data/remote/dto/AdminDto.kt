package com.windhands.attendix.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: T? = null,
    @SerialName("error") val error: String? = null,
    @SerialName("code") val code: String? = null
)

@Serializable
data class AdminDashboardStatsResponse(
    @SerialName("totalEmployees") val totalEmployees: Int = 0,
    @SerialName("presentToday") val presentToday: Int = 0,
    // Backend uses different field names
    @SerialName("onLeave") val _onLeave: Int? = null,
    @SerialName("onLeaveToday") val _onLeaveToday: Int? = null,
    @SerialName("lateArrivals") val _lateArrivals: Int? = null,
    @SerialName("lateToday") val _lateToday: Int? = null,
    @SerialName("absentToday") val absentToday: Int = 0,
    @SerialName("pendingLeaveRequests") val pendingLeaveRequests: Int = 0
) {
    // Computed properties for compatibility
    val onLeave: Int get() = _onLeave ?: _onLeaveToday ?: 0
    val lateArrivals: Int get() = _lateArrivals ?: _lateToday ?: 0
}

@Serializable
data class EmployeeListResponse(
    @SerialName("employees") val employees: List<EmployeeDto>,
    @SerialName("pagination") val pagination: PaginationResponse? = null
)

@Serializable
data class EmployeeResponse(
    @SerialName("employee") val employee: EmployeeDto
)

@Serializable
data class EmployeeDto(
    @SerialName("id") val id: Int,
    @SerialName("employee_id") val employeeId: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("email") val email: String,
    @SerialName("temporary_password") val temporaryPassword: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("department") val department: String? = null,
    @SerialName("designation") val designation: String? = null,
    @SerialName("role") val role: String,
    @SerialName("status") val _status: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("date_of_joining") val dateOfJoining: String? = null,
    @SerialName("created_at") val createdAt: String? = null
) {
    // Computed status: use _status if present, otherwise derive from isActive
    val status: String get() = _status ?: if (isActive == true) "active" else "inactive"
}

@Serializable
data class PaginationResponse(
    @SerialName("currentPage") val currentPage: Int,
    @SerialName("limit") val limit: Int,
    @SerialName("totalPages") val totalPages: Int,
    @SerialName("totalRecords") val totalRecords: Int
) {
    // Computed properties for backward compatibility
    val page: Int get() = currentPage
    val totalItems: Int get() = totalRecords
}

@Serializable
data class LeaveBalanceResponse(
    // Direct balance fields from backend
    @SerialName("paidLeavesBalance") val paidLeavesBalance: Int = 0,
    @SerialName("paidLeavesTotal") val paidLeavesTotal: Int = 0,
    @SerialName("unpaidLeavesTaken") val unpaidLeavesTaken: Int = 0,
    @SerialName("pendingRequests") val pendingRequests: Int = 0,
    @SerialName("approvedRequests") val approvedRequests: Int = 0,
    // Legacy fields for backward compatibility
    @SerialName("balance") val balance: List<LeaveBalanceDto>? = null,
    @SerialName("annual") val annual: LeaveBalanceDto? = null,
    @SerialName("sick") val sick: LeaveBalanceDto? = null,
    @SerialName("personal") val personal: LeaveBalanceDto? = null,
    @SerialName("unpaid") val unpaid: LeaveBalanceDto? = null
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

@Serializable
data class LeaveBalanceDto(
    @SerialName("type") val type: String,
    @SerialName("total") val total: Int,
    @SerialName("used") val used: Int,
    @SerialName("remaining") val remaining: Int,
    @SerialName("pending") val pending: Int = 0
)

@Serializable
data class LeaveRequestListResponse(
    @SerialName("requests") val requests: List<LeaveRequestDto>? = null,
    @SerialName("leaves") val leaves: List<LeaveRequestDto>? = null,
    @SerialName("pagination") val pagination: PaginationResponse? = null
)

@Serializable
data class LeaveRequestResponse(
    @SerialName("request") val request: LeaveRequestDto? = null,
    @SerialName("leave") val leave: LeaveRequestDto? = null
)

@Serializable
data class LeaveRequestDto(
    @SerialName("id") val id: Int,
    @SerialName("employee_id") val _employeeIdString: String? = null,
    // Employee name fields - backend uses different names in different endpoints
    @SerialName("employee_first_name") val _employeeFirstName: String? = null,
    @SerialName("employee_last_name") val _employeeLastName: String? = null,
    @SerialName("first_name") val _firstName: String? = null,
    @SerialName("last_name") val _lastName: String? = null,
    @SerialName("employee_name") val _employeeName: String? = null,
    @SerialName("department") val department: String? = null,
    @SerialName("designation") val designation: String? = null,
    @SerialName("leave_type") val leaveType: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("total_days") val totalDays: Int = 0,
    @SerialName("reason") val reason: String? = null,
    @SerialName("status") val status: String,
    // Reviewer fields - backend uses different names
    @SerialName("reviewer_id") val _reviewerId: Int? = null,
    @SerialName("reviewed_by") val _reviewedBy: Int? = null,
    @SerialName("reviewed_by_name") val reviewedByName: String? = null,
    @SerialName("reviewed_at") val reviewedAt: String? = null,
    @SerialName("reviewer_remarks") val _reviewerRemarks: String? = null,
    @SerialName("review_remarks") val _reviewRemarks: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    // Computed properties for compatibility
    val employeeId: Int get() = _employeeIdString?.toIntOrNull() ?: 0
    val employeeFirstName: String get() = _employeeFirstName ?: _firstName ?: ""
    val employeeLastName: String get() = _employeeLastName ?: _lastName ?: ""
    val employeeName: String get() = _employeeName ?: "$employeeFirstName $employeeLastName".trim()
    val reviewerId: Int? get() = _reviewerId ?: _reviewedBy
    val reviewerRemarks: String? get() = _reviewerRemarks ?: _reviewRemarks
}

// Request DTOs for API calls

@Serializable
data class ApplyLeaveRequest(
    @SerialName("leaveType") val leaveType: String,
    @SerialName("startDate") val startDate: String,
    @SerialName("endDate") val endDate: String,
    @SerialName("reason") val reason: String? = null
)

@Serializable
data class LeaveReviewRequest(
    @SerialName("status") val status: String,
    @SerialName("remarks") val remarks: String? = null
)

@Serializable
data class CreateEmployeeRequest(
    @SerialName("email") val email: String,
    @SerialName("firstName") val firstName: String,
    @SerialName("lastName") val lastName: String,
    @SerialName("phone") val phone: String? = null,
    @SerialName("designation") val designation: String,
    @SerialName("department") val department: String,
    @SerialName("roleId") val roleId: Int,
    @SerialName("date_of_joining") val dateOfJoining: String? = null
)

@Serializable
data class UpdateEmployeeRequest(
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("department") val department: String? = null,
    @SerialName("designation") val designation: String? = null,
    @SerialName("role_id") val roleId: Int? = null,
    @SerialName("date_of_joining") val dateOfJoining: String? = null,
    @SerialName("password") val password: String? = null
)

@Serializable
data class CreateEmployeeResponse(
    @SerialName("employee") val employee: EmployeeDto,
    @SerialName("temporaryPassword") val temporaryPassword: String? = null
)
