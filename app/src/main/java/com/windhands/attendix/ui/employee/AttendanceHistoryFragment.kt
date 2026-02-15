package com.windhands.attendix.ui.employee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentAttendanceHistoryBinding
import com.windhands.attendix.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AttendanceHistoryFragment : BaseFragment<FragmentAttendanceHistoryBinding>() {

    private val viewModel: AttendanceHistoryViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAttendanceHistoryBinding {
        return FragmentAttendanceHistoryBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        setupCalendarView()
        setupCheckInOutButtons()
        setupCalendarNavigation()
    }

    private fun setupCalendarView() {
        android.util.Log.d("AttendanceHistory", "Setting up calendar view listener")
        binding.calendarView.onDateSelectedListener = { date ->
            android.util.Log.d("AttendanceHistory", "Calendar listener triggered for date: $date")
            viewModel.onDateSelected(date)
        }
    }

    private fun setupCalendarNavigation() {
        binding.btnPrevMonth.setOnClickListener {
            binding.calendarView.previousMonth()
            viewModel.clearSelectedDate()
            updateMonthDisplay()
        }
        
        binding.btnNextMonth.setOnClickListener {
            binding.calendarView.nextMonth()
            viewModel.clearSelectedDate()
            updateMonthDisplay()
        }
        
        binding.btnToday.setOnClickListener {
            binding.calendarView.goToToday()
            updateMonthDisplay()
        }
        
        updateMonthDisplay()
    }

    private fun updateMonthDisplay() {
        val currentMonth = binding.calendarView.getCurrentMonth()
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        binding.tvCurrentMonth.text = currentMonth.format(formatter)
    }

    private fun setupCheckInOutButtons() {
        binding.btnCheckIn.setOnClickListener {
            showCheckInConfirmation()
        }
        binding.btnCheckOut.setOnClickListener {
            showCheckOutConfirmation()
        }
    }

    private fun showCheckInConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Check In")
            .setMessage("Are you sure you want to check in?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.checkIn()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCheckOutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Check Out")
            .setMessage("Are you sure you want to check out?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.checkOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.historyState.collect { state ->
                        binding.loadingOverlay.isVisible = state is AttendanceHistoryState.Loading
                        
                        when (state) {
                            is AttendanceHistoryState.Error -> showError(state.message)
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.attendanceMap.collect { attendanceMap ->
                        binding.calendarView.setAttendanceData(attendanceMap)
                        binding.layoutEmpty.isVisible = attendanceMap.isEmpty() && 
                            viewModel.historyState.value !is AttendanceHistoryState.Loading
                    }
                }

                launch {
                    viewModel.selectedDateDetails.collect { details ->
                        android.util.Log.d("AttendanceHistory", "Fragment received details: ${details != null}")
                        if (details != null) {
                            android.util.Log.d("AttendanceHistory", "Showing details for: ${details.date}")
                            showDateDetails(details)
                        } else {
                            android.util.Log.d("AttendanceHistory", "Hiding details card")
                            binding.cardDayDetails.isVisible = false
                        }
                    }
                }

                launch {
                    viewModel.stats.collect { stats ->
                        stats?.let {
                            binding.tvPresentDays.text = it.presentDays.toString()
                            binding.tvAbsentDays.text = it.absentDays.toString()
                            binding.tvLateDays.text = it.lateDays.toString()
                            binding.tvLeaveDays.text = it.leaveDays.toString()
                        }
                    }
                }

                // Current time and date
                launch {
                    viewModel.currentTime.collect { time ->
                        binding.tvCurrentTime.text = time
                    }
                }

                launch {
                    viewModel.currentDate.collect { date ->
                        binding.tvCurrentDate.text = date
                    }
                }

                // Today's attendance status
                launch {
                    viewModel.todayAttendance.collect { status ->
                        updateCheckInOutButtons(status)
                    }
                }

                // Check-in/out loading state
                launch {
                    viewModel.isCheckingInOut.collect { isLoading ->
                        binding.btnCheckIn.isEnabled = !isLoading
                        binding.btnCheckOut.isEnabled = !isLoading
                    }
                }

                // Success events
                launch {
                    viewModel.successEvent.collect { message ->
                        showSuccess(message)
                    }
                }

                // Live working hours
                launch {
                    viewModel.liveWorkingHours.collect { hours ->
                        hours?.let { updateLiveHours(it) }
                    }
                }
            }
        }
    }

    private fun showDateDetails(details: AttendanceRecordItem) {
        binding.cardDayDetails.isVisible = true
        
        // Set date
        binding.tvSelectedDate.text = details.date
        
        // Set status with appropriate styling
        val (statusText, statusBg, statusColor) = when {
            details.isNoRecord -> Triple(
                getString(R.string.attendance_no_record),
                R.drawable.bg_chip_neutral,
                R.color.text_secondary
            )
            details.isPresent -> Triple(
                getString(R.string.attendance_filter_present),
                R.drawable.bg_chip_success,
                R.color.success
            )
            details.isAbsent -> Triple(
                getString(R.string.attendance_filter_absent),
                R.drawable.bg_chip_error,
                R.color.error
            )
            details.isLate -> Triple(
                getString(R.string.attendance_filter_late),
                R.drawable.bg_chip_warning,
                R.color.warning
            )
            details.isOnLeave -> Triple(
                getString(R.string.admin_on_leave),
                R.drawable.bg_chip_info,
                R.color.info
            )
            else -> Triple(
                details.status,
                R.drawable.bg_chip_default,
                R.color.text_secondary
            )
        }
        
        binding.tvSelectedStatus.text = statusText
        binding.tvSelectedStatus.setBackgroundResource(statusBg)
        binding.tvSelectedStatus.setTextColor(requireContext().getColor(statusColor))
        
        // Set times and work hours
        binding.tvSelectedCheckIn.text = details.checkIn
        binding.tvSelectedCheckOut.text = details.checkOut
        binding.tvSelectedWorkHours.text = details.formattedHours

        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, binding.cardDayDetails.top)
        }
    }

    private fun updateCheckInOutButtons(status: TodayAttendanceStatus?) {
        if (status == null) {
            // Not checked in yet
            binding.tvStatusBadge.text = getString(R.string.dashboard_not_checked_in)
            binding.btnCheckIn.isEnabled = true
            binding.btnCheckOut.isEnabled = false
            return
        }

        when {
            status.isCheckedOut -> {
                // Already checked out
                binding.tvStatusBadge.text = getString(R.string.dashboard_checked_out)
                binding.btnCheckIn.isEnabled = false
                binding.btnCheckOut.isEnabled = false
            }
            status.isCheckedIn -> {
                // Checked in, can check out
                val hoursText = viewModel.liveWorkingHours.value?.let { hours ->
                    val h = hours.toInt()
                    val minutes = ((hours - h) * 60).toInt()
                    " • ${h}h ${minutes}m"
                } ?: ""
                binding.tvStatusBadge.text = getString(R.string.dashboard_checked_in) + hoursText
                binding.btnCheckIn.isEnabled = false
                binding.btnCheckOut.isEnabled = true
            }
            else -> {
                // Not checked in
                binding.tvStatusBadge.text = getString(R.string.dashboard_not_checked_in)
                binding.btnCheckIn.isEnabled = true
                binding.btnCheckOut.isEnabled = false
            }
        }
    }

    private fun updateLiveHours(hours: Double) {
        val status = viewModel.todayAttendance.value
        if (status?.isCheckedIn == true && !status.isCheckedOut) {
            val h = hours.toInt()
            val minutes = ((hours - h) * 60).toInt()
            binding.tvStatusBadge.text = getString(R.string.dashboard_checked_in) + " • ${h}h ${minutes}m"
        }
    }
}