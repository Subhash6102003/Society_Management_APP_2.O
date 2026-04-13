package com.mgbheights.android.data.repository

import com.mgbheights.android.data.remote.dto.WorkOrderDto
import com.mgbheights.android.data.remote.dto.WorkerDto
import com.mgbheights.android.data.remote.dto.toDto
import com.mgbheights.android.data.remote.dto.toWorkOrder
import com.mgbheights.android.data.remote.dto.toWorker
import com.mgbheights.shared.domain.model.Worker
import com.mgbheights.shared.domain.model.WorkerCategory
import com.mgbheights.shared.domain.model.WorkOrder
import com.mgbheights.shared.domain.model.WorkOrderStatus
import com.mgbheights.shared.domain.repository.WorkerRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkerRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : WorkerRepository {

    private val workersTable = Constants.COLLECTION_WORKERS
    private val ordersTable = Constants.COLLECTION_WORK_ORDERS

    override suspend fun getWorkerById(workerId: String): Resource<Worker> = try {
        val dto = supabase.from(workersTable).select { filter { eq("id", workerId) } }
            .decodeSingleOrNull<WorkerDto>() ?: return Resource.error("Worker not found")
        Resource.success(dto.toWorker())
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getWorkerByUserId(userId: String): Resource<Worker> = try {
        val dto = supabase.from(workersTable).select { filter { eq("user_id", userId) } }
            .decodeSingleOrNull<WorkerDto>() ?: return Resource.error("Worker not found")
        Resource.success(dto.toWorker())
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeWorker(workerId: String): Flow<Resource<Worker>> = flow {
        emit(getWorkerById(workerId))
    }

    override suspend fun getAllWorkers(): Resource<List<Worker>> = try {
        val dtos = supabase.from(workersTable).select().decodeList<WorkerDto>()
        Resource.success(dtos.map { it.toWorker() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeAllWorkers(): Flow<Resource<List<Worker>>> = flow {
        emit(getAllWorkers())
    }

    override suspend fun getWorkersByCategory(category: WorkerCategory): Resource<List<Worker>> = try {
        val dtos = supabase.from(workersTable).select { filter { eq("category", category.name) } }
            .decodeList<WorkerDto>()
        Resource.success(dtos.map { it.toWorker() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getAvailableWorkers(): Resource<List<Worker>> = try {
        val dtos = supabase.from(workersTable).select {
            filter { eq("is_available", true); eq("is_duty_on", true) }
        }.decodeList<WorkerDto>()
        Resource.success(dtos.map { it.toWorker() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun createWorker(worker: Worker): Resource<Worker> = try {
        val w = if (worker.id.isBlank()) worker.copy(
            id = UUID.randomUUID().toString(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ) else worker
        supabase.from(workersTable).insert(w.toDto())
        Resource.success(w)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateWorker(worker: Worker): Resource<Worker> = try {
        val updated = worker.copy(updatedAt = System.currentTimeMillis())
        supabase.from(workersTable).update(updated.toDto()) { filter { eq("id", worker.id) } }
        Resource.success(updated)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun toggleDuty(workerId: String, isDutyOn: Boolean): Resource<Unit> = try {
        supabase.from(workersTable).update(
            mapOf("is_duty_on" to isDutyOn, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", workerId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun toggleAvailability(workerId: String, isAvailable: Boolean): Resource<Unit> = try {
        supabase.from(workersTable).update(
            mapOf("is_available" to isAvailable, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", workerId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun rateWorker(workerId: String, rating: Float, feedback: String): Resource<Unit> = try {
        val current = getWorkerById(workerId).getOrNull()
            ?: return Resource.error("Worker not found")
        val newTotal = current.totalRatings + 1
        val newRating = ((current.rating * current.totalRatings) + rating) / newTotal
        supabase.from(workersTable).update(
            mapOf("rating" to newRating, "total_ratings" to newTotal, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", workerId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    // Work Orders
    override suspend fun getWorkOrderById(orderId: String): Resource<WorkOrder> = try {
        val dto = supabase.from(ordersTable).select { filter { eq("id", orderId) } }
            .decodeSingleOrNull<WorkOrderDto>() ?: return Resource.error("Work order not found")
        Resource.success(dto.toWorkOrder())
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeWorkOrder(orderId: String): Flow<Resource<WorkOrder>> = flow {
        emit(getWorkOrderById(orderId))
    }

    override suspend fun getWorkOrdersByWorker(workerId: String): Resource<List<WorkOrder>> = try {
        val dtos = supabase.from(ordersTable).select { filter { eq("worker_id", workerId) } }
            .decodeList<WorkOrderDto>()
        Resource.success(dtos.map { it.toWorkOrder() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeWorkOrdersByWorker(workerId: String): Flow<Resource<List<WorkOrder>>> = flow {
        emit(getWorkOrdersByWorker(workerId))
    }

    override suspend fun getWorkOrdersByFlat(flatId: String): Resource<List<WorkOrder>> = try {
        val dtos = supabase.from(ordersTable).select { filter { eq("flat_id", flatId) } }
            .decodeList<WorkOrderDto>()
        Resource.success(dtos.map { it.toWorkOrder() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getWorkOrdersByStatus(status: WorkOrderStatus): Resource<List<WorkOrder>> = try {
        val dtos = supabase.from(ordersTable).select { filter { eq("status", status.name) } }
            .decodeList<WorkOrderDto>()
        Resource.success(dtos.map { it.toWorkOrder() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getAllWorkOrders(): Resource<List<WorkOrder>> = try {
        val dtos = supabase.from(ordersTable).select().decodeList<WorkOrderDto>()
        Resource.success(dtos.map { it.toWorkOrder() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeAllWorkOrders(): Flow<Resource<List<WorkOrder>>> = flow {
        emit(getAllWorkOrders())
    }

    override suspend fun createWorkOrder(workOrder: WorkOrder): Resource<WorkOrder> = try {
        val wo = if (workOrder.id.isBlank()) workOrder.copy(
            id = UUID.randomUUID().toString(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ) else workOrder
        supabase.from(ordersTable).insert(wo.toDto())
        Resource.success(wo)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateWorkOrderStatus(orderId: String, status: WorkOrderStatus): Resource<Unit> = try {
        val updates = mutableMapOf<String, Any>(
            "status" to status.name,
            "updated_at" to System.currentTimeMillis()
        )
        if (status == WorkOrderStatus.IN_PROGRESS) updates["started_at"] = System.currentTimeMillis()
        if (status == WorkOrderStatus.COMPLETED) updates["completed_at"] = System.currentTimeMillis()
        supabase.from(ordersTable).update(updates) { filter { eq("id", orderId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun markWorkOrderPaid(orderId: String, paymentId: String): Resource<Unit> = try {
        supabase.from(ordersTable).update(
            mapOf("is_paid" to true, "payment_id" to paymentId, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", orderId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getWorkerEarnings(workerId: String): Resource<Double> = try {
        val dtos = supabase.from(ordersTable).select {
            filter { eq("worker_id", workerId); eq("is_paid", true) }
        }.decodeList<WorkOrderDto>()
        Resource.success(dtos.sumOf { it.amount })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }
}
