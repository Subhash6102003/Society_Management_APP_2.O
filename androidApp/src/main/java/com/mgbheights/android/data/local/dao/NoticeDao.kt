package com.mgbheights.android.data.local.dao

import androidx.room.*
import com.mgbheights.android.data.local.entity.NoticeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices WHERE id = :noticeId")
    suspend fun getNoticeById(noticeId: String): NoticeEntity?

    @Query("SELECT * FROM notices WHERE id = :noticeId")
    fun observeNotice(noticeId: String): Flow<NoticeEntity?>

    @Query("SELECT * FROM notices WHERE category = :category ORDER BY createdAt DESC")
    suspend fun getNoticesByCategory(category: String): List<NoticeEntity>

    @Query("SELECT * FROM notices ORDER BY createdAt DESC")
    suspend fun getAllNotices(): List<NoticeEntity>

    @Query("SELECT * FROM notices ORDER BY createdAt DESC")
    fun observeAllNotices(): Flow<List<NoticeEntity>>

    @Query("SELECT * FROM notices WHERE isEmergency = 1 ORDER BY createdAt DESC")
    suspend fun getEmergencyNotices(): List<NoticeEntity>

    @Query("SELECT * FROM notices WHERE isEmergency = 1 ORDER BY createdAt DESC")
    fun observeEmergencyNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Update
    suspend fun updateNotice(notice: NoticeEntity)

    @Delete
    suspend fun deleteNotice(notice: NoticeEntity)

    @Query("DELETE FROM notices")
    suspend fun deleteAll()
}

