package com.windhands.attendix.ui.auth

import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.repository.AuthRepository
import com.windhands.attendix.ui.base.BaseViewModel
import com.windhands.attendix.ui.base.NavigationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Forgot Password Screen
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    fun setEmail(value: String) {
        _email.value = value
        _emailError.value = null
    }

    fun submit() {
        if (!validateInput()) return

        viewModelScope.launch {
            _forgotPasswordState.value = ForgotPasswordState.Loading
            setLoading(true)

            val result = authRepository.forgotPassword(_email.value.trim())
            
            result.fold(
                onSuccess = {
                    _forgotPasswordState.value = ForgotPasswordState.Success
                    emitSuccess("Password reset link sent to your email")
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "Request failed"
                    _forgotPasswordState.value = ForgotPasswordState.Error(errorMessage)
                    emitError(errorMessage)
                }
            )
            setLoading(false)
        }
    }

    private fun validateInput(): Boolean {
        val emailValue = _email.value.trim()

        if (emailValue.isBlank()) {
            _emailError.value = "Please enter your email address"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            _emailError.value = "Please enter a valid email address"
            return false
        }

        return true
    }

    fun navigateBack() {
        navigate(NavigationEvent.NavigateBack)
    }
}

/**
 * Forgot Password screen states
 */
sealed class ForgotPasswordState {
    data object Idle : ForgotPasswordState()
    data object Loading : ForgotPasswordState()
    data object Success : ForgotPasswordState()
    data class Error(val message: String) : ForgotPasswordState()
}
