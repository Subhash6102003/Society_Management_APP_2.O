package com.mgbheights.android.ui.maintenance

import androidx.lifecycle.*
import com.mgbheights.shared.domain.model.BillStatus
import com.mgbheights.shared.domain.model.MaintenanceBill
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.domain.usecase.maintenance.GetBillsUseCase
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val getBillsUseCase: GetBillsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _bills = MutableLiveData<Resource<List<MaintenanceBill>>>()
    val bills: LiveData<Resource<List<MaintenanceBill>>> = _bills

    private val _selectedFilter = MutableLiveData("All")
    val selectedFilter: LiveData<String> = _selectedFilter

    init { loadBills() }

    fun loadBills() {
        viewModelScope.launch {
            _bills.value = Resource.Loading
            val userResult = getCurrentUserUseCase()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!
                val result = if (user.role.name == "ADMIN") {
                    getBillsUseCase.all()
                } else {
                    getBillsUseCase.all().map { bills ->
                        bills.filter { it.residentId == user.id || it.flatNumber == user.flatNumber }
                    }
                }
                _bills.value = applyFilter(result)
            } else {
                _bills.value = Resource.error(userResult.errorMessageOrNull() ?: "Error loading bills")
            }
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
        loadBills()
    }

    private fun applyFilter(result: Resource<List<MaintenanceBill>>): Resource<List<MaintenanceBill>> {
        if (!result.isSuccess) return result
        val bills = result.getOrNull() ?: return result
        val filtered = when (_selectedFilter.value) {
            "Pending" -> bills.filter { it.status == BillStatus.PENDING }
            "Paid" -> bills.filter { it.status == BillStatus.PAID }
            "Overdue" -> bills.filter { it.status == BillStatus.OVERDUE }
            else -> bills
        }
        return Resource.success(filtered)
    }
}

