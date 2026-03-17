package com.mgbheights.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mgbheights.android.data.mapper.*
import com.mgbheights.shared.domain.model.Visitor
import com.mgbheights.shared.domain.model.VisitorStatus
import com.mgbheights.shared.domain.repository.VisitorRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitorRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : VisitorRepository {

    private val visitorsRef = firestore.collection(Constants.COLLECTION_VISITORS)

    override suspend fun getVisitorById(visitorId: String): Resource<Visitor> = try {
        val doc = visitorsRef.document(visitorId).get().await()
        if (doc.exists()) Resource.success(doc.data!!.toVisitor().copy(id = doc.id))
        else Resource.error("Visitor not found")
    } catch (e: Exception) {
        Resource.error(e.message ?: "Error", e)
    }

    override fun observeVisitor(visitorId: String): Flow<Resource<Visitor>> = callbackFlow {
        val listener = visitorsRef.document(visitorId).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            if (snap != null && snap.exists()) trySend(Resource.success(snap.data!!.toVisitor().copy(id = snap.id)))
            else trySend(Resource.error("Not found"))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getVisitorsByFlat(flatId: String): Resource<List<Visitor>> = try {
        val snap = visitorsRef.whereEqualTo("flatId", flatId).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        val visitors = snap.documents.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) }
        Resource.success(visitors)
    } catch (e: Exception) {
        Resource.error(e.message ?: "Error", e)
    }

    override fun observeVisitorsByFlat(flatId: String): Flow<Resource<List<Visitor>>> = callbackFlow {
        val listener = visitorsRef.whereEqualTo("flatId", flatId).orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
                val visitors = snap?.documents?.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) } ?: emptyList()
                trySend(Resource.success(visitors))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getVisitorsByGuard(guardId: String): Resource<List<Visitor>> = try {
        val snap = visitorsRef.whereEqualTo("guardId", guardId).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getActiveVisitors(): Resource<List<Visitor>> = try {
        val snap = visitorsRef.whereIn("status", listOf(VisitorStatus.APPROVED.name, VisitorStatus.CHECKED_IN.name)).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeActiveVisitors(): Flow<Resource<List<Visitor>>> = callbackFlow {
        val listener = visitorsRef.whereIn("status", listOf(VisitorStatus.APPROVED.name, VisitorStatus.CHECKED_IN.name))
            .addSnapshotListener { snap, error ->
                if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
                val visitors = snap?.documents?.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) } ?: emptyList()
                trySend(Resource.success(visitors))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getAllVisitors(): Resource<List<Visitor>> = try {
        val snap = visitorsRef.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        val visitors = snap.documents.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) }
        Resource.success(visitors)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeAllVisitors(): Flow<Resource<List<Visitor>>> = callbackFlow {
        val listener = visitorsRef.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
                val visitors = snap?.documents?.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) } ?: emptyList()
                trySend(Resource.success(visitors))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getTodaysVisitors(): Resource<List<Visitor>> = try {
        val startOfDay = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0)
        }.timeInMillis
        val snap = visitorsRef.whereGreaterThanOrEqualTo("createdAt", startOfDay).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeTodaysVisitors(): Flow<Resource<List<Visitor>>> = callbackFlow {
        val startOfDay = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0)
        }.timeInMillis
        val listener = visitorsRef.whereGreaterThanOrEqualTo("createdAt", startOfDay)
            .orderBy("createdAt", Query.Direction.DESCENDING).addSnapshotListener { snap, error ->
                if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
                trySend(Resource.success(snap?.documents?.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) } ?: emptyList()))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun createVisitor(visitor: Visitor): Resource<Visitor> = try {
        val docRef = visitorsRef.document()
        val newVisitor = visitor.copy(id = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        docRef.set(newVisitor.toFirestoreMap()).await()
        Resource.success(newVisitor)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateVisitor(visitor: Visitor): Resource<Visitor> = try {
        val updated = visitor.copy(updatedAt = System.currentTimeMillis())
        visitorsRef.document(visitor.id).set(updated.toFirestoreMap()).await()
        Resource.success(updated)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun approveVisitor(visitorId: String, approvedBy: String): Resource<Unit> = try {
        visitorsRef.document(visitorId).update(mapOf(
            "status" to VisitorStatus.APPROVED.name, "approvedBy" to approvedBy,
            "approvedAt" to System.currentTimeMillis(), "updatedAt" to System.currentTimeMillis()
        )).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun denyVisitor(visitorId: String, reason: String): Resource<Unit> = try {
        visitorsRef.document(visitorId).update(mapOf(
            "status" to VisitorStatus.DENIED.name, "denialReason" to reason, "updatedAt" to System.currentTimeMillis()
        )).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun checkInVisitor(visitorId: String): Resource<Unit> = try {
        visitorsRef.document(visitorId).update(mapOf(
            "status" to VisitorStatus.CHECKED_IN.name, "entryTime" to System.currentTimeMillis(), "updatedAt" to System.currentTimeMillis()
        )).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun checkOutVisitor(visitorId: String): Resource<Unit> = try {
        visitorsRef.document(visitorId).update(mapOf(
            "status" to VisitorStatus.CHECKED_OUT.name, "exitTime" to System.currentTimeMillis(), "updatedAt" to System.currentTimeMillis()
        )).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun markAsFrequent(visitorId: String): Resource<Unit> = try {
        visitorsRef.document(visitorId).update("isFrequentVisitor", true).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun blacklistVisitor(visitorId: String): Resource<Unit> = try {
        visitorsRef.document(visitorId).update("isBlacklisted", true).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun unblacklistVisitor(visitorId: String): Resource<Unit> = try {
        visitorsRef.document(visitorId).update("isBlacklisted", false).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getFrequentVisitors(flatId: String): Resource<List<Visitor>> = try {
        val snap = visitorsRef.whereEqualTo("flatId", flatId).whereEqualTo("isFrequentVisitor", true).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getBlacklistedVisitors(): Resource<List<Visitor>> = try {
        val snap = visitorsRef.whereEqualTo("isBlacklisted", true).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getVisitorHistory(flatId: String): Resource<List<Visitor>> = getVisitorsByFlat(flatId)

    override fun observePendingApprovals(residentId: String): Flow<Resource<List<Visitor>>> = callbackFlow {
        val listener = visitorsRef.whereEqualTo("residentId", residentId)
            .whereEqualTo("status", VisitorStatus.PENDING.name)
            .addSnapshotListener { snap, error ->
                if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
                val visitors = snap?.documents?.mapNotNull { it.data?.toVisitor()?.copy(id = it.id) } ?: emptyList()
                trySend(Resource.success(visitors))
            }
        awaitClose { listener.remove() }
    }
}
