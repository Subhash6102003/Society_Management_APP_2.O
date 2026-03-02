package com.mgbheights.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Complaint(
    val id: String = "",
    val flatId: String = "",
    val flatNumber: String = "",
    val towerBlock: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val description: String = "",
    val category: ComplaintCategory = ComplaintCategory.OTHER,
    val status: ComplaintStatus = ComplaintStatus.OPEN,
    val priority: ComplaintPriority = ComplaintPriority.MEDIUM,
    val imageUrls: List<String> = emptyList(),
    val assignedWorkerId: String = "",
    val assignedWorkerName: String = "",
    val resolution: String = "",
    val resolvedAt: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
enum class ComplaintCategory {
    PLUMBING,
    ELECTRICAL,
    CLEANING,
    SECURITY,
    NOISE,
    STRUCTURAL,
    OTHER
}

@Serializable
enum class ComplaintStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    REJECTED
}

@Serializable
enum class ComplaintPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

