package com.mgbheights.shared.util

object Constants {
    // App
    const val APP_NAME = "MGB Heights"
    const val SOCIETY_NAME = "MGB Heights"

    // Admin
    const val ADMIN_DEFAULT_EMAIL = "admin@mgbheights.com"

    // Supabase Tables
    const val COLLECTION_USERS = "users"
    const val COLLECTION_FLATS = "flats"
    const val COLLECTION_MAINTENANCE_BILLS = "maintenance_bills"
    const val COLLECTION_PAYMENTS = "payments"
    const val COLLECTION_NOTICES = "notices"
    const val COLLECTION_COMPLAINTS = "complaints"
    const val COLLECTION_VISITORS = "visitors"
    const val COLLECTION_WORKERS = "workers"
    const val COLLECTION_WORK_ORDERS = "work_orders"
    const val COLLECTION_SECURITY_LOGS = "security_logs"
    const val COLLECTION_AUDIT_LOGS = "audit_logs"
    const val COLLECTION_EDIT_REQUESTS = "edit_requests"

    // Supabase Storage Buckets
    const val STORAGE_PROFILE_PHOTOS = "profile_photos"
    const val STORAGE_VISITOR_PHOTOS = "visitor_photos"
    const val STORAGE_ID_PROOFS = "id_proofs"
    const val STORAGE_NOTICE_IMAGES = "notice_images"
    const val STORAGE_COMPLAINT_IMAGES = "complaint_images"
    const val STORAGE_RECEIPTS = "receipts"

    // Pagination
    const val PAGE_SIZE = 20
    const val INITIAL_LOAD_SIZE = 40

    // Late Fee
    const val LATE_FEE_PERCENTAGE = 0.02 // 2%
    const val GRACE_PERIOD_DAYS = 15

    // Maintenance
    const val DEFAULT_MONTHLY_AMOUNT = 5000.0 // Default ₹5000/month

    // Visitor
    const val VISITOR_APPROVAL_TIMEOUT_MINUTES = 30L
    const val MAX_VISITOR_PHOTOS = 3

    // Validation
    const val MIN_NAME_LENGTH = 2
    const val MAX_NAME_LENGTH = 100
    const val PHONE_NUMBER_LENGTH = 10
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_COMPLAINT_DESCRIPTION_LENGTH = 1000
    const val MAX_NOTICE_BODY_LENGTH = 5000
}
