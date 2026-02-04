package com.windhands.attendix.ui.auth

import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.local.SessionManager
import com.windhands.attendix.data.repository.AuthRepository
import com.windhands.attendix.ui.base.BaseViewModel
import com.windhands.attendix.ui.base.NavigationEvent
import com.windhands.attendix.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Login Screen
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    // Form fields
    private val _employeeId = MutableStateFlow("")
    val employeeId: StateFlow<String> = _employeeId.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _rememberMe = MutableStateFlow(true)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    // Validation errors
    private val _employeeIdError = MutableStateFlow<String?>(null)
    val employeeIdError: StateFlow<String?> = _employeeIdError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    fun setEmployeeId(value: String) {
        _employeeId.value = value
        _employeeIdError.value = null
    }

    fun setPassword(value: String) {
        _password.value = value
        _passwordError.value = null
    }

    fun setRememberMe(value: Boolean) {
        _rememberMe.value = value
    }

    fun login() {
        if (!validateInput()) return

        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            setLoading(true)

            val result = authRepository.login(
                employeeId = _employeeId.value.trim(),
                password = _password.value
            )

            result.fold(
                onSuccess = { response ->
                    // Save session
                    sessionManager.saveSession(
                        token = response.token,
                        userId = response.user.id,
                        employeeId = response.user.employeeId,
                        firstName = response.user.firstName,
                        lastName = response.user.lastName,
                        email = response.user.email,
                        phone = response.user.phone ?: "",
                        role = response.user.role,
                        department = response.user.department,
                        designation = response.user.designation ?: "",
                        status = response.user.status,
                        joiningDate = response.user.dateOfJoining ?: "",
                        rememberMe = _rememberMe.value
                    )
                    
                    _loginState.value = LoginState.Success

                    // Check role and navigate
                    val isAdmin = response.user.role == Constants.ROLE_ADMIN || 
                                  response.user.role == Constants.ROLE_HR
                    if (isAdmin) {
                        navigate(NavigationEvent.NavigateToAdminDashboard)
                    } else {
                        navigate(NavigationEvent.NavigateToEmployeeDashboard)
                    }
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "Login failed"
                    // Check for IP restriction
                    if (errorMessage.contains("IP_RESTRICTED", ignoreCase = true)) {
                        _loginState.value = LoginState.NetworkRestricted
                        navigate(NavigationEvent.NavigateToNetworkRestricted)
                    } else {
                        _loginState.value = LoginState.Error(errorMessage)
                        emitError(errorMessage)
                    }
                }
            )
            setLoading(false)
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        if (_employeeId.value.isBlank()) {
            _employeeIdError.value = "Please enter your Employee ID"
            isValid = false
        }

        if (_password.value.isBlank()) {
            _passwordError.value = "Please enter your password"
            isValid = false
        }

        return isValid
    }

    fun navigateToForgotPassword() {
        navigate(NavigationEvent.NavigateToForgotPassword)
    }
}

/**
 * Login screen states
 */
sealed class LoginState {
    data object Idle : LoginState()
    data object Loading : LoginState()
    data object Success : LoginState()
    data object NetworkRestricted : LoginState()
    data class Error(val message: String) : LoginState()
}
