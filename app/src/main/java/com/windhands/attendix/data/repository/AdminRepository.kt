package com.windhands.attendix.data.repository

import com.windhands.attendix.data.remote.AdminApiService
import com.windhands.attendix.data.remote.dto.AdminDashboardStatsResponse
import com.windhands.attendix.data.remote.dto.ApiResponse
import com.windhands.attendix.data.remote.dto.CreateEmployeeRequest
import com.windhands.attendix.data.remote.dto.CreateEmployeeResponse
import com.windhands.attendix.data.remote.dto.EmployeeDto
import com.windhands.attendix.data.remote.dto.EmployeeListResponse
import com.windhands.attendix.data.remote.dto.UpdateEmployeeRequest
import com.windhands.attendix.util.ErrorMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val adminApiService: AdminApiService
) {

    suspend fun getDashboardStats(): Result<ApiResponse<AdminDashboardStatsResponse>> {
        return try {
            val response = adminApiService.getDashboardStats()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(ErrorMessage.toException(e))
        }
    }

    suspend fun getEmployees(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null,
        department: String? = null,
        status: String? = null
    ): Result<ApiResponse<EmployeeListResponse>> {
        return try {
            val response = adminApiService.getEmployees(page, limit, search, department, status)
            val wrappedData = EmployeeListResponse(
                employees = response.data ?: emptyList(),
                pagination = null
            )
            Result.success(ApiResponse(
                success = response.success,
                message = response.message,
                data = wrappedData
            ))
        } catch (e: Exception) {
            Result.failure(ErrorMessage.toException(e))
        }
    }

    suspend fun getEmployee(employeeId: Int): Result<ApiResponse<EmployeeDto>> {
        return try {
            val response = adminApiService.getEmployee(employeeId)
            Result.success(
                ApiResponse(
                    success = response.success,
                    message = response.message,
                    data = response.data?.employee
                )
            )
        } catch (e: Exception) {
            Result.failure(ErrorMessage.toException(e))
        }
    }

    suspend fun createEmployee(
        firstName: String,
        lastName: String,
        email: String,
        phone: String?,
        department: String?,
        designation: String?,
        role: String,
        joiningDate: String?
    ): Result<ApiResponse<CreateEmployeeResponse>> {
        return try {
            // Map role name to roleId
            val roleId = when (role.lowercase()) {
                "admin" -> 1
                "hr" -> 2
                "employee" -> 3
                else -> 3 // Default to employee
            }
            
            val request = CreateEmployeeRequest(
                email = email,
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                designation = designation ?: "Employee",
                department = department ?: "General",
                roleId = roleId,
                dateOfJoining = joiningDate
            )

            val response = adminApiService.createEmployee(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(ErrorMessage.toException(e))
        }
    }

    suspend fun updateEmployee(
        employeeId: Int,
        firstName: String?,
        lastName: String?,
        phone: String?,
        department: String?,
        designation: String?,
        role: String?,
        joiningDate: String?,
        password: String? = null
    ): Result<ApiResponse<EmployeeDto>> {
        return try {
            val roleId = role?.let {
                when (it.lowercase()) {
                    "admin" -> 1
                    "hr" -> 2
                    "employee" -> 3
                    else -> 3
                }
            }
            
            val request = UpdateEmployeeRequest(
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                department = department,
                designation = designation,
                roleId = roleId,
                dateOfJoining = joiningDate,
                password = password
            )

            val response = adminApiService.updateEmployee(employeeId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(ErrorMessage.toException(e))
        }
    }

    suspend fun deleteEmployee(employeeId: Int): Result<ApiResponse<Unit>> {
        return try {
            val response = adminApiService.deleteEmployee(employeeId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(ErrorMessage.toException(e))
        }
    }

    suspend fun activateEmployee(employeeId: Int): Result<ApiResponse<Unit>> {
        return try {
            val response = adminApiService.activateEmployee(employeeId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(ErrorMessage.toException(e))
        }
    }

    suspend fun deactivateEmployee(employeeId: Int): Result<ApiResponse<Unit>> {
        return try {
            val response = adminApiService.deactivateEmployee(employeeId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(ErrorMessage.toException(e))
        }
    }
}
