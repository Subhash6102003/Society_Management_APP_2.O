package com.mgbheights.shared.domain.usecase.payment

import com.mgbheights.shared.domain.model.Payment
import com.mgbheights.shared.domain.model.PaymentStatus
import com.mgbheights.shared.domain.repository.PaymentRepository
import com.mgbheights.shared.util.Resource

class InitiatePaymentUseCase(private val paymentRepository: PaymentRepository) {
    suspend operator fun invoke(payment: Payment): Resource<Payment> {
        if (payment.amount <= 0) return Resource.error("Amount must be greater than 0")
        if (payment.flatId.isBlank()) return Resource.error("Flat ID is required")
        if (payment.userId.isBlank()) return Resource.error("User ID is required")
        return paymentRepository.createPayment(payment)
    }

    suspend fun updateStatus(
        paymentId: String,
        status: PaymentStatus,
        razorpayPaymentId: String = "",
        razorpaySignature: String = ""
    ): Resource<Unit> {
        return paymentRepository.updatePaymentStatus(paymentId, status, razorpayPaymentId, razorpaySignature)
    }

    suspend fun manualEntry(payment: Payment, adminId: String): Resource<Payment> {
        if (payment.amount <= 0) return Resource.error("Amount must be greater than 0")
        return paymentRepository.createManualPayment(payment, adminId)
    }
}

