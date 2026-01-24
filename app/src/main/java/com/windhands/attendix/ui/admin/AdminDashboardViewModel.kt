package com.windhands.attendix.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.repository.AdminRepository
import com.windhands.attendix.data.repository.LeaveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val leaveRepository: LeaveRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _pendingApprovalsState = MutableStateFlow<PendingApprovalsState>(PendingApprovalsState.Loading)
    val pendingApprovalsState: StateFlow<PendingApprovalsState> = _pendingApprovalsState.asStateFlow()

    private val _pendingApprovals = MutableStateFlow<List<PendingApprovalItem>>(emptyList())
    val pendingApprovals: StateFlow<List<PendingApprovalItem>> = _pendingApprovals.asStateFlow()

    private val _approveRejectState = MutableStateFlow<ApproveRejectState>(ApproveRejectState.Idle)
    val approveRejectState: StateFlow<ApproveRejectState> = _approveRejectState.asStateFlow()

    private val _currentDate = MutableStateFlow(
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"))
    )
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        loadDashboardStats()
        loadPendingApprovals()
    }

    private fun loadDashboardStats() {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            
            adminRepository.getDashboardStats()
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        val stats = response.data
                        _dashboardState.value = DashboardState.Success(
                            DashboardStats(
                                totalEmployees = stats.totalEmployees,
                                presentToday = stats.presentToday,
                                onLeave = stats.onLeave,
                                lateArrivals = stats.lateArrivals,
                                absentToday = stats.absentToday,
                                pendingLeaveRequests = stats.pendingLeaveRequests
                            )
                        )
                    } else {
                        _dashboardState.value = DashboardState.Error(
                            response.message ?: "Failed to load dashboard"
                        )
                    }
                }
                .onFailure { exception ->
                    _dashboardState.value = DashboardState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    private fun loadPendingApprovals() {
        viewModelScope.launch {
            _pendingApprovalsState.value = PendingApprovalsState.Loading
            
            leaveRepository.getPendingLeaveRequests()
                .onSuccess { response ->
                    if (response.success) {
                        val requestsList = response.data?.requests ?: response.data?.leaves ?: emptyList()
                        val approvals = requestsList.take(5).map { request ->
                            PendingApprovalItem(
                                id = request.id,
                                employeeName = "${request.employeeFirstName} ${request.employeeLastName}",
                                leaveType = formatLeaveType(request.leaveType),
                                days = calculateDays(request.startDate, request.endDate),
                                startDate = request.startDate,
                                endDate = request.endDate
                            )
                        }
                        _pendingApprovals.value = approvals
                        _pendingApprovalsState.value = PendingApprovalsState.Success
                    } else {
                        _pendingApprovalsState.value = PendingApprovalsState.Error(
                            response.message ?: "Failed to load pending approvals"
                        )
                    }
                }
                .onFailure { exception ->
                    _pendingApprovalsState.value = PendingApprovalsState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun approveLeaveRequest(requestId: Int) {
        viewModelScope.launch {
            _approveRejectState.value = ApproveRejectState.Processing
            
            leaveRepository.approveLeaveRequest(requestId, null)
                .onSuccess { response ->
                    if (response.success) {
                        _approveRejectState.value = ApproveRejectState.Success("Leave request approved")
                        loadPendingApprovals() // Refresh list
                    } else {
                        _approveRejectState.value = ApproveRejectState.Error(
                            response.message ?: "Failed to approve request"
                        )
                    }
                }
                .onFailure { exception ->
                    _approveRejectState.value = ApproveRejectState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun rejectLeaveRequest(requestId: Int, remarks: String? = null) {
        viewModelScope.launch {
            _approveRejectState.value = ApproveRejectState.Processing
            
            leaveRepository.rejectLeaveRequest(requestId, remarks)
                .onSuccess { response ->
                    if (response.success) {
                        _approveRejectState.value = ApproveRejectState.Success("Leave request rejected")
                        loadPendingApprovals() // Refresh list
                    } else {
                        _approveRejectState.value = ApproveRejectState.Error(
                            response.message ?: "Failed to reject request"
                        )
                    }
                }
                .onFailure { exception ->
                    _approveRejectState.value = ApproveRejectState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun resetApproveRejectState() {
        _approveRejectState.value = ApproveRejectState.Idle
    }

    private fun formatLeaveType(type: String): String {
        return when (type.lowercase()) {
            "annual" -> "Annual Leave"
            "sick" -> "Sick Leave"
            "personal" -> "Personal Leave"
            "unpaid" -> "Unpaid Leave"
            else -> type.replaceFirstChar { it.uppercase() }
        }
    }

    private fun calculateDays(startDate: String, endDate: String): Int {
        return try {
            val formatter = DateTimeFormatter.ISO_DATE
            val start = LocalDate.parse(startDate.substring(0, 10), formatter)
            val end = LocalDate.parse(endDate.substring(0, 10), formatter)
            (end.toEpochDay() - start.toEpochDay() + 1).toInt()
        } catch (e: Exception) {
            1
        }
    }

    fun refresh() {
        loadDashboard()
    }
}

sealed class DashboardState {
    data object Loading : DashboardState()
    data class Success(val stats: DashboardStats) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

sealed class PendingApprovalsState {
    data object Loading : PendingApprovalsState()
    data object Success : PendingApprovalsState()
    data class Error(val message: String) : PendingApprovalsState()
}

sealed class ApproveRejectState {
    data object Idle : ApproveRejectState()
    data object Processing : ApproveRejectState()
    data class Success(val message: String) : ApproveRejectState()
    data class Error(val message: String) : ApproveRejectState()
}

data class DashboardStats(
    val totalEmployees: Int,
    val presentToday: Int,
    val onLeave: Int,
    val lateArrivals: Int,
    val absentToday: Int,
    val pendingLeaveRequests: Int
)

data class PendingApprovalItem(
    val id: Int,
    val employeeName: String,
    val leaveType: String,
    val days: Int,
    val startDate: String,
    val endDate: String
)
