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
import com.windhands.attendix.databinding.FragmentForgotPasswordBinding
import com.windhands.attendix.ui.base.BaseFragment
import com.windhands.attendix.util.gone
import com.windhands.attendix.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Forgot Password Screen Fragment
 */
@AndroidEntryPoint
class ForgotPasswordFragment : BaseFragment<FragmentForgotPasswordBinding>() {

    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentForgotPasswordBinding {
        return FragmentForgotPasswordBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        setupInputListeners()
        setupClickListeners()
    }

    private fun setupInputListeners() {
        with(binding) {
            // Email input
            etEmail.doAfterTextChanged { text ->
                viewModel.setEmail(text?.toString() ?: "")
            }

            // Email done action
            etEmail.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    viewModel.submit()
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            // Back button
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            // Submit button
            btnSubmit.setOnClickListener {
                viewModel.submit()
            }

            // Back to login link
            btnBackToLogin.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Forgot password state
                launch {
                    viewModel.forgotPasswordState.collect { state ->
                        when (state) {
                            is ForgotPasswordState.Loading -> showLoading(true)
                            is ForgotPasswordState.Success -> {
                                showLoading(false)
                            }
                            is ForgotPasswordState.Error -> showLoading(false)
                            is ForgotPasswordState.Idle -> showLoading(false)
                        }
                    }
                }

                // Email error
                launch {
                    viewModel.emailError.collect { error ->
                        binding.tilEmail.error = error
                    }
                }

                // Error events
                launch {
                    viewModel.errorEvent.collect { message ->
                        showError(message)
                    }
                }

                // Success events
                launch {
                    viewModel.successEvent.collect { message ->
                        showSuccess(message)
                        findNavController().navigateUp()
                    }
                }
            }
        }
    }

    override fun showLoading(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                loadingOverlay.visible()
                btnSubmit.isEnabled = false
            } else {
                loadingOverlay.gone()
                btnSubmit.isEnabled = true
            }
        }
    }
}
