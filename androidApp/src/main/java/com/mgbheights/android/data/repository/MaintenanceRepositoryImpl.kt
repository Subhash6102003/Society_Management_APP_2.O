package com.mgbheights.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mgbheights.android.data.local.dao.MaintenanceBillDao
import com.mgbheights.android.data.mapper.*
import com.mgbheights.shared.domain.model.BillStatus
import com.mgbheights.shared.domain.model.MaintenanceBill
import com.mgbheights.shared.domain.repository.MaintenanceRepository
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
class MaintenanceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val billDao: MaintenanceBillDao
) : MaintenanceRepository {

    private val billsRef = firestore.collection(Constants.COLLECTION_MAINTENANCE_BILLS)

    override suspend fun getBillById(billId: String): Resource<MaintenanceBill> = try {
        val doc = billsRef.document(billId).get().await()
        if (doc.exists()) {
            val bill = doc.data!!.toMaintenanceBill().copy(id = doc.id)
            billDao.insertBill(bill.toEntity())
            Resource.success(bill)
        } else Resource.error("Bill not found")
    } catch (e: Exception) {
        val cached = billDao.getBillById(billId)
        if (cached != null) Resource.success(cached.toDomain()) else Resource.error(e.message ?: "Error", e)
    }

    override fun observeBill(billId: String): Flow<Resource<MaintenanceBill>> = callbackFlow {
        val listener = billsRef.document(billId).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            if (snap != null && snap.exists()) trySend(Resource.success(snap.data!!.toMaintenanceBill().copy(id = snap.id)))
            else trySend(Resource.error("Bill not found"))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getBillsByFlat(flatId: String): Resource<List<MaintenanceBill>> = try {
        val snap = billsRef.whereEqualTo("flatId", flatId).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        val bills = snap.documents.mapNotNull { it.data?.toMaintenanceBill()?.copy(id = it.id) }
        billDao.insertBills(bills.map { it.toEntity() })
        Resource.success(bills)
    } catch (e: Exception) {
        val cached = billDao.getBillsByFlat(flatId)
        if (cached.isNotEmpty()) Resource.success(cached.map { it.toDomain() }) else Resource.error(e.message ?: "Error", e)
    }

    override fun observeBillsByFlat(flatId: String): Flow<Resource<List<MaintenanceBill>>> = callbackFlow {
        val listener = billsRef.whereEqualTo("flatId", flatId).orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
                val bills = snap?.documents?.mapNotNull { it.data?.toMaintenanceBill()?.copy(id = it.id) } ?: emptyList()
                trySend(Resource.success(bills))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getBillsByStatus(status: BillStatus): Resource<List<MaintenanceBill>> = try {
        val snap = billsRef.whereEqualTo("status", status.name).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toMaintenanceBill()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeBillsByStatus(status: BillStatus): Flow<Resource<List<MaintenanceBill>>> =
        billDao.observeBillsByStatus(status.name).map { entities -> Resource.success(entities.map { it.toDomain() }) }

    override suspend fun getBillsByMonth(month: String): Resource<List<MaintenanceBill>> = try {
        val snap = billsRef.whereEqualTo("month", month).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toMaintenanceBill()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getAllBills(): Resource<List<MaintenanceBill>> = try {
        val snap = billsRef.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        val bills = snap.documents.mapNotNull { it.data?.toMaintenanceBill()?.copy(id = it.id) }
        billDao.insertBills(bills.map { it.toEntity() })
        Resource.success(bills)
    } catch (e: Exception) {
        val cached = billDao.getAllBills()
        if (cached.isNotEmpty()) Resource.success(cached.map { it.toDomain() }) else Resource.error(e.message ?: "Error", e)
    }

    override fun observeAllBills(): Flow<Resource<List<MaintenanceBill>>> =
        billDao.observeAllBills().map { entities -> Resource.success(entities.map { it.toDomain() }) }

    override suspend fun createBill(bill: MaintenanceBill): Resource<MaintenanceBill> = try {
        val docRef = billsRef.document()
        val newBill = bill.copy(id = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        docRef.set(newBill.toFirestoreMap()).await()
        billDao.insertBill(newBill.toEntity())
        Resource.success(newBill)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateBill(bill: MaintenanceBill): Resource<MaintenanceBill> = try {
        val updated = bill.copy(updatedAt = System.currentTimeMillis())
        billsRef.document(bill.id).set(updated.toFirestoreMap()).await()
        billDao.updateBill(updated.toEntity())
        Resource.success(updated)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun markBillPaid(billId: String, paymentId: String): Resource<Unit> = try {
        billsRef.document(billId).update(
            mapOf("status" to BillStatus.PAID.name, "paymentId" to paymentId, "paidAt" to System.currentTimeMillis(), "updatedAt" to System.currentTimeMillis())
        ).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun generateMonthlyBills(month: String, baseAmount: Double): Resource<List<MaintenanceBill>> = try {
        val flatsSnap = firestore.collection(Constants.COLLECTION_FLATS).get().await()
        val bills = mutableListOf<MaintenanceBill>()
        val batch = firestore.batch()
        for (flatDoc in flatsSnap.documents) {
            val flat = flatDoc.data ?: continue
            val docRef = billsRef.document()
            val bill = MaintenanceBill(
                id = docRef.id, flatId = flatDoc.id, flatNumber = flat["flatNumber"] as? String ?: "",
                towerBlock = flat["towerBlock"] as? String ?: "", residentId = flat["ownerId"] as? String ?: "",
                residentName = flat["ownerName"] as? String ?: "", amount = baseAmount, totalAmount = baseAmount,
                month = month, status = BillStatus.PENDING,
                createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
            )
            batch.set(docRef, bill.toFirestoreMap())
            bills.add(bill)
        }
        batch.commit().await()
        billDao.insertBills(bills.map { it.toEntity() })
        Resource.success(bills)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun applyLateFees(): Resource<Int> = try {
        val overdue = billsRef.whereEqualTo("status", BillStatus.PENDING.name)
            .whereLessThan("dueDate", System.currentTimeMillis()).get().await()
        var count = 0
        val batch = firestore.batch()
        for (doc in overdue.documents) {
            val amount = (doc.data?.get("amount") as? Number)?.toDouble() ?: 0.0
            val lateFee = amount * Constants.LATE_FEE_PERCENTAGE
            batch.update(doc.reference, mapOf(
                "status" to BillStatus.OVERDUE.name, "lateFee" to lateFee,
                "totalAmount" to (amount + lateFee), "updatedAt" to System.currentTimeMillis()
            ))
            count++
        }
        batch.commit().await()
        Resource.success(count)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun waiveBill(billId: String): Resource<Unit> = try {
        billsRef.document(billId).update("status", BillStatus.WAIVED.name, "updatedAt", System.currentTimeMillis()).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getOverdueBills(): Resource<List<MaintenanceBill>> = try {
        val snap = billsRef.whereEqualTo("status", BillStatus.OVERDUE.name).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toMaintenanceBill()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getLedgerByFlat(flatId: String): Resource<List<MaintenanceBill>> = getBillsByFlat(flatId)
}

