package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.domain.model.Worker
import com.mgbheights.shared.domain.model.WorkerCategory
import com.mgbheights.shared.domain.model.WorkOrder
import com.mgbheights.shared.domain.model.WorkOrderStatus
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

interface WorkerRepository {
    // Worker management
    suspend fun getWorkerById(workerId: String): Resource<Worker>
    suspend fun getWorkerByUserId(userId: String): Resource<Worker>
    fun observeWorker(workerId: String): Flow<Resource<Worker>>
    suspend fun getAllWorkers(): Resource<List<Worker>>
    fun observeAllWorkers(): Flow<Resource<List<Worker>>>
    suspend fun getWorkersByCategory(category: WorkerCategory): Resource<List<Worker>>
    suspend fun getAvailableWorkers(): Resource<List<Worker>>
    suspend fun createWorker(worker: Worker): Resource<Worker>
    suspend fun updateWorker(worker: Worker): Resource<Worker>
    suspend fun toggleDuty(workerId: String, isDutyOn: Boolean): Resource<Unit>
    suspend fun toggleAvailability(workerId: String, isAvailable: Boolean): Resource<Unit>
    suspend fun rateWorker(workerId: String, rating: Float, feedback: String): Resource<Unit>

    // Work orders
    suspend fun getWorkOrderById(orderId: String): Resource<WorkOrder>
    fun observeWorkOrder(orderId: String): Flow<Resource<WorkOrder>>
    suspend fun getWorkOrdersByWorker(workerId: String): Resource<List<WorkOrder>>
    fun observeWorkOrdersByWorker(workerId: String): Flow<Resource<List<WorkOrder>>>
    suspend fun getWorkOrdersByFlat(flatId: String): Resource<List<WorkOrder>>
    suspend fun getWorkOrdersByStatus(status: WorkOrderStatus): Resource<List<WorkOrder>>
    suspend fun getAllWorkOrders(): Resource<List<WorkOrder>>
    fun observeAllWorkOrders(): Flow<Resource<List<WorkOrder>>>
    suspend fun createWorkOrder(workOrder: WorkOrder): Resource<WorkOrder>
    suspend fun updateWorkOrderStatus(orderId: String, status: WorkOrderStatus): Resource<Unit>
    suspend fun markWorkOrderPaid(orderId: String, paymentId: String): Resource<Unit>
    suspend fun getWorkerEarnings(workerId: String): Resource<Double>
}

