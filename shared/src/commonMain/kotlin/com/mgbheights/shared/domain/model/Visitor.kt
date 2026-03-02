package com.mgbheights.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Visitor(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val purpose: String = "",
    val flatId: String = "",
    val flatNumber: String = "",
    val towerBlock: String = "",
    val residentId: String = "",
    val residentName: String = "",
    val guardId: String = "",
    val guardName: String = "",
    val photoUrl: String = "",
    val idProofUrl: String = "",
    val vehicleNumber: String = "",
    val vehicleType: VehicleType = VehicleType.NONE,
    val status: VisitorStatus = VisitorStatus.PENDING,
    val isFrequentVisitor: Boolean = false,
    val isBlacklisted: Boolean = false,
    val entryTime: Long = 0L,
    val exitTime: Long = 0L,
    val approvedAt: Long = 0L,
    val approvedBy: String = "",
    val denialReason: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
enum class VisitorStatus {
    PENDING,
    APPROVED,
    DENIED,
    CHECKED_IN,
    CHECKED_OUT,
    EXPIRED
}

@Serializable
enum class VehicleType {
    NONE,
    TWO_WHEELER,
    FOUR_WHEELER,
    AUTO,
    COMMERCIAL,
    OTHER
}

