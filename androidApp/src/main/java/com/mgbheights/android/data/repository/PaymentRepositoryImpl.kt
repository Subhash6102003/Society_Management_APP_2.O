package com.mgbheights.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mgbheights.android.data.local.dao.PaymentDao
import com.mgbheights.android.data.mapper.*
import com.mgbheights.shared.domain.model.Payment
import com.mgbheights.shared.domain.model.PaymentStatus
import com.mgbheights.shared.domain.model.PaymentType
import com.mgbheights.shared.domain.repository.PaymentRepository
import com.mgbheights.shared.domain.repository.PaymentSummary
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
class PaymentRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val paymentDao: PaymentDao
) : PaymentRepository {

    private val paymentsRef = firestore.collection(Constants.COLLECTION_PAYMENTS)

    override suspend fun getPaymentById(paymentId: String): Resource<Payment> = try {
        val doc = paymentsRef.document(paymentId).get().await()
        if (doc.exists()) Resource.success(doc.data!!.toPayment().copy(id = doc.id))
        else Resource.error("Payment not found")
    } catch (e: Exception) {
        val cached = paymentDao.getPaymentById(paymentId)
        if (cached != null) Resource.success(cached.toDomain()) else Resource.error(e.message ?: "Error", e)
    }

    override fun observePayment(paymentId: String): Flow<Resource<Payment>> = callbackFlow {
        val listener = paymentsRef.document(paymentId).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            if (snap != null && snap.exists()) trySend(Resource.success(snap.data!!.toPayment().copy(id = snap.id)))
            else trySend(Resource.error("Payment not found"))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getPaymentsByFlat(flatId: String): Resource<List<Payment>> = try {
        val snap = paymentsRef.whereEqualTo("flatId", flatId).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        val payments = snap.documents.mapNotNull { it.data?.toPayment()?.copy(id = it.id) }
        paymentDao.insertPayments(payments.map { it.toEntity() })
        Resource.success(payments)
    } catch (e: Exception) {
        val cached = paymentDao.getPaymentsByFlat(flatId)
        if (cached.isNotEmpty()) Resource.success(cached.map { it.toDomain() }) else Resource.error(e.message ?: "Error", e)
    }

    override fun observePaymentsByFlat(flatId: String): Flow<Resource<List<Payment>>> =
        paymentDao.observePaymentsByFlat(flatId).map { Resource.success(it.map { e -> e.toDomain() }) }

    override suspend fun getPaymentsByUser(userId: String): Resource<List<Payment>> = try {
        val snap = paymentsRef.whereEqualTo("userId", userId).orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toPayment()?.copy(id = it.id) })
    } catch (e: Exception) {
        val cached = paymentDao.getPaymentsByUser(userId)
        if (cached.isNotEmpty()) Resource.success(cached.map { it.toDomain() }) else Resource.error(e.message ?: "Error", e)
    }

    override fun observePaymentsByUser(userId: String): Flow<Resource<List<Payment>>> =
        paymentDao.observePaymentsByUser(userId).map { Resource.success(it.map { e -> e.toDomain() }) }

    override suspend fun getPaymentsByStatus(status: PaymentStatus): Resource<List<Payment>> = try {
        val snap = paymentsRef.whereEqualTo("status", status.name).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toPayment()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getPaymentsByType(type: PaymentType): Resource<List<Payment>> = try {
        val snap = paymentsRef.whereEqualTo("type", type.name).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toPayment()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getAllPayments(): Resource<List<Payment>> = try {
        val snap = paymentsRef.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
        val payments = snap.documents.mapNotNull { it.data?.toPayment()?.copy(id = it.id) }
        paymentDao.insertPayments(payments.map { it.toEntity() })
        Resource.success(payments)
    } catch (e: Exception) {
        val cached = paymentDao.getAllPayments()
        if (cached.isNotEmpty()) Resource.success(cached.map { it.toDomain() }) else Resource.error(e.message ?: "Error", e)
    }

    override fun observeAllPayments(): Flow<Resource<List<Payment>>> =
        paymentDao.observeAllPayments().map { Resource.success(it.map { e -> e.toDomain() }) }

    override suspend fun createPayment(payment: Payment): Resource<Payment> = try {
        val docRef = paymentsRef.document()
        val newPayment = payment.copy(id = docRef.id, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        docRef.set(newPayment.toFirestoreMap()).await()
        paymentDao.insertPayment(newPayment.toEntity())
        Resource.success(newPayment)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updatePayment(payment: Payment): Resource<Payment> = try {
        val updated = payment.copy(updatedAt = System.currentTimeMillis())
        paymentsRef.document(payment.id).set(updated.toFirestoreMap()).await()
        paymentDao.updatePayment(updated.toEntity())
        Resource.success(updated)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updatePaymentStatus(paymentId: String, status: PaymentStatus, razorpayPaymentId: String, razorpaySignature: String): Resource<Unit> = try {
        val updates = mutableMapOf<String, Any>("status" to status.name, "updatedAt" to System.currentTimeMillis())
        if (razorpayPaymentId.isNotBlank()) updates["razorpayPaymentId"] = razorpayPaymentId
        if (razorpaySignature.isNotBlank()) updates["razorpaySignature"] = razorpaySignature
        paymentsRef.document(paymentId).update(updates).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun createManualPayment(payment: Payment, adminId: String): Resource<Payment> {
        val manualPayment = payment.copy(isManualEntry = true, manualEntryBy = adminId, status = PaymentStatus.SUCCESS)
        return createPayment(manualPayment)
    }

    override suspend fun generateReceipt(paymentId: String): Resource<String> = try {
        // In production, this would generate a PDF via Cloud Functions
        Resource.success("receipt_$paymentId.pdf")
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun retryFailedPayment(paymentId: String): Resource<Payment> = try {
        val result = getPaymentById(paymentId)
        if (result.isSuccess) {
            val payment = result.getOrNull()!!
            updatePaymentStatus(paymentId, PaymentStatus.PENDING)
            Resource.success(payment.copy(status = PaymentStatus.PENDING))
        } else Resource.error("Payment not found")
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getPaymentSummary(flatId: String): Resource<PaymentSummary> = try {
        val snap = paymentsRef.whereEqualTo("flatId", flatId).get().await()
        val payments = snap.documents.mapNotNull { it.data?.toPayment()?.copy(id = it.id) }
        val summary = PaymentSummary(
            totalPaid = payments.filter { it.status == PaymentStatus.SUCCESS }.sumOf { it.amount },
            totalPending = payments.filter { it.status == PaymentStatus.PENDING }.sumOf { it.amount },
            totalOverdue = 0.0,
            lastPaymentDate = payments.filter { it.status == PaymentStatus.SUCCESS }.maxOfOrNull { it.createdAt } ?: 0L,
            paymentCount = payments.count { it.status == PaymentStatus.SUCCESS }
        )
        Resource.success(summary)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }
}


