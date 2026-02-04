package com.windhands.attendix.ui.admin

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.windhands.attendix.databinding.FragmentLeaveApprovalsBinding
import com.windhands.attendix.ui.admin.adapter.LeaveApprovalAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LeaveApprovalsFragment : Fragment() {

    private var _binding: FragmentLeaveApprovalsBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val TAG = "LeaveApprovalsFragment"
    }

    private val viewModel: LeaveApprovalsViewModel by viewModels()

    private lateinit var leaveApprovalAdapter: LeaveApprovalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaveApprovalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupFilterChips()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupFilterChips() {
        binding.chipPending.setOnClickListener {
            viewModel.setFilter(LeaveApprovalFilter.PENDING)
        }
        binding.chipAll.setOnClickListener {
            viewModel.setFilter(LeaveApprovalFilter.ALL)
        }
        binding.chipApproved.setOnClickListener {
            viewModel.setFilter(LeaveApprovalFilter.APPROVED)
        }
        binding.chipRejected.setOnClickListener {
            viewModel.setFilter(LeaveApprovalFilter.REJECTED)
        }
    }

    private fun setupRecyclerView() {
        leaveApprovalAdapter = LeaveApprovalAdapter(
            onApproveClick = { item ->
                showApproveDialog(item)
            },
            onRejectClick = { item ->
                showRejectDialog(item)
            }
        )

        binding.rvLeaveRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leaveApprovalAdapter
        }
    }

    private fun showApproveDialog(item: LeaveApprovalItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.leave_approve)
            .setMessage(getString(R.string.admin_approve_confirm, item.employeeName))
            .setPositiveButton(R.string.leave_approve) { _, _ ->
                viewModel.approveRequest(item.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showRejectDialog(item: LeaveApprovalItem) {
        val editText = EditText(requireContext()).apply {
            hint = getString(R.string.leave_remarks_hint)
            setPadding(48, 32, 48, 16)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.leave_reject)
            .setMessage(getString(R.string.admin_reject_confirm, item.employeeName))
            .setView(editText)
            .setPositiveButton(R.string.leave_reject) { _, _ ->
                val remarks = editText.text.toString().takeIf { it.isNotBlank() }
                viewModel.rejectRequest(item.id, remarks)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentFilter.collectLatest { filter ->
                        binding.chipPending.isChecked = filter == LeaveApprovalFilter.PENDING
                        binding.chipAll.isChecked = filter == LeaveApprovalFilter.ALL
                        binding.chipApproved.isChecked = filter == LeaveApprovalFilter.APPROVED
                        binding.chipRejected.isChecked = filter == LeaveApprovalFilter.REJECTED
                        
                        // Update empty state subtitle based on filter
                        binding.tvEmptySubtitle.text = when (filter) {
                            LeaveApprovalFilter.PENDING -> getString(R.string.empty_pending_leave_requests)
                            LeaveApprovalFilter.APPROVED -> getString(R.string.empty_approved_leave_requests)
                            LeaveApprovalFilter.REJECTED -> getString(R.string.empty_rejected_leave_requests)
                            LeaveApprovalFilter.ALL -> getString(R.string.empty_leave_requests_subtitle)
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
                    viewModel.leaveRequests.collectLatest { requests ->
                        leaveApprovalAdapter.submitList(requests)
                        binding.layoutEmpty.isVisible = requests.isEmpty() && 
                            viewModel.leaveRequestsState.value !is LeaveRequestsState.Loading
                        binding.rvLeaveRequests.isVisible = requests.isNotEmpty()
                    }
                }

                launch {
                    viewModel.actionState.collectLatest { state ->
                        when (state) {
                            is LeaveActionState.Processing -> {
                                binding.loadingOverlay.isVisible = true
                            }
                            is LeaveActionState.Success -> {
                                binding.loadingOverlay.isVisible = false
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetActionState()
                            }
                            is LeaveActionState.Error -> {
                                binding.loadingOverlay.isVisible = false
                                Log.e(TAG, "LeaveActionState Error: ${state.message}")
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetActionState()
                            }
                            is LeaveActionState.Idle -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
