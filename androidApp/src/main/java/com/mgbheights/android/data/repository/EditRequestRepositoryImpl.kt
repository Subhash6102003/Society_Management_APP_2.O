package com.mgbheights.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.data.mapper.toEditRequest
import com.mgbheights.android.data.mapper.toFirestoreMap
import com.mgbheights.android.data.mapper.toUser
import com.mgbheights.shared.domain.model.EditRequest
import com.mgbheights.shared.domain.model.EditRequestStatus
import com.mgbheights.shared.domain.repository.EditRequestRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditRequestRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : EditRequestRepository {

    private val editRequestsRef = firestore.collection(Constants.COLLECTION_EDIT_REQUESTS)
    private val usersRef = firestore.collection(Constants.COLLECTION_USERS)

    override suspend fun submitEditRequest(editRequest: EditRequest): Resource<EditRequest> = try {
        val docRef = editRequestsRef.document()
        val request = editRequest.copy(
            id = docRef.id,
            createdAt = System.currentTimeMillis()
        )
        docRef.set(request.toFirestoreMap()).await()
        Resource.success(request)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to submit edit request", e)
    }

    override suspend fun getEditRequestsByUser(userId: String): Resource<List<EditRequest>> = try {
        val snap = editRequestsRef.whereEqualTo("userId", userId)
            .get().await()
        val requests = snap.documents.mapNotNull { it.data?.toEditRequest()?.copy(id = it.id) }
        Resource.success(requests)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Error", e)
    }

    override suspend fun getPendingEditRequests(): Resource<List<EditRequest>> = try {
        val snap = editRequestsRef.whereEqualTo("status", EditRequestStatus.PENDING.name)
            .get().await()
        val requests = snap.documents.mapNotNull { it.data?.toEditRequest()?.copy(id = it.id) }
        Resource.success(requests)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Error", e)
    }

    override fun observePendingEditRequests(): Flow<Resource<List<EditRequest>>> = callbackFlow {
        val listener = editRequestsRef.whereEqualTo("status", EditRequestStatus.PENDING.name)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    trySend(Resource.error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val requests = snap?.documents?.mapNotNull {
                    it.data?.toEditRequest()?.copy(id = it.id)
                } ?: emptyList()
                trySend(Resource.success(requests))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun approveEditRequest(requestId: String, adminId: String): Resource<Unit> = try {
        // Get the edit request
        val doc = editRequestsRef.document(requestId).get().await()
        val editRequest = doc.data?.toEditRequest()?.copy(id = doc.id)
            ?: return Resource.error("Edit request not found")

        // Apply changes to user document
        val updates = mutableMapOf<String, Any>()
        editRequest.requestedChanges.forEach { (key, value) ->
            updates[key] = value
        }
        updates["updatedAt"] = System.currentTimeMillis()
        usersRef.document(editRequest.userId).update(updates).await()

        // Mark request as approved
        editRequestsRef.document(requestId).update(
            "status", EditRequestStatus.APPROVED.name,
            "resolvedAt", System.currentTimeMillis(),
            "resolvedBy", adminId
        ).await()

        Resource.success(Unit)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to approve edit request", e)
    }

    override suspend fun rejectEditRequest(requestId: String, adminId: String, note: String): Resource<Unit> = try {
        editRequestsRef.document(requestId).update(
            "status", EditRequestStatus.REJECTED.name,
            "adminNote", note,
            "resolvedAt", System.currentTimeMillis(),
            "resolvedBy", adminId
        ).await()
        Resource.success(Unit)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Failed to reject edit request", e)
    }
}

