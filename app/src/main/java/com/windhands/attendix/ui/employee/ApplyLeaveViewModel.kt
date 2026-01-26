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
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ApplyLeaveViewModel @Inject constructor(
    private val leaveRepository: LeaveRepository
) : ViewModel() {

    private val _leaveBalanceState = MutableStateFlow<LeaveBalanceState>(LeaveBalanceState.Loading)
    val leaveBalanceState: StateFlow<LeaveBalanceState> = _leaveBalanceState.asStateFlow()

    private val _applyState = MutableStateFlow<ApplyLeaveState>(ApplyLeaveState.Idle)
    val applyState: StateFlow<ApplyLeaveState> = _applyState.asStateFlow()

    private val _selectedLeaveType = MutableStateFlow<String?>(null)
    val selectedLeaveType: StateFlow<String?> = _selectedLeaveType.asStateFlow()

    private val _startDate = MutableStateFlow<LocalDate?>(null)
    val startDate: StateFlow<LocalDate?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<LocalDate?>(null)
    val endDate: StateFlow<LocalDate?> = _endDate.asStateFlow()

    private val _reason = MutableStateFlow("")
    val reason: StateFlow<String> = _reason.asStateFlow()

    private val _duration = MutableStateFlow<Int?>(null)
    val duration: StateFlow<Int?> = _duration.asStateFlow()

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    val leaveTypes = listOf(
        LeaveTypeOption("annual", "Annual Leave"),
        LeaveTypeOption("sick", "Sick Leave"),
        LeaveTypeOption("personal", "Personal Leave"),
        LeaveTypeOption("unpaid", "Unpaid Leave")
    )

    init {
        loadLeaveBalance()
    }

    private fun loadLeaveBalance() {
        viewModelScope.launch {
            _leaveBalanceState.value = LeaveBalanceState.Loading
            leaveRepository.getLeaveBalance()
                .onSuccess { response ->
                    if (response.success) {
                        val data = response.data
                        val balanceItems = mutableListOf<LeaveBalanceItem>()
                        
                        // Use the new toBalanceItems() method which handles both formats
                        data?.toBalanceItems()?.forEach { dto ->
                            balanceItems.add(
                                LeaveBalanceItem(
                                    type = dto.type,
                                    displayName = when(dto.type) {
                                        "paid" -> "Paid Leave"
                                        "unpaid" -> "Unpaid"
                                        "annual" -> "Annual"
                                        "sick" -> "Sick"
                                        "personal" -> "Personal"
                                        else -> dto.type.replaceFirstChar { it.uppercase() }
                                    },
                                    used = dto.used,
                                    total = dto.total,
                                    remaining = dto.remaining
                                )
                            )
                        }
                        
                        // Also check legacy fields if toBalanceItems didn't work
                        if (balanceItems.isEmpty()) {
                            data?.annual?.let { annual ->
                                balanceItems.add(
                                    LeaveBalanceItem(
                                        type = "annual",
                                        displayName = "Annual",
                                        used = annual.used,
                                        total = annual.total,
                                        remaining = annual.remaining
                                    )
                                )
                            }
                            data?.sick?.let { sick ->
                                balanceItems.add(
                                    LeaveBalanceItem(
                                        type = "sick",
                                        displayName = "Sick",
                                        used = sick.used,
                                        total = sick.total,
                                        remaining = sick.remaining
                                    )
                                )
                            }
                            data?.personal?.let { personal ->
                                balanceItems.add(
                                    LeaveBalanceItem(
                                        type = "personal",
                                        displayName = "Personal",
                                        used = personal.used,
                                        total = personal.total,
                                        remaining = personal.remaining
                                    )
                                )
                            }
                            data?.unpaid?.let { unpaid ->
                                balanceItems.add(
                                    LeaveBalanceItem(
                                        type = "unpaid",
                                        displayName = "Unpaid",
                                        used = unpaid.used,
                                        total = unpaid.total,
                                        remaining = unpaid.remaining
                                    )
                                )
                            }
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

    fun setLeaveType(type: String) {
        _selectedLeaveType.value = type
        clearValidationError()
    }

    fun setStartDate(date: LocalDate) {
        _startDate.value = date
        // If end date is before start date, reset it
        _endDate.value?.let { endDate ->
            if (endDate.isBefore(date)) {
                _endDate.value = null
            }
        }
        calculateDuration()
        clearValidationError()
    }

    fun setEndDate(date: LocalDate) {
        _endDate.value = date
        calculateDuration()
        clearValidationError()
    }

    fun setReason(text: String) {
        _reason.value = text
        clearValidationError()
    }

    private fun calculateDuration() {
        val start = _startDate.value
        val end = _endDate.value
        
        if (start != null && end != null && !end.isBefore(start)) {
            _duration.value = (ChronoUnit.DAYS.between(start, end) + 1).toInt()
        } else {
            _duration.value = null
        }
    }

    fun validateAndSubmit() {
        val leaveType = _selectedLeaveType.value
        val startDate = _startDate.value
        val endDate = _endDate.value
        val reason = _reason.value.trim()

        // Validation
        when {
            leaveType.isNullOrBlank() -> {
                _validationError.value = "Please select a leave type"
                return
            }
            startDate == null -> {
                _validationError.value = "Please select a start date"
                return
            }
            endDate == null -> {
                _validationError.value = "Please select an end date"
                return
            }
            endDate.isBefore(startDate) -> {
                _validationError.value = "End date cannot be before start date"
                return
            }
            startDate.isBefore(LocalDate.now()) -> {
                _validationError.value = "Start date cannot be in the past"
                return
            }
            reason.isBlank() -> {
                _validationError.value = "Please provide a reason for your leave"
                return
            }
            reason.length < 10 -> {
                _validationError.value = "Reason must be at least 10 characters"
                return
            }
        }

        // Check if enough leave balance
        val balanceState = _leaveBalanceState.value
        if (balanceState is LeaveBalanceState.Success) {
            val balance = balanceState.balances.find { it.type == leaveType }
            val daysRequested = _duration.value ?: 0
            if (balance != null && leaveType != "unpaid" && balance.remaining < daysRequested) {
                _validationError.value = "Insufficient leave balance. You have ${balance.remaining} days remaining."
                return
            }
        }

        // Submit
        submitLeaveRequest(
            leaveType = leaveType,
            startDate = startDate.format(DateTimeFormatter.ISO_DATE),
            endDate = endDate.format(DateTimeFormatter.ISO_DATE),
            reason = reason
        )
    }

    private fun submitLeaveRequest(
        leaveType: String,
        startDate: String,
        endDate: String,
        reason: String
    ) {
        viewModelScope.launch {
            _applyState.value = ApplyLeaveState.Submitting
            
            leaveRepository.applyLeave(
                leaveType = leaveType,
                startDate = startDate,
                endDate = endDate,
                reason = reason
            )
                .onSuccess { response ->
                    if (response.success) {
                        _applyState.value = ApplyLeaveState.Success(response.message ?: "Leave request submitted successfully")
                    } else {
                        _applyState.value = ApplyLeaveState.Error(response.message ?: "Failed to submit leave request")
                    }
                }
                .onFailure { exception ->
                    _applyState.value = ApplyLeaveState.Error(exception.message ?: "Network error")
                }
        }
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    fun resetApplyState() {
        _applyState.value = ApplyLeaveState.Idle
    }

    fun getMinStartDate(): LocalDate = LocalDate.now()

    fun getMinEndDate(): LocalDate = _startDate.value ?: LocalDate.now()
}

sealed class ApplyLeaveState {
    data object Idle : ApplyLeaveState()
    data object Submitting : ApplyLeaveState()
    data class Success(val message: String) : ApplyLeaveState()
    data class Error(val message: String) : ApplyLeaveState()
}

data class LeaveTypeOption(
    val type: String,
    val displayName: String
)
