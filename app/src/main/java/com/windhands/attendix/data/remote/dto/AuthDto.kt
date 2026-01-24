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
    @Json(name = "employee") val employee: UserDto? = null
)

@JsonClass(generateAdapter = true)
data class AttendanceResponse(
    @Json(name = "attendance") val attendance: AttendanceDto? = null
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
    @Json(name = "date") val date: String,
    @Json(name = "check_in") val checkIn: String? = null,
    @Json(name = "check_out") val checkOut: String? = null,
    @Json(name = "status") val status: String,
    @Json(name = "working_hours") val workingHours: Double? = null,
    @Json(name = "overtime_hours") val overtimeHours: Double? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    // For admin views
    @Json(name = "employee_first_name") val employeeFirstName: String? = null,
    @Json(name = "employee_last_name") val employeeLastName: String? = null
)

@JsonClass(generateAdapter = true)
data class AttendanceStatsResponse(
    @Json(name = "totalDays") val totalDays: Int = 0,
    @Json(name = "presentDays") val presentDays: Int = 0,
    @Json(name = "absentDays") val absentDays: Int = 0,
    @Json(name = "lateDays") val lateDays: Int = 0,
    @Json(name = "halfDays") val halfDays: Int = 0,
    @Json(name = "leaveDays") val leaveDays: Int = 0,
    @Json(name = "totalWorkingHours") val totalWorkingHours: Double = 0.0,
    @Json(name = "averageWorkingHours") val averageWorkingHours: Double = 0.0,
    @Json(name = "overtimeHours") val overtimeHours: Double = 0.0
)
