package com.mgbheights.android.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgbheights.shared.domain.model.*
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.domain.usecase.maintenance.GetBillsUseCase
import com.mgbheights.shared.domain.usecase.notice.GetNoticesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val user: User? = null,
    val pendingDues: Double = 0.0,
    val pendingBills: List<MaintenanceBill> = emptyList(),
    val recentNotices: List<Notice> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val needsApproval: Boolean = false   // user is not yet approved — skip Firestore reads
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getBillsUseCase: GetBillsUseCase,
    private val getNoticesUseCase: GetNoticesUseCase
) : ViewModel() {

    private val _state = MutableLiveData(DashboardState())
    val state: LiveData<DashboardState> = _state

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _state.value = _state.value?.copy(isLoading = true)
            loadRealData()
            _state.value = _state.value?.copy(isLoading = false)
        }
    }


    /** Load real data from Firebase/Room */
    private suspend fun loadRealData() {
        // Get current user
        val userResult = getCurrentUserUseCase()
        if (userResult.isSuccess) {
            val user = userResult.getOrNull()!!
            _state.value = _state.value?.copy(user = user)

            // Fix #7: Do NOT fire any Firestore queries for unapproved users —
            // the security rules would deny them and the fragment should redirect to awaiting.
            if (!user.isApproved && user.role != UserRole.ADMIN) {
                _state.value = _state.value?.copy(isLoading = false, needsApproval = true)
                return
            }

            // Load bills for this user's flat
            if (user.flatNumber.isNotBlank()) {
                loadBills(user)
            }

            // Load notices based on role
            loadNotices(user.role)
        } else {
            _state.value = _state.value?.copy(error = userResult.errorMessageOrNull())
        }
    }

    private suspend fun loadBills(user: User) {
        val billsResult = getBillsUseCase.all()
        if (billsResult.isSuccess) {
            val allBills = billsResult.getOrNull() ?: emptyList()
            val userBills = allBills.filter {
                it.residentId == user.id || it.flatNumber == user.flatNumber
            }
            val pending = userBills.filter {
                it.status == BillStatus.PENDING || it.status == BillStatus.OVERDUE
            }
            _state.value = _state.value?.copy(
                pendingBills = pending,
                pendingDues = pending.sumOf { it.totalAmount }
            )
        }
    }

    private suspend fun loadNotices(role: UserRole) {
        val noticesResult = getNoticesUseCase.byRole(role)
        if (noticesResult.isSuccess) {
            _state.value = _state.value?.copy(
                recentNotices = noticesResult.getOrNull()?.take(5) ?: emptyList()
            )
        }
    }

    fun refresh() {
        loadDashboard()
    }
}

