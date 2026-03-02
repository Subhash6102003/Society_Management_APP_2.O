package com.mgbheights.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    ADMIN,
    RESIDENT,
    TENANT,
    SECURITY_GUARD,
    WORKER
}

@Serializable
data class User(
    val id: String = "",
    val phoneNumber: String = "",
    val name: String = "",
    val email: String = "",
    val profilePhotoUrl: String = "",
    val role: UserRole = UserRole.RESIDENT,
    val flatNumber: String = "",
    val towerBlock: String = "",
    val houseNumber: String = "",
    val isApproved: Boolean = false,
    val isBlocked: Boolean = false,
    val isProfileComplete: Boolean = false,
    val isOnboarded: Boolean = false,
    val tenantOf: String = "", // Resident user ID if this user is a tenant
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

