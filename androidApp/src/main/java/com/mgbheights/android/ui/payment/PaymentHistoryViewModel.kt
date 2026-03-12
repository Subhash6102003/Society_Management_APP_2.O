package com.mgbheights.android.ui.payment

import androidx.lifecycle.*
import com.mgbheights.shared.domain.model.BillStatus
import com.mgbheights.shared.domain.model.MaintenanceBill
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.domain.usecase.maintenance.GetBillsUseCase
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getBillsUseCase: GetBillsUseCase
) : ViewModel() {

    private val _payments = MutableLiveData<Resource<List<MaintenanceBill>>>()
    val payments: LiveData<Resource<List<MaintenanceBill>>> = _payments

    init { loadPayments() }

    fun loadPayments() {
        viewModelScope.launch {
            _payments.value = Resource.Loading
            val userResult = getCurrentUserUseCase()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!
                val result = getBillsUseCase.all()
                _payments.value = result.map { bills ->
                    val filtered = if (user.role == UserRole.ADMIN) {
                        // Admin sees all paid bills
                        bills.filter { it.status == BillStatus.PAID }
                    } else {
                        // Residents/tenants see only their paid bills
                        bills.filter {
                            it.status == BillStatus.PAID &&
                            (it.residentId == user.id || it.flatNumber == user.flatNumber)
                        }
                    }
                    filtered.sortedByDescending { it.paidAt }
                }
            } else {
                _payments.value = Resource.error(userResult.errorMessageOrNull() ?: "Error loading user")
            }
        }
    }

    fun refresh() = loadPayments()
}

