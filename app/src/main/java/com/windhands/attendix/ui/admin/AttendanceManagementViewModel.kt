package com.windhands.attendix.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.repository.AttendanceRepository
import com.windhands.attendix.util.toDisplayTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AttendanceManagementViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _attendanceState = MutableStateFlow<AttendanceListState>(AttendanceListState.Loading)
    val attendanceState: StateFlow<AttendanceListState> = _attendanceState.asStateFlow()

    private val _attendanceRecords = MutableStateFlow<List<AttendanceAdminItem>>(emptyList())
    val attendanceRecords: StateFlow<List<AttendanceAdminItem>> = _attendanceRecords.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _selectedDateFormatted = MutableStateFlow(formatDateForDisplay(LocalDate.now()))
    val selectedDateFormatted: StateFlow<String> = _selectedDateFormatted.asStateFlow()

    private val _currentFilter = MutableStateFlow(AttendanceAdminFilter.ALL)
    val currentFilter: StateFlow<AttendanceAdminFilter> = _currentFilter.asStateFlow()

    init {
        loadAttendance()
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        _selectedDateFormatted.value = formatDateForDisplay(date)
        loadAttendance()
    }

    fun setFilter(filter: AttendanceAdminFilter) {
        if (_currentFilter.value != filter) {
            _currentFilter.value = filter
            loadAttendance()
        }
    }

    fun loadAttendance() {
        viewModelScope.launch {
            _attendanceState.value = AttendanceListState.Loading
            
            val dateString = _selectedDate.value.format(DateTimeFormatter.ISO_DATE)
            val status = when (_currentFilter.value) {
                AttendanceAdminFilter.ALL -> null
                AttendanceAdminFilter.PRESENT -> "present"
                AttendanceAdminFilter.ABSENT -> "absent"
                AttendanceAdminFilter.LATE -> "late"
                AttendanceAdminFilter.ON_LEAVE -> "leave"
            }
            
            attendanceRepository.getAllAttendance(date = dateString, status = status)
                .onSuccess { response ->
                    if (response.success) {
                        val records = (response.data?.attendance ?: response.data?.records ?: emptyList())
                            .map { record ->
                                AttendanceAdminItem(
                                    id = record.id,
                                    employeeId = record.employeeId,
                                    employeeName = "${record.firstName ?: ""} ${record.lastName ?: ""}".trim()
                                        .ifEmpty { "Employee #${record.employeeId}" },
                                    department = record.department ?: "",
                                    checkIn = formatTime(record.checkIn),
                                    checkOut = formatTime(record.checkOut),
                                    status = record.status,
                                    workingHours = record.workingHours ?: 0.0
                                )
                            }
                        _attendanceRecords.value = records
                        _attendanceState.value = AttendanceListState.Success
                    } else {
                        _attendanceState.value = AttendanceListState.Error(
                            response.message ?: "Failed to load attendance"
                        )
                    }
                }
                .onFailure { exception ->
                    _attendanceState.value = AttendanceListState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    private fun formatDateForDisplay(date: LocalDate): String {
        val today = LocalDate.now()
        val prefix = when (date) {
            today -> "Today, "
            today.minusDays(1) -> "Yesterday, "
            else -> ""
        }
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        return prefix + date.format(formatter)
    }

    private fun formatTime(timeString: String?): String {
        if (timeString.isNullOrBlank()) return "--:--"
        return timeString.toDisplayTime()
    }
}

sealed class AttendanceListState {
    data object Loading : AttendanceListState()
    data object Success : AttendanceListState()
    data class Error(val message: String) : AttendanceListState()
}

enum class AttendanceAdminFilter {
    ALL, PRESENT, ABSENT, LATE, ON_LEAVE
}

data class AttendanceAdminItem(
    val id: Int,
    val employeeId: Int,
    val employeeName: String,
    val department: String,
    val checkIn: String,
    val checkOut: String,
    val status: String,
    val workingHours: Double
) {
    val initials: String get() {
        val parts = employeeName.split(" ")
        return when {
            parts.size >= 2 -> "${parts[0].firstOrNull() ?: ""}${parts[1].firstOrNull() ?: ""}".uppercase()
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "??"
        }
    }

    val formattedWorkingHours: String get() {
        val hours = workingHours.toInt()
        val minutes = ((workingHours - hours) * 60).toInt()
        return if (hours > 0 || minutes > 0) {
            "${hours}h ${minutes}m"
        } else {
            "--"
        }
    }

    val isPresent: Boolean get() = status.lowercase() in listOf("present", "checked_in", "checked_out")
    val isAbsent: Boolean get() = status.lowercase() == "absent"
    val isLate: Boolean get() = status.lowercase() == "late"
    val isOnLeave: Boolean get() = status.lowercase() in listOf("leave", "on_leave")
}
