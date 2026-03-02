package com.mgbheights.shared.domain.usecase.payment

import com.mgbheights.shared.domain.model.Payment
import com.mgbheights.shared.domain.repository.PaymentRepository
import com.mgbheights.shared.domain.repository.PaymentSummary
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

class GetPaymentHistoryUseCase(private val paymentRepository: PaymentRepository) {
    suspend fun byFlat(flatId: String): Resource<List<Payment>> {
        return paymentRepository.getPaymentsByFlat(flatId)
    }

    fun observeByFlat(flatId: String): Flow<Resource<List<Payment>>> {
        return paymentRepository.observePaymentsByFlat(flatId)
    }

    suspend fun byUser(userId: String): Resource<List<Payment>> {
        return paymentRepository.getPaymentsByUser(userId)
    }

    fun observeByUser(userId: String): Flow<Resource<List<Payment>>> {
        return paymentRepository.observePaymentsByUser(userId)
    }

    suspend fun all(): Resource<List<Payment>> {
        return paymentRepository.getAllPayments()
    }

    fun observeAll(): Flow<Resource<List<Payment>>> {
        return paymentRepository.observeAllPayments()
    }

    suspend fun summary(flatId: String): Resource<PaymentSummary> {
        return paymentRepository.getPaymentSummary(flatId)
    }

    suspend fun generateReceipt(paymentId: String): Resource<String> {
        return paymentRepository.generateReceipt(paymentId)
    }
}

