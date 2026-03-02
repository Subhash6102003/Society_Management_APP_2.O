package com.mgbheights.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Notice(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val category: NoticeCategory = NoticeCategory.GENERAL,
    val priority: NoticePriority = NoticePriority.NORMAL,
    val targetRoles: List<UserRole> = emptyList(),
    val imageUrl: String = "",
    val createdBy: String = "",
    val createdByName: String = "",
    val isEmergency: Boolean = false,
    val expiresAt: Long = 0L,
    val readBy: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
enum class NoticeCategory {
    GENERAL,
    MAINTENANCE,
    SECURITY,
    EVENT,
    EMERGENCY,
    RULE,
    OTHER
}

@Serializable
enum class NoticePriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

