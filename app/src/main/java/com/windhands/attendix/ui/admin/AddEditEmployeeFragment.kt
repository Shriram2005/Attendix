package com.windhands.attendix.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentAddEditEmployeeBinding
import com.windhands.attendix.databinding.DialogPasswordDisplayBinding
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
                                if (state.temporaryPassword != null) {
                                    // Show password dialog for new employee
                                    showPasswordDialog(state.temporaryPassword)
                                } else {
                                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                    viewModel.resetSubmitState()
                                    findNavController().navigateUp()
                                }
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

    private fun showPasswordDialog(password: String) {
        val dialogBinding = DialogPasswordDisplayBinding.inflate(layoutInflater)
        
        // Set the password text
        dialogBinding.etPassword.setText(password)
        
        // Setup visibility toggle
        dialogBinding.btnToggleVisibility.setOnClickListener {
            val isPasswordVisible = dialogBinding.etPassword.inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            if (isPasswordVisible) {
                dialogBinding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                dialogBinding.btnToggleVisibility.setImageResource(R.drawable.ic_visibility)
            } else {
                dialogBinding.etPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                dialogBinding.btnToggleVisibility.setImageResource(R.drawable.ic_visibility_off)
            }
            // Move cursor to end
            dialogBinding.etPassword.setSelection(dialogBinding.etPassword.text?.length ?: 0)
        }
        
        // Setup copy button
        dialogBinding.btnCopy.setOnClickListener {
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Password", password)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Password copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.employee_created_title)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.done) { dialog, _ ->
                dialog.dismiss()
                viewModel.resetSubmitState()
                findNavController().navigateUp()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
