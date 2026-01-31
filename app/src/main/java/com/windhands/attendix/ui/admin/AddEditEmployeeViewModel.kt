package com.windhands.attendix.ui.admin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditEmployeeViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val employeeId: Int? = savedStateHandle.get<Int>("employeeId")?.takeIf { it > 0 }
    val isEditMode: Boolean = employeeId != null

    private val _formState = MutableStateFlow(EmployeeFormState())
    val formState: StateFlow<EmployeeFormState> = _formState.asStateFlow()

    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    private val _loadState = MutableStateFlow<LoadState>(LoadState.Idle)
    val loadState: StateFlow<LoadState> = _loadState.asStateFlow()

    val departments = listOf(
        "Engineering",
        "Human Resources",
        "Finance",
        "Marketing",
        "Sales",
        "Operations",
        "IT",
        "Design",
        "Legal",
        "Other"
    )

    val roles = listOf("employee", "hr", "admin")

    init {
        if (isEditMode && employeeId != null) {
            loadEmployee(employeeId)
        }
    }

    private fun loadEmployee(id: Int) {
        viewModelScope.launch {
            _loadState.value = LoadState.Loading
            
            adminRepository.getEmployee(id)
                .onSuccess { response ->
                    if (response.success && response.data != null) {
                        val employee = response.data
                        _formState.value = EmployeeFormState(
                            firstName = employee.firstName,
                            lastName = employee.lastName,
                            email = employee.email,
                            phone = employee.phone ?: "",
                            department = employee.department ?: "",
                            designation = employee.designation ?: "",
                            role = employee.role,
                            joiningDate = employee.joiningDate?.substring(0, 10) ?: ""
                        )
                        _loadState.value = LoadState.Success
                    } else {
                        _loadState.value = LoadState.Error(response.message ?: "Failed to load employee")
                    }
                }
                .onFailure { exception ->
                    _loadState.value = LoadState.Error(exception.message ?: "Network error")
                }
        }
    }

    fun updateFirstName(value: String) {
        _formState.value = _formState.value.copy(firstName = value, firstNameError = null)
    }

    fun updateLastName(value: String) {
        _formState.value = _formState.value.copy(lastName = value, lastNameError = null)
    }

    fun updateEmail(value: String) {
        _formState.value = _formState.value.copy(email = value, emailError = null)
    }

    fun updatePhone(value: String) {
        _formState.value = _formState.value.copy(phone = value)
    }

    fun updateDepartment(value: String) {
        _formState.value = _formState.value.copy(department = value)
    }

    fun updateDesignation(value: String) {
        _formState.value = _formState.value.copy(designation = value)
    }

    fun updateRole(value: String) {
        _formState.value = _formState.value.copy(role = value, roleError = null)
    }

    fun updateJoiningDate(value: String) {
        _formState.value = _formState.value.copy(joiningDate = value)
    }

    fun submit() {
        val currentForm = _formState.value
        
        // Validate
        var hasError = false
        var updatedForm = currentForm
        
        if (currentForm.firstName.isBlank()) {
            updatedForm = updatedForm.copy(firstNameError = "First name is required")
            hasError = true
        }
        
        if (currentForm.lastName.isBlank()) {
            updatedForm = updatedForm.copy(lastNameError = "Last name is required")
            hasError = true
        }
        
        if (currentForm.email.isBlank()) {
            updatedForm = updatedForm.copy(emailError = "Email is required")
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(currentForm.email).matches()) {
            updatedForm = updatedForm.copy(emailError = "Invalid email format")
            hasError = true
        }
        
        if (currentForm.role.isBlank()) {
            updatedForm = updatedForm.copy(roleError = "Role is required")
            hasError = true
        }
        
        if (hasError) {
            _formState.value = updatedForm
            return
        }
        
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            
            if (isEditMode && employeeId != null) {
                // Update existing employee
                adminRepository.updateEmployee(
                    employeeId = employeeId,
                    firstName = currentForm.firstName,
                    lastName = currentForm.lastName,
                    phone = currentForm.phone.takeIf { it.isNotBlank() },
                    department = currentForm.department.takeIf { it.isNotBlank() },
                    designation = currentForm.designation.takeIf { it.isNotBlank() },
                    role = currentForm.role,
                    joiningDate = currentForm.joiningDate.takeIf { it.isNotBlank() }
                ).onSuccess { response ->
                    if (response.success) {
                        _submitState.value = SubmitState.Success("Employee updated successfully")
                    } else {
                        _submitState.value = SubmitState.Error(response.message ?: "Update failed")
                    }
                }.onFailure { exception ->
                    _submitState.value = SubmitState.Error(exception.message ?: "Network error")
                }
            } else {
                // Create new employee
                adminRepository.createEmployee(
                    firstName = currentForm.firstName,
                    lastName = currentForm.lastName,
                    email = currentForm.email,
                    phone = currentForm.phone.takeIf { it.isNotBlank() },
                    department = currentForm.department.takeIf { it.isNotBlank() },
                    designation = currentForm.designation.takeIf { it.isNotBlank() },
                    role = currentForm.role,
                    joiningDate = currentForm.joiningDate.takeIf { it.isNotBlank() }
                ).onSuccess { response ->
                    if (response.success) {
                        val temporaryPassword = response.data?.temporaryPassword
                        _submitState.value = SubmitState.Success(
                            message = "Employee created successfully",
                            temporaryPassword = temporaryPassword
                        )
                    } else {
                        _submitState.value = SubmitState.Error(response.message ?: "Creation failed")
                    }
                }.onFailure { exception ->
                    _submitState.value = SubmitState.Error(exception.message ?: "Network error")
                }
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = SubmitState.Idle
    }
}

data class EmployeeFormState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val department: String = "",
    val designation: String = "",
    val role: String = "employee",
    val joiningDate: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val roleError: String? = null
)

sealed class SubmitState {
    data object Idle : SubmitState()
    data object Loading : SubmitState()
    data class Success(val message: String, val temporaryPassword: String? = null) : SubmitState()
    data class Error(val message: String) : SubmitState()
}

sealed class LoadState {
    data object Idle : LoadState()
    data object Loading : LoadState()
    data object Success : LoadState()
    data class Error(val message: String) : LoadState()
}
