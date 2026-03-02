package com.mgbheights.android.data.local.dao

import androidx.room.*
import com.mgbheights.android.data.local.entity.VisitorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitorDao {
    @Query("SELECT * FROM visitors WHERE id = :visitorId")
    suspend fun getVisitorById(visitorId: String): VisitorEntity?

    @Query("SELECT * FROM visitors WHERE id = :visitorId")
    fun observeVisitor(visitorId: String): Flow<VisitorEntity?>

    @Query("SELECT * FROM visitors WHERE flatId = :flatId ORDER BY createdAt DESC")
    suspend fun getVisitorsByFlat(flatId: String): List<VisitorEntity>

    @Query("SELECT * FROM visitors WHERE flatId = :flatId ORDER BY createdAt DESC")
    fun observeVisitorsByFlat(flatId: String): Flow<List<VisitorEntity>>

    @Query("SELECT * FROM visitors WHERE guardId = :guardId ORDER BY createdAt DESC")
    suspend fun getVisitorsByGuard(guardId: String): List<VisitorEntity>

    @Query("SELECT * FROM visitors WHERE status IN ('APPROVED', 'CHECKED_IN') ORDER BY createdAt DESC")
    suspend fun getActiveVisitors(): List<VisitorEntity>

    @Query("SELECT * FROM visitors WHERE status IN ('APPROVED', 'CHECKED_IN') ORDER BY createdAt DESC")
    fun observeActiveVisitors(): Flow<List<VisitorEntity>>

    @Query("SELECT * FROM visitors ORDER BY createdAt DESC")
    suspend fun getAllVisitors(): List<VisitorEntity>

    @Query("SELECT * FROM visitors ORDER BY createdAt DESC")
    fun observeAllVisitors(): Flow<List<VisitorEntity>>

    @Query("SELECT * FROM visitors WHERE status = 'PENDING' AND residentId = :residentId ORDER BY createdAt DESC")
    fun observePendingForResident(residentId: String): Flow<List<VisitorEntity>>

    @Query("SELECT * FROM visitors WHERE isFrequentVisitor = 1 AND flatId = :flatId")
    suspend fun getFrequentVisitors(flatId: String): List<VisitorEntity>

    @Query("SELECT * FROM visitors WHERE isBlacklisted = 1")
    suspend fun getBlacklistedVisitors(): List<VisitorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitor(visitor: VisitorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisitors(visitors: List<VisitorEntity>)

    @Update
    suspend fun updateVisitor(visitor: VisitorEntity)

    @Query("DELETE FROM visitors")
    suspend fun deleteAll()
}

