package com.mgbheights.shared.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the admin approval state for a user account.
 * PENDING  → newly registered, awaiting admin review
 * APPROVED → admin has granted access
 * REJECTED → admin has denied access
 */
@Serializable
enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED;

    companion object {
        fun fromString(value: String?): ApprovalStatus = try {
            valueOf(value?.uppercase() ?: "PENDING")
        } catch (_: Exception) {
            PENDING
        }
    }
}

