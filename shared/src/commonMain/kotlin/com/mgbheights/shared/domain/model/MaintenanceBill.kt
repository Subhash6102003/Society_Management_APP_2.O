package com.mgbheights.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceBill(
    val id: String = "",
    val flatId: String = "",
    val flatNumber: String = "",
    val towerBlock: String = "",
    val residentId: String = "",
    val residentName: String = "",
    val amount: Double = 0.0,
    val lateFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val month: String = "",       // e.g., "2026-02"
    val year: Int = 0,
    val dueDate: Long = 0L,
    val status: BillStatus = BillStatus.PENDING,
    val description: String = "",
    val lineItems: List<BillLineItem> = emptyList(),
    val paymentId: String = "",
    val paidAt: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class BillLineItem(
    val description: String = "",
    val amount: Double = 0.0
)

@Serializable
enum class BillStatus {
    PENDING,
    PAID,
    OVERDUE,
    PARTIALLY_PAID,
    WAIVED
}

