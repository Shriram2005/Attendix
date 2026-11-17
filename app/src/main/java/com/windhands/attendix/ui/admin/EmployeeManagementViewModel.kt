package com.windhands.attendix.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.windhands.attendix.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class EmployeeManagementViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _employeesState = MutableStateFlow<EmployeesState>(EmployeesState.Loading)
    val employeesState: StateFlow<EmployeesState> = _employeesState.asStateFlow()

    private val _employees = MutableStateFlow<List<EmployeeItem>>(emptyList())
    val employees: StateFlow<List<EmployeeItem>> = _employees.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow(EmployeeStatusFilter.ALL)
    val statusFilter: StateFlow<EmployeeStatusFilter> = _statusFilter.asStateFlow()

    private val _actionState = MutableStateFlow<EmployeeActionState>(EmployeeActionState.Idle)
    val actionState: StateFlow<EmployeeActionState> = _actionState.asStateFlow()

    init {
        loadEmployees()
        setupSearchObserver()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchObserver() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300.milliseconds)
                .distinctUntilChanged()
                .collect {
                    loadEmployees()
                }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: EmployeeStatusFilter) {
        if (_statusFilter.value != filter) {
            _statusFilter.value = filter
            loadEmployees()
        }
    }

    fun loadEmployees() {
        viewModelScope.launch {
            _employeesState.value = EmployeesState.Loading
            
            val status = when (_statusFilter.value) {
                EmployeeStatusFilter.ALL -> null
                EmployeeStatusFilter.ACTIVE -> "active"
                EmployeeStatusFilter.INACTIVE -> "inactive"
            }
            
            val search = _searchQuery.value.takeIf { it.isNotBlank() }
            
            adminRepository.getEmployees(
                search = search,
                status = status
            )
                .onSuccess { response ->
                    if (response.success) {
                        val employeeList = response.data?.employees?.map { employee ->
                            EmployeeItem(
                                id = employee.id,
                                employeeId = employee.employeeId,
                                firstName = employee.firstName,
                                lastName = employee.lastName,
                                email = employee.email,
                                phone = employee.phone,
                                department = employee.department,
                                designation = employee.designation,
                                role = employee.role,
                                status = employee.status,
                                joiningDate = employee.dateOfJoining
                            )
                        } ?: emptyList()
                        _employees.value = employeeList
                        _employeesState.value = EmployeesState.Success
                    } else {
                        _employeesState.value = EmployeesState.Error(
                            response.message ?: "Failed to load employees"
                        )
                    }
                }
                .onFailure { exception ->
                    _employeesState.value = EmployeesState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun deactivateEmployee(employeeId: Int) {
        viewModelScope.launch {
            _actionState.value = EmployeeActionState.Processing
            
            adminRepository.deactivateEmployee(employeeId)
                .onSuccess { response ->
                    if (response.success) {
                        _actionState.value = EmployeeActionState.Success("Employee deactivated")
                        loadEmployees()
                    } else {
                        _actionState.value = EmployeeActionState.Error(
                            response.message ?: "Failed to deactivate"
                        )
                    }
                }
                .onFailure { exception ->
                    _actionState.value = EmployeeActionState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun activateEmployee(employeeId: Int) {
        viewModelScope.launch {
            _actionState.value = EmployeeActionState.Processing
            
            adminRepository.activateEmployee(employeeId)
                .onSuccess { response ->
                    if (response.success) {
                        _actionState.value = EmployeeActionState.Success("Employee activated")
                        loadEmployees()
                    } else {
                        _actionState.value = EmployeeActionState.Error(
                            response.message ?: "Failed to activate"
                        )
                    }
                }
                .onFailure { exception ->
                    _actionState.value = EmployeeActionState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun deleteEmployee(employeeId: Int) {
        viewModelScope.launch {
            _actionState.value = EmployeeActionState.Processing
            
            adminRepository.deleteEmployee(employeeId)
                .onSuccess { response ->
                    if (response.success) {
                        _actionState.value = EmployeeActionState.Success("Employee deleted")
                        loadEmployees()
                    } else {
                        _actionState.value = EmployeeActionState.Error(
                            response.message ?: "Failed to delete"
                        )
                    }
                }
                .onFailure { exception ->
                    _actionState.value = EmployeeActionState.Error(
                        exception.message ?: "Network error"
                    )
                }
        }
    }

    fun resetActionState() {
        _actionState.value = EmployeeActionState.Idle
    }
}

sealed class EmployeesState {
    data object Loading : EmployeesState()
    data object Success : EmployeesState()
    data class Error(val message: String) : EmployeesState()
}

sealed class EmployeeActionState {
    data object Idle : EmployeeActionState()
    data object Processing : EmployeeActionState()
    data class Success(val message: String) : EmployeeActionState()
    data class Error(val message: String) : EmployeeActionState()
}

enum class EmployeeStatusFilter {
    ALL, ACTIVE, INACTIVE
}

data class EmployeeItem(
    val id: Int,
    val employeeId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val department: String?,
    val designation: String?,
    val role: String,
    val status: String,
    val joiningDate: String?
) {
    val fullName: String get() = "$firstName $lastName"
    val initials: String get() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase()
    val isActive: Boolean get() = status.lowercase() == "active"
}
