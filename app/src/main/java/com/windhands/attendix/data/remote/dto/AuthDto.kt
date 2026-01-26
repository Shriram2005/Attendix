package com.windhands.attendix.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "employeeId") val employeeId: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "token") val token: String,
    @Json(name = "user") val user: UserDto
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: Int,
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    @Json(name = "email") val email: String,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: String,
    @Json(name = "department") val department: String? = null,
    @Json(name = "designation") val designation: String? = null,
    @Json(name = "status") val status: String = "active",
    @Json(name = "joining_date") val joiningDate: String? = null
)

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    @Json(name = "user") val user: UserDto? = null,
    @Json(name = "employee") val employee: UserDto? = null,
    // Direct profile fields (returned by GET /auth/me)
    @Json(name = "id") val id: Int? = null,
    @Json(name = "employee_id") val employeeId: String? = null,
    @Json(name = "first_name") val firstName: String? = null,
    @Json(name = "last_name") val lastName: String? = null,
    @Json(name = "email") val email: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "role") val role: String? = null,
    @Json(name = "department") val department: String? = null,
    @Json(name = "designation") val designation: String? = null,
    @Json(name = "is_active") val isActive: Boolean? = null,
    @Json(name = "date_of_joining") val dateOfJoining: String? = null
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

@JsonClass(generateAdapter = true)
data class AttendanceResponse(
    @Json(name = "attendance") val attendance: AttendanceDto? = null,
    @Json(name = "hasCheckedIn") val hasCheckedIn: Boolean = false,
    @Json(name = "hasCheckedOut") val hasCheckedOut: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AttendanceListResponse(
    @Json(name = "attendance") val attendance: List<AttendanceDto>? = null,
    @Json(name = "records") val records: List<AttendanceDto>? = null,
    @Json(name = "pagination") val pagination: PaginationResponse? = null
)

@JsonClass(generateAdapter = true)
data class AttendanceDto(
    @Json(name = "id") val id: Int,
    @Json(name = "employee_id") val employeeId: Int,
    @Json(name = "date") val _date: String? = null,
    @Json(name = "attendance_date") val _attendanceDate: String? = null,
    @Json(name = "check_in") val _checkIn: String? = null,
    @Json(name = "check_in_time") val _checkInTime: String? = null,
    @Json(name = "check_out") val _checkOut: String? = null,
    @Json(name = "check_out_time") val _checkOutTime: String? = null,
    @Json(name = "status") val status: String,
    @Json(name = "working_hours") val _workingHours: Double? = null,
    @Json(name = "total_hours") val _totalHours: Double? = null,
    @Json(name = "overtime_hours") val overtimeHours: Double? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "is_late") val isLate: Boolean? = null,
    @Json(name = "late_by_minutes") val lateByMinutes: Int? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    // For admin views
    @Json(name = "employee_first_name") val employeeFirstName: String? = null,
    @Json(name = "employee_last_name") val employeeLastName: String? = null,
    @Json(name = "first_name") val _firstName: String? = null,
    @Json(name = "last_name") val _lastName: String? = null
) {
    // Computed properties for compatibility
    val date: String get() = _date ?: _attendanceDate ?: ""
    val checkIn: String? get() = _checkIn ?: _checkInTime
    val checkOut: String? get() = _checkOut ?: _checkOutTime
    val workingHours: Double? get() = _workingHours ?: _totalHours
    val firstName: String? get() = employeeFirstName ?: _firstName
    val lastName: String? get() = employeeLastName ?: _lastName
}

@JsonClass(generateAdapter = true)
data class AttendanceStatsResponse(
    @Json(name = "totalDays") val _totalDays: Int? = null,
    @Json(name = "total_days") val _total_days: Int? = null,
    @Json(name = "presentDays") val _presentDays: Int? = null,
    @Json(name = "present_days") val _present_days: Int? = null,
    @Json(name = "absentDays") val _absentDays: Int? = null,
    @Json(name = "absent_days") val _absent_days: Int? = null,
    @Json(name = "lateDays") val _lateDays: Int? = null,
    @Json(name = "late_days") val _late_days: Int? = null,
    @Json(name = "halfDays") val _halfDays: Int? = null,
    @Json(name = "half_days") val _half_days: Int? = null,
    @Json(name = "leaveDays") val _leaveDays: Int? = null,
    @Json(name = "leave_days") val _leave_days: Int? = null,
    @Json(name = "totalWorkingHours") val _totalWorkingHours: Double? = null,
    @Json(name = "averageWorkingHours") val _averageWorkingHours: Double? = null,
    @Json(name = "avg_hours") val _avg_hours: Double? = null,
    @Json(name = "overtimeHours") val overtimeHours: Double = 0.0
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
