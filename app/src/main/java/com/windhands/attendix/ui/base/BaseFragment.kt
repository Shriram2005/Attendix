package com.windhands.attendix.ui.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.windhands.attendix.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar

/**
 * Base Fragment class for all fragments
 * Provides common functionality like view binding management, loading state, etc.
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    protected open fun setupView() {}

    protected open fun setupObservers() {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Show a Snackbar message
     */
    protected fun showSnackbar(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        actionText: String? = null,
        action: (() -> Unit)? = null
    ) {
        requireActivity().hideKeyboard()
        val snackbar = Snackbar.make(binding.root, message, duration)
        if (actionText != null && action != null) {
            snackbar.setAction(actionText) { action() }
        }
        snackbar.show()
    }

    /**
     * Show error Snackbar
     */
    protected fun showError(message: String) {
        Log.e(this::class.java.simpleName, "Error: $message")
        showSnackbar(message, Snackbar.LENGTH_LONG)
    }

    /**
     * Show success Snackbar
     */
    protected fun showSuccess(message: String) {
        showSnackbar(message, Snackbar.LENGTH_SHORT)
    }

    /**
     * Handle loading state - override in child if needed
     */
    protected open fun showLoading(isLoading: Boolean) {
        // Override in child to show/hide loading indicator
    }

    /**
     * Check if fragment is currently visible and binding is available
     */
    protected fun isBindingAvailable(): Boolean = _binding != null && isAdded
}
