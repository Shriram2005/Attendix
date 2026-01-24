package com.windhands.attendix.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState.asStateFlow()

    private val _currentPassword = MutableStateFlow("")
    val currentPassword: StateFlow<String> = _currentPassword.asStateFlow()

    private val _newPassword = MutableStateFlow("")
    val newPassword: StateFlow<String> = _newPassword.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    private val _validationError = MutableStateFlow<ValidationError?>(null)
    val validationError: StateFlow<ValidationError?> = _validationError.asStateFlow()

    fun setCurrentPassword(password: String) {
        _currentPassword.value = password
        clearValidationError()
    }

    fun setNewPassword(password: String) {
        _newPassword.value = password
        clearValidationError()
    }

    fun setConfirmPassword(password: String) {
        _confirmPassword.value = password
        clearValidationError()
    }

    fun validateAndSubmit() {
        val currentPwd = _currentPassword.value.trim()
        val newPwd = _newPassword.value.trim()
        val confirmPwd = _confirmPassword.value.trim()

        // Validation
        when {
            currentPwd.isBlank() -> {
                _validationError.value = ValidationError.CurrentPasswordEmpty
                return
            }
            newPwd.isBlank() -> {
                _validationError.value = ValidationError.NewPasswordEmpty
                return
            }
            newPwd.length < 8 -> {
                _validationError.value = ValidationError.NewPasswordTooShort
                return
            }
            !isPasswordStrong(newPwd) -> {
                _validationError.value = ValidationError.NewPasswordWeak
                return
            }
            confirmPwd.isBlank() -> {
                _validationError.value = ValidationError.ConfirmPasswordEmpty
                return
            }
            newPwd != confirmPwd -> {
                _validationError.value = ValidationError.PasswordsDoNotMatch
                return
            }
            currentPwd == newPwd -> {
                _validationError.value = ValidationError.SameAsCurrentPassword
                return
            }
        }

        changePassword(currentPwd, newPwd)
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState.Loading
            
            authRepository.changePassword(currentPassword, newPassword)
                .onSuccess { response ->
                    if (response.success) {
                        _changePasswordState.value = ChangePasswordState.Success(
                            response.message ?: "Password changed successfully"
                        )
                    } else {
                        _changePasswordState.value = ChangePasswordState.Error(
                            response.message ?: "Failed to change password"
                        )
                    }
                }
                .onFailure { exception ->
                    _changePasswordState.value = ChangePasswordState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        // At least one uppercase, one lowercase, one digit
        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        
        return hasUppercase && hasLowercase && hasDigit
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    fun resetState() {
        _changePasswordState.value = ChangePasswordState.Idle
    }
}

sealed class ChangePasswordState {
    data object Idle : ChangePasswordState()
    data object Loading : ChangePasswordState()
    data class Success(val message: String) : ChangePasswordState()
    data class Error(val message: String) : ChangePasswordState()
}

sealed class ValidationError {
    data object CurrentPasswordEmpty : ValidationError()
    data object NewPasswordEmpty : ValidationError()
    data object NewPasswordTooShort : ValidationError()
    data object NewPasswordWeak : ValidationError()
    data object ConfirmPasswordEmpty : ValidationError()
    data object PasswordsDoNotMatch : ValidationError()
    data object SameAsCurrentPassword : ValidationError()
}
