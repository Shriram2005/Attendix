package com.windhands.attendix.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.local.SessionManager
import com.windhands.attendix.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            
            authRepository.getProfile()
                .onSuccess { response ->
                    val user = response.toUserDto()
                    if (user != null) {
                        _profileState.value = ProfileState.Success(
                            ProfileData(
                                fullName = "${user.firstName} ${user.lastName}",
                                email = user.email,
                                phone = user.phone ?: "Not provided",
                                employeeId = user.employeeId,
                                designation = user.designation ?: "Employee",
                                department = user.department ?: "Not assigned",
                                role = formatRole(user.role),
                                dateOfJoining = formatDate(user.dateOfJoining),
                                isActive = user.status == "active"
                            )
                        )
                    } else {
                        _profileState.value = ProfileState.Error("Failed to load profile")
                    }
                }
                .onFailure { exception ->
                    _profileState.value = ProfileState.Error(exception.message ?: "Network error")
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.LoggingOut
            
            authRepository.logout()
                .onSuccess { response ->
                    // Clear session regardless of API response
                    sessionManager.clearSession()
                    _logoutState.value = LogoutState.Success
                }
                .onFailure { exception ->
                    // Still clear session on failure
                    sessionManager.clearSession()
                    _logoutState.value = LogoutState.Success
                }
        }
    }

    private fun formatRole(role: String): String {
        return when (role.lowercase()) {
            "admin" -> "Administrator"
            "employee" -> "Employee"
            else -> role.replaceFirstChar { it.uppercase() }
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return "Not available"
        
        return try {
            val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
            val outputFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
            val date = LocalDate.parse(dateString.substring(0, 10))
            date.format(outputFormatter)
        } catch (e: Exception) {
            dateString
        }
    }
}

sealed class ProfileState {
    data object Loading : ProfileState()
    data class Success(val data: ProfileData) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class LogoutState {
    data object Idle : LogoutState()
    data object LoggingOut : LogoutState()
    data object Success : LogoutState()
}

data class ProfileData(
    val fullName: String,
    val email: String,
    val phone: String,
    val employeeId: String,
    val designation: String,
    val department: String,
    val role: String,
    val dateOfJoining: String,
    val isActive: Boolean
)
