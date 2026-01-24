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

        binding.bottomNav.setupWithNavController(navController)

        // Handle reselection to go back to root of that section
        binding.bottomNav.setOnItemReselectedListener { item ->
            navController.popBackStack(item.itemId, inclusive = false)
        }

        // Map menu items to destinations
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    navController.navigate(R.id.adminDashboardFragment)
                    true
                }
                R.id.nav_employees -> {
                    navController.navigate(R.id.employeeManagementFragment)
                    true
                }
                R.id.nav_attendance -> {
                    navController.navigate(R.id.attendanceManagementFragment)
                    true
                }
                R.id.nav_leave -> {
                    navController.navigate(R.id.leaveApprovalsFragment)
                    true
                }
                R.id.nav_profile -> {
                    navController.navigate(R.id.adminProfileFragment)
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
