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

        binding.bottomNav.setupWithNavController(navController)

        // Handle reselection to go back to root of that section
        binding.bottomNav.setOnItemReselectedListener { item ->
            navController.popBackStack(item.itemId, inclusive = false)
        }

        // Map menu items to destinations
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    navController.navigate(R.id.employeeDashboardFragment)
                    true
                }
                R.id.nav_attendance -> {
                    navController.navigate(R.id.attendanceHistoryFragment)
                    true
                }
                R.id.nav_leave -> {
                    navController.navigate(R.id.leaveManagementFragment)
                    true
                }
                R.id.nav_profile -> {
                    navController.navigate(R.id.profileFragment)
                    true
                }
                else -> false
            }
        }
    }

    override fun setupObservers() {
        // Container doesn't need observers
    }
}
