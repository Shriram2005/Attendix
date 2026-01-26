package com.windhands.attendix.ui.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentAdminContainerBinding
import com.windhands.attendix.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Container Fragment for Admin section with Bottom Navigation
 */
@AndroidEntryPoint
class AdminContainerFragment : BaseFragment<FragmentAdminContainerBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAdminContainerBinding {
        return FragmentAdminContainerBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_fragment_admin) as NavHostFragment
        val navController = navHostFragment.navController

        // Handle reselection to go back to root of that section
        binding.bottomNav.setOnItemReselectedListener { item ->
            navController.popBackStack(item.itemId, inclusive = false)
        }

        // Map menu items to destinations - handle navigation manually to avoid conflicts
        binding.bottomNav.setOnItemSelectedListener { item ->
            val currentDestination = navController.currentDestination?.id
            val targetDestination = when (item.itemId) {
                R.id.nav_dashboard -> R.id.adminDashboardFragment
                R.id.nav_employees -> R.id.employeeManagementFragment
                R.id.nav_attendance -> R.id.attendanceManagementFragment
                R.id.nav_leave -> R.id.leaveApprovalsFragment
                else -> null
            }

            // Only navigate if we're not already at the destination
            if (targetDestination != null && currentDestination != targetDestination) {
                try {
                    navController.navigate(targetDestination)
                } catch (e: Exception) {
                    // Ignore navigation errors if destination is already in stack
                }
            }
            true
        }

        // Update bottom navigation selection on back navigation
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.adminDashboardFragment -> binding.bottomNav.selectedItemId = R.id.nav_dashboard
                R.id.employeeManagementFragment -> binding.bottomNav.selectedItemId = R.id.nav_employees
                R.id.attendanceManagementFragment -> binding.bottomNav.selectedItemId = R.id.nav_attendance
                R.id.leaveApprovalsFragment -> binding.bottomNav.selectedItemId = R.id.nav_leave
            }
        }
    }

    override fun setupObservers() {
        // Container doesn't need observers
    }
}
