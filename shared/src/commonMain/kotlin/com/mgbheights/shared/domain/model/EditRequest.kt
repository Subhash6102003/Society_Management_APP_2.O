package com.mgbheights.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class EditRequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Serializable
data class EditRequest(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userRole: String = "",
    val requestedChanges: Map<String, String> = emptyMap(),
    val currentValues: Map<String, String> = emptyMap(),
    val status: EditRequestStatus = EditRequestStatus.PENDING,
    val adminNote: String = "",
    val createdAt: Long = 0L,
    val resolvedAt: Long = 0L,
    val resolvedBy: String = ""
)

