package com.mgbheights.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mgbheights.android.data.local.dao.ComplaintDao
import com.mgbheights.android.data.mapper.*
import com.mgbheights.shared.domain.model.Complaint
import com.mgbheights.shared.domain.model.ComplaintCategory
import com.mgbheights.shared.domain.model.ComplaintStatus
import com.mgbheights.shared.domain.repository.ComplaintRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComplaintRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val complaintDao: ComplaintDao
) : ComplaintRepository {

    private val complaintsRef = firestore.collection(Constants.COLLECTION_COMPLAINTS)

    override suspend fun getComplaintById(complaintId: String): Resource<Complaint> = try {
        val doc = complaintsRef.document(complaintId).get().await()
        if (doc.exists()) Resource.success(doc.data!!.toComplaint().copy(id = doc.id))
        else Resource.error("Complaint not found")
    } catch (e: Exception) {
        val cached = complaintDao.getComplaintById(complaintId)
        if (cached != null) Resource.success(cached.toDomain()) else Resource.error(e.message ?: "Error", e)
    }

    override fun observeComplaint(complaintId: String): Flow<Resource<Complaint>> = callbackFlow {
        val listener = complaintsRef.document(complaintId).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            if (snap != null && snap.exists()) trySend(Resource.success(snap.data!!.toComplaint().copy(id = snap.id)))
            else trySend(Resource.error("Not found"))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getComplaintsByFlat(flatId: String): Resource<List<Complaint>> = try {
        val snap = complaintsRef.whereEqualTo("flatId", flatId).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        val complaints = snap.documents.mapNotNull { it.data?.toComplaint()?.copy(id = it.id) }
        complaintDao.insertComplaints(complaints.map { it.toEntity() })
        Resource.success(complaints)
    } catch (e: Exception) {
        val cached = complaintDao.getComplaintsByFlat(flatId)
        if (cached.isNotEmpty()) Resource.success(cached.map { it.toDomain() }) else Resource.error(e.message ?: "Error", e)
    }

    override fun observeComplaintsByFlat(flatId: String): Flow<Resource<List<Complaint>>> =
        complaintDao.observeComplaintsByFlat(flatId).map { Resource.success(it.map { e -> e.toDomain() }) }

    override suspend fun getComplaintsByUser(userId: String): Resource<List<Complaint>> = try {
        val snap = complaintsRef.whereEqualTo("userId", userId).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toComplaint()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getComplaintsByStatus(status: ComplaintStatus): Resource<List<Complaint>> = try {
        val snap = complaintsRef.whereEqualTo("status", status.name).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toComplaint()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeComplaintsByStatus(status: ComplaintStatus): Flow<Resource<List<Complaint>>> =
        complaintDao.observeComplaintsByStatus(status.name).map { Resource.success(it.map { e -> e.toDomain() }) }

    override suspend fun getComplaintsByCategory(category: ComplaintCategory): Resource<List<Complaint>> = try {
        val snap = complaintsRef.whereEqualTo("category", category.name).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toComplaint()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getAllComplaints(): Resource<List<Complaint>> = try {
        val snap = complaintsRef.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        val complaints = snap.documents.mapNotNull { it.data?.toComplaint()?.copy(id = it.id) }
        complaintDao.insertComplaints(complaints.map { it.toEntity() })
        Resource.success(complaints)
    } catch (e: Exception) {
        val cached = complaintDao.getAllComplaints()
        if (cached.isNotEmpty()) Resource.success(cached.map { it.toDomain() }) else Resource.error(e.message ?: "Error", e)
    }

    override fun observeAllComplaints(): Flow<Resource<List<Complaint>>> =
        complaintDao.observeAllComplaints().map { Resource.success(it.map { e -> e.toDomain() }) }

    override suspend fun createComplaint(complaint: Complaint): Resource<Complaint> = try {
        val docRef = complaintsRef.document()
        val newComplaint = complaint.copy(id = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        docRef.set(newComplaint.toFirestoreMap()).await()
        complaintDao.insertComplaint(newComplaint.toEntity())
        Resource.success(newComplaint)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateComplaint(complaint: Complaint): Resource<Complaint> = try {
        val updated = complaint.copy(updatedAt = System.currentTimeMillis())
        complaintsRef.document(complaint.id).set(updated.toFirestoreMap()).await()
        complaintDao.updateComplaint(updated.toEntity())
        Resource.success(updated)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateStatus(complaintId: String, status: ComplaintStatus, resolution: String): Resource<Unit> = try {
        val updates = mutableMapOf<String, Any>("status" to status.name, "updatedAt" to System.currentTimeMillis())
        if (resolution.isNotBlank()) { updates["resolution"] = resolution; updates["resolvedAt"] = System.currentTimeMillis() }
        complaintsRef.document(complaintId).update(updates).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun assignWorker(complaintId: String, workerId: String, workerName: String): Resource<Unit> = try {
        complaintsRef.document(complaintId).update(
            mapOf("assignedWorkerId" to workerId, "assignedWorkerName" to workerName, "status" to ComplaintStatus.IN_PROGRESS.name, "updatedAt" to System.currentTimeMillis())
        ).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun deleteComplaint(complaintId: String): Resource<Unit> = try {
        complaintsRef.document(complaintId).delete().await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }
}

