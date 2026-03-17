package com.mgbheights.android.data.mapper

import com.google.firebase.Timestamp
import com.mgbheights.shared.domain.model.*

// Helper to handle both Long and Timestamp from Firestore
private fun Any?.toLongTime(): Long {
    return when (this) {
        is Long -> this
        is Timestamp -> this.toDate().time
        is Number -> this.toLong()
        else -> 0L
    }
}

// ======================== USER ========================

private fun parseUserRole(role: String?): UserRole = try {
    UserRole.valueOf(role ?: "RESIDENT")
} catch (_: Exception) {
    UserRole.RESIDENT
}

fun Map<String, Any?>.toUser(): User = User(
    id = this["id"] as? String ?: "",
    phoneNumber = this["phoneNumber"] as? String ?: "",
    name = this["name"] as? String ?: "",
    email = this["email"] as? String ?: "",
    profilePhotoUrl = this["profilePhotoUrl"] as? String ?: "",
    idProofUrl = this["idProofUrl"] as? String ?: "",
    role = parseUserRole(this["role"] as? String),
    flatNumber = this["flatNumber"] as? String ?: "",
    towerBlock = this["towerBlock"] as? String ?: "",
    houseNumber = this["houseNumber"] as? String ?: "",
    isApproved = this["isApproved"] as? Boolean ?: false,
    isBlocked = this["isBlocked"] as? Boolean ?: false,
    isProfileComplete = this["isProfileComplete"] as? Boolean ?: false,
    isOnboarded = this["isOnboarded"] as? Boolean ?: false,
    tenantOf = this["tenantOf"] as? String ?: "",
    createdAt = this["createdAt"].toLongTime(),
    updatedAt = this["updatedAt"].toLongTime()
)

fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "phoneNumber" to phoneNumber,
    "name" to name,
    "email" to email,
    "profilePhotoUrl" to profilePhotoUrl,
    "idProofUrl" to idProofUrl,
    "role" to role.name,
    "flatNumber" to flatNumber,
    "towerBlock" to towerBlock,
    "houseNumber" to houseNumber,
    "isApproved" to isApproved,
    "isBlocked" to isBlocked,
    "isProfileComplete" to isProfileComplete,
    "isOnboarded" to isOnboarded,
    "tenantOf" to tenantOf,
    "createdAt" to createdAt,
    "updatedAt" to updatedAt
)

// ======================== MAINTENANCE BILL ========================

fun Map<String, Any?>.toMaintenanceBill(): MaintenanceBill = MaintenanceBill(
    id = this["id"] as? String ?: "",
    flatId = this["flatId"] as? String ?: "",
    flatNumber = this["flatNumber"] as? String ?: "",
    towerBlock = this["towerBlock"] as? String ?: "",
    residentId = this["residentId"] as? String ?: "",
    residentName = this["residentName"] as? String ?: "",
    amount = (this["amount"] as? Number)?.toDouble() ?: 0.0,
    lateFee = (this["lateFee"] as? Number)?.toDouble() ?: 0.0,
    totalAmount = (this["totalAmount"] as? Number)?.toDouble() ?: 0.0,
    month = this["month"] as? String ?: "",
    year = (this["year"] as? Number)?.toInt() ?: 0,
    dueDate = this["dueDate"].toLongTime(),
    status = try { BillStatus.valueOf(this["status"] as? String ?: "PENDING") } catch (_: Exception) { BillStatus.PENDING },
    description = this["description"] as? String ?: "",
    paymentId = this["paymentId"] as? String ?: "",
    paidAt = this["paidAt"].toLongTime(),
    createdAt = this["createdAt"].toLongTime(),
    updatedAt = this["updatedAt"].toLongTime()
)

fun MaintenanceBill.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id, "flatId" to flatId, "flatNumber" to flatNumber, "towerBlock" to towerBlock,
    "residentId" to residentId, "residentName" to residentName, "amount" to amount,
    "lateFee" to lateFee, "totalAmount" to totalAmount, "month" to month, "year" to year,
    "dueDate" to dueDate, "status" to status.name, "description" to description,
    "paymentId" to paymentId, "paidAt" to paidAt, "createdAt" to createdAt, "updatedAt" to updatedAt
)

// ======================== PAYMENT ========================

fun Map<String, Any?>.toPayment(): Payment = Payment(
    id = this["id"] as? String ?: "",
    flatId = this["flatId"] as? String ?: "",
    flatNumber = this["flatNumber"] as? String ?: "",
    towerBlock = this["towerBlock"] as? String ?: "",
    userId = this["userId"] as? String ?: "",
    userName = this["userName"] as? String ?: "",
    billId = this["billId"] as? String ?: "",
    amount = (this["amount"] as? Number)?.toDouble() ?: 0.0,
    type = try { PaymentType.valueOf(this["type"] as? String ?: "MAINTENANCE") } catch (_: Exception) { PaymentType.MAINTENANCE },
    status = try { PaymentStatus.valueOf(this["status"] as? String ?: "PENDING") } catch (_: Exception) { PaymentStatus.PENDING },
    razorpayOrderId = this["razorpayOrderId"] as? String ?: "",
    razorpayPaymentId = this["razorpayPaymentId"] as? String ?: "",
    receiptUrl = this["receiptUrl"] as? String ?: "",
    receiptNumber = this["receiptNumber"] as? String ?: "",
    description = this["description"] as? String ?: "",
    isManualEntry = this["isManualEntry"] as? Boolean ?: false,
    paidAt = this["paidAt"].toLongTime(),
    createdAt = this["createdAt"].toLongTime(),
    updatedAt = this["updatedAt"].toLongTime()
)

fun Payment.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id, "flatId" to flatId, "flatNumber" to flatNumber, "towerBlock" to towerBlock,
    "userId" to userId, "userName" to userName, "billId" to billId, "amount" to amount,
    "type" to type.name, "status" to status.name, "razorpayOrderId" to razorpayOrderId,
    "razorpayPaymentId" to razorpayPaymentId, "razorpaySignature" to razorpaySignature,
    "receiptUrl" to receiptUrl, "receiptNumber" to receiptNumber, "description" to description,
    "isManualEntry" to isManualEntry, "manualEntryBy" to manualEntryBy,
    "paidAt" to paidAt, "createdAt" to createdAt, "updatedAt" to updatedAt
)

// ======================== NOTICE ========================

fun Map<String, Any?>.toNotice(): Notice = Notice(
    id = this["id"] as? String ?: "",
    title = this["title"] as? String ?: "",
    body = this["body"] as? String ?: "",
    category = try { NoticeCategory.valueOf(this["category"] as? String ?: "GENERAL") } catch (_: Exception) { NoticeCategory.GENERAL },
    priority = try { NoticePriority.valueOf(this["priority"] as? String ?: "NORMAL") } catch (_: Exception) { NoticePriority.NORMAL },
    targetRoles = (this["targetRoles"] as? List<*>)?.mapNotNull {
        try { UserRole.valueOf(it as String) } catch (_: Exception) { null }
    } ?: emptyList(),
    imageUrl = this["imageUrl"] as? String ?: "",
    createdBy = this["createdBy"] as? String ?: "",
    createdByName = this["createdByName"] as? String ?: "",
    isEmergency = this["isEmergency"] as? Boolean ?: false,
    expiresAt = this["expiresAt"].toLongTime(),
    readBy = (this["readBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    createdAt = this["createdAt"].toLongTime(),
    updatedAt = this["updatedAt"].toLongTime()
)

fun Notice.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id, "title" to title, "body" to body, "category" to category.name,
    "priority" to priority.name, "targetRoles" to targetRoles.map { it.name },
    "imageUrl" to imageUrl, "createdBy" to createdBy, "createdByName" to createdByName,
    "isEmergency" to isEmergency, "expiresAt" to expiresAt, "readBy" to readBy,
    "createdAt" to createdAt, "updatedAt" to updatedAt
)

// ======================== COMPLAINT ========================

fun Map<String, Any?>.toComplaint(): Complaint = Complaint(
    id = this["id"] as? String ?: "",
    flatId = this["flatId"] as? String ?: "",
    flatNumber = this["flatNumber"] as? String ?: "",
    towerBlock = this["towerBlock"] as? String ?: "",
    userId = this["userId"] as? String ?: "",
    userName = this["userName"] as? String ?: "",
    title = this["title"] as? String ?: "",
    description = this["description"] as? String ?: "",
    category = try { ComplaintCategory.valueOf(this["category"] as? String ?: "OTHER") } catch (_: Exception) { ComplaintCategory.OTHER },
    status = try { ComplaintStatus.valueOf(this["status"] as? String ?: "OPEN") } catch (_: Exception) { ComplaintStatus.OPEN },
    priority = try { ComplaintPriority.valueOf(this["priority"] as? String ?: "MEDIUM") } catch (_: Exception) { ComplaintPriority.MEDIUM },
    imageUrls = (this["imageUrls"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    assignedWorkerId = this["assignedWorkerId"] as? String ?: "",
    assignedWorkerName = this["assignedWorkerName"] as? String ?: "",
    resolution = this["resolution"] as? String ?: "",
    resolvedAt = this["resolvedAt"].toLongTime(),
    createdAt = this["createdAt"].toLongTime(),
    updatedAt = this["updatedAt"].toLongTime()
)

fun Complaint.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id, "flatId" to flatId, "flatNumber" to flatNumber, "towerBlock" to towerBlock,
    "userId" to userId, "userName" to userName, "title" to title, "description" to description,
    "category" to category.name, "status" to status.name, "priority" to priority.name,
    "imageUrls" to imageUrls, "assignedWorkerId" to assignedWorkerId,
    "assignedWorkerName" to assignedWorkerName, "resolution" to resolution,
    "resolvedAt" to resolvedAt, "createdAt" to createdAt, "updatedAt" to updatedAt
)

// ======================== VISITOR ========================

fun Map<String, Any?>.toVisitor(): Visitor = Visitor(
    id = this["id"] as? String ?: "",
    name = this["name"] as? String ?: "",
    phoneNumber = this["phoneNumber"] as? String ?: "",
    purpose = this["purpose"] as? String ?: "",
    flatId = this["flatId"] as? String ?: "",
    flatNumber = this["flatNumber"] as? String ?: "",
    towerBlock = this["towerBlock"] as? String ?: "",
    residentId = this["residentId"] as? String ?: "",
    residentName = this["residentName"] as? String ?: "",
    guardId = this["guardId"] as? String ?: "",
    guardName = this["guardName"] as? String ?: "",
    photoUrl = this["photoUrl"] as? String ?: "",
    idProofUrl = this["idProofUrl"] as? String ?: "",
    vehicleNumber = this["vehicleNumber"] as? String ?: "",
    vehicleType = try { VehicleType.valueOf(this["vehicleType"] as? String ?: "NONE") } catch (_: Exception) { VehicleType.NONE },
    status = try { VisitorStatus.valueOf(this["status"] as? String ?: "PENDING") } catch (_: Exception) { VisitorStatus.PENDING },
    isFrequentVisitor = this["isFrequentVisitor"] as? Boolean ?: false,
    isBlacklisted = this["isBlacklisted"] as? Boolean ?: false,
    entryTime = this["entryTime"].toLongTime(),
    exitTime = this["exitTime"].toLongTime(),
    approvedAt = this["approvedAt"].toLongTime(),
    createdAt = this["createdAt"].toLongTime(),
    updatedAt = this["updatedAt"].toLongTime()
)

fun Visitor.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id, "name" to name, "phoneNumber" to phoneNumber, "purpose" to purpose,
    "flatId" to flatId, "flatNumber" to flatNumber, "towerBlock" to towerBlock,
    "residentId" to residentId, "residentName" to residentName, "guardId" to guardId,
    "guardName" to guardName, "photoUrl" to photoUrl, "idProofUrl" to idProofUrl,
    "vehicleNumber" to vehicleNumber, "vehicleType" to vehicleType.name, "status" to status.name,
    "isFrequentVisitor" to isFrequentVisitor, "isBlacklisted" to isBlacklisted,
    "entryTime" to entryTime, "exitTime" to exitTime, "approvedAt" to approvedAt,
    "approvedBy" to approvedBy, "denialReason" to denialReason,
    "createdAt" to createdAt, "updatedAt" to updatedAt
)

// ======================== EDIT REQUEST ========================

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.toEditRequest(): EditRequest = EditRequest(
    id = this["id"] as? String ?: "",
    userId = this["userId"] as? String ?: "",
    userName = this["userName"] as? String ?: "",
    userRole = this["userRole"] as? String ?: "",
    requestedChanges = (this["requestedChanges"] as? Map<String, String>) ?: emptyMap(),
    currentValues = (this["currentValues"] as? Map<String, String>) ?: emptyMap(),
    status = try { EditRequestStatus.valueOf(this["status"] as? String ?: "PENDING") } catch (_: Exception) { EditRequestStatus.PENDING },
    adminNote = this["adminNote"] as? String ?: "",
    createdAt = this["createdAt"].toLongTime(),
    resolvedAt = this["resolvedAt"].toLongTime(),
    resolvedBy = this["resolvedBy"] as? String ?: ""
)

fun EditRequest.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id, "userId" to userId, "userName" to userName, "userRole" to userRole,
    "requestedChanges" to requestedChanges, "currentValues" to currentValues,
    "status" to status.name, "adminNote" to adminNote,
    "createdAt" to createdAt, "resolvedAt" to resolvedAt, "resolvedBy" to resolvedBy
)
