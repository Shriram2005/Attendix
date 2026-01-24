package com.windhands.attendix.ui.admin

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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentAddEditEmployeeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class AddEditEmployeeFragment : Fragment() {

    private var _binding: FragmentAddEditEmployeeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditEmployeeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditEmployeeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupDropdowns()
        setupDatePicker()
        setupTextListeners()
        setupSubmitButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.title = if (viewModel.isEditMode) {
            getString(R.string.employee_edit)
        } else {
            getString(R.string.employee_add)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Hide info card in edit mode
        binding.cardInfo.isVisible = !viewModel.isEditMode
        
        // Disable email in edit mode
        binding.tilEmail.isEnabled = !viewModel.isEditMode
        
        // Update button text
        binding.btnSubmit.text = if (viewModel.isEditMode) {
            getString(R.string.save)
        } else {
            getString(R.string.employee_add)
        }
    }

    private fun setupDropdowns() {
        // Department dropdown
        val departmentAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            viewModel.departments
        )
        binding.actvDepartment.setAdapter(departmentAdapter)
        binding.actvDepartment.setOnItemClickListener { _, _, position, _ ->
            viewModel.updateDepartment(viewModel.departments[position])
        }

        // Role dropdown
        val roleNames = viewModel.roles.map { role ->
            role.replaceFirstChar { it.uppercase() }
        }
        val roleAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            roleNames
        )
        binding.actvRole.setAdapter(roleAdapter)
        binding.actvRole.setOnItemClickListener { _, _, position, _ ->
            viewModel.updateRole(viewModel.roles[position])
        }
    }

    private fun setupDatePicker() {
        binding.etJoiningDate.setOnClickListener {
            showDatePicker()
        }
        binding.tilJoiningDate.setEndIconOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.employee_date_of_joining))
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(selection))
            viewModel.updateJoiningDate(formattedDate)
            
            val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.etJoiningDate.setText(displayFormat.format(Date(selection)))
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun setupTextListeners() {
        binding.etFirstName.doAfterTextChanged { 
            viewModel.updateFirstName(it?.toString() ?: "") 
        }
        binding.etLastName.doAfterTextChanged { 
            viewModel.updateLastName(it?.toString() ?: "") 
        }
        binding.etEmail.doAfterTextChanged { 
            viewModel.updateEmail(it?.toString() ?: "") 
        }
        binding.etPhone.doAfterTextChanged { 
            viewModel.updatePhone(it?.toString() ?: "") 
        }
        binding.etDesignation.doAfterTextChanged { 
            viewModel.updateDesignation(it?.toString() ?: "") 
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            viewModel.submit()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loadState.collectLatest { state ->
                        binding.loadingOverlay.isVisible = state is LoadState.Loading
                        
                        when (state) {
                            is LoadState.Error -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.formState.collectLatest { form ->
                        // Only set text if different to avoid cursor issues
                        if (binding.etFirstName.text.toString() != form.firstName) {
                            binding.etFirstName.setText(form.firstName)
                        }
                        if (binding.etLastName.text.toString() != form.lastName) {
                            binding.etLastName.setText(form.lastName)
                        }
                        if (binding.etEmail.text.toString() != form.email) {
                            binding.etEmail.setText(form.email)
                        }
                        if (binding.etPhone.text.toString() != form.phone) {
                            binding.etPhone.setText(form.phone)
                        }
                        if (binding.actvDepartment.text.toString() != form.department) {
                            binding.actvDepartment.setText(form.department, false)
                        }
                        if (binding.etDesignation.text.toString() != form.designation) {
                            binding.etDesignation.setText(form.designation)
                        }
                        if (binding.actvRole.text.toString().lowercase() != form.role) {
                            binding.actvRole.setText(form.role.replaceFirstChar { it.uppercase() }, false)
                        }

                        // Errors
                        binding.tilFirstName.error = form.firstNameError
                        binding.tilLastName.error = form.lastNameError
                        binding.tilEmail.error = form.emailError
                        binding.tilRole.error = form.roleError
                    }
                }

                launch {
                    viewModel.submitState.collectLatest { state ->
                        binding.loadingOverlay.isVisible = state is SubmitState.Loading
                        binding.btnSubmit.isEnabled = state !is SubmitState.Loading
                        
                        when (state) {
                            is SubmitState.Success -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetSubmitState()
                                findNavController().navigateUp()
                            }
                            is SubmitState.Error -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetSubmitState()
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
