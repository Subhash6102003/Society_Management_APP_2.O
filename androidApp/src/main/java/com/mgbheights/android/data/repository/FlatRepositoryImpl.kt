package com.mgbheights.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.data.mapper.*
import com.mgbheights.shared.domain.model.Flat
import com.mgbheights.shared.domain.repository.FlatRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FlatRepository {

    private val flatsRef = firestore.collection(Constants.COLLECTION_FLATS)

    override suspend fun getFlatById(flatId: String): Resource<Flat> = try {
        val doc = flatsRef.document(flatId).get().await()
        if (doc.exists()) Resource.success(doc.data!!.toFlat().copy(id = doc.id))
        else Resource.error("Flat not found")
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getFlatByNumber(flatNumber: String, towerBlock: String): Resource<Flat> = try {
        val snap = flatsRef.whereEqualTo("flatNumber", flatNumber).whereEqualTo("towerBlock", towerBlock).limit(1).get().await()
        if (snap.documents.isNotEmpty()) Resource.success(snap.documents[0].data!!.toFlat().copy(id = snap.documents[0].id))
        else Resource.error("Flat not found")
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeFlat(flatId: String): Flow<Resource<Flat>> = callbackFlow {
        val listener = flatsRef.document(flatId).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            if (snap != null && snap.exists()) trySend(Resource.success(snap.data!!.toFlat().copy(id = snap.id)))
            else trySend(Resource.error("Flat not found"))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun createFlat(flat: Flat): Resource<Flat> = try {
        val docRef = flatsRef.document()
        val newFlat = flat.copy(id = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        docRef.set(newFlat.toFirestoreMap()).await()
        Resource.success(newFlat)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateFlat(flat: Flat): Resource<Flat> = try {
        val updated = flat.copy(updatedAt = System.currentTimeMillis())
        flatsRef.document(flat.id).set(updated.toFirestoreMap()).await()
        Resource.success(updated)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getAllFlats(): Resource<List<Flat>> = try {
        val snap = flatsRef.orderBy("flatNumber").get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toFlat()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeAllFlats(): Flow<Resource<List<Flat>>> = callbackFlow {
        val listener = flatsRef.orderBy("flatNumber").addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            val flats = snap?.documents?.mapNotNull { it.data?.toFlat()?.copy(id = it.id) } ?: emptyList()
            trySend(Resource.success(flats))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getFlatsByTower(towerBlock: String): Resource<List<Flat>> = try {
        val snap = flatsRef.whereEqualTo("towerBlock", towerBlock).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toFlat()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getFlatsByOwner(ownerId: String): Resource<List<Flat>> = try {
        val snap = flatsRef.whereEqualTo("ownerId", ownerId).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toFlat()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun assignTenant(flatId: String, tenantId: String, tenantName: String, tenantPhone: String): Resource<Unit> = try {
        flatsRef.document(flatId).update(
            mapOf("tenantId" to tenantId, "tenantName" to tenantName, "tenantPhone" to tenantPhone, "hasTenant" to true, "updatedAt" to System.currentTimeMillis())
        ).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun removeTenant(flatId: String): Resource<Unit> = try {
        flatsRef.document(flatId).update(
            mapOf("tenantId" to "", "tenantName" to "", "tenantPhone" to "", "hasTenant" to false, "updatedAt" to System.currentTimeMillis())
        ).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun assignWorker(flatId: String, workerId: String): Resource<Unit> = try {
        flatsRef.document(flatId).update("assignedWorkers", com.google.firebase.firestore.FieldValue.arrayUnion(workerId)).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun removeWorker(flatId: String, workerId: String): Resource<Unit> = try {
        flatsRef.document(flatId).update("assignedWorkers", com.google.firebase.firestore.FieldValue.arrayRemove(workerId)).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    private fun Map<String, Any?>.toFlat(): Flat = Flat(
        id = this["id"] as? String ?: "",
        houseNumber = this["houseNumber"] as? String ?: "",
        flatNumber = this["flatNumber"] as? String ?: "",
        towerBlock = this["towerBlock"] as? String ?: "",
        ownerId = this["ownerId"] as? String ?: "",
        ownerName = this["ownerName"] as? String ?: "",
        ownerPhone = this["ownerPhone"] as? String ?: "",
        tenantId = this["tenantId"] as? String ?: "",
        tenantName = this["tenantName"] as? String ?: "",
        tenantPhone = this["tenantPhone"] as? String ?: "",
        hasTenant = this["hasTenant"] as? Boolean ?: false,
        assignedWorkers = (this["assignedWorkers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        createdAt = (this["createdAt"] as? Long) ?: 0L,
        updatedAt = (this["updatedAt"] as? Long) ?: 0L
    )

    private fun Flat.toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id, "houseNumber" to houseNumber, "flatNumber" to flatNumber, "towerBlock" to towerBlock,
        "ownerId" to ownerId, "ownerName" to ownerName, "ownerPhone" to ownerPhone,
        "tenantId" to tenantId, "tenantName" to tenantName, "tenantPhone" to tenantPhone,
        "hasTenant" to hasTenant, "assignedWorkers" to assignedWorkers,
        "createdAt" to createdAt, "updatedAt" to updatedAt
    )
}

