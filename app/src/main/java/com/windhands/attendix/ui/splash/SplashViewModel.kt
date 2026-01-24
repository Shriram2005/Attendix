package com.windhands.attendix.ui.splash

import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.local.SessionManager
import com.windhands.attendix.data.repository.AuthRepository
import com.windhands.attendix.ui.base.BaseViewModel
import com.windhands.attendix.ui.base.NavigationEvent
import com.windhands.attendix.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Splash Screen
 * Handles network check, session validation, and navigation decisions
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _splashState = MutableStateFlow<SplashState>(SplashState.Loading)
    val splashState: StateFlow<SplashState> = _splashState.asStateFlow()

    init {
        checkAppState()
    }

    private fun checkAppState() {
        viewModelScope.launch {
            // Minimum splash display time
            delay(1500)

            // Check if user is logged in
            checkLoginState()
        }
    }

    private suspend fun checkLoginState() {
        val isLoggedIn = sessionManager.isLoggedIn.first()
        
        if (isLoggedIn) {
            // Validate session by making a profile request
            val result = authRepository.getProfile()
            
            result.fold(
                onSuccess = {
                    // Token is valid, navigate based on role
                    navigateToAppropriateScreen()
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: ""
                    // Check for IP restriction
                    if (errorMessage.contains("IP_RESTRICTED", ignoreCase = true)) {
                        _splashState.value = SplashState.NetworkRestricted
                        navigate(NavigationEvent.NavigateToNetworkRestricted)
                    } else if (errorMessage.contains("401", ignoreCase = true) ||
                               errorMessage.contains("unauthorized", ignoreCase = true)) {
                        // Token expired/invalid, clear session and go to login
                        sessionManager.clearSession()
                        _splashState.value = SplashState.NotLoggedIn
                        navigate(NavigationEvent.NavigateToLogin)
                    } else {
                        // Other errors - still try to navigate if we have session
                        navigateToAppropriateScreen()
                    }
                }
            )
        } else {
            // Not logged in
            _splashState.value = SplashState.NotLoggedIn
            navigate(NavigationEvent.NavigateToLogin)
        }
    }

    private suspend fun navigateToAppropriateScreen() {
        val role = sessionManager.role.first()
        val isAdmin = role == Constants.ROLE_ADMIN || role == Constants.ROLE_HR
        
        if (isAdmin) {
            _splashState.value = SplashState.LoggedInAsAdmin
            navigate(NavigationEvent.NavigateToAdminDashboard)
        } else {
            _splashState.value = SplashState.LoggedInAsEmployee
            navigate(NavigationEvent.NavigateToEmployeeDashboard)
        }
    }
}

/**
 * Splash screen states
 */
sealed class SplashState {
    data object Loading : SplashState()
    data object NotLoggedIn : SplashState()
    data object LoggedInAsEmployee : SplashState()
    data object LoggedInAsAdmin : SplashState()
    data object NetworkRestricted : SplashState()
}
