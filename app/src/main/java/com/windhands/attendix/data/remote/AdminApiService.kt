package com.windhands.attendix.data.remote

import com.windhands.attendix.data.remote.dto.AdminDashboardStatsResponse
import com.windhands.attendix.data.remote.dto.ApiResponse
import com.windhands.attendix.data.remote.dto.EmployeeDto
import com.windhands.attendix.data.remote.dto.EmployeeListResponse
import com.windhands.attendix.data.remote.dto.EmployeeResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminApiService {

    @GET("admin/dashboard")
    suspend fun getDashboardStats(): ApiResponse<AdminDashboardStatsResponse>

    @GET("admin/employees")
    suspend fun getEmployees(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("department") department: String? = null,
        @Query("status") status: String? = null
    ): ApiResponse<List<EmployeeDto>>

    @POST("admin/employees")
    suspend fun createEmployee(@Body request: Map<String, @JvmSuppressWildcards Any?>): ApiResponse<EmployeeResponse>

    @PUT("admin/employees/{id}")
    suspend fun updateEmployee(
        @Path("id") employeeId: Int,
        @Body request: Map<String, @JvmSuppressWildcards Any?>
    ): ApiResponse<EmployeeResponse>

    @GET("admin/employees/{id}")
    suspend fun getEmployee(@Path("id") employeeId: Int): ApiResponse<EmployeeResponse>

    @DELETE("admin/employees/{id}")
    suspend fun deleteEmployee(@Path("id") employeeId: Int): ApiResponse<Unit>

    @POST("admin/employees/{id}/activate")
    suspend fun activateEmployee(@Path("id") employeeId: Int): ApiResponse<Unit>

    @POST("admin/employees/{id}/deactivate")
    suspend fun deactivateEmployee(@Path("id") employeeId: Int): ApiResponse<Unit>
}
