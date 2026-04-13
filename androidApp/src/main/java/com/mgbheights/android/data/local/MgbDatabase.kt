package com.mgbheights.android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mgbheights.android.data.local.dao.*
import com.mgbheights.android.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        FlatEntity::class,
        MaintenanceBillEntity::class,
        PaymentEntity::class,
        NoticeEntity::class,
        ComplaintEntity::class,
        VisitorEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class MgbDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun maintenanceBillDao(): MaintenanceBillDao
    abstract fun paymentDao(): PaymentDao
    abstract fun noticeDao(): NoticeDao
    abstract fun complaintDao(): ComplaintDao
    abstract fun visitorDao(): VisitorDao
}
