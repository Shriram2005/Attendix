package com.windhands.attendix.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("employeeId") val employeeId: String,
    @SerialName("password") val password: String
)

@Serializable
data class LoginResponse(
    @SerialName("token") val token: String,
    @SerialName("user") val user: UserDto
)

@Serializable
data class UserDto(
    @SerialName("id") val id: Int,
    @SerialName("employee_id") val employeeId: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String? = null,
    @SerialName("role") val role: String,
    @SerialName("department") val department: String? = null,
    @SerialName("designation") val designation: String? = null,
    @SerialName("status") val status: String = "active",
    @SerialName("joining_date") val joiningDate: String? = null
)

@Serializable
data class ProfileResponse(
    @SerialName("user") val user: UserDto? = null,
    @SerialName("employee") val employee: UserDto? = null,
    // Direct profile fields (returned by GET /auth/me)
    @SerialName("id") val id: Int? = null,
    @SerialName("employee_id") val employeeId: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("role") val role: String? = null,
    @SerialName("department") val department: String? = null,
    @SerialName("designation") val designation: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("date_of_joining") val dateOfJoining: String? = null
) {
    /**
     * Converts the profile response to a unified UserDto,
     * handling both wrapped (user/employee) and direct profile formats
     */
    fun toUserDto(): UserDto? {
        // If user or employee is present, use it
        user?.let { return it }
        employee?.let { return it }
        
        // Otherwise, construct from direct fields
        if (id != null && employeeId != null && firstName != null && lastName != null && email != null && role != null) {
            return UserDto(
                id = id,
                employeeId = employeeId,
                firstName = firstName,
                lastName = lastName,
                email = email,
                phone = phone,
                role = role,
                department = department,
                designation = designation,
                status = if (isActive == true) "active" else "inactive",
                joiningDate = dateOfJoining
            )
        }
        return null
    }
}

@Serializable
data class AttendanceResponse(
    @SerialName("attendance") val attendance: AttendanceDto? = null,
    @SerialName("hasCheckedIn") val hasCheckedIn: Boolean = false,
    @SerialName("hasCheckedOut") val hasCheckedOut: Boolean = false
)

@Serializable
data class AttendanceListResponse(
    @SerialName("attendance") val attendance: List<AttendanceDto>? = null,
    @SerialName("records") val records: List<AttendanceDto>? = null,
    @SerialName("pagination") val pagination: PaginationResponse? = null
)

@Serializable
data class AttendanceDto(
    @SerialName("id") val id: Int,
    @SerialName("employee_id") val employeeId: Int,
    @SerialName("date") val _date: String? = null,
    @SerialName("attendance_date") val _attendanceDate: String? = null,
    @SerialName("check_in") val _checkIn: String? = null,
    @SerialName("check_in_time") val _checkInTime: String? = null,
    @SerialName("check_out") val _checkOut: String? = null,
    @SerialName("check_out_time") val _checkOutTime: String? = null,
    @SerialName("status") val status: String,
    @SerialName("working_hours") val _workingHours: Double? = null,
    @SerialName("total_hours") val _totalHours: Double? = null,
    @SerialName("overtime_hours") val overtimeHours: Double? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("is_late") val isLate: Boolean? = null,
    @SerialName("late_by_minutes") val lateByMinutes: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // For admin views
    @SerialName("employee_first_name") val employeeFirstName: String? = null,
    @SerialName("employee_last_name") val employeeLastName: String? = null,
    @SerialName("first_name") val _firstName: String? = null,
    @SerialName("last_name") val _lastName: String? = null
) {
    // Computed properties for compatibility
    val date: String get() = _date ?: _attendanceDate ?: ""
    val checkIn: String? get() = _checkIn ?: _checkInTime
    val checkOut: String? get() = _checkOut ?: _checkOutTime
    val workingHours: Double? get() = _workingHours ?: _totalHours
    val firstName: String? get() = employeeFirstName ?: _firstName
    val lastName: String? get() = employeeLastName ?: _lastName
}

@Serializable
data class AttendanceStatsResponse(
    @SerialName("totalDays") val _totalDays: Int? = null,
    @SerialName("total_days") val _total_days: Int? = null,
    @SerialName("presentDays") val _presentDays: Int? = null,
    @SerialName("present_days") val _present_days: Int? = null,
    @SerialName("absentDays") val _absentDays: Int? = null,
    @SerialName("absent_days") val _absent_days: Int? = null,
    @SerialName("lateDays") val _lateDays: Int? = null,
    @SerialName("late_days") val _late_days: Int? = null,
    @SerialName("halfDays") val _halfDays: Int? = null,
    @SerialName("half_days") val _half_days: Int? = null,
    @SerialName("leaveDays") val _leaveDays: Int? = null,
    @SerialName("leave_days") val _leave_days: Int? = null,
    @SerialName("totalWorkingHours") val _totalWorkingHours: Double? = null,
    @SerialName("averageWorkingHours") val _averageWorkingHours: Double? = null,
    @SerialName("avg_hours") val _avg_hours: Double? = null,
    @SerialName("overtimeHours") val overtimeHours: Double = 0.0
) {
    val totalDays: Int get() = _totalDays ?: _total_days ?: 0
    val presentDays: Int get() = _presentDays ?: _present_days ?: 0
    val absentDays: Int get() = _absentDays ?: _absent_days ?: 0
    val lateDays: Int get() = _lateDays ?: _late_days ?: 0
    val halfDays: Int get() = _halfDays ?: _half_days ?: 0
    val leaveDays: Int get() = _leaveDays ?: _leave_days ?: 0
    val totalWorkingHours: Double get() = _totalWorkingHours ?: 0.0
    val averageWorkingHours: Double get() = _averageWorkingHours ?: _avg_hours ?: 0.0
}
