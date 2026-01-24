package com.windhands.attendix.ui.employee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentApplyLeaveBinding
import com.windhands.attendix.ui.employee.adapter.LeaveBalanceMiniAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ApplyLeaveFragment : Fragment() {

    private var _binding: FragmentApplyLeaveBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ApplyLeaveViewModel by viewModels()

    private lateinit var leaveBalanceAdapter: LeaveBalanceMiniAdapter
    private val displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplyLeaveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupLeaveBalanceRecyclerView()
        setupLeaveTypeDropdown()
        setupDatePickers()
        setupReasonInput()
        setupSubmitButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupLeaveBalanceRecyclerView() {
        leaveBalanceAdapter = LeaveBalanceMiniAdapter()
        binding.rvLeaveBalance.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = leaveBalanceAdapter
        }
    }

    private fun setupLeaveTypeDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            viewModel.leaveTypes.map { it.displayName }
        )
        binding.actvLeaveType.setAdapter(adapter)
        
        binding.actvLeaveType.setOnItemClickListener { _, _, position, _ ->
            val selectedType = viewModel.leaveTypes[position]
            viewModel.setLeaveType(selectedType.type)
        }
    }

    private fun setupDatePickers() {
        binding.etStartDate.setOnClickListener {
            showStartDatePicker()
        }
        binding.tilStartDate.setEndIconOnClickListener {
            showStartDatePicker()
        }

        binding.etEndDate.setOnClickListener {
            showEndDatePicker()
        }
        binding.tilEndDate.setEndIconOnClickListener {
            showEndDatePicker()
        }
    }

    private fun showStartDatePicker() {
        val minDate = viewModel.getMinStartDate()
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(minDate.toEpochMillis()))
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.leave_start_date))
            .setCalendarConstraints(constraints)
            .setSelection(viewModel.startDate.value?.toEpochMillis() ?: minDate.toEpochMillis())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val date = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
            viewModel.setStartDate(date)
            binding.etStartDate.setText(date.format(displayFormatter))
        }

        picker.show(parentFragmentManager, "START_DATE_PICKER")
    }

    private fun showEndDatePicker() {
        val minDate = viewModel.getMinEndDate()
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(minDate.toEpochMillis()))
            .build()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.leave_end_date))
            .setCalendarConstraints(constraints)
            .setSelection(viewModel.endDate.value?.toEpochMillis() ?: minDate.toEpochMillis())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val date = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate()
            viewModel.setEndDate(date)
            binding.etEndDate.setText(date.format(displayFormatter))
        }

        picker.show(parentFragmentManager, "END_DATE_PICKER")
    }

    private fun LocalDate.toEpochMillis(): Long {
        return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun setupReasonInput() {
        binding.etReason.doAfterTextChanged { text ->
            viewModel.setReason(text?.toString() ?: "")
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            viewModel.validateAndSubmit()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.leaveBalanceState.collectLatest { state ->
                        when (state) {
                            is LeaveBalanceState.Success -> {
                                leaveBalanceAdapter.submitList(state.balances)
                            }
                            is LeaveBalanceState.Error -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            }
                            is LeaveBalanceState.Loading -> {}
                        }
                    }
                }

                launch {
                    viewModel.duration.collectLatest { duration ->
                        if (duration != null && duration > 0) {
                            binding.tvDuration.isVisible = true
                            binding.tvDuration.text = getString(R.string.apply_leave_duration, duration)
                        } else {
                            binding.tvDuration.isVisible = false
                        }
                    }
                }

                launch {
                    viewModel.validationError.collectLatest { error ->
                        error?.let {
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }

                launch {
                    viewModel.applyState.collectLatest { state ->
                        binding.loadingOverlay.isVisible = state is ApplyLeaveState.Submitting
                        binding.btnSubmit.isEnabled = state !is ApplyLeaveState.Submitting
                        
                        when (state) {
                            is ApplyLeaveState.Success -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetApplyState()
                                findNavController().navigateUp()
                            }
                            is ApplyLeaveState.Error -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetApplyState()
                            }
                            else -> {}
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
