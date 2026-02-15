package com.windhands.attendix.ui.employee

import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.local.SessionManager
import com.windhands.attendix.data.repository.AttendanceRepository
import com.windhands.attendix.data.repository.LeaveRepository
import com.windhands.attendix.ui.base.BaseViewModel
import com.windhands.attendix.ui.employee.model.LeaveBalanceItem
import com.windhands.attendix.ui.employee.model.LeaveIconType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for Employee Dashboard Screen
 */
@HiltViewModel
class EmployeeDashboardViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val attendanceRepository: AttendanceRepository,
    private val leaveRepository: LeaveRepository
) : BaseViewModel() {

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _department = MutableStateFlow("")
    val department: StateFlow<String> = _department.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _todayAttendance = MutableStateFlow<TodayAttendance?>(null)
    val todayAttendance: StateFlow<TodayAttendance?> = _todayAttendance.asStateFlow()

    private val _leaveBalances = MutableStateFlow<List<LeaveBalanceItem>>(emptyList())
    val leaveBalances: StateFlow<List<LeaveBalanceItem>> = _leaveBalances.asStateFlow()

    private val _recentActivities = MutableStateFlow<List<ActivityItem>>(emptyList())
    val recentActivities: StateFlow<List<ActivityItem>> = _recentActivities.asStateFlow()

    private val _isCheckingInOut = MutableStateFlow(false)
    val isCheckingInOut: StateFlow<Boolean> = _isCheckingInOut.asStateFlow()

    private val _liveWorkingHours = MutableStateFlow<Double?>(null)
    val liveWorkingHours: StateFlow<Double?> = _liveWorkingHours.asStateFlow()

    private var timerJob: Job? = null

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    private val checkInTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    init {
        loadUserInfo()
        updateTime()
        loadDashboardData()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val firstName = sessionManager.firstName.first() ?: ""
            val department = sessionManager.department.first() ?: ""
            
            _userName.value = "${getGreeting()}, ${firstName.ifEmpty { "User" }}"
            _department.value = department.ifEmpty { "Employee" }
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good Morning"
            hour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    private fun updateTime() {
        val now = Date()
        _currentTime.value = timeFormat.format(now)
        _currentDate.value = dateFormat.format(now)
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            setLoading(true)

            try {
                // Load today's attendance
                val attendanceResult = attendanceRepository.getTodayStatus()
                attendanceResult.fold(
                    onSuccess = { response ->
                        response.data?.let { att ->
                            _todayAttendance.value = TodayAttendance(
                                isCheckedIn = att.checkIn != null,
                                isCheckedOut = att.checkOut != null,
                                checkInTime = att.checkIn,
                                checkOutTime = att.checkOut,
                                status = att.status,
                                totalHours = att.workingHours
                            )

                            // Start or stop timer based on check-in/out status
                            if (att.checkIn != null && att.checkOut == null) {
                                startWorkingHoursTimer(att.checkIn)
                            } else {
                                stopWorkingHoursTimer()
                                _liveWorkingHours.value = att.workingHours
                            }
                        } ?: run {
                            _todayAttendance.value = null
                            stopWorkingHoursTimer()
                            _liveWorkingHours.value = null
                        }
                    },
                    onFailure = {
                        _todayAttendance.value = null
                        stopWorkingHoursTimer()
                        _liveWorkingHours.value = null
                    }
                )

                // Load leave balance
                val leaveResult = leaveRepository.getLeaveBalance()
                leaveResult.fold(
                    onSuccess = { response ->
                        val balanceList = mutableListOf<LeaveBalanceItem>()
                        
                        // Use the new toBalanceItems() method which handles both formats
                        response.data?.toBalanceItems()?.forEach { balance ->
                            balanceList.add(
                                LeaveBalanceItem(
                                    type = balance.type,
                                    displayName = when(balance.type) {
                                        "paid" -> "Paid Leave"
                                        "unpaid" -> "Unpaid"
                                        "annual" -> "Annual"
                                        "sick" -> "Sick"
                                        "personal" -> "Casual"
                                        else -> balance.type.replaceFirstChar { it.uppercase() }
                                    },
                                    used = balance.used,
                                    remaining = balance.remaining,
                                    total = balance.total,
                                    iconType = when(balance.type) {
                                        "paid", "annual" -> LeaveIconType.ANNUAL
                                        "sick" -> LeaveIconType.SICK
                                        "personal" -> LeaveIconType.CASUAL
                                        "unpaid" -> LeaveIconType.UNPAID
                                        else -> LeaveIconType.ANNUAL
                                    }
                                )
                            )
                        }
                        
                        // Also check legacy fields if toBalanceItems didn't work
                        if (balanceList.isEmpty()) {
                            response.data?.annual?.let { balance ->
                                balanceList.add(
                                    LeaveBalanceItem(
                                        type = "annual",
                                        displayName = "Annual",
                                        used = balance.used,
                                        remaining = balance.remaining,
                                        total = balance.total,
                                        iconType = LeaveIconType.ANNUAL
                                    )
                                )
                            }
                            response.data?.sick?.let { balance ->
                                balanceList.add(
                                    LeaveBalanceItem(
                                        type = "sick",
                                        displayName = "Sick",
                                        used = balance.used,
                                        remaining = balance.remaining,
                                        total = balance.total,
                                        iconType = LeaveIconType.SICK
                                    )
                                )
                            }
                            response.data?.personal?.let { balance ->
                                balanceList.add(
                                    LeaveBalanceItem(
                                        type = "personal",
                                        displayName = "Casual",
                                        used = balance.used,
                                        remaining = balance.remaining,
                                        total = balance.total,
                                        iconType = LeaveIconType.CASUAL
                                    )
                                )
                            }
                            response.data?.unpaid?.let { balance ->
                                balanceList.add(
                                    LeaveBalanceItem(
                                        type = "unpaid",
                                        displayName = "Unpaid",
                                        used = balance.used,
                                        remaining = balance.remaining,
                                        total = balance.total,
                                        iconType = LeaveIconType.UNPAID
                                    )
                                )
                            }
                        }
                        _leaveBalances.value = balanceList
                    },
                    onFailure = {
                        _leaveBalances.value = emptyList()
                    }
                )

                _dashboardState.value = DashboardState.Success
            } catch (e: Exception) {
                _dashboardState.value = DashboardState.Error(e.message ?: "Failed to load dashboard")
                emitError(e.message ?: "Failed to load dashboard")
            }

            setLoading(false)
        }
    }

    private fun getLeaveIconType(leaveType: String): LeaveIconType {
        return when (leaveType.lowercase()) {
            "annual", "vacation" -> LeaveIconType.ANNUAL
            "sick", "medical" -> LeaveIconType.SICK
            "casual" -> LeaveIconType.CASUAL
            "unpaid" -> LeaveIconType.UNPAID
            else -> LeaveIconType.OTHER
        }
    }

    fun checkInOut() {
        val currentAttendance = _todayAttendance.value

        viewModelScope.launch {
            _isCheckingInOut.value = true

            val result = if (currentAttendance?.isCheckedIn == true && currentAttendance.isCheckedOut != true) {
                // Check out
                attendanceRepository.checkOut()
            } else {
                // Check in
                attendanceRepository.checkIn()
            }

            result.fold(
                onSuccess = { response ->
                    val action = if (currentAttendance?.isCheckedIn == true) "Checked out" else "Checked in"
                    emitSuccess("$action successfully")
                    loadDashboardData() // Refresh
                },
                onFailure = { error ->
                    val errorMsg = error.message ?: "Failed to record attendance"
                    emitError(errorMsg)
                    // If server says already checked in, directly update UI state
                    if (errorMsg.contains("already checked in", ignoreCase = true)) {
                        _todayAttendance.value = TodayAttendance(
                            isCheckedIn = true,
                            isCheckedOut = false,
                            checkInTime = null,
                            checkOutTime = null,
                            status = "present",
                            totalHours = null
                        )
                    }
                    loadDashboardData() // Refresh to sync state
                }
            )

            _isCheckingInOut.value = false
        }
    }

    private fun startWorkingHoursTimer(checkInTime: String?) {
        stopWorkingHoursTimer()

        if (checkInTime.isNullOrBlank()) return

        timerJob = viewModelScope.launch {
            while (true) {
                try {
                    val checkInDate = parseCheckInTime(checkInTime)
                    val now = Date()
                    val diffInMillis = now.time - checkInDate.time
                    val diffInHours = diffInMillis / (1000.0 * 60 * 60)
                    _liveWorkingHours.value = diffInHours
                } catch (e: Exception) {
                    // If parsing fails, stop the timer
                    break
                }
                delay(1000) // Update every second
            }
        }
    }

    private fun stopWorkingHoursTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun parseCheckInTime(timeString: String): Date {
        return try {
            val normalized = timeString.trim().replace(' ', 'T')
            
            val zonedDateTime = try {
                DateTimeFormatter.ISO_DATE_TIME.parseBest(
                    normalized,
                    ZonedDateTime::from,
                    OffsetDateTime::from,
                    LocalDateTime::from
                )
            } catch (e: Exception) {
                null
            }
            
            val convertedDateTime = when (zonedDateTime) {
                is ZonedDateTime -> zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
                is OffsetDateTime -> zonedDateTime.atZoneSameInstant(ZoneId.systemDefault())
                is LocalDateTime -> zonedDateTime.atZone(ZoneId.systemDefault())
                else -> null
            }
            
            convertedDateTime?.let {
                Date.from(it.toInstant())
            } ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopWorkingHoursTimer()
    }
}

// Data classes for dashboard state
sealed class DashboardState {
    data object Loading : DashboardState()
    data object Success : DashboardState()
    data class Error(val message: String) : DashboardState()
}

data class TodayAttendance(
    val isCheckedIn: Boolean,
    val isCheckedOut: Boolean,
    val checkInTime: String?,
    val checkOutTime: String?,
    val status: String,
    val totalHours: Double?
)

data class ActivityItem(
    val title: String,
    val subtitle: String,
    val badge: String,
    val type: ActivityType
)

enum class ActivityType {
    CHECK_IN, CHECK_OUT, LEAVE
}
