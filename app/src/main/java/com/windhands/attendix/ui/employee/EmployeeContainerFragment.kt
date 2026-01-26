package com.windhands.attendix.ui.employee

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentEmployeeContainerBinding
import com.windhands.attendix.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Container Fragment for Employee section with Bottom Navigation
 */
@AndroidEntryPoint
class EmployeeContainerFragment : BaseFragment<FragmentEmployeeContainerBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentEmployeeContainerBinding {
        return FragmentEmployeeContainerBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_fragment_employee) as NavHostFragment
        val navController = navHostFragment.navController

        // Handle reselection to go back to root of that section
        binding.bottomNav.setOnItemReselectedListener { item ->
            navController.popBackStack(item.itemId, inclusive = false)
        }

        // Map menu items to destinations - handle navigation manually to avoid conflicts
        binding.bottomNav.setOnItemSelectedListener { item ->
            val currentDestination = navController.currentDestination?.id
            val targetDestination = when (item.itemId) {
                R.id.nav_dashboard -> R.id.employeeDashboardFragment
                R.id.nav_attendance -> R.id.attendanceHistoryFragment
                R.id.nav_leave -> R.id.leaveManagementFragment
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
                R.id.employeeDashboardFragment -> binding.bottomNav.selectedItemId = R.id.nav_dashboard
                R.id.attendanceHistoryFragment -> binding.bottomNav.selectedItemId = R.id.nav_attendance
                R.id.leaveManagementFragment -> binding.bottomNav.selectedItemId = R.id.nav_leave
            }
        }
    }

    override fun setupObservers() {
        // Container doesn't need observers
    }
}
