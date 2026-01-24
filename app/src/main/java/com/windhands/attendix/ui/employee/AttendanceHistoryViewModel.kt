package com.windhands.attendix.ui.employee

import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.remote.dto.AttendanceDto
import com.windhands.attendix.data.repository.AttendanceRepository
import com.windhands.attendix.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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

    private var allRecords: List<AttendanceRecordItem> = emptyList()

    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    private val apiDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadAttendanceHistory()
        loadStats()
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
                            checkIn = att.checkIn ?: "--:--",
                            checkOut = att.checkOut ?: "--:--",
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
