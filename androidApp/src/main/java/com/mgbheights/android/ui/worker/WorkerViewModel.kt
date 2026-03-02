package com.mgbheights.android.ui.worker

import androidx.lifecycle.*
import com.mgbheights.shared.domain.model.WorkOrder
import com.mgbheights.shared.domain.model.WorkOrderStatus
import com.mgbheights.shared.domain.repository.WorkerRepository
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerViewModel @Inject constructor(
    private val workerRepository: WorkerRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _workOrders = MutableLiveData<Resource<List<WorkOrder>>>()
    val workOrders: LiveData<Resource<List<WorkOrder>>> = _workOrders

    private val _earnings = MutableLiveData<Resource<Double>>()
    val earnings: LiveData<Resource<Double>> = _earnings

    private val _isDutyOn = MutableLiveData(false)
    val isDutyOn: LiveData<Boolean> = _isDutyOn

    private var workerId: String = ""

    init { loadWorkerData() }

    private fun loadWorkerData() {
        viewModelScope.launch {
            val userResult = getCurrentUserUseCase()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!
                val workerResult = workerRepository.getWorkerByUserId(user.id)
                if (workerResult.isSuccess) {
                    val worker = workerResult.getOrNull()!!
                    workerId = worker.id
                    _isDutyOn.value = worker.isDutyOn
                    loadWorkOrders()
                    loadEarnings()
                }
            }
        }
    }

    fun loadWorkOrders() {
        viewModelScope.launch {
            if (workerId.isNotBlank()) {
                _workOrders.value = Resource.Loading
                _workOrders.value = workerRepository.getWorkOrdersByWorker(workerId)
            }
        }
    }

    fun loadEarnings() {
        viewModelScope.launch {
            if (workerId.isNotBlank()) {
                _earnings.value = workerRepository.getWorkerEarnings(workerId)
            }
        }
    }

    fun toggleDuty() {
        viewModelScope.launch {
            if (workerId.isNotBlank()) {
                val newState = _isDutyOn.value != true
                workerRepository.toggleDuty(workerId, newState)
                _isDutyOn.value = newState
            }
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            workerRepository.updateWorkOrderStatus(orderId, WorkOrderStatus.ACCEPTED)
            loadWorkOrders()
        }
    }

    fun rejectOrder(orderId: String) {
        viewModelScope.launch {
            workerRepository.updateWorkOrderStatus(orderId, WorkOrderStatus.REJECTED)
            loadWorkOrders()
        }
    }

    fun startWork(orderId: String) {
        viewModelScope.launch {
            workerRepository.updateWorkOrderStatus(orderId, WorkOrderStatus.IN_PROGRESS)
            loadWorkOrders()
        }
    }

    fun completeWork(orderId: String) {
        viewModelScope.launch {
            workerRepository.updateWorkOrderStatus(orderId, WorkOrderStatus.COMPLETED)
            loadWorkOrders()
            loadEarnings()
        }
    }
}

