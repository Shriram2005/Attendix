package com.windhands.attendix.ui.employee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentAttendanceHistoryBinding
import com.windhands.attendix.ui.base.BaseFragment
import com.windhands.attendix.ui.employee.adapter.AttendanceRecordAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AttendanceHistoryFragment : BaseFragment<FragmentAttendanceHistoryBinding>() {

    private val viewModel: AttendanceHistoryViewModel by viewModels()
    private lateinit var attendanceAdapter: AttendanceRecordAdapter

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAttendanceHistoryBinding {
        return FragmentAttendanceHistoryBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        setupRecyclerView()
        setupFilterChips()
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceRecordAdapter()
        binding.rvAttendance.apply {
            adapter = attendanceAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener {
            viewModel.setFilter(AttendanceFilter.ALL)
        }
        binding.chipPresent.setOnClickListener {
            viewModel.setFilter(AttendanceFilter.PRESENT)
        }
        binding.chipAbsent.setOnClickListener {
            viewModel.setFilter(AttendanceFilter.ABSENT)
        }
        binding.chipLate.setOnClickListener {
            viewModel.setFilter(AttendanceFilter.LATE)
        }
        binding.chipLeave.setOnClickListener {
            viewModel.setFilter(AttendanceFilter.LEAVE)
        }
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
                    viewModel.currentFilter.collect { filter ->
                        binding.chipAll.isChecked = filter == AttendanceFilter.ALL
                        binding.chipPresent.isChecked = filter == AttendanceFilter.PRESENT
                        binding.chipAbsent.isChecked = filter == AttendanceFilter.ABSENT
                        binding.chipLate.isChecked = filter == AttendanceFilter.LATE
                        binding.chipLeave.isChecked = filter == AttendanceFilter.LEAVE
                    }
                }

                launch {
                    viewModel.attendanceRecords.collect { records ->
                        attendanceAdapter.submitList(records)
                        binding.layoutEmpty.isVisible = records.isEmpty() && 
                            viewModel.historyState.value !is AttendanceHistoryState.Loading
                        binding.rvAttendance.isVisible = records.isNotEmpty()
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
            }
        }
    }
}
