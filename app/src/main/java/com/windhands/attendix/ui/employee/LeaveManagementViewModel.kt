package com.windhands.attendix.ui.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.repository.LeaveRepository
import com.windhands.attendix.ui.employee.model.LeaveBalanceItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class LeaveManagementViewModel @Inject constructor(
    private val leaveRepository: LeaveRepository
) : ViewModel() {

    private val _leaveBalanceState = MutableStateFlow<LeaveBalanceState>(LeaveBalanceState.Loading)
    val leaveBalanceState: StateFlow<LeaveBalanceState> = _leaveBalanceState.asStateFlow()

    private val _leaveRequestsState = MutableStateFlow<LeaveRequestsState>(LeaveRequestsState.Loading)
    val leaveRequestsState: StateFlow<LeaveRequestsState> = _leaveRequestsState.asStateFlow()

    private val _allRequests = MutableStateFlow<List<LeaveRequestItem>>(emptyList())
    
    private val _filteredRequests = MutableStateFlow<List<LeaveRequestItem>>(emptyList())
    val filteredRequests: StateFlow<List<LeaveRequestItem>> = _filteredRequests.asStateFlow()

    private val _currentFilter = MutableStateFlow(LeaveFilter.ALL)
    val currentFilter: StateFlow<LeaveFilter> = _currentFilter.asStateFlow()

    private val _cancelState = MutableStateFlow<CancelState>(CancelState.Idle)
    val cancelState: StateFlow<CancelState> = _cancelState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        loadLeaveBalance()
        loadLeaveRequests()
    }

    private fun loadLeaveBalance() {
        viewModelScope.launch {
            _leaveBalanceState.value = LeaveBalanceState.Loading
            leaveRepository.getLeaveBalance()
                .onSuccess { response ->
                    if (response.success) {
                        val data = response.data
                        val balanceItems = mutableListOf<LeaveBalanceItem>()
                        
                        // Use the toBalanceItems() method which handles the backend format
                        data?.toBalanceItems()?.forEach { dto ->
                            balanceItems.add(
                                LeaveBalanceItem(
                                    type = dto.type,
                                    displayName = when(dto.type) {
                                        "paid" -> "Paid Leave"
                                        "unpaid" -> "Unpaid Leave"
                                        "annual" -> "Annual Leave"
                                        "sick" -> "Sick Leave"
                                        "personal" -> "Personal Leave"
                                        else -> dto.type.replaceFirstChar { it.uppercase() }
                                    },
                                    used = dto.used,
                                    total = dto.total,
                                    remaining = dto.remaining
                                )
                            )
                        }
                        
                        _leaveBalanceState.value = LeaveBalanceState.Success(balanceItems)
                    } else {
                        _leaveBalanceState.value = LeaveBalanceState.Error(response.message ?: "Failed to load leave balance")
                    }
                }
                .onFailure { exception ->
                    _leaveBalanceState.value = LeaveBalanceState.Error(exception.message ?: "Network error")
                }
        }
    }

    private fun loadLeaveRequests() {
        viewModelScope.launch {
            _leaveRequestsState.value = LeaveRequestsState.Loading
            leaveRepository.getLeaveRequests()
                .onSuccess { response ->
                    if (response.success) {
                        val requestsList = response.data?.requests ?: response.data?.leaves ?: emptyList()
                        val requests = requestsList.map { dto ->
                            LeaveRequestItem(
                                id = dto.id,
                                leaveType = dto.leaveType,
                                displayType = formatLeaveType(dto.leaveType),
                                startDate = dto.startDate,
                                endDate = dto.endDate,
                                formattedDateRange = formatDateRange(dto.startDate, dto.endDate),
                                days = calculateDays(dto.startDate, dto.endDate),
                                reason = dto.reason ?: "",
                                status = dto.status,
                                canCancel = dto.status == "pending"
                            )
                        }
                        _allRequests.value = requests
                        applyFilter()
                        _leaveRequestsState.value = LeaveRequestsState.Success
                    } else {
                        _leaveRequestsState.value = LeaveRequestsState.Error(response.message ?: "Failed to load leave requests")
                    }
                }
                .onFailure { exception ->
                    _leaveRequestsState.value = LeaveRequestsState.Error(exception.message ?: "Network error")
                }
        }
    }

    fun setFilter(filter: LeaveFilter) {
        _currentFilter.value = filter
        applyFilter()
    }

    private fun applyFilter() {
        val allRequests = _allRequests.value
        _filteredRequests.value = when (_currentFilter.value) {
            LeaveFilter.ALL -> allRequests
            LeaveFilter.PENDING -> allRequests.filter { it.status == "pending" }
            LeaveFilter.APPROVED -> allRequests.filter { it.status == "approved" }
            LeaveFilter.REJECTED -> allRequests.filter { it.status == "rejected" }
        }
    }

    fun cancelLeaveRequest(requestId: Int) {
        viewModelScope.launch {
            _cancelState.value = CancelState.Cancelling
            leaveRepository.cancelLeaveRequest(requestId)
                .onSuccess { response ->
                    if (response.success) {
                        _cancelState.value = CancelState.Success(response.message ?: "Leave request cancelled")
                        loadLeaveRequests() // Refresh the list
                    } else {
                        _cancelState.value = CancelState.Error(response.message ?: "Failed to cancel leave request")
                    }
                }
                .onFailure { exception ->
                    _cancelState.value = CancelState.Error(exception.message ?: "Network error")
                }
        }
    }

    fun resetCancelState() {
        _cancelState.value = CancelState.Idle
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

    private fun formatDateRange(startDate: String, endDate: String): String {
        return try {
            val inputFormatter = DateTimeFormatter.ISO_DATE
            val outputFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            
            val start = LocalDate.parse(startDate.substring(0, 10), inputFormatter)
            val end = LocalDate.parse(endDate.substring(0, 10), inputFormatter)
            
            if (start == end) {
                start.format(outputFormatter)
            } else if (start.year == end.year && start.month == end.month) {
                "${start.dayOfMonth} - ${end.format(outputFormatter)}"
            } else if (start.year == end.year) {
                "${start.format(DateTimeFormatter.ofPattern("MMM dd"))} - ${end.format(outputFormatter)}"
            } else {
                "${start.format(outputFormatter)} - ${end.format(outputFormatter)}"
            }
        } catch (e: Exception) {
            "$startDate - $endDate"
        }
    }

    private fun calculateDays(startDate: String, endDate: String): Int {
        return try {
            val inputFormatter = DateTimeFormatter.ISO_DATE
            val start = LocalDate.parse(startDate.substring(0, 10), inputFormatter)
            val end = LocalDate.parse(endDate.substring(0, 10), inputFormatter)
            (end.toEpochDay() - start.toEpochDay() + 1).toInt()
        } catch (e: Exception) {
            1
        }
    }

    fun refresh() {
        loadData()
    }
}

sealed class LeaveBalanceState {
    data object Loading : LeaveBalanceState()
    data class Success(val balances: List<LeaveBalanceItem>) : LeaveBalanceState()
    data class Error(val message: String) : LeaveBalanceState()
}

sealed class LeaveRequestsState {
    data object Loading : LeaveRequestsState()
    data object Success : LeaveRequestsState()
    data class Error(val message: String) : LeaveRequestsState()
}

sealed class CancelState {
    data object Idle : CancelState()
    data object Cancelling : CancelState()
    data class Success(val message: String) : CancelState()
    data class Error(val message: String) : CancelState()
}

data class LeaveRequestItem(
    val id: Int,
    val leaveType: String,
    val displayType: String,
    val startDate: String,
    val endDate: String,
    val formattedDateRange: String,
    val days: Int,
    val reason: String,
    val status: String,
    val canCancel: Boolean
)

enum class LeaveFilter {
    ALL, PENDING, APPROVED, REJECTED
}
