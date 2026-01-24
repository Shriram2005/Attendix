package com.windhands.attendix.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
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
import com.windhands.attendix.databinding.FragmentEmployeeManagementBinding
import com.windhands.attendix.ui.admin.adapter.EmployeeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmployeeManagementFragment : Fragment() {

    private var _binding: FragmentEmployeeManagementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EmployeeManagementViewModel by viewModels()

    private lateinit var employeeAdapter: EmployeeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeeManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupSearch()
        setupFilterChips()
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text?.toString() ?: "")
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.loadEmployees()
                true
            } else false
        }
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener {
            viewModel.setStatusFilter(EmployeeStatusFilter.ALL)
        }
        binding.chipActive.setOnClickListener {
            viewModel.setStatusFilter(EmployeeStatusFilter.ACTIVE)
        }
        binding.chipInactive.setOnClickListener {
            viewModel.setStatusFilter(EmployeeStatusFilter.INACTIVE)
        }
    }

    private fun setupRecyclerView() {
        employeeAdapter = EmployeeAdapter(
            onItemClick = { employee ->
                // Navigate to employee details/edit
                navigateToEditEmployee(employee.id)
            },
            onEditClick = { employee ->
                navigateToEditEmployee(employee.id)
            },
            onViewAttendanceClick = { employee ->
                // Navigate to employee attendance
                navigateToEmployeeAttendance(employee.id)
            },
            onDeactivateClick = { employee ->
                showDeactivateConfirmation(employee)
            },
            onActivateClick = { employee ->
                showActivateConfirmation(employee)
            },
            onDeleteClick = { employee ->
                showDeleteConfirmation(employee)
            }
        )

        binding.rvEmployees.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = employeeAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddEmployee.setOnClickListener {
            navigateToAddEmployee()
        }
    }

    private fun navigateToAddEmployee() {
        try {
            findNavController().navigate(R.id.action_employeeManagement_to_addEmployee)
        } catch (e: Exception) {
            // Handle navigation error
        }
    }

    private fun navigateToEditEmployee(employeeId: Int) {
        try {
            val bundle = android.os.Bundle().apply {
                putInt("employeeId", employeeId)
            }
            findNavController().navigate(R.id.action_employeeManagement_to_editEmployee, bundle)
        } catch (e: Exception) {
            // Handle navigation error
        }
    }

    private fun navigateToEmployeeAttendance(employeeId: Int) {
        // Navigate to employee attendance screen
        // This would require a specific navigation action
    }

    private fun showDeactivateConfirmation(employee: EmployeeItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.employee_deactivate)
            .setMessage(getString(R.string.employee_deactivate_confirm_format, employee.fullName))
            .setPositiveButton(R.string.employee_deactivate) { _, _ ->
                viewModel.deactivateEmployee(employee.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showActivateConfirmation(employee: EmployeeItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.employee_activate)
            .setMessage(getString(R.string.employee_activate_confirm_format, employee.fullName))
            .setPositiveButton(R.string.employee_activate) { _, _ ->
                viewModel.activateEmployee(employee.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteConfirmation(employee: EmployeeItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(getString(R.string.employee_delete_confirm_format, employee.fullName))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteEmployee(employee.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.employeesState.collectLatest { state ->
                        binding.loadingOverlay.isVisible = state is EmployeesState.Loading
                        
                        when (state) {
                            is EmployeesState.Error -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.employees.collectLatest { employees ->
                        employeeAdapter.submitList(employees)
                        binding.layoutEmpty.isVisible = employees.isEmpty() && 
                            viewModel.employeesState.value !is EmployeesState.Loading
                        binding.rvEmployees.isVisible = employees.isNotEmpty()
                    }
                }

                launch {
                    viewModel.statusFilter.collectLatest { filter ->
                        binding.chipAll.isChecked = filter == EmployeeStatusFilter.ALL
                        binding.chipActive.isChecked = filter == EmployeeStatusFilter.ACTIVE
                        binding.chipInactive.isChecked = filter == EmployeeStatusFilter.INACTIVE
                    }
                }

                launch {
                    viewModel.actionState.collectLatest { state ->
                        when (state) {
                            is EmployeeActionState.Processing -> {
                                binding.loadingOverlay.isVisible = true
                            }
                            is EmployeeActionState.Success -> {
                                binding.loadingOverlay.isVisible = false
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetActionState()
                            }
                            is EmployeeActionState.Error -> {
                                binding.loadingOverlay.isVisible = false
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetActionState()
                            }
                            is EmployeeActionState.Idle -> {}
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
