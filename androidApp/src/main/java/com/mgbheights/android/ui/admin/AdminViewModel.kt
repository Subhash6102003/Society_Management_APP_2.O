package com.mgbheights.android.ui.admin

import androidx.lifecycle.*
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.repository.MaintenanceRepository
import com.mgbheights.shared.domain.repository.UserRepository
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardState(
    val totalUsers: Int = 0,
    val pendingApprovals: Int = 0,
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
    private val maintenanceRepository: MaintenanceRepository
) : ViewModel() {

    private val _state = MutableLiveData(AdminDashboardState())
    val state: LiveData<AdminDashboardState> = _state

    private val _pendingUsers = MutableLiveData<Resource<List<User>>>()
    val pendingUsers: LiveData<Resource<List<User>>> = _pendingUsers

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
    }

    fun loadPendingApprovals() {
        viewModelScope.launch {
            _pendingUsers.value = Resource.Loading
            _pendingUsers.value = userRepository.getPendingApprovals()
        }
    }

    fun approveUser(userId: String) {
        viewModelScope.launch {
            userRepository.approveUser(userId)
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

