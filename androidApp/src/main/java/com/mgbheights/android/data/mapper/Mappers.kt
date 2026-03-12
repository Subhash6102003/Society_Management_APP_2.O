package com.mgbheights.android.data.mapper

import com.mgbheights.android.data.local.entity.*
import com.mgbheights.shared.domain.model.*

// ======================== USER ========================

private fun parseUserRole(role: String): UserRole = try {
    UserRole.valueOf(role)
} catch (_: Exception) {
    UserRole.RESIDENT
}

fun UserEntity.toDomain(): User = User(
    id = id,
    phoneNumber = phoneNumber,
    name = name,
    email = email,
    profilePhotoUrl = profilePhotoUrl,
    role = parseUserRole(role),
    flatNumber = flatNumber,
    towerBlock = towerBlock,
    houseNumber = houseNumber,
    isApproved = isApproved,
    isBlocked = isBlocked,
    isProfileComplete = isProfileComplete,
    isOnboarded = isOnboarded,
    tenantOf = tenantOf,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    phoneNumber = phoneNumber,
    name = name,
    email = email,
    profilePhotoUrl = profilePhotoUrl,
    role = role.name,
    flatNumber = flatNumber,
    towerBlock = towerBlock,
    houseNumber = houseNumber,
    isApproved = isApproved,
    isBlocked = isBlocked,
    isProfileComplete = isProfileComplete,
    isOnboarded = isOnboarded,
    tenantOf = tenantOf,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Map<String, Any?>.toUser(): User = User(
    id = this["id"] as? String ?: "",
    phoneNumber = this["phoneNumber"] as? String ?: "",
    name = this["name"] as? String ?: "",
    email = this["email"] as? String ?: "",
    profilePhotoUrl = this["profilePhotoUrl"] as? String ?: "",
    role = parseUserRole(this["role"] as? String ?: "RESIDENT"),
    flatNumber = this["flatNumber"] as? String ?: "",
    towerBlock = this["towerBlock"] as? String ?: "",
    houseNumber = this["houseNumber"] as? String ?: "",
    isApproved = this["isApproved"] as? Boolean ?: false,
    isBlocked = this["isBlocked"] as? Boolean ?: false,
    isProfileComplete = this["isProfileComplete"] as? Boolean ?: false,
    isOnboarded = this["isOnboarded"] as? Boolean ?: false,
    tenantOf = this["tenantOf"] as? String ?: "",
    createdAt = (this["createdAt"] as? Long) ?: 0L,
    updatedAt = (this["updatedAt"] as? Long) ?: 0L
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

fun MaintenanceBillEntity.toDomain(): MaintenanceBill = MaintenanceBill(
    id = id,
    flatId = flatId,
    flatNumber = flatNumber,
    towerBlock = towerBlock,
    residentId = residentId,
    residentName = residentName,
    amount = amount,
    lateFee = lateFee,
    totalAmount = totalAmount,
    month = month,
    year = year,
    dueDate = dueDate,
    status = try { BillStatus.valueOf(status) } catch (_: Exception) { BillStatus.PENDING },
    description = description,
    paymentId = paymentId,
    paidAt = paidAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun MaintenanceBill.toEntity(): MaintenanceBillEntity = MaintenanceBillEntity(
    id = id,
    flatId = flatId,
    flatNumber = flatNumber,
    towerBlock = towerBlock,
    residentId = residentId,
    residentName = residentName,
    amount = amount,
    lateFee = lateFee,
    totalAmount = totalAmount,
    month = month,
    year = year,
    dueDate = dueDate,
    status = status.name,
    description = description,
    paymentId = paymentId,
    paidAt = paidAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

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
    dueDate = (this["dueDate"] as? Long) ?: 0L,
    status = try { BillStatus.valueOf(this["status"] as? String ?: "PENDING") } catch (_: Exception) { BillStatus.PENDING },
    description = this["description"] as? String ?: "",
    paymentId = this["paymentId"] as? String ?: "",
    paidAt = (this["paidAt"] as? Long) ?: 0L,
    createdAt = (this["createdAt"] as? Long) ?: 0L,
    updatedAt = (this["updatedAt"] as? Long) ?: 0L
)

fun MaintenanceBill.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id, "flatId" to flatId, "flatNumber" to flatNumber, "towerBlock" to towerBlock,
    "residentId" to residentId, "residentName" to residentName, "amount" to amount,
    "lateFee" to lateFee, "totalAmount" to totalAmount, "month" to month, "year" to year,
    "dueDate" to dueDate, "status" to status.name, "description" to description,
    "paymentId" to paymentId, "paidAt" to paidAt, "createdAt" to createdAt, "updatedAt" to updatedAt
)

// ======================== PAYMENT ========================

fun PaymentEntity.toDomain(): Payment = Payment(
    id = id, flatId = flatId, flatNumber = flatNumber, towerBlock = towerBlock,
    userId = userId, userName = userName, billId = billId, amount = amount,
    type = try { PaymentType.valueOf(type) } catch (_: Exception) { PaymentType.MAINTENANCE },
    status = try { PaymentStatus.valueOf(status) } catch (_: Exception) { PaymentStatus.PENDING },
    razorpayOrderId = razorpayOrderId, razorpayPaymentId = razorpayPaymentId,
    receiptUrl = receiptUrl, receiptNumber = receiptNumber, description = description,
    isManualEntry = isManualEntry, createdAt = createdAt, updatedAt = updatedAt
)

fun Payment.toEntity(): PaymentEntity = PaymentEntity(
    id = id, flatId = flatId, flatNumber = flatNumber, towerBlock = towerBlock,
    userId = userId, userName = userName, billId = billId, amount = amount,
    type = type.name, status = status.name, razorpayOrderId = razorpayOrderId,
    razorpayPaymentId = razorpayPaymentId, receiptUrl = receiptUrl,
    receiptNumber = receiptNumber, description = description,
    isManualEntry = isManualEntry, createdAt = createdAt, updatedAt = updatedAt
)

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
    createdAt = (this["createdAt"] as? Long) ?: 0L,
    updatedAt = (this["updatedAt"] as? Long) ?: 0L
)

fun Payment.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id, "flatId" to flatId, "flatNumber" to flatNumber, "towerBlock" to towerBlock,
    "userId" to userId, "userName" to userName, "billId" to billId, "amount" to amount,
    "type" to type.name, "status" to status.name, "razorpayOrderId" to razorpayOrderId,
    "razorpayPaymentId" to razorpayPaymentId, "razorpaySignature" to razorpaySignature,
    "receiptUrl" to receiptUrl, "receiptNumber" to receiptNumber, "description" to description,
    "isManualEntry" to isManualEntry, "manualEntryBy" to manualEntryBy,
    "createdAt" to createdAt, "updatedAt" to updatedAt
)

// ======================== NOTICE ========================

fun NoticeEntity.toDomain(): Notice = Notice(
    id = id, title = title, body = body,
    category = try { NoticeCategory.valueOf(category) } catch (_: Exception) { NoticeCategory.GENERAL },
    priority = try { NoticePriority.valueOf(priority) } catch (_: Exception) { NoticePriority.NORMAL },
    imageUrl = imageUrl, createdBy = createdBy, createdByName = createdByName,
    isEmergency = isEmergency, expiresAt = expiresAt, createdAt = createdAt, updatedAt = updatedAt
)

fun Notice.toEntity(): NoticeEntity = NoticeEntity(
    id = id, title = title, body = body, category = category.name, priority = priority.name,
    imageUrl = imageUrl, createdBy = createdBy, createdByName = createdByName,
    isEmergency = isEmergency, expiresAt = expiresAt, createdAt = createdAt, updatedAt = updatedAt
)

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
    expiresAt = (this["expiresAt"] as? Long) ?: 0L,
    readBy = (this["readBy"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
    createdAt = (this["createdAt"] as? Long) ?: 0L,
    updatedAt = (this["updatedAt"] as? Long) ?: 0L
)

fun Notice.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id, "title" to title, "body" to body, "category" to category.name,
    "priority" to priority.name, "targetRoles" to targetRoles.map { it.name },
    "imageUrl" to imageUrl, "createdBy" to createdBy, "createdByName" to createdByName,
    "isEmergency" to isEmergency, "expiresAt" to expiresAt, "readBy" to readBy,
    "createdAt" to createdAt, "updatedAt" to updatedAt
)

// ======================== COMPLAINT ========================

fun ComplaintEntity.toDomain(): Complaint = Complaint(
    id = id, flatId = flatId, flatNumber = flatNumber, towerBlock = towerBlock,
    userId = userId, userName = userName, title = title, description = description,
    category = try { ComplaintCategory.valueOf(category) } catch (_: Exception) { ComplaintCategory.OTHER },
    status = try { ComplaintStatus.valueOf(status) } catch (_: Exception) { ComplaintStatus.OPEN },
    priority = try { ComplaintPriority.valueOf(priority) } catch (_: Exception) { ComplaintPriority.MEDIUM },
    assignedWorkerId = assignedWorkerId, assignedWorkerName = assignedWorkerName,
    resolution = resolution, resolvedAt = resolvedAt, createdAt = createdAt, updatedAt = updatedAt
)

fun Complaint.toEntity(): ComplaintEntity = ComplaintEntity(
    id = id, flatId = flatId, flatNumber = flatNumber, towerBlock = towerBlock,
    userId = userId, userName = userName, title = title, description = description,
    category = category.name, status = status.name, priority = priority.name,
    assignedWorkerId = assignedWorkerId, assignedWorkerName = assignedWorkerName,
    resolution = resolution, resolvedAt = resolvedAt, createdAt = createdAt, updatedAt = updatedAt
)

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
    resolvedAt = (this["resolvedAt"] as? Long) ?: 0L,
    createdAt = (this["createdAt"] as? Long) ?: 0L,
    updatedAt = (this["updatedAt"] as? Long) ?: 0L
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

fun VisitorEntity.toDomain(): Visitor = Visitor(
    id = id, name = name, phoneNumber = phoneNumber, purpose = purpose,
    flatId = flatId, flatNumber = flatNumber, towerBlock = towerBlock,
    residentId = residentId, residentName = residentName, guardId = guardId, guardName = guardName,
    photoUrl = photoUrl, idProofUrl = idProofUrl, vehicleNumber = vehicleNumber,
    vehicleType = try { VehicleType.valueOf(vehicleType) } catch (_: Exception) { VehicleType.NONE },
    status = try { VisitorStatus.valueOf(status) } catch (_: Exception) { VisitorStatus.PENDING },
    isFrequentVisitor = isFrequentVisitor, isBlacklisted = isBlacklisted,
    entryTime = entryTime, exitTime = exitTime, approvedAt = approvedAt,
    createdAt = createdAt, updatedAt = updatedAt
)

fun Visitor.toEntity(): VisitorEntity = VisitorEntity(
    id = id, name = name, phoneNumber = phoneNumber, purpose = purpose,
    flatId = flatId, flatNumber = flatNumber, towerBlock = towerBlock,
    residentId = residentId, residentName = residentName, guardId = guardId, guardName = guardName,
    photoUrl = photoUrl, idProofUrl = idProofUrl, vehicleNumber = vehicleNumber,
    vehicleType = vehicleType.name, status = status.name,
    isFrequentVisitor = isFrequentVisitor, isBlacklisted = isBlacklisted,
    entryTime = entryTime, exitTime = exitTime, approvedAt = approvedAt,
    createdAt = createdAt, updatedAt = updatedAt
)

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
    entryTime = (this["entryTime"] as? Long) ?: 0L,
    exitTime = (this["exitTime"] as? Long) ?: 0L,
    approvedAt = (this["approvedAt"] as? Long) ?: 0L,
    createdAt = (this["createdAt"] as? Long) ?: 0L,
    updatedAt = (this["updatedAt"] as? Long) ?: 0L
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
    createdAt = (this["createdAt"] as? Long) ?: 0L,
    resolvedAt = (this["resolvedAt"] as? Long) ?: 0L,
    resolvedBy = this["resolvedBy"] as? String ?: ""
)

fun EditRequest.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id, "userId" to userId, "userName" to userName, "userRole" to userRole,
    "requestedChanges" to requestedChanges, "currentValues" to currentValues,
    "status" to status.name, "adminNote" to adminNote,
    "createdAt" to createdAt, "resolvedAt" to resolvedAt, "resolvedBy" to resolvedBy
)

