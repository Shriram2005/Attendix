package com.windhands.attendix.ui.auth

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentLoginBinding
import com.windhands.attendix.ui.base.BaseFragment
import com.windhands.attendix.ui.base.NavigationEvent
import com.windhands.attendix.util.gone
import com.windhands.attendix.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Login Screen Fragment
 */
@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        setupInputListeners()
        setupClickListeners()
    }

    private fun setupInputListeners() {
        with(binding) {
            // Employee ID input
            etEmployeeId.doAfterTextChanged { text ->
                viewModel.setEmployeeId(text?.toString() ?: "")
            }

            // Password input
            etPassword.doAfterTextChanged { text ->
                viewModel.setPassword(text?.toString() ?: "")
            }

            // Password done action
            etPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    viewModel.login()
                    true
                } else {
                    false
                }
            }

            // Remember me checkbox
            cbRememberMe.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setRememberMe(isChecked)
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            // Login button
            btnLogin.setOnClickListener {
                viewModel.login()
            }

            // Forgot password
            btnForgotPassword.setOnClickListener {
                viewModel.navigateToForgotPassword()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Login state
                launch {
                    viewModel.loginState.collect { state ->
                        when (state) {
                            is LoginState.Loading -> showLoading(true)
                            is LoginState.Success -> showLoading(false)
                            is LoginState.Error -> {
                                showLoading(false)
                            }
                            is LoginState.Idle -> showLoading(false)
                            is LoginState.NetworkRestricted -> showLoading(false)
                        }
                    }
                }

                // Employee ID error
                launch {
                    viewModel.employeeIdError.collect { error ->
                        binding.tilEmployeeId.error = error
                    }
                }

                // Password error
                launch {
                    viewModel.passwordError.collect { error ->
                        binding.tilPassword.error = error
                    }
                }

                // Error events
                launch {
                    viewModel.errorEvent.collect { message ->
                        showError(message)
                    }
                }

                // Navigation events
                launch {
                    viewModel.navigationEvent.collect { event ->
                        handleNavigation(event)
                    }
                }
            }
        }
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                loadingOverlay.visible()
                btnLogin.isEnabled = false
            } else {
                loadingOverlay.gone()
                btnLogin.isEnabled = true
            }
        }
    }

    private fun handleNavigation(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateToEmployeeDashboard -> {
                findNavController().navigate(R.id.action_login_to_employee_dashboard)
            }
            is NavigationEvent.NavigateToAdminDashboard -> {
                findNavController().navigate(R.id.action_login_to_admin_dashboard)
            }
            is NavigationEvent.NavigateToNetworkRestricted -> {
                findNavController().navigate(R.id.action_login_to_restricted)
            }
            is NavigationEvent.NavigateToForgotPassword -> {
                findNavController().navigate(R.id.action_login_to_forgotPassword)
            }
            else -> {
                // Not handled here
            }
        }
    }
}
