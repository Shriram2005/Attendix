package com.windhands.attendix.ui.employee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentEmployeeDashboardBinding
import com.windhands.attendix.ui.base.BaseFragment
import com.windhands.attendix.ui.employee.adapter.LeaveBalanceMiniAdapter
import com.windhands.attendix.util.toDisplayTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Employee Dashboard Screen Fragment
 */
@AndroidEntryPoint
class EmployeeDashboardFragment : BaseFragment<FragmentEmployeeDashboardBinding>() {

    private val viewModel: EmployeeDashboardViewModel by viewModels()
    private lateinit var leaveBalanceAdapter: LeaveBalanceMiniAdapter

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEmployeeDashboardBinding {
        return FragmentEmployeeDashboardBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        leaveBalanceAdapter = LeaveBalanceMiniAdapter()
        binding.rvLeaveBalance.apply {
            adapter = leaveBalanceAdapter
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            // Check-in/Check-out button
            btnCheckInOut.setOnClickListener {
                showCheckInOutConfirmation()
            }

            // View all leaves
            btnViewAllLeaves.setOnClickListener {
                findNavController().navigate(R.id.action_employeeDashboard_to_leaveManagement)
            }

            // Profile click
            ivProfile.setOnClickListener {
                findNavController().navigate(R.id.action_employeeDashboard_to_profile)
            }
        }
    }

    private fun showCheckInOutConfirmation() {
        val currentAttendance = viewModel.todayAttendance.value
        val isCheckIn = currentAttendance?.isCheckedIn != true || currentAttendance.isCheckedOut

        val title = if (isCheckIn) "Check In" else "Check Out"
        val message = if (isCheckIn) {
            "Are you sure you want to check in?"
        } else {
            "Are you sure you want to check out?"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                viewModel.checkInOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Dashboard state
                launch {
                    viewModel.dashboardState.collect { state ->
                        when (state) {
                            is DashboardState.Loading -> showLoading(true)
                            is DashboardState.Success -> showLoading(false)
                            is DashboardState.Error -> {
                                showLoading(false)
                                showError(state.message)
                            }
                        }
                    }
                }

                // User name
                launch {
                    viewModel.userName.collect { name ->
                        binding.tvGreeting.text = name
                    }
                }

                // Department
                launch {
                    viewModel.department.collect { dept ->
                        binding.tvDepartment.text = dept
                    }
                }

                // Current time
                launch {
                    viewModel.currentTime.collect { time ->
                        binding.tvCurrentTime.text = time
                    }
                }

                // Current date
                launch {
                    viewModel.currentDate.collect { date ->
                        binding.tvCurrentDate.text = date
                    }
                }

                // Today's attendance
                launch {
                    viewModel.todayAttendance.collect { attendance ->
                        updateAttendanceUI(attendance)
                    }
                }

                // Leave balances
                launch {
                    viewModel.leaveBalances.collect { balances ->
                        leaveBalanceAdapter.submitList(balances)
                    }
                }

                // Recent activities
                launch {
                    viewModel.recentActivities.collect { activities ->
                        updateRecentActivities(activities)
                    }
                }

                // Check-in/out loading
                launch {
                    viewModel.isCheckingInOut.collect { isLoading ->
                        binding.btnCheckInOut.isEnabled = !isLoading
                    }
                }

                // Success events
                launch {
                    viewModel.successEvent.collect { message ->
                        showSuccess(message)
                    }
                }

                // Error events
                launch {
                    viewModel.errorEvent.collect { message ->
                        showError(message)
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

    private fun updateRecentActivities(activities: List<ActivityItem>) {
        binding.layoutRecentActivity.removeAllViews()
        
        if (activities.isEmpty()) {
            // Show empty state
            binding.layoutEmptyActivity.isVisible = true
            binding.layoutRecentActivity.isVisible = false
        } else {
            binding.layoutEmptyActivity.isVisible = false
            binding.layoutRecentActivity.isVisible = true
            
            activities.forEach { activity ->
                val activityView = createActivityItemView(activity)
                binding.layoutRecentActivity.addView(activityView)
            }
        }
    }

    private fun createActivityItemView(activity: ActivityItem): View {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.item_recent_activity, binding.layoutRecentActivity, false)
        
        view.findViewById<TextView>(R.id.tv_activity_title)?.text = activity.title
        view.findViewById<TextView>(R.id.tv_activity_subtitle)?.text = activity.subtitle
        view.findViewById<TextView>(R.id.tv_activity_badge)?.text = activity.badge
        
        return view
    }

    private fun updateAttendanceUI(attendance: TodayAttendance?) {
        with(binding) {
            // Show/hide times layout based on check-in status
            layoutTimes.isVisible = attendance?.isCheckedIn == true
            
            if (attendance == null) {
                // Not checked in yet
                tvCheckInTime.text = "--:--"
                tvCheckOutTime.text = "--:--"
                tvTotalHours.text = "0h 0m"
                tvStatusSubtitle.text = getString(R.string.dashboard_not_checked_in)
                btnCheckInOut.text = getString(R.string.dashboard_check_in)
                btnCheckInOut.isEnabled = true
            } else {
                tvCheckInTime.text = formatTimeDisplay(attendance.checkInTime) ?: "--:--"
                tvCheckOutTime.text = formatTimeDisplay(attendance.checkOutTime) ?: "--:--"
                
                // Use live hours if available, otherwise use totalHours
                val hoursToDisplay = viewModel.liveWorkingHours.value ?: attendance.totalHours
                val hours = hoursToDisplay?.toInt() ?: 0
                val minutes = ((hoursToDisplay ?: 0.0) - hours) * 60
                tvTotalHours.text = "${hours}h ${minutes.toInt()}m"

                when {
                    attendance.isCheckedOut -> {
                        tvStatusSubtitle.text = getString(R.string.dashboard_checked_out)
                        btnCheckInOut.text = getString(R.string.dashboard_completed)
                        btnCheckInOut.isEnabled = false
                    }
                    attendance.isCheckedIn -> {
                        tvStatusSubtitle.text = getString(R.string.dashboard_checked_in)
                        btnCheckInOut.text = getString(R.string.dashboard_check_out)
                        btnCheckInOut.isEnabled = true
                    }
                    else -> {
                        tvStatusSubtitle.text = getString(R.string.dashboard_not_checked_in)
                        btnCheckInOut.text = getString(R.string.dashboard_check_in)
                        btnCheckInOut.isEnabled = true
                    }
                }
            }
        }
    }

    private fun updateLiveHours(hours: Double) {
        with(binding) {
            val h = hours.toInt()
            val minutes = ((hours - h) * 60).toInt()
            tvTotalHours.text = "${h}h ${minutes}m"
        }
    }

    override fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.isVisible = isLoading
    }
    
    private fun formatTimeDisplay(timeString: String?): String? {
        if (timeString.isNullOrBlank()) return null
        return timeString.toDisplayTime()
    }
}
