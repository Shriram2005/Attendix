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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.windhands.attendix.databinding.FragmentAttendanceManagementBinding
import com.windhands.attendix.ui.admin.adapter.AttendanceAdminAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@AndroidEntryPoint
class AttendanceManagementFragment : Fragment() {

    private var _binding: FragmentAttendanceManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceManagementViewModel by viewModels()

    private lateinit var attendanceAdapter: AttendanceAdminAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupDateSelector()
        setupFilterChips()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupDateSelector() {
        binding.cardDateSelector.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val currentDate = viewModel.selectedDate.value
        val selection = currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(selection)
            .build()

        datePicker.addOnPositiveButtonClickListener { millis ->
            val selectedDate = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            viewModel.setSelectedDate(selectedDate)
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener {
            viewModel.setFilter(AttendanceAdminFilter.ALL)
        }
        binding.chipPresent.setOnClickListener {
            viewModel.setFilter(AttendanceAdminFilter.PRESENT)
        }
        binding.chipAbsent.setOnClickListener {
            viewModel.setFilter(AttendanceAdminFilter.ABSENT)
        }
        binding.chipLate.setOnClickListener {
            viewModel.setFilter(AttendanceAdminFilter.LATE)
        }
        binding.chipOnLeave.setOnClickListener {
            viewModel.setFilter(AttendanceAdminFilter.ON_LEAVE)
        }
    }

    private fun setupRecyclerView() {
        attendanceAdapter = AttendanceAdminAdapter()
        binding.rvAttendance.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = attendanceAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedDateFormatted.collectLatest { dateText ->
                        binding.tvSelectedDate.text = dateText
                    }
                }

                launch {
                    viewModel.currentFilter.collectLatest { filter ->
                        binding.chipAll.isChecked = filter == AttendanceAdminFilter.ALL
                        binding.chipPresent.isChecked = filter == AttendanceAdminFilter.PRESENT
                        binding.chipAbsent.isChecked = filter == AttendanceAdminFilter.ABSENT
                        binding.chipLate.isChecked = filter == AttendanceAdminFilter.LATE
                        binding.chipOnLeave.isChecked = filter == AttendanceAdminFilter.ON_LEAVE
                    }
                }

                launch {
                    viewModel.attendanceState.collectLatest { state ->
                        binding.loadingOverlay.isVisible = state is AttendanceListState.Loading
                        
                        when (state) {
                            is AttendanceListState.Error -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.attendanceRecords.collectLatest { records ->
                        attendanceAdapter.submitList(records)
                        binding.layoutEmpty.isVisible = records.isEmpty() && 
                            viewModel.attendanceState.value !is AttendanceListState.Loading
                        binding.rvAttendance.isVisible = records.isNotEmpty()
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
