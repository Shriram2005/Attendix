package com.windhands.attendix.ui.base

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windhands.attendix.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class for all ViewModels
 * Provides common functionality for state management and events
 */
abstract class BaseViewModel : ViewModel() {

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error messages as events (one-time)
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    // Success messages as events (one-time)
    private val _successEvent = MutableSharedFlow<String>()
    val successEvent: SharedFlow<String> = _successEvent.asSharedFlow()

    // Generic navigation event
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    /**
     * Set loading state
     */
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    /**
     * Emit error message
     */
    protected fun emitError(message: String) {
        viewModelScope.launch {
            _errorEvent.emit(message)
        }
    }

    /**
     * Emit success message
     */
    protected fun emitSuccess(message: String) {
        viewModelScope.launch {
            _successEvent.emit(message)
        }
    }

    /**
     * Emit navigation event
     */
    protected fun navigate(event: NavigationEvent) {
        viewModelScope.launch {
            _navigationEvent.emit(event)
        }
    }

    /**
     * Execute a suspend function with loading state handling
     */
    protected fun <T> launchWithLoading(
        block: suspend () -> Resource<T>,
        onSuccess: (T) -> Unit = {},
        onError: (String) -> Unit = { emitError(it) }
    ) {
        viewModelScope.launch {
            setLoading(true)
            when (val result = block()) {
                is Resource.Success -> {
                    result.data?.let { onSuccess(it) }
                }
                is Resource.Error -> {
                    onError(result.message ?: "Unknown error occurred")
                }
                is Resource.Loading -> {
                    // Already handled by setLoading
                }
            }
            setLoading(false)
        }
    }

    /**
     * Execute a suspend function without loading state
     */
    protected fun <T> launchSilent(
        block: suspend () -> Resource<T>,
        onSuccess: (T) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            when (val result = block()) {
                is Resource.Success -> {
                    result.data?.let { onSuccess(it) }
                }
                is Resource.Error -> {
                    onError(result.message ?: "Unknown error occurred")
                }
                is Resource.Loading -> {}
            }
        }
    }
}

/**
 * Sealed class for navigation events
 */
sealed class NavigationEvent {
    data object NavigateBack : NavigationEvent()
    data object NavigateToLogin : NavigationEvent()
    data object NavigateToEmployeeDashboard : NavigationEvent()
    data object NavigateToAdminDashboard : NavigationEvent()
    data object NavigateToNetworkRestricted : NavigationEvent()
    data object NavigateToForgotPassword : NavigationEvent()
    data class NavigateToDestination(val destinationId: Int) : NavigationEvent()
    data class NavigateWithArgs(val destinationId: Int, val args: Bundle) : NavigationEvent()
}
