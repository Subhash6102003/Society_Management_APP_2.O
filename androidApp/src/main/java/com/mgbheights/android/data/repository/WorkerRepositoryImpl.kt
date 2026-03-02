package com.mgbheights.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mgbheights.android.data.mapper.*
import com.mgbheights.shared.domain.model.*
import com.mgbheights.shared.domain.repository.WorkerRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkerRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WorkerRepository {

    private val workersRef = firestore.collection(Constants.COLLECTION_WORKERS)
    private val ordersRef = firestore.collection(Constants.COLLECTION_WORK_ORDERS)

    private fun Map<String, Any?>.toWorker(): Worker = Worker(
        id = this["id"] as? String ?: "", userId = this["userId"] as? String ?: "",
        name = this["name"] as? String ?: "", phoneNumber = this["phoneNumber"] as? String ?: "",
        profilePhotoUrl = this["profilePhotoUrl"] as? String ?: "", idProofUrl = this["idProofUrl"] as? String ?: "",
        skills = (this["skills"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        category = try { WorkerCategory.valueOf(this["category"] as? String ?: "OTHER") } catch (_: Exception) { WorkerCategory.OTHER },
        isAvailable = this["isAvailable"] as? Boolean ?: true, isDutyOn = this["isDutyOn"] as? Boolean ?: false,
        rating = (this["rating"] as? Number)?.toFloat() ?: 0f, totalRatings = (this["totalRatings"] as? Number)?.toInt() ?: 0,
        totalJobs = (this["totalJobs"] as? Number)?.toInt() ?: 0, totalEarnings = (this["totalEarnings"] as? Number)?.toDouble() ?: 0.0,
        assignedFlats = (this["assignedFlats"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        createdAt = (this["createdAt"] as? Long) ?: 0L, updatedAt = (this["updatedAt"] as? Long) ?: 0L
    )

    private fun Worker.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "userId" to userId, "name" to name, "phoneNumber" to phoneNumber,
        "profilePhotoUrl" to profilePhotoUrl, "idProofUrl" to idProofUrl, "skills" to skills,
        "category" to category.name, "isAvailable" to isAvailable, "isDutyOn" to isDutyOn,
        "rating" to rating, "totalRatings" to totalRatings, "totalJobs" to totalJobs,
        "totalEarnings" to totalEarnings, "assignedFlats" to assignedFlats,
        "createdAt" to createdAt, "updatedAt" to updatedAt
    )

    private fun Map<String, Any?>.toWorkOrder(): WorkOrder = WorkOrder(
        id = this["id"] as? String ?: "", workerId = this["workerId"] as? String ?: "",
        workerName = this["workerName"] as? String ?: "", flatId = this["flatId"] as? String ?: "",
        flatNumber = this["flatNumber"] as? String ?: "", towerBlock = this["towerBlock"] as? String ?: "",
        residentId = this["residentId"] as? String ?: "", residentName = this["residentName"] as? String ?: "",
        title = this["title"] as? String ?: "", description = this["description"] as? String ?: "",
        category = try { WorkerCategory.valueOf(this["category"] as? String ?: "OTHER") } catch (_: Exception) { WorkerCategory.OTHER },
        status = try { WorkOrderStatus.valueOf(this["status"] as? String ?: "PENDING") } catch (_: Exception) { WorkOrderStatus.PENDING },
        amount = (this["amount"] as? Number)?.toDouble() ?: 0.0, isPaid = this["isPaid"] as? Boolean ?: false,
        paymentId = this["paymentId"] as? String ?: "", rating = (this["rating"] as? Number)?.toFloat() ?: 0f,
        feedback = this["feedback"] as? String ?: "", scheduledAt = (this["scheduledAt"] as? Long) ?: 0L,
        startedAt = (this["startedAt"] as? Long) ?: 0L, completedAt = (this["completedAt"] as? Long) ?: 0L,
        createdAt = (this["createdAt"] as? Long) ?: 0L, updatedAt = (this["updatedAt"] as? Long) ?: 0L
    )

    private fun WorkOrder.toMap(): Map<String, Any?> = mapOf(
        "id" to id, "workerId" to workerId, "workerName" to workerName, "flatId" to flatId,
        "flatNumber" to flatNumber, "towerBlock" to towerBlock, "residentId" to residentId,
        "residentName" to residentName, "title" to title, "description" to description,
        "category" to category.name, "status" to status.name, "amount" to amount,
        "isPaid" to isPaid, "paymentId" to paymentId, "rating" to rating, "feedback" to feedback,
        "scheduledAt" to scheduledAt, "startedAt" to startedAt, "completedAt" to completedAt,
        "createdAt" to createdAt, "updatedAt" to updatedAt
    )

    override suspend fun getWorkerById(workerId: String): Resource<Worker> = try {
        val doc = workersRef.document(workerId).get().await()
        if (doc.exists()) Resource.success(doc.data!!.toWorker().copy(id = doc.id))
        else Resource.error("Worker not found")
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getWorkerByUserId(userId: String): Resource<Worker> = try {
        val snap = workersRef.whereEqualTo("userId", userId).limit(1).get().await()
        if (snap.documents.isNotEmpty()) Resource.success(snap.documents[0].data!!.toWorker().copy(id = snap.documents[0].id))
        else Resource.error("Worker not found")
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeWorker(workerId: String): Flow<Resource<Worker>> = callbackFlow {
        val listener = workersRef.document(workerId).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            if (snap != null && snap.exists()) trySend(Resource.success(snap.data!!.toWorker().copy(id = snap.id)))
            else trySend(Resource.error("Not found"))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getAllWorkers(): Resource<List<Worker>> = try {
        val snap = workersRef.get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toWorker()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeAllWorkers(): Flow<Resource<List<Worker>>> = callbackFlow {
        val listener = workersRef.addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            trySend(Resource.success(snap?.documents?.mapNotNull { it.data?.toWorker()?.copy(id = it.id) } ?: emptyList()))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getWorkersByCategory(category: WorkerCategory): Resource<List<Worker>> = try {
        val snap = workersRef.whereEqualTo("category", category.name).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toWorker()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getAvailableWorkers(): Resource<List<Worker>> = try {
        val snap = workersRef.whereEqualTo("isAvailable", true).whereEqualTo("isDutyOn", true).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toWorker()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun createWorker(worker: Worker): Resource<Worker> = try {
        val docRef = workersRef.document()
        val w = worker.copy(id = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        docRef.set(w.toMap()).await()
        Resource.success(w)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateWorker(worker: Worker): Resource<Worker> = try {
        val updated = worker.copy(updatedAt = System.currentTimeMillis())
        workersRef.document(worker.id).set(updated.toMap()).await()
        Resource.success(updated)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun toggleDuty(workerId: String, isDutyOn: Boolean): Resource<Unit> = try {
        workersRef.document(workerId).update("isDutyOn", isDutyOn, "updatedAt", System.currentTimeMillis()).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun toggleAvailability(workerId: String, isAvailable: Boolean): Resource<Unit> = try {
        workersRef.document(workerId).update("isAvailable", isAvailable, "updatedAt", System.currentTimeMillis()).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun rateWorker(workerId: String, rating: Float, feedback: String): Resource<Unit> = try {
        val doc = workersRef.document(workerId).get().await()
        val current = doc.data?.toWorker() ?: return Resource.error("Worker not found")
        val newTotal = current.totalRatings + 1
        val newRating = ((current.rating * current.totalRatings) + rating) / newTotal
        workersRef.document(workerId).update(mapOf("rating" to newRating, "totalRatings" to newTotal, "updatedAt" to System.currentTimeMillis())).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    // Work orders
    override suspend fun getWorkOrderById(orderId: String): Resource<WorkOrder> = try {
        val doc = ordersRef.document(orderId).get().await()
        if (doc.exists()) Resource.success(doc.data!!.toWorkOrder().copy(id = doc.id))
        else Resource.error("Work order not found")
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeWorkOrder(orderId: String): Flow<Resource<WorkOrder>> = callbackFlow {
        val listener = ordersRef.document(orderId).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            if (snap != null && snap.exists()) trySend(Resource.success(snap.data!!.toWorkOrder().copy(id = snap.id)))
            else trySend(Resource.error("Not found"))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getWorkOrdersByWorker(workerId: String): Resource<List<WorkOrder>> = try {
        val snap = ordersRef.whereEqualTo("workerId", workerId).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toWorkOrder()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeWorkOrdersByWorker(workerId: String): Flow<Resource<List<WorkOrder>>> = callbackFlow {
        val listener = ordersRef.whereEqualTo("workerId", workerId).orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
                trySend(Resource.success(snap?.documents?.mapNotNull { it.data?.toWorkOrder()?.copy(id = it.id) } ?: emptyList()))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getWorkOrdersByFlat(flatId: String): Resource<List<WorkOrder>> = try {
        val snap = ordersRef.whereEqualTo("flatId", flatId).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toWorkOrder()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getWorkOrdersByStatus(status: WorkOrderStatus): Resource<List<WorkOrder>> = try {
        val snap = ordersRef.whereEqualTo("status", status.name).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toWorkOrder()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getAllWorkOrders(): Resource<List<WorkOrder>> = try {
        val snap = ordersRef.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toWorkOrder()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeAllWorkOrders(): Flow<Resource<List<WorkOrder>>> = callbackFlow {
        val listener = ordersRef.orderBy("createdAt", Query.Direction.DESCENDING).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            trySend(Resource.success(snap?.documents?.mapNotNull { it.data?.toWorkOrder()?.copy(id = it.id) } ?: emptyList()))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun createWorkOrder(workOrder: WorkOrder): Resource<WorkOrder> = try {
        val docRef = ordersRef.document()
        val wo = workOrder.copy(id = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        docRef.set(wo.toMap()).await()
        Resource.success(wo)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateWorkOrderStatus(orderId: String, status: WorkOrderStatus): Resource<Unit> = try {
        val updates = mutableMapOf<String, Any>("status" to status.name, "updatedAt" to System.currentTimeMillis())
        if (status == WorkOrderStatus.IN_PROGRESS) updates["startedAt"] = System.currentTimeMillis()
        if (status == WorkOrderStatus.COMPLETED) updates["completedAt"] = System.currentTimeMillis()
        ordersRef.document(orderId).update(updates).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun markWorkOrderPaid(orderId: String, paymentId: String): Resource<Unit> = try {
        ordersRef.document(orderId).update(mapOf("isPaid" to true, "paymentId" to paymentId, "updatedAt" to System.currentTimeMillis())).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getWorkerEarnings(workerId: String): Resource<Double> = try {
        val snap = ordersRef.whereEqualTo("workerId", workerId).whereEqualTo("isPaid", true).get().await()
        val total = snap.documents.sumOf { (it.data?.get("amount") as? Number)?.toDouble() ?: 0.0 }
        Resource.success(total)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }
}

