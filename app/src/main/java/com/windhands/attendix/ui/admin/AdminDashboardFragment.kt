package com.windhands.attendix.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentAdminDashboardBinding
import com.windhands.attendix.databinding.ItemMetricCardBinding
import com.windhands.attendix.ui.admin.adapter.PendingApprovalAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminDashboardViewModel by viewModels()

    private lateinit var pendingApprovalAdapter: PendingApprovalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        pendingApprovalAdapter = PendingApprovalAdapter(
            onApproveClick = { item ->
                showApproveConfirmationDialog(item)
            },
            onRejectClick = { item ->
                showRejectConfirmationDialog(item)
            }
        )
        binding.rvPendingApprovals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pendingApprovalAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.tvViewAllApprovals.setOnClickListener {
            // Navigate to leave approvals
            navigateToBottomNavDestination(R.id.leaveApprovalsFragment)
        }

        binding.cardManageEmployees.setOnClickListener {
            // Navigate to employee management
            navigateToBottomNavDestination(R.id.employeeManagementFragment)
        }

        binding.cardViewAttendance.setOnClickListener {
            // Navigate to attendance management
            navigateToBottomNavDestination(R.id.attendanceManagementFragment)
        }

        binding.ivProfileAvatar.setOnClickListener {
            // Navigate to profile
            navigateToBottomNavDestination(R.id.adminProfileFragment)
        }
    }

    private fun navigateToBottomNavDestination(destinationId: Int) {
        try {
            findNavController().navigate(destinationId)
        } catch (e: Exception) {
            // If direct navigation fails, try parent fragment's nav controller
            parentFragment?.parentFragment?.let { containerFragment ->
                // Handle bottom nav selection programmatically
            }
        }
    }

    private fun showApproveConfirmationDialog(item: PendingApprovalItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.leave_approve)
            .setMessage(getString(R.string.admin_approve_confirm, item.employeeName))
            .setPositiveButton(R.string.leave_approve) { _, _ ->
                viewModel.approveLeaveRequest(item.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showRejectConfirmationDialog(item: PendingApprovalItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.leave_reject)
            .setMessage(getString(R.string.admin_reject_confirm, item.employeeName))
            .setPositiveButton(R.string.leave_reject) { _, _ ->
                viewModel.rejectLeaveRequest(item.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentDate.collectLatest { date ->
                        binding.tvDate.text = date
                    }
                }

                launch {
                    viewModel.dashboardState.collectLatest { state ->
                        binding.loadingOverlay.isVisible = state is DashboardState.Loading
                        
                        when (state) {
                            is DashboardState.Success -> {
                                bindStats(state.stats)
                            }
                            is DashboardState.Error -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            }
                            is DashboardState.Loading -> {}
                        }
                    }
                }

                launch {
                    viewModel.pendingApprovals.collectLatest { approvals ->
                        pendingApprovalAdapter.submitList(approvals)
                        binding.layoutEmptyApprovals.isVisible = approvals.isEmpty()
                        binding.rvPendingApprovals.isVisible = approvals.isNotEmpty()
                    }
                }

                launch {
                    viewModel.approveRejectState.collectLatest { state ->
                        when (state) {
                            is ApproveRejectState.Processing -> {
                                binding.loadingOverlay.isVisible = true
                            }
                            is ApproveRejectState.Success -> {
                                binding.loadingOverlay.isVisible = false
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetApproveRejectState()
                            }
                            is ApproveRejectState.Error -> {
                                binding.loadingOverlay.isVisible = false
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetApproveRejectState()
                            }
                            is ApproveRejectState.Idle -> {}
                        }
                    }
                }
            }
        }
    }

    private fun bindStats(stats: DashboardStats) {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        
        // Total Employees
        bindMetricCard(
            binding.metricTotalEmployees,
            R.drawable.ic_people,
            R.color.primary,
            getString(R.string.admin_total_employees),
            numberFormat.format(stats.totalEmployees)
        )

        // Present Today
        bindMetricCard(
            binding.metricPresentToday,
            R.drawable.ic_check_circle,
            R.color.success,
            getString(R.string.admin_present_today),
            numberFormat.format(stats.presentToday)
        )

        // On Leave
        bindMetricCard(
            binding.metricOnLeave,
            R.drawable.ic_event_busy,
            R.color.error,
            getString(R.string.admin_on_leave),
            numberFormat.format(stats.onLeave)
        )

        // Late Arrivals
        bindMetricCard(
            binding.metricLateArrivals,
            R.drawable.ic_timer,
            R.color.warning,
            getString(R.string.admin_late_arrivals),
            numberFormat.format(stats.lateArrivals)
        )
    }

    private fun bindMetricCard(
        cardBinding: ItemMetricCardBinding,
        iconRes: Int,
        iconColorRes: Int,
        label: String,
        value: String
    ) {
        cardBinding.ivIcon.setImageResource(iconRes)
        cardBinding.ivIcon.setColorFilter(requireContext().getColor(iconColorRes))
        cardBinding.tvLabel.text = label
        cardBinding.tvValue.text = value
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
