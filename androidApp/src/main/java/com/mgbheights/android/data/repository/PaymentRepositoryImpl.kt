package com.mgbheights.android.data.repository

import com.mgbheights.android.data.remote.dto.PaymentDto
import com.mgbheights.android.data.remote.dto.toDto
import com.mgbheights.android.data.remote.dto.toPayment
import com.mgbheights.shared.domain.model.Payment
import com.mgbheights.shared.domain.model.PaymentStatus
import com.mgbheights.shared.domain.repository.PaymentRepository
import com.mgbheights.shared.domain.repository.PaymentSummary
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
class PaymentRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : PaymentRepository {

    private val table = Constants.COLLECTION_PAYMENTS

    override suspend fun getPaymentById(paymentId: String): Resource<Payment> = try {
        val dto = supabase.from(table).select { filter { eq("id", paymentId) } }
            .decodeSingleOrNull<PaymentDto>() ?: return Resource.error("Payment not found")
        Resource.success(dto.toPayment())
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observePayment(paymentId: String): Flow<Resource<Payment>> = flow {
        emit(getPaymentById(paymentId))
    }

    override suspend fun getPaymentsByUser(userId: String): Resource<List<Payment>> = try {
        val dtos = supabase.from(table).select { filter { eq("user_id", userId) } }
            .decodeList<PaymentDto>()
        Resource.success(dtos.map { it.toPayment() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observePaymentsByUser(userId: String): Flow<Resource<List<Payment>>> = flow {
        emit(getPaymentsByUser(userId))
    }

    override suspend fun getAllPayments(): Resource<List<Payment>> = try {
        val dtos = supabase.from(table).select().decodeList<PaymentDto>()
        Resource.success(dtos.map { it.toPayment() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun createPayment(payment: Payment): Resource<Payment> = try {
        val newPayment = if (payment.id.isBlank()) payment.copy(id = UUID.randomUUID().toString()) else payment
        supabase.from(table).insert(newPayment.toDto())
        Resource.success(newPayment)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updatePaymentStatus(paymentId: String, status: PaymentStatus): Resource<Unit> = try {
        supabase.from(table).update(
            mapOf("status" to status.name, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", paymentId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getPaymentSummary(userId: String): Resource<PaymentSummary> = try {
        val dtos = supabase.from(table).select { filter { eq("user_id", userId) } }
            .decodeList<PaymentDto>()
        val payments = dtos.map { it.toPayment() }
        val total = payments.sumOf { it.amount }
        val paid = payments.filter { it.status == PaymentStatus.SUCCESS }.sumOf { it.amount }
        Resource.success(PaymentSummary(totalDue = total, totalPaid = paid, outstanding = total - paid))
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }
}
