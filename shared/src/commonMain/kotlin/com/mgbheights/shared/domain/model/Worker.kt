package com.mgbheights.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Worker(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val profilePhotoUrl: String = "",
    val idProofUrl: String = "",
    val skills: List<String> = emptyList(),
    val category: WorkerCategory = WorkerCategory.OTHER,
    val isAvailable: Boolean = true,
    val isDutyOn: Boolean = false,
    val rating: Float = 0f,
    val totalRatings: Int = 0,
    val totalJobs: Int = 0,
    val totalEarnings: Double = 0.0,
    val assignedFlats: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
enum class WorkerCategory {
    PLUMBER,
    ELECTRICIAN,
    CARPENTER,
    CLEANER,
    GARDENER,
    PAINTER,
    SECURITY,
    OTHER
}

@Serializable
data class WorkOrder(
    val id: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val flatId: String = "",
    val flatNumber: String = "",
    val towerBlock: String = "",
    val residentId: String = "",
    val residentName: String = "",
    val title: String = "",
    val description: String = "",
    val category: WorkerCategory = WorkerCategory.OTHER,
    val status: WorkOrderStatus = WorkOrderStatus.PENDING,
    val amount: Double = 0.0,
    val isPaid: Boolean = false,
    val paymentId: String = "",
    val rating: Float = 0f,
    val feedback: String = "",
    val scheduledAt: Long = 0L,
    val startedAt: Long = 0L,
    val completedAt: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
enum class WorkOrderStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

