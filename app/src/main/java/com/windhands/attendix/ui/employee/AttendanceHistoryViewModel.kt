package com.windhands.attendix.ui.employee

import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.remote.dto.AttendanceDto
import com.windhands.attendix.data.repository.AttendanceRepository
import com.windhands.attendix.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AttendanceHistoryViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) : BaseViewModel() {

    private val _historyState = MutableStateFlow<AttendanceHistoryState>(AttendanceHistoryState.Loading)
    val historyState: StateFlow<AttendanceHistoryState> = _historyState.asStateFlow()

    private val _attendanceRecords = MutableStateFlow<List<AttendanceRecordItem>>(emptyList())
    val attendanceRecords: StateFlow<List<AttendanceRecordItem>> = _attendanceRecords.asStateFlow()

    private val _stats = MutableStateFlow<AttendanceStats?>(null)
    val stats: StateFlow<AttendanceStats?> = _stats.asStateFlow()

    private val _currentFilter = MutableStateFlow(AttendanceFilter.ALL)
    val currentFilter: StateFlow<AttendanceFilter> = _currentFilter.asStateFlow()

    private val _todayAttendance = MutableStateFlow<TodayAttendanceStatus?>(null)
    val todayAttendance: StateFlow<TodayAttendanceStatus?> = _todayAttendance.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _isCheckingInOut = MutableStateFlow(false)
    val isCheckingInOut: StateFlow<Boolean> = _isCheckingInOut.asStateFlow()

    private val _successEvent = MutableSharedFlow<String>()
    override val successEvent: SharedFlow<String> = _successEvent.asSharedFlow()

    private var allRecords: List<AttendanceRecordItem> = emptyList()

    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private val apiDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())

    init {
        updateTime()
        loadTodayStatus()
        loadAttendanceHistory()
        loadStats()
    }

    private fun updateTime() {
        val now = Date()
        _currentTime.value = timeFormat.format(now)
        _currentDate.value = dateFormat.format(now)
    }

    private fun loadTodayStatus() {
        viewModelScope.launch {
            attendanceRepository.getTodayStatus()
                .onSuccess { response ->
                    response.data?.let { att ->
                        _todayAttendance.value = TodayAttendanceStatus(
                            isCheckedIn = att.checkIn != null,
                            isCheckedOut = att.checkOut != null,
                            checkInTime = att.checkIn,
                            checkOutTime = att.checkOut,
                            status = att.status
                        )
                    } ?: run {
                        _todayAttendance.value = TodayAttendanceStatus(
                            isCheckedIn = false,
                            isCheckedOut = false,
                            checkInTime = null,
                            checkOutTime = null,
                            status = "not_checked_in"
                        )
                    }
                }
                .onFailure {
                    _todayAttendance.value = TodayAttendanceStatus(
                        isCheckedIn = false,
                        isCheckedOut = false,
                        checkInTime = null,
                        checkOutTime = null,
                        status = "not_checked_in"
                    )
                }
        }
    }

    fun checkIn() {
        viewModelScope.launch {
            _isCheckingInOut.value = true
            attendanceRepository.checkIn()
                .onSuccess { response ->
                    _successEvent.emit("Checked in successfully")
                    loadTodayStatus()
                    loadAttendanceHistory()
                }
                .onFailure { error ->
                    emitError(error.message ?: "Failed to check in")
                }
            _isCheckingInOut.value = false
        }
    }

    fun checkOut() {
        viewModelScope.launch {
            _isCheckingInOut.value = true
            attendanceRepository.checkOut()
                .onSuccess { response ->
                    _successEvent.emit("Checked out successfully")
                    loadTodayStatus()
                    loadAttendanceHistory()
                }
                .onFailure { error ->
                    emitError(error.message ?: "Failed to check out")
                }
            _isCheckingInOut.value = false
        }
    }

    fun setFilter(filter: AttendanceFilter) {
        _currentFilter.value = filter
        applyFilter()
    }

    private fun applyFilter() {
        val filter = _currentFilter.value
        _attendanceRecords.value = when (filter) {
            AttendanceFilter.ALL -> allRecords
            AttendanceFilter.PRESENT -> allRecords.filter { it.status.equals("present", true) }
            AttendanceFilter.ABSENT -> allRecords.filter { it.status.equals("absent", true) }
            AttendanceFilter.LATE -> allRecords.filter { it.status.equals("late", true) }
            AttendanceFilter.LEAVE -> allRecords.filter { it.status.equals("on-leave", true) || it.status.equals("leave", true) }
        }
    }

    fun loadAttendanceHistory() {
        viewModelScope.launch {
            _historyState.value = AttendanceHistoryState.Loading

            val result = attendanceRepository.getAttendanceHistory()
            
            result.fold(
                onSuccess = { response ->
                    val attendanceList = response.data?.attendance ?: response.data?.records ?: emptyList()
                    val records = attendanceList.map { att ->
                        AttendanceRecordItem(
                            id = att.id,
                            date = formatDate(att.date),
                            dateRaw = att.date,
                            checkIn = formatTimeFromApi(att.checkIn),
                            checkOut = formatTimeFromApi(att.checkOut),
                            status = att.status,
                            workHours = att.workingHours ?: 0.0,
                            dayOfWeek = getDayOfWeek(att.date)
                        )
                    }
                    
                    allRecords = records
                    applyFilter()
                    _historyState.value = AttendanceHistoryState.Success
                },
                onFailure = { error ->
                    _historyState.value = AttendanceHistoryState.Error(error.message ?: "Failed to load history")
                    emitError(error.message ?: "Failed to load attendance history")
                }
            )
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            val result = attendanceRepository.getAttendanceStats()
            
            result.fold(
                onSuccess = { response ->
                    val statsData = response.data
                    _stats.value = AttendanceStats(
                        presentDays = statsData?.presentDays ?: 0,
                        absentDays = statsData?.absentDays ?: 0,
                        lateDays = statsData?.lateDays ?: 0,
                        leaveDays = statsData?.leaveDays ?: 0
                    )
                },
                onFailure = {
                    // Stats loading failed, non-critical
                }
            )
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, apiDateFormatter)
            date.format(displayDateFormatter)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatTimeFromApi(timeString: String?): String {
        if (timeString.isNullOrBlank()) return "--:--"
        return try {
            // Handle ISO datetime format like "2024-01-15T09:00:00.000Z"
            if (timeString.contains("T")) {
                val formatter = DateTimeFormatter.ISO_DATE_TIME
                val dateTime = java.time.LocalDateTime.parse(timeString.substring(0, 19), formatter)
                dateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
            } else {
                timeString
            }
        } catch (e: Exception) {
            timeString
        }
    }

    private fun getDayOfWeek(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, apiDateFormatter)
            date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            ""
        }
    }
}

sealed class AttendanceHistoryState {
    data object Loading : AttendanceHistoryState()
    data object Success : AttendanceHistoryState()
    data class Error(val message: String) : AttendanceHistoryState()
}

enum class AttendanceFilter {
    ALL, PRESENT, ABSENT, LATE, LEAVE
}

data class AttendanceRecordItem(
    val id: Int,
    val date: String,
    val dateRaw: String,
    val checkIn: String,
    val checkOut: String,
    val status: String,
    val workHours: Double,
    val dayOfWeek: String
) {
    val formattedHours: String
        get() {
            val hours = workHours.toInt()
            val minutes = ((workHours - hours) * 60).toInt()
            return "${hours}h ${minutes}m"
        }

    val isPresent: Boolean get() = status.equals("present", true)
    val isAbsent: Boolean get() = status.equals("absent", true)
    val isLate: Boolean get() = status.equals("late", true)
    val isOnLeave: Boolean get() = status.equals("on-leave", true) || status.equals("leave", true)
}

data class AttendanceStats(
    val presentDays: Int,
    val absentDays: Int,
    val lateDays: Int,
    val leaveDays: Int
)

data class TodayAttendanceStatus(
    val isCheckedIn: Boolean,
    val isCheckedOut: Boolean,
    val checkInTime: String?,
    val checkOutTime: String?,
    val status: String
)
