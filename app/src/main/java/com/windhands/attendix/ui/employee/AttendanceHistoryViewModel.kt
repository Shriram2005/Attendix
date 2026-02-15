package com.windhands.attendix.ui.employee

import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.remote.dto.AttendanceDto
import com.windhands.attendix.data.repository.AttendanceRepository
import com.windhands.attendix.ui.base.BaseViewModel
import com.windhands.attendix.ui.employee.view.AttendanceStatus
import com.windhands.attendix.util.toDisplayTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
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

    // Calendar-specific state
    private val _attendanceMap = MutableStateFlow<Map<LocalDate, AttendanceStatus>>(emptyMap())
    val attendanceMap: StateFlow<Map<LocalDate, AttendanceStatus>> = _attendanceMap.asStateFlow()

    private val _selectedDateDetails = MutableStateFlow<AttendanceRecordItem?>(null)
    val selectedDateDetails: StateFlow<AttendanceRecordItem?> = _selectedDateDetails.asStateFlow()

    private val _stats = MutableStateFlow<AttendanceStats?>(null)
    val stats: StateFlow<AttendanceStats?> = _stats.asStateFlow()

    private val _todayAttendance = MutableStateFlow<TodayAttendanceStatus?>(null)
    val todayAttendance: StateFlow<TodayAttendanceStatus?> = _todayAttendance.asStateFlow()

    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _isCheckingInOut = MutableStateFlow(false)
    val isCheckingInOut: StateFlow<Boolean> = _isCheckingInOut.asStateFlow()

    private val _liveWorkingHours = MutableStateFlow<Double?>(null)
    val liveWorkingHours: StateFlow<Double?> = _liveWorkingHours.asStateFlow()

    private var timerJob: Job? = null

    private val _successEvent = MutableSharedFlow<String>()
    override val successEvent: SharedFlow<String> = _successEvent.asSharedFlow()

    // Store all records for quick lookup
    private var allRecordsMap: Map<LocalDate, AttendanceRecordItem> = emptyMap()

    private val displayDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")
    private val apiDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
    private val checkInTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

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

                        // Start or stop timer based on check-in/out status
                        if (att.checkIn != null && att.checkOut == null) {
                            startWorkingHoursTimer(att.checkIn)
                        } else {
                            stopWorkingHoursTimer()
                            _liveWorkingHours.value = att.workingHours
                        }
                    } ?: run {
                        _todayAttendance.value = TodayAttendanceStatus(
                            isCheckedIn = false,
                            isCheckedOut = false,
                            checkInTime = null,
                            checkOutTime = null,
                            status = "not_checked_in"
                        )
                        stopWorkingHoursTimer()
                        _liveWorkingHours.value = null
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
                    stopWorkingHoursTimer()
                    _liveWorkingHours.value = null
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

    fun onDateSelected(date: LocalDate) {
        val details = allRecordsMap[date] ?: buildPlaceholderRecord(date)
        _selectedDateDetails.value = details
        
        // Log for debugging
        android.util.Log.d("AttendanceHistory", "Date selected: $date")
        android.util.Log.d("AttendanceHistory", "Details found: ${details != null}")
        android.util.Log.d("AttendanceHistory", "Total records in map: ${allRecordsMap.size}")
        if (details != null) {
            android.util.Log.d("AttendanceHistory", "Details: checkIn=${details.checkIn}, checkOut=${details.checkOut}")
        }
    }

    private fun buildPlaceholderRecord(date: LocalDate): AttendanceRecordItem {
        val formattedDate = date.format(displayDateFormatter)
        val rawDate = date.format(apiDateFormatter)
        val dayOfWeek = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

        return AttendanceRecordItem(
            id = -1,
            date = formattedDate,
            dateRaw = rawDate,
            checkIn = "--:--",
            checkOut = "--:--",
            status = "no_record",
            workHours = 0.0,
            dayOfWeek = dayOfWeek
        )
    }

    fun loadAttendanceHistory() {
        viewModelScope.launch {
            _historyState.value = AttendanceHistoryState.Loading

            val result = attendanceRepository.getAttendanceHistory()
            
            result.fold(
                onSuccess = { response ->
                    val attendanceList = response.data?.attendance ?: response.data?.records ?: emptyList()
                    
                    // Build calendar map and records map
                    val calendarMap = mutableMapOf<LocalDate, AttendanceStatus>()
                    val recordsMap = mutableMapOf<LocalDate, AttendanceRecordItem>()
                    
                    attendanceList.forEach { att ->
                        val localDate = parseDate(att.date)
                        if (localDate != null) {
                            val status = AttendanceStatus.fromString(att.status)
                            calendarMap[localDate] = status
                            
                            val record = AttendanceRecordItem(
                                id = att.id,
                                date = formatDate(att.date),
                                dateRaw = att.date,
                                checkIn = formatTimeFromApi(att.checkIn),
                                checkOut = formatTimeFromApi(att.checkOut),
                                status = att.status,
                                workHours = att.workingHours ?: 0.0,
                                dayOfWeek = getDayOfWeek(att.date)
                            )
                            recordsMap[localDate] = record
                        }
                    }
                    
                    allRecordsMap = recordsMap
                    _attendanceMap.value = calendarMap
                    _historyState.value = AttendanceHistoryState.Success
                },
                onFailure = { error ->
                    _historyState.value = AttendanceHistoryState.Error(error.message ?: "Failed to load history")
                    emitError(error.message ?: "Failed to load attendance history")
                }
            )
        }
    }

    fun clearSelectedDate() {
        _selectedDateDetails.value = null
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

    private fun parseDate(dateString: String): LocalDate? {
        return try {
            // Handle both date formats: "yyyy-MM-dd" and "yyyy-MM-ddThh:mm:ss.sssZ"
            if (dateString.contains("T")) {
                LocalDate.parse(dateString.substring(0, 10), apiDateFormatter)
            } else {
                LocalDate.parse(dateString, apiDateFormatter)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val date = parseDate(dateString)
            date?.format(displayDateFormatter) ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatTimeFromApi(timeString: String?): String {
        if (timeString.isNullOrBlank()) return "--:--"
        return timeString.toDisplayTime()
    }

    private fun getDayOfWeek(dateString: String): String {
        return try {
            val date = parseDate(dateString)
            date?.dayOfWeek?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""
        } catch (e: Exception) {
            ""
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

sealed class AttendanceHistoryState {
    data object Loading : AttendanceHistoryState()
    data object Success : AttendanceHistoryState()
    data class Error(val message: String) : AttendanceHistoryState()
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
            if (isNoRecord) return "--"
            val hours = workHours.toInt()
            val minutes = ((workHours - hours) * 60).toInt()
            return "${hours}h ${minutes}m"
        }

    val isPresent: Boolean get() = status.equals("present", true)
    val isAbsent: Boolean get() = status.equals("absent", true)
    val isLate: Boolean get() = status.equals("late", true)
    val isOnLeave: Boolean get() = status.equals("on-leave", true) || status.equals("leave", true)
    val isNoRecord: Boolean get() = status.equals("no_record", true) || status.equals("no record", true)
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
