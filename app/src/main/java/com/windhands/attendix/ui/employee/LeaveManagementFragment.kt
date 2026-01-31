package com.windhands.attendix.ui.employee

import android.os.Bundle
import android.util.Log
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
import com.windhands.attendix.databinding.FragmentLeaveManagementBinding
import com.windhands.attendix.ui.employee.adapter.LeaveBalanceCardAdapter
import com.windhands.attendix.ui.employee.adapter.LeaveRequestAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LeaveManagementFragment : Fragment() {

    private var _binding: FragmentLeaveManagementBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val TAG = "LeaveManagementFragment"
    }

    private val viewModel: LeaveManagementViewModel by viewModels()

    private lateinit var leaveBalanceAdapter: LeaveBalanceCardAdapter
    private lateinit var leaveRequestAdapter: LeaveRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaveManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupFilterChips()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupAdapters() {
        // Leave Balance RecyclerView (Horizontal)
        leaveBalanceAdapter = LeaveBalanceCardAdapter()
        binding.rvLeaveBalance.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = leaveBalanceAdapter
        }

        // Leave Requests RecyclerView (Vertical)
        leaveRequestAdapter = LeaveRequestAdapter(
            onItemClick = { item ->
                // Show leave request details dialog
                showLeaveDetailDialog(item)
            },
            onCancelClick = { item ->
                showCancelConfirmationDialog(item)
            }
        )
        binding.rvLeaveRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leaveRequestAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when {
                checkedIds.contains(R.id.chipPending) -> LeaveFilter.PENDING
                checkedIds.contains(R.id.chipApproved) -> LeaveFilter.APPROVED
                checkedIds.contains(R.id.chipRejected) -> LeaveFilter.REJECTED
                else -> LeaveFilter.ALL
            }
            viewModel.setFilter(filter)
        }
    }

    private fun setupClickListeners() {
        binding.fabApplyLeave.setOnClickListener {
            findNavController().navigate(R.id.action_leaveManagementFragment_to_applyLeaveFragment)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.leaveBalanceState.collectLatest { state ->
                        when (state) {
                            is LeaveBalanceState.Loading -> {
                                // Show loading for balance section if needed
                            }
                            is LeaveBalanceState.Success -> {
                                leaveBalanceAdapter.submitList(state.balances)
                            }
                            is LeaveBalanceState.Error -> {
                                Log.e(TAG, "LeaveBalanceState Error: ${state.message}")
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.leaveRequestsState.collectLatest { state ->
                        binding.loadingOverlay.isVisible = state is LeaveRequestsState.Loading
                        
                        when (state) {
                            is LeaveRequestsState.Error -> {
                                Log.e(TAG, "LeaveRequestsState Error: ${state.message}")
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.filteredRequests.collectLatest { requests ->
                        leaveRequestAdapter.submitList(requests)
                        binding.layoutEmpty.isVisible = requests.isEmpty()
                        binding.rvLeaveRequests.isVisible = requests.isNotEmpty()
                    }
                }

                launch {
                    viewModel.cancelState.collectLatest { state ->
                        when (state) {
                            is CancelState.Cancelling -> {
                                binding.loadingOverlay.isVisible = true
                            }
                            is CancelState.Success -> {
                                binding.loadingOverlay.isVisible = false
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetCancelState()
                            }
                            is CancelState.Error -> {
                                binding.loadingOverlay.isVisible = false
                                Log.e(TAG, "CancelState Error: ${state.message}")
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetCancelState()
                            }
                            is CancelState.Idle -> {}
                        }
                    }
                }
            }
        }
    }

    private fun showLeaveDetailDialog(item: LeaveRequestItem) {
        val statusText = when (item.status.lowercase()) {
            "approved" -> getString(R.string.status_approved)
            "pending" -> getString(R.string.status_pending)
            "rejected" -> getString(R.string.status_rejected)
            "cancelled" -> getString(R.string.status_cancelled)
            else -> item.status.replaceFirstChar { it.uppercase() }
        }

        val message = buildString {
            appendLine("${getString(R.string.leave_type_label)}: ${item.displayType}")
            appendLine("${getString(R.string.leave_date_range)}: ${item.formattedDateRange}")
            appendLine("${getString(R.string.leave_duration)}: ${item.days} ${if (item.days == 1) "day" else "days"}")
            appendLine("${getString(R.string.leave_status)}: $statusText")
            if (item.reason.isNotBlank()) {
                appendLine()
                appendLine("${getString(R.string.leave_reason)}: ${item.reason}")
            }
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(item.displayType)
            .setMessage(message)
            .setPositiveButton(R.string.common_ok, null)

        if (item.canCancel) {
            builder.setNegativeButton(R.string.leave_cancel) { _, _ ->
                showCancelConfirmationDialog(item)
            }
        }

        builder.show()
    }

    private fun showCancelConfirmationDialog(item: LeaveRequestItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.leave_cancel_title)
            .setMessage(R.string.leave_cancel_message)
            .setPositiveButton(R.string.leave_cancel_confirm) { _, _ ->
                viewModel.cancelLeaveRequest(item.id)
            }
            .setNegativeButton(R.string.common_no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
