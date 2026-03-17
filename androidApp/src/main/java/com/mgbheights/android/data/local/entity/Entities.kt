package com.mgbheights.android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val phoneNumber: String = "",
    val name: String = "",
    val email: String = "",
    val profilePhotoUrl: String = "",
    val idProofUrl: String = "",
    val role: String = "RESIDENT",
    val flatNumber: String = "",
    val towerBlock: String = "",
    val houseNumber: String = "",
    val isApproved: Boolean = false,
    val isBlocked: Boolean = false,
    val isProfileComplete: Boolean = false,
    val isOnboarded: Boolean = false,
    val tenantOf: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Entity(tableName = "flats")
data class FlatEntity(
    @PrimaryKey val id: String,
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
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Entity(tableName = "maintenance_bills")
data class MaintenanceBillEntity(
    @PrimaryKey val id: String,
    val flatId: String = "",
    val flatNumber: String = "",
    val towerBlock: String = "",
    val residentId: String = "",
    val residentName: String = "",
    val amount: Double = 0.0,
    val lateFee: Double = 0.0,
    val totalAmount: Double = 0.0,
    val month: String = "",
    val year: Int = 0,
    val dueDate: Long = 0L,
    val status: String = "PENDING",
    val description: String = "",
    val paymentId: String = "",
    val paidAt: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val flatId: String = "",
    val flatNumber: String = "",
    val towerBlock: String = "",
    val userId: String = "",
    val userName: String = "",
    val billId: String = "",
    val amount: Double = 0.0,
    val type: String = "MAINTENANCE",
    val status: String = "PENDING",
    val razorpayOrderId: String = "",
    val razorpayPaymentId: String = "",
    val receiptUrl: String = "",
    val receiptNumber: String = "",
    val description: String = "",
    val isManualEntry: Boolean = false,
    val paidAt: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey val id: String,
    val title: String = "",
    val body: String = "",
    val category: String = "GENERAL",
    val priority: String = "NORMAL",
    val imageUrl: String = "",
    val createdBy: String = "",
    val createdByName: String = "",
    val isEmergency: Boolean = false,
    val expiresAt: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String,
    val flatId: String = "",
    val flatNumber: String = "",
    val towerBlock: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "OTHER",
    val status: String = "OPEN",
    val priority: String = "MEDIUM",
    val assignedWorkerId: String = "",
    val assignedWorkerName: String = "",
    val resolution: String = "",
    val resolvedAt: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Entity(tableName = "visitors")
data class VisitorEntity(
    @PrimaryKey val id: String,
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
    val vehicleType: String = "NONE",
    val status: String = "PENDING",
    val isFrequentVisitor: Boolean = false,
    val isBlacklisted: Boolean = false,
    val entryTime: Long = 0L,
    val exitTime: Long = 0L,
    val approvedAt: Long = 0L,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
