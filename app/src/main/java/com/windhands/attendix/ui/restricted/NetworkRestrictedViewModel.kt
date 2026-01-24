package com.windhands.attendix.ui.restricted

import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.repository.AuthRepository
import com.windhands.attendix.ui.base.BaseViewModel
import com.windhands.attendix.ui.base.NavigationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Network Restricted Screen
 */
@HiltViewModel
class NetworkRestrictedViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    /**
     * Retry network access check
     */
    fun retry() {
        viewModelScope.launch {
            setLoading(true)

            val result = authRepository.getProfile()
            
            result.fold(
                onSuccess = {
                    emitSuccess("Network access restored")
                    // Network is now accessible, navigate back to splash for proper flow
                    navigate(NavigationEvent.NavigateBack)
                },
                onFailure = { error ->
                    val errorMessage = error.message ?: "Network still restricted"
                    if (errorMessage.contains("IP_RESTRICTED", ignoreCase = true)) {
                        emitError("Network still restricted. Please connect to the office network.")
                    } else {
                        emitSuccess("Network access restored")
                        navigate(NavigationEvent.NavigateBack)
                    }
                }
            )

            setLoading(false)
        }
    }
}
