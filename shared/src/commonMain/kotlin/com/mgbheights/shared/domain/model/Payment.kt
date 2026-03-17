package com.mgbheights.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: String = "",
    val flatId: String = "",
    val flatNumber: String = "",
    val towerBlock: String = "",
    val userId: String = "",
    val userName: String = "",
    val billId: String = "",
    val amount: Double = 0.0,
    val type: PaymentType = PaymentType.MAINTENANCE,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val razorpayOrderId: String = "",
    val razorpayPaymentId: String = "",
    val razorpaySignature: String = "",
    val receiptUrl: String = "",
    val receiptNumber: String = "",
    val description: String = "",
    val isManualEntry: Boolean = false,
    val manualEntryBy: String = "",
    val failureReason: String = "",
    val paidAt: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
enum class PaymentType {
    MAINTENANCE,
    WORKER_PAYMENT,
    SERVICE_CHARGE,
    LATE_FEE,
    OTHER
}

@Serializable
enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}
