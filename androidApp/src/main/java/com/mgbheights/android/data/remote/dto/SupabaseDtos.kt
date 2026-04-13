package com.mgbheights.android.data.remote.dto

import com.mgbheights.shared.domain.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ======================== USER ========================

@Serializable
data class UserDto(
    @SerialName("id") val id: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("phone_number") val phoneNumber: String = "",
    @SerialName("profile_photo_url") val profilePhotoUrl: String = "",
    @SerialName("id_proof_url") val idProofUrl: String = "",
    @SerialName("role") val role: String = "RESIDENT",
    @SerialName("flat_number") val flatNumber: String = "",
    @SerialName("tower_block") val towerBlock: String = "",
    @SerialName("house_number") val houseNumber: String = "",
    /** Replaces legacy is_approved boolean — values: PENDING | APPROVED | REJECTED */
    @SerialName("approval_status") val approvalStatus: String = "PENDING",
    @SerialName("is_blocked") val isBlocked: Boolean = false,
    @SerialName("is_profile_complete") val isProfileComplete: Boolean = false,
    @SerialName("is_onboarded") val isOnboarded: Boolean = false,
    @SerialName("tenant_of") val tenantOf: String = "",
    @SerialName("created_at") val createdAt: Long = 0L,
    @SerialName("updated_at") val updatedAt: Long = 0L
)

fun UserDto.toUser(): User = User(
    id = id, email = email, name = name, phoneNumber = phoneNumber,
    profilePhotoUrl = profilePhotoUrl, idProofUrl = idProofUrl,
    role = try { UserRole.valueOf(role) } catch (_: Exception) { UserRole.RESIDENT },
    flatNumber = flatNumber, towerBlock = towerBlock, houseNumber = houseNumber,
    approvalStatus = ApprovalStatus.fromString(approvalStatus),
    isBlocked = isBlocked, isProfileComplete = isProfileComplete,
    isOnboarded = isOnboarded, tenantOf = tenantOf, createdAt = createdAt, updatedAt = updatedAt
)

fun User.toDto(): UserDto = UserDto(
    id = id, email = email, name = name, phoneNumber = phoneNumber,
    profilePhotoUrl = profilePhotoUrl, idProofUrl = idProofUrl, role = role.name,
    flatNumber = flatNumber, towerBlock = towerBlock, houseNumber = houseNumber,
    approvalStatus = approvalStatus.name,
    isBlocked = isBlocked, isProfileComplete = isProfileComplete,
    isOnboarded = isOnboarded, tenantOf = tenantOf, createdAt = createdAt, updatedAt = updatedAt
)

// ======================== MAINTENANCE BILL ========================

@Serializable
data class MaintenanceBillDto(
    @SerialName("id") val id: String = "",
    @SerialName("flat_id") val flatId: String = "",
    @SerialName("flat_number") val flatNumber: String = "",
    @SerialName("tower_block") val towerBlock: String = "",
    @SerialName("resident_id") val residentId: String = "",
    @SerialName("resident_name") val residentName: String = "",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("late_fee") val lateFee: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double = 0.0,
    @SerialName("month") val month: String = "",
    @SerialName("year") val year: Int = 0,
    @SerialName("due_date") val dueDate: Long = 0L,
    @SerialName("status") val status: String = "PENDING",
    @SerialName("description") val description: String = "",
    @SerialName("paid_at") val paidAt: Long = 0L,
    @SerialName("created_at") val createdAt: Long = 0L,
    @SerialName("updated_at") val updatedAt: Long = 0L
)

fun MaintenanceBillDto.toBill(): MaintenanceBill = MaintenanceBill(
    id = id, flatId = flatId, flatNumber = flatNumber, towerBlock = towerBlock,
    residentId = residentId, residentName = residentName, amount = amount, lateFee = lateFee,
    totalAmount = totalAmount, month = month, year = year, dueDate = dueDate,
    status = try { BillStatus.valueOf(status) } catch (_: Exception) { BillStatus.PENDING },
    description = description, paidAt = paidAt, createdAt = createdAt, updatedAt = updatedAt
)

fun MaintenanceBill.toDto(): MaintenanceBillDto = MaintenanceBillDto(
    id = id, flatId = flatId, flatNumber = flatNumber, towerBlock = towerBlock,
    residentId = residentId, residentName = residentName, amount = amount, lateFee = lateFee,
    totalAmount = totalAmount, month = month, year = year, dueDate = dueDate,
    status = status.name, description = description, paidAt = paidAt,
    createdAt = createdAt, updatedAt = updatedAt
)

// ======================== PAYMENT ========================

@Serializable
data class PaymentDto(
    @SerialName("id") val id: String = "",
    @SerialName("bill_id") val billId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_name") val userName: String = "",
    @SerialName("flat_number") val flatNumber: String = "",
    @SerialName("tower_block") val towerBlock: String = "",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("status") val status: String = "PENDING",
    @SerialName("type") val type: String = "MAINTENANCE",
    @SerialName("razorpay_order_id") val razorpayOrderId: String = "",
    @SerialName("razorpay_payment_id") val razorpayPaymentId: String = "",
    @SerialName("receipt_url") val receiptUrl: String = "",
    @SerialName("created_at") val createdAt: Long = 0L,
    @SerialName("updated_at") val updatedAt: Long = 0L
)

fun PaymentDto.toPayment(): Payment = Payment(
    id = id, billId = billId, userId = userId, userName = userName,
    flatNumber = flatNumber, towerBlock = towerBlock, amount = amount,
    status = try { PaymentStatus.valueOf(status) } catch (_: Exception) { PaymentStatus.PENDING },
    type = try { PaymentType.valueOf(type) } catch (_: Exception) { PaymentType.MAINTENANCE },
    razorpayOrderId = razorpayOrderId, razorpayPaymentId = razorpayPaymentId,
    receiptUrl = receiptUrl, createdAt = createdAt, updatedAt = updatedAt
)

fun Payment.toDto(): PaymentDto = PaymentDto(
    id = id, billId = billId, userId = userId, userName = userName,
    flatNumber = flatNumber, towerBlock = towerBlock, amount = amount,
    status = status.name, type = type.name, razorpayOrderId = razorpayOrderId,
    razorpayPaymentId = razorpayPaymentId, receiptUrl = receiptUrl,
    createdAt = createdAt, updatedAt = updatedAt
)

// ======================== NOTICE ========================
// Supabase column names differ from domain model field names — mapper bridges the gap.

@Serializable
data class NoticeDto(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("content") val content: String = "",          // domain: body
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("author_id") val authorId: String = "",       // domain: createdBy
    @SerialName("author_name") val authorName: String = "",   // domain: createdByName
    @SerialName("category") val category: String = "GENERAL",
    @SerialName("target_roles") val targetRoles: List<String> = emptyList(),
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("created_at") val createdAt: Long = 0L,
    @SerialName("updated_at") val updatedAt: Long = 0L
)

fun NoticeDto.toNotice(): Notice = Notice(
    id = id, title = title,
    body = content,                 // column 'content' → domain field 'body'
    imageUrl = imageUrl,
    createdBy = authorId,           // column 'author_id' → domain field 'createdBy'
    createdByName = authorName,     // column 'author_name' → domain field 'createdByName'
    category = try { NoticeCategory.valueOf(category) } catch (_: Exception) { NoticeCategory.GENERAL },
    targetRoles = targetRoles.mapNotNull { try { UserRole.valueOf(it) } catch (_: Exception) { null } },
    createdAt = createdAt, updatedAt = updatedAt
)

fun Notice.toDto(): NoticeDto = NoticeDto(
    id = id, title = title,
    content = body,                 // domain field 'body' → column 'content'
    imageUrl = imageUrl,
    authorId = createdBy,           // domain field 'createdBy' → column 'author_id'
    authorName = createdByName,     // domain field 'createdByName' → column 'author_name'
    category = category.name,
    targetRoles = targetRoles.map { it.name },
    createdAt = createdAt, updatedAt = updatedAt
)

// ======================== COMPLAINT ========================

@Serializable
data class ComplaintDto(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_name") val userName: String = "",
    @SerialName("flat_number") val flatNumber: String = "",
    @SerialName("tower_block") val towerBlock: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("category") val category: String = "OTHER",
    @SerialName("status") val status: String = "OPEN",
    @SerialName("image_url") val imageUrl: String = "",        // single URL; domain uses List
    @SerialName("admin_response") val adminResponse: String = "", // domain: resolution
    @SerialName("created_at") val createdAt: Long = 0L,
    @SerialName("updated_at") val updatedAt: Long = 0L
)

fun ComplaintDto.toComplaint(): Complaint = Complaint(
    id = id, userId = userId, userName = userName, flatNumber = flatNumber,
    towerBlock = towerBlock, title = title, description = description,
    category = try { ComplaintCategory.valueOf(category) } catch (_: Exception) { ComplaintCategory.OTHER },
    status = try { ComplaintStatus.valueOf(status) } catch (_: Exception) { ComplaintStatus.OPEN },
    imageUrls = if (imageUrl.isNotBlank()) listOf(imageUrl) else emptyList(),
    resolution = adminResponse,
    createdAt = createdAt, updatedAt = updatedAt
)

fun Complaint.toDto(): ComplaintDto = ComplaintDto(
    id = id, userId = userId, userName = userName, flatNumber = flatNumber,
    towerBlock = towerBlock, title = title, description = description,
    category = category.name, status = status.name,
    imageUrl = imageUrls.firstOrNull() ?: "",
    adminResponse = resolution,
    createdAt = createdAt, updatedAt = updatedAt
)

// ======================== VISITOR ========================

@Serializable
data class VisitorDto(
    @SerialName("id") val id: String = "",
    @SerialName("resident_id") val residentId: String = "",
    @SerialName("resident_name") val residentName: String = "",
    @SerialName("flat_number") val flatNumber: String = "",
    @SerialName("tower_block") val towerBlock: String = "",
    @SerialName("visitor_name") val visitorName: String = "",  // domain: name
    @SerialName("visitor_phone") val visitorPhone: String = "", // domain: phoneNumber
    @SerialName("purpose") val purpose: String = "",
    @SerialName("vehicle_number") val vehicleNumber: String = "",
    @SerialName("photo_url") val photoUrl: String = "",
    @SerialName("status") val status: String = "PENDING",
    @SerialName("entry_time") val entryTime: Long = 0L,
    @SerialName("exit_time") val exitTime: Long = 0L,
    @SerialName("created_at") val createdAt: Long = 0L,
    @SerialName("updated_at") val updatedAt: Long = 0L
)

fun VisitorDto.toVisitor(): Visitor = Visitor(
    id = id, residentId = residentId, residentName = residentName,
    flatNumber = flatNumber, towerBlock = towerBlock,
    name = visitorName,             // column 'visitor_name' → domain field 'name'
    phoneNumber = visitorPhone,     // column 'visitor_phone' → domain field 'phoneNumber'
    purpose = purpose, vehicleNumber = vehicleNumber, photoUrl = photoUrl,
    status = try { VisitorStatus.valueOf(status) } catch (_: Exception) { VisitorStatus.PENDING },
    entryTime = entryTime, exitTime = exitTime, createdAt = createdAt, updatedAt = updatedAt
)

fun Visitor.toDto(): VisitorDto = VisitorDto(
    id = id, residentId = residentId, residentName = residentName,
    flatNumber = flatNumber, towerBlock = towerBlock,
    visitorName = name,             // domain field 'name' → column 'visitor_name'
    visitorPhone = phoneNumber,     // domain field 'phoneNumber' → column 'visitor_phone'
    purpose = purpose, vehicleNumber = vehicleNumber, photoUrl = photoUrl,
    status = status.name,
    entryTime = entryTime, exitTime = exitTime, createdAt = createdAt, updatedAt = updatedAt
)

// ======================== FLAT ========================

@Serializable
data class FlatDto(
    @SerialName("id") val id: String = "",
    @SerialName("house_number") val houseNumber: String = "",
    @SerialName("flat_number") val flatNumber: String = "",
    @SerialName("tower_block") val towerBlock: String = "",
    @SerialName("owner_id") val ownerId: String = "",
    @SerialName("owner_name") val ownerName: String = "",
    @SerialName("owner_phone") val ownerPhone: String = "",
    @SerialName("tenant_id") val tenantId: String = "",
    @SerialName("tenant_name") val tenantName: String = "",
    @SerialName("tenant_phone") val tenantPhone: String = "",
    @SerialName("has_tenant") val hasTenant: Boolean = false,
    @SerialName("created_at") val createdAt: Long = 0L,
    @SerialName("updated_at") val updatedAt: Long = 0L
)

fun FlatDto.toFlat(): Flat = Flat(
    id = id, houseNumber = houseNumber, flatNumber = flatNumber, towerBlock = towerBlock,
    ownerId = ownerId, ownerName = ownerName, ownerPhone = ownerPhone,
    tenantId = tenantId, tenantName = tenantName, tenantPhone = tenantPhone,
    hasTenant = hasTenant, createdAt = createdAt, updatedAt = updatedAt
)

fun Flat.toDto(): FlatDto = FlatDto(
    id = id, houseNumber = houseNumber, flatNumber = flatNumber, towerBlock = towerBlock,
    ownerId = ownerId, ownerName = ownerName, ownerPhone = ownerPhone,
    tenantId = tenantId, tenantName = tenantName, tenantPhone = tenantPhone,
    hasTenant = hasTenant, createdAt = createdAt, updatedAt = updatedAt
)

// ======================== EDIT REQUEST ========================

@Serializable
data class EditRequestDto(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_name") val userName: String = "",
    @SerialName("user_role") val userRole: String = "",
    @SerialName("requested_changes") val requestedChanges: Map<String, String> = emptyMap(),
    @SerialName("current_values") val currentValues: Map<String, String> = emptyMap(),
    @SerialName("status") val status: String = "PENDING",
    @SerialName("admin_note") val adminNote: String = "",
    @SerialName("created_at") val createdAt: Long = 0L,
    @SerialName("resolved_at") val resolvedAt: Long = 0L,
    @SerialName("resolved_by") val resolvedBy: String = ""
)

fun EditRequestDto.toEditRequest(): EditRequest = EditRequest(
    id = id, userId = userId, userName = userName, userRole = userRole,
    requestedChanges = requestedChanges, currentValues = currentValues,
    status = try { EditRequestStatus.valueOf(status) } catch (_: Exception) { EditRequestStatus.PENDING },
    adminNote = adminNote, createdAt = createdAt, resolvedAt = resolvedAt, resolvedBy = resolvedBy
)

fun EditRequest.toDto(): EditRequestDto = EditRequestDto(
    id = id, userId = userId, userName = userName, userRole = userRole,
    requestedChanges = requestedChanges, currentValues = currentValues,
    status = status.name, adminNote = adminNote,
    createdAt = createdAt, resolvedAt = resolvedAt, resolvedBy = resolvedBy
)

// ======================== WORKER ========================

@Serializable
data class WorkerDto(
    @SerialName("id") val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("phone_number") val phoneNumber: String = "",
    @SerialName("profile_photo_url") val profilePhotoUrl: String = "",
    @SerialName("id_proof_url") val idProofUrl: String = "",
    @SerialName("skills") val skills: List<String> = emptyList(),
    @SerialName("category") val category: String = "OTHER",
    @SerialName("is_available") val isAvailable: Boolean = true,
    @SerialName("is_duty_on") val isDutyOn: Boolean = false,
    @SerialName("rating") val rating: Float = 0f,
    @SerialName("total_ratings") val totalRatings: Int = 0,
    @SerialName("total_jobs") val totalJobs: Int = 0,
    @SerialName("total_earnings") val totalEarnings: Double = 0.0,
    @SerialName("assigned_flats") val assignedFlats: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: Long = 0L,
    @SerialName("updated_at") val updatedAt: Long = 0L
)

fun WorkerDto.toWorker(): Worker = Worker(
    id = id, userId = userId, name = name, phoneNumber = phoneNumber,
    profilePhotoUrl = profilePhotoUrl, idProofUrl = idProofUrl, skills = skills,
    category = try { WorkerCategory.valueOf(category) } catch (_: Exception) { WorkerCategory.OTHER },
    isAvailable = isAvailable, isDutyOn = isDutyOn, rating = rating, totalRatings = totalRatings,
    totalJobs = totalJobs, totalEarnings = totalEarnings, assignedFlats = assignedFlats,
    createdAt = createdAt, updatedAt = updatedAt
)

fun Worker.toDto(): WorkerDto = WorkerDto(
    id = id, userId = userId, name = name, phoneNumber = phoneNumber,
    profilePhotoUrl = profilePhotoUrl, idProofUrl = idProofUrl, skills = skills,
    category = category.name, isAvailable = isAvailable, isDutyOn = isDutyOn,
    rating = rating, totalRatings = totalRatings, totalJobs = totalJobs,
    totalEarnings = totalEarnings, assignedFlats = assignedFlats,
    createdAt = createdAt, updatedAt = updatedAt
)

@Serializable
data class WorkOrderDto(
    @SerialName("id") val id: String = "",
    @SerialName("worker_id") val workerId: String = "",
    @SerialName("worker_name") val workerName: String = "",
    @SerialName("flat_id") val flatId: String = "",
    @SerialName("flat_number") val flatNumber: String = "",
    @SerialName("tower_block") val towerBlock: String = "",
    @SerialName("resident_id") val residentId: String = "",
    @SerialName("resident_name") val residentName: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("category") val category: String = "OTHER",
    @SerialName("status") val status: String = "PENDING",
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("is_paid") val isPaid: Boolean = false,
    @SerialName("payment_id") val paymentId: String = "",
    @SerialName("rating") val rating: Float = 0f,
    @SerialName("feedback") val feedback: String = "",
    @SerialName("scheduled_at") val scheduledAt: Long = 0L,
    @SerialName("started_at") val startedAt: Long = 0L,
    @SerialName("completed_at") val completedAt: Long = 0L,
    @SerialName("created_at") val createdAt: Long = 0L,
    @SerialName("updated_at") val updatedAt: Long = 0L
)

fun WorkOrderDto.toWorkOrder(): WorkOrder = WorkOrder(
    id = id, workerId = workerId, workerName = workerName, flatId = flatId,
    flatNumber = flatNumber, towerBlock = towerBlock, residentId = residentId,
    residentName = residentName, title = title, description = description,
    category = try { WorkerCategory.valueOf(category) } catch (_: Exception) { WorkerCategory.OTHER },
    status = try { WorkOrderStatus.valueOf(status) } catch (_: Exception) { WorkOrderStatus.PENDING },
    amount = amount, isPaid = isPaid, paymentId = paymentId, rating = rating,
    feedback = feedback, scheduledAt = scheduledAt, startedAt = startedAt,
    completedAt = completedAt, createdAt = createdAt, updatedAt = updatedAt
)

fun WorkOrder.toDto(): WorkOrderDto = WorkOrderDto(
    id = id, workerId = workerId, workerName = workerName, flatId = flatId,
    flatNumber = flatNumber, towerBlock = towerBlock, residentId = residentId,
    residentName = residentName, title = title, description = description,
    category = category.name, status = status.name, amount = amount, isPaid = isPaid,
    paymentId = paymentId, rating = rating, feedback = feedback,
    scheduledAt = scheduledAt, startedAt = startedAt, completedAt = completedAt,
    createdAt = createdAt, updatedAt = updatedAt
)

