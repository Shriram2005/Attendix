package com.windhands.attendix.ui.restricted

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.windhands.attendix.databinding.FragmentNetworkRestrictedBinding
import com.windhands.attendix.ui.base.BaseFragment
import com.windhands.attendix.ui.base.NavigationEvent
import com.windhands.attendix.util.gone
import com.windhands.attendix.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Network Restricted Screen Fragment
 * Shown when the user's network is not allowed to access the app
 */
@AndroidEntryPoint
class NetworkRestrictedFragment : BaseFragment<FragmentNetworkRestrictedBinding>() {

    private val viewModel: NetworkRestrictedViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNetworkRestrictedBinding {
        return FragmentNetworkRestrictedBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        setupClickListeners()
    }

    private fun setupClickListeners() {
        with(binding) {
            // Retry button
            btnRetry.setOnClickListener {
                viewModel.retry()
            }

            // Contact admin button - opens email
            btnContact.setOnClickListener {
                openEmailClient()
            }
        }
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Loading state
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        showLoading(isLoading)
                    }
                }

                // Error events
                launch {
                    viewModel.errorEvent.collect { message ->
                        showError(message)
                    }
                }

                // Success events (network now accessible)
                launch {
                    viewModel.successEvent.collect { message ->
                        showSuccess(message)
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
                progressLoading.visible()
                btnRetry.isEnabled = false
            } else {
                progressLoading.gone()
                btnRetry.isEnabled = true
            }
        }
    }

    private fun openEmailClient() {
        try {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:admin@company.com")
                putExtra(Intent.EXTRA_SUBJECT, "Network Access Request - Attendix App")
            }
            startActivity(Intent.createChooser(emailIntent, "Contact Administrator"))
        } catch (e: Exception) {
            showError("No email app found")
        }
    }

    private fun handleNavigation(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateBack -> {
                findNavController().navigateUp()
            }
            else -> {
                // Not handled here
            }
        }
    }
}
