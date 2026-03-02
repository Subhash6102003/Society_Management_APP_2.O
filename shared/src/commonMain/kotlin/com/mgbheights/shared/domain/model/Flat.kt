package com.mgbheights.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Flat(
    val id: String = "",
    val houseNumber: String = "",
    val flatNumber: String = "",
    val towerBlock: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerPhone: String = "",
    val tenantId: String = "",
    val tenantName: String = "",
    val tenantPhone: String = "",
    val hasTenant: Boolean = false,
    val assignedWorkers: List<String> = emptyList(),
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
data class EmergencyContact(
    val name: String = "",
    val phone: String = "",
    val relation: String = ""
)

