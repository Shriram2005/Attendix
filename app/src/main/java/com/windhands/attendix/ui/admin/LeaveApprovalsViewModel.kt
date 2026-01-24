package com.windhands.attendix.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class LeaveApprovalsViewModel @Inject constructor(
    private val leaveRepository: LeaveRepository
) : ViewModel() {

    private val _leaveRequestsState = MutableStateFlow<LeaveRequestsState>(LeaveRequestsState.Loading)
    val leaveRequestsState: StateFlow<LeaveRequestsState> = _leaveRequestsState.asStateFlow()

    private val _leaveRequests = MutableStateFlow<List<LeaveApprovalItem>>(emptyList())
    val leaveRequests: StateFlow<List<LeaveApprovalItem>> = _leaveRequests.asStateFlow()

    private val _currentFilter = MutableStateFlow(LeaveApprovalFilter.PENDING)
    val currentFilter: StateFlow<LeaveApprovalFilter> = _currentFilter.asStateFlow()

    private val _actionState = MutableStateFlow<LeaveActionState>(LeaveActionState.Idle)
    val actionState: StateFlow<LeaveActionState> = _actionState.asStateFlow()

    init {
        loadLeaveRequests()
    }

    fun setFilter(filter: LeaveApprovalFilter) {
        if (_currentFilter.value != filter) {
            _currentFilter.value = filter
            loadLeaveRequests()
        }
    }

    fun loadLeaveRequests() {
        viewModelScope.launch {
            _leaveRequestsState.value = LeaveRequestsState.Loading
            
            val status = when (_currentFilter.value) {
                LeaveApprovalFilter.ALL -> null
                LeaveApprovalFilter.PENDING -> "pending"
                LeaveApprovalFilter.APPROVED -> "approved"
                LeaveApprovalFilter.REJECTED -> "rejected"
            }
            
            leaveRepository.getAllLeaveRequests(status = status)
                .onSuccess { response ->
                    if (response.success) {
                        val requests = (response.data?.requests ?: response.data?.leaves ?: emptyList())
                            .map { request ->
                                LeaveApprovalItem(
                                    id = request.id,
                                    employeeId = request.employeeId,
                                    employeeName = "${request.employeeFirstName} ${request.employeeLastName}",
                                    leaveType = formatLeaveType(request.leaveType),
                                    startDate = formatDate(request.startDate),
                                    endDate = formatDate(request.endDate),
                                    days = calculateDays(request.startDate, request.endDate),
                                    reason = request.reason ?: "",
                                    status = request.status,
                                    reviewerRemarks = request.reviewerRemarks
                                )
                            }
                        _leaveRequests.value = requests
                        _leaveRequestsState.value = LeaveRequestsState.Success
                    } else {
                        _leaveRequestsState.value = LeaveRequestsState.Error(
                            response.message ?: "Failed to load leave requests"
                        )
                    }
                }
                .onFailure { exception ->
                    _leaveRequestsState.value = LeaveRequestsState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun approveRequest(requestId: Int, remarks: String? = null) {
        viewModelScope.launch {
            _actionState.value = LeaveActionState.Processing
            
            leaveRepository.approveLeaveRequest(requestId, remarks)
                .onSuccess { response ->
                    if (response.success) {
                        _actionState.value = LeaveActionState.Success("Leave request approved")
                        loadLeaveRequests()
                    } else {
                        _actionState.value = LeaveActionState.Error(
                            response.message ?: "Failed to approve"
                        )
                    }
                }
                .onFailure { exception ->
                    _actionState.value = LeaveActionState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun rejectRequest(requestId: Int, remarks: String?) {
        viewModelScope.launch {
            _actionState.value = LeaveActionState.Processing
            
            leaveRepository.rejectLeaveRequest(requestId, remarks)
                .onSuccess { response ->
                    if (response.success) {
                        _actionState.value = LeaveActionState.Success("Leave request rejected")
                        loadLeaveRequests()
                    } else {
                        _actionState.value = LeaveActionState.Error(
                            response.message ?: "Failed to reject"
                        )
                    }
                }
                .onFailure { exception ->
                    _actionState.value = LeaveActionState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun resetActionState() {
        _actionState.value = LeaveActionState.Idle
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

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormatter = DateTimeFormatter.ISO_DATE
            val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            val date = LocalDate.parse(dateString.substring(0, 10), inputFormatter)
            date.format(outputFormatter)
        } catch (e: Exception) {
            dateString
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
}

sealed class LeaveRequestsState {
    data object Loading : LeaveRequestsState()
    data object Success : LeaveRequestsState()
    data class Error(val message: String) : LeaveRequestsState()
}

sealed class LeaveActionState {
    data object Idle : LeaveActionState()
    data object Processing : LeaveActionState()
    data class Success(val message: String) : LeaveActionState()
    data class Error(val message: String) : LeaveActionState()
}

enum class LeaveApprovalFilter {
    ALL, PENDING, APPROVED, REJECTED
}

data class LeaveApprovalItem(
    val id: Int,
    val employeeId: Int,
    val employeeName: String,
    val leaveType: String,
    val startDate: String,
    val endDate: String,
    val days: Int,
    val reason: String,
    val status: String,
    val reviewerRemarks: String?
) {
    val initials: String get() {
        val parts = employeeName.split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull() ?: ""}${parts[1].firstOrNull() ?: ""}".uppercase()
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "??"
        }
    }
    
    val isPending: Boolean get() = status.lowercase() == "pending"
    val isApproved: Boolean get() = status.lowercase() == "approved"
    val isRejected: Boolean get() = status.lowercase() == "rejected"
}
