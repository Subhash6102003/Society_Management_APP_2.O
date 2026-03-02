package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.domain.model.Payment
import com.mgbheights.shared.domain.model.PaymentStatus
import com.mgbheights.shared.domain.model.PaymentType
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun getPaymentById(paymentId: String): Resource<Payment>
    fun observePayment(paymentId: String): Flow<Resource<Payment>>
    suspend fun getPaymentsByFlat(flatId: String): Resource<List<Payment>>
    fun observePaymentsByFlat(flatId: String): Flow<Resource<List<Payment>>>
    suspend fun getPaymentsByUser(userId: String): Resource<List<Payment>>
    fun observePaymentsByUser(userId: String): Flow<Resource<List<Payment>>>
    suspend fun getPaymentsByStatus(status: PaymentStatus): Resource<List<Payment>>
    suspend fun getPaymentsByType(type: PaymentType): Resource<List<Payment>>
    suspend fun getAllPayments(): Resource<List<Payment>>
    fun observeAllPayments(): Flow<Resource<List<Payment>>>
    suspend fun createPayment(payment: Payment): Resource<Payment>
    suspend fun updatePayment(payment: Payment): Resource<Payment>
    suspend fun updatePaymentStatus(paymentId: String, status: PaymentStatus, razorpayPaymentId: String = "", razorpaySignature: String = ""): Resource<Unit>
    suspend fun createManualPayment(payment: Payment, adminId: String): Resource<Payment>
    suspend fun generateReceipt(paymentId: String): Resource<String> // returns receipt URL
    suspend fun retryFailedPayment(paymentId: String): Resource<Payment>
    suspend fun getPaymentSummary(flatId: String): Resource<PaymentSummary>
}

data class PaymentSummary(
    val totalPaid: Double = 0.0,
    val totalPending: Double = 0.0,
    val totalOverdue: Double = 0.0,
    val lastPaymentDate: Long = 0L,
    val paymentCount: Int = 0
)

