package com.windhands.attendix.ui.common

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.windhands.attendix.BuildConfig
import com.windhands.attendix.R
import com.windhands.attendix.databinding.FragmentProfileBinding
import com.windhands.attendix.databinding.ItemProfileInfoBinding
import com.windhands.attendix.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupAppVersion()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.layoutChangePassword.setOnClickListener {
            // Navigate to change password - handle different nav graphs
            try {
                findNavController().navigate(R.id.action_profile_to_changePassword)
            } catch (e: Exception) {
                try {
                    findNavController().navigate(R.id.changePasswordFragment)
                } catch (e2: Exception) {
                    // Admin nav graph has different id
                    findNavController().navigate(R.id.adminChangePasswordFragment)
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun setupAppVersion() {
        binding.tvAppVersion.text = getString(R.string.app_version_format, BuildConfig.VERSION_NAME)
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_logout)
            .setMessage(R.string.profile_logout_confirm)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.profileState.collectLatest { state ->
                        binding.loadingOverlay.isVisible = state is ProfileState.Loading
                        
                        when (state) {
                            is ProfileState.Success -> {
                                bindProfileData(state.data)
                            }
                            is ProfileState.Error -> {
                                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_SHORT).show()
                            }
                            is ProfileState.Loading -> {}
                        }
                    }
                }

                launch {
                    viewModel.logoutState.collectLatest { state ->
                        binding.loadingOverlay.isVisible = state is LogoutState.LoggingOut
                        
                        if (state is LogoutState.Success) {
                            navigateToLogin()
                        }
                    }
                }
            }
        }
    }

    private fun bindProfileData(data: ProfileData) {
        binding.tvName.text = data.fullName
        binding.tvDesignation.text = data.designation
        binding.tvEmployeeId.text = getString(R.string.profile_employee_id_format, data.employeeId)
        
        // Status indicator
        binding.viewStatusIndicator.setBackgroundResource(
            if (data.isActive) R.drawable.bg_status_indicator else R.drawable.bg_status_indicator_inactive
        )

        // Personal Information
        bindInfoItem(binding.itemEmail, R.drawable.ic_email, getString(R.string.profile_email), data.email)
        bindInfoItem(binding.itemPhone, R.drawable.ic_phone, getString(R.string.profile_phone), data.phone)

        // Job Details
        bindInfoItem(binding.itemDepartment, R.drawable.ic_business, getString(R.string.profile_department), data.department)
        bindInfoItem(binding.itemRole, R.drawable.ic_work, getString(R.string.profile_role), data.role)
        bindInfoItem(binding.itemJoiningDate, R.drawable.ic_calendar, getString(R.string.profile_joining_date), data.dateOfJoining)
    }

    private fun bindInfoItem(itemBinding: ItemProfileInfoBinding, iconRes: Int, label: String, value: String) {
        itemBinding.ivIcon.setImageResource(iconRes)
        itemBinding.tvLabel.text = label
        itemBinding.tvValue.text = value
    }

    private fun navigateToLogin() {
        // Restart activity to go to login
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
