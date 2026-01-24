package com.windhands.attendix.ui.splash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentSplashBinding
import com.windhands.attendix.ui.base.BaseFragment
import com.windhands.attendix.ui.base.NavigationEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Splash Screen Fragment
 * Shows loading state while checking network access and login status
 */
@AndroidEntryPoint
class SplashFragment : BaseFragment<FragmentSplashBinding>() {

    private val viewModel: SplashViewModel by viewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSplashBinding {
        return FragmentSplashBinding.inflate(inflater, container, false)
    }

    override fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe navigation events
                launch {
                    viewModel.navigationEvent.collect { event ->
                        handleNavigation(event)
                    }
                }

                // Observe splash state for UI updates
                launch {
                    viewModel.splashState.collect { state ->
                        when (state) {
                            is SplashState.Loading -> {
                                binding.progressLoading.visibility = View.VISIBLE
                                binding.tvStatus.text = getString(R.string.splash_checking)
                            }
                            else -> {
                                // Navigation will handle the rest
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleNavigation(event: NavigationEvent) {
        when (event) {
            is NavigationEvent.NavigateToLogin -> {
                findNavController().navigate(R.id.action_splash_to_login)
            }
            is NavigationEvent.NavigateToEmployeeDashboard -> {
                findNavController().navigate(R.id.action_splash_to_employee_dashboard)
            }
            is NavigationEvent.NavigateToAdminDashboard -> {
                findNavController().navigate(R.id.action_splash_to_admin_dashboard)
            }
            is NavigationEvent.NavigateToNetworkRestricted -> {
                findNavController().navigate(R.id.action_splash_to_restricted)
            }
            else -> {
                // Not handled here
            }
        }
    }
}
