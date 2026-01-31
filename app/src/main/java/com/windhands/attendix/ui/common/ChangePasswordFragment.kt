package com.windhands.attendix.ui.common

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentChangePasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    
    companion object {
        private const val TAG = "ChangePasswordFragment"
    }

    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupInputListeners()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupInputListeners() {
        binding.etCurrentPassword.doAfterTextChanged { text ->
            viewModel.setCurrentPassword(text?.toString() ?: "")
            binding.tilCurrentPassword.error = null
        }

        binding.etNewPassword.doAfterTextChanged { text ->
            viewModel.setNewPassword(text?.toString() ?: "")
            binding.tilNewPassword.error = null
        }

        binding.etConfirmPassword.doAfterTextChanged { text ->
            viewModel.setConfirmPassword(text?.toString() ?: "")
            binding.tilConfirmPassword.error = null
        }
    }

    private fun setupClickListeners() {
        binding.btnChangePassword.setOnClickListener {
            viewModel.validateAndSubmit()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.validationError.collectLatest { error ->
                        clearAllErrors()
                        error?.let { showValidationError(it) }
                    }
                }

                launch {
                    viewModel.changePasswordState.collectLatest { state ->
                        binding.loadingOverlay.isVisible = state is ChangePasswordState.Loading
                        binding.btnChangePassword.isEnabled = state !is ChangePasswordState.Loading
                        
                        when (state) {
                            is ChangePasswordState.Success -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetState()
                                findNavController().navigateUp()
                            }
                            is ChangePasswordState.Error -> {
                                Log.e(TAG, "ChangePasswordState Error: ${state.message}")
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                                viewModel.resetState()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun clearAllErrors() {
        binding.tilCurrentPassword.error = null
        binding.tilNewPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    private fun showValidationError(error: ValidationError) {
        when (error) {
            is ValidationError.CurrentPasswordEmpty -> {
                binding.tilCurrentPassword.error = getString(R.string.error_current_password_empty)
            }
            is ValidationError.NewPasswordEmpty -> {
                binding.tilNewPassword.error = getString(R.string.error_new_password_empty)
            }
            is ValidationError.NewPasswordTooShort -> {
                binding.tilNewPassword.error = getString(R.string.error_password_short)
            }
            is ValidationError.NewPasswordWeak -> {
                binding.tilNewPassword.error = getString(R.string.error_password_weak)
            }
            is ValidationError.ConfirmPasswordEmpty -> {
                binding.tilConfirmPassword.error = getString(R.string.error_confirm_password_empty)
            }
            is ValidationError.PasswordsDoNotMatch -> {
                binding.tilConfirmPassword.error = getString(R.string.error_password_mismatch)
            }
            is ValidationError.SameAsCurrentPassword -> {
                binding.tilNewPassword.error = getString(R.string.error_same_as_current_password)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
