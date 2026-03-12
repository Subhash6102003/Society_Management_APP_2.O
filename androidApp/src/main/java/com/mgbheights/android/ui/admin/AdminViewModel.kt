package com.mgbheights.android.ui.admin

import androidx.lifecycle.*
import com.mgbheights.shared.domain.model.EditRequest
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.repository.EditRequestRepository
import com.mgbheights.shared.domain.repository.MaintenanceRepository
import com.mgbheights.shared.domain.repository.UserRepository
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.DateTimeUtil
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardState(
    val totalUsers: Int = 0,
    val pendingApprovals: Int = 0,
    val pendingEditRequests: Int = 0,
    val totalCollected: Double = 0.0,
    val totalPending: Double = 0.0,
    val overdueBills: Int = 0,
    val todayVisitors: Int = 0,
    val openComplaints: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val editRequestRepository: EditRequestRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableLiveData(AdminDashboardState())
    val state: LiveData<AdminDashboardState> = _state

    private val _pendingUsers = MutableLiveData<Resource<List<User>>>()
    val pendingUsers: LiveData<Resource<List<User>>> = _pendingUsers

    private val _editRequests = MutableLiveData<Resource<List<EditRequest>>>()
    val editRequests: LiveData<Resource<List<EditRequest>>> = _editRequests

    private val _actionResult = MutableLiveData<Resource<Unit>>()
    val actionResult: LiveData<Resource<Unit>> = _actionResult

    private val _allUsersState = MutableLiveData<Resource<List<User>>>()
    val allUsersState: LiveData<Resource<List<User>>> = _allUsersState

    init { loadAdminDashboard() }

    fun loadAdminDashboard() {
        viewModelScope.launch {
            _state.value = _state.value?.copy(isLoading = true)
            loadRealAdminData()
            _state.value = _state.value?.copy(isLoading = false)
        }
    }

    private suspend fun loadRealAdminData() {
        // Load users count
        val usersResult = userRepository.getAllUsers()
        if (usersResult.isSuccess) {
            val users = usersResult.getOrNull()!!
            _state.value = _state.value?.copy(
                totalUsers = users.size,
                pendingApprovals = users.count { !it.isApproved }
            )
        }

        // Load billing summary
        val billsResult = maintenanceRepository.getAllBills()
        if (billsResult.isSuccess) {
            val bills = billsResult.getOrNull()!!
            _state.value = _state.value?.copy(
                totalCollected = bills.filter { it.status.name == "PAID" }.sumOf { it.totalAmount },
                totalPending = bills.filter { it.status.name == "PENDING" || it.status.name == "OVERDUE" }.sumOf { it.totalAmount },
                overdueBills = bills.count { it.status.name == "OVERDUE" }
            )
        }

        // Load edit requests count
        val editResult = editRequestRepository.getPendingEditRequests()
        if (editResult.isSuccess) {
            _state.value = _state.value?.copy(
                pendingEditRequests = editResult.getOrNull()?.size ?: 0
            )
        }

        // Auto-generate bills for current month if none exist yet
        autoGenerateBillsIfNeeded()
    }

    private suspend fun autoGenerateBillsIfNeeded() {
        try {
            val currentMonth = DateTimeUtil.currentMonthKey()
            val monthBills = maintenanceRepository.getBillsByMonth(currentMonth)
            if (monthBills.isSuccess && (monthBills.getOrNull()?.isEmpty() == true)) {
                maintenanceRepository.generateMonthlyBills(currentMonth, Constants.DEFAULT_MONTHLY_AMOUNT)
            }
        } catch (_: Exception) {
            // Silently fail — admin can manually generate if auto fails
        }
    }

    fun loadPendingApprovals() {
        viewModelScope.launch {
            _pendingUsers.value = Resource.Loading
            _pendingUsers.value = userRepository.getPendingApprovals()
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _allUsersState.value = Resource.Loading
            _allUsersState.value = userRepository.getAllUsers()
        }
    }

    fun approveUser(userId: String) {
        viewModelScope.launch {
            userRepository.approveUser(userId)
            loadAllUsers()
            loadPendingApprovals()
            loadAdminDashboard()
        }
    }

    fun rejectUser(userId: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val result = userRepository.deleteUser(userId)
            _actionResult.value = result
            loadAllUsers()
            loadPendingApprovals()
            loadAdminDashboard()
        }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            userRepository.blockUser(userId)
            loadPendingApprovals()
        }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            userRepository.unblockUser(userId)
        }
    }

    // Edit Request management
    fun loadEditRequests() {
        viewModelScope.launch {
            _editRequests.value = Resource.Loading
            _editRequests.value = editRequestRepository.getPendingEditRequests()
        }
    }

    fun approveEditRequest(requestId: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val currentUser = getCurrentUserUseCase()
            val adminId = currentUser.getOrNull()?.id ?: ""
            _actionResult.value = editRequestRepository.approveEditRequest(requestId, adminId)
            loadEditRequests()
            loadAdminDashboard()
        }
    }

    fun rejectEditRequest(requestId: String, note: String = "") {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val currentUser = getCurrentUserUseCase()
            val adminId = currentUser.getOrNull()?.id ?: ""
            _actionResult.value = editRequestRepository.rejectEditRequest(requestId, adminId, note)
            loadEditRequests()
            loadAdminDashboard()
        }
    }

    fun generateMonthlyBills(month: String, amount: Double) {
        viewModelScope.launch {
            maintenanceRepository.generateMonthlyBills(month, amount)
            loadAdminDashboard()
        }
    }

    fun applyLateFees() {
        viewModelScope.launch {
            maintenanceRepository.applyLateFees()
            loadAdminDashboard()
        }
    }
}
