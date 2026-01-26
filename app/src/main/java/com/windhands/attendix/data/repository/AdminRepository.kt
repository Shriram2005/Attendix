package com.windhands.attendix.data.repository

import com.windhands.attendix.data.remote.AdminApiService
import com.windhands.attendix.data.remote.dto.AdminDashboardStatsResponse
import com.windhands.attendix.data.remote.dto.ApiResponse
import com.windhands.attendix.data.remote.dto.EmployeeDto
import com.windhands.attendix.data.remote.dto.EmployeeListResponse
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
            Result.failure(e)
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
            Result.failure(e)
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
            Result.failure(e)
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
    ): Result<ApiResponse<EmployeeDto>> {
        return try {
            // Map role name to roleId
            val roleId = when (role.lowercase()) {
                "admin" -> 1
                "hr" -> 2
                "employee" -> 3
                else -> 3 // Default to employee
            }
            
            val request = mutableMapOf<String, Any?>(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "roleId" to roleId,
                "designation" to (designation ?: "Employee"),
                "department" to (department ?: "General")
            )
            phone?.let { request["phone"] = it }
            joiningDate?.let { request["dateOfJoining"] = it }

            val response = adminApiService.createEmployee(request)
            Result.success(
                ApiResponse(
                    success = response.success,
                    message = response.message,
                    data = response.data?.employee
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
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
        joiningDate: String?
    ): Result<ApiResponse<EmployeeDto>> {
        return try {
            val request = mutableMapOf<String, Any?>()
            firstName?.let { request["first_name"] = it }
            lastName?.let { request["last_name"] = it }
            phone?.let { request["phone"] = it }
            department?.let { request["department"] = it }
            designation?.let { request["designation"] = it }
            role?.let {
                // Map role name to roleId
                val roleId = when (it.lowercase()) {
                    "admin" -> 1
                    "hr" -> 2
                    "employee" -> 3
                    else -> 3
                }
                request["role_id"] = roleId
            }
            joiningDate?.let { request["date_of_joining"] = it }

            val response = adminApiService.updateEmployee(employeeId, request)
            Result.success(
                ApiResponse(
                    success = response.success,
                    message = response.message,
                    data = response.data?.employee
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEmployee(employeeId: Int): Result<ApiResponse<Unit>> {
        return try {
            val response = adminApiService.deleteEmployee(employeeId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activateEmployee(employeeId: Int): Result<ApiResponse<Unit>> {
        return try {
            val response = adminApiService.activateEmployee(employeeId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deactivateEmployee(employeeId: Int): Result<ApiResponse<Unit>> {
        return try {
            val response = adminApiService.deactivateEmployee(employeeId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
