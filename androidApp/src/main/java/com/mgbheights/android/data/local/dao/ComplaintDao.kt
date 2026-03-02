package com.mgbheights.android.data.local.dao

import androidx.room.*
import com.mgbheights.android.data.local.entity.ComplaintEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints WHERE id = :complaintId")
    suspend fun getComplaintById(complaintId: String): ComplaintEntity?

    @Query("SELECT * FROM complaints WHERE id = :complaintId")
    fun observeComplaint(complaintId: String): Flow<ComplaintEntity?>

    @Query("SELECT * FROM complaints WHERE flatId = :flatId ORDER BY createdAt DESC")
    suspend fun getComplaintsByFlat(flatId: String): List<ComplaintEntity>

    @Query("SELECT * FROM complaints WHERE flatId = :flatId ORDER BY createdAt DESC")
    fun observeComplaintsByFlat(flatId: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getComplaintsByUser(userId: String): List<ComplaintEntity>

    @Query("SELECT * FROM complaints WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getComplaintsByStatus(status: String): List<ComplaintEntity>

    @Query("SELECT * FROM complaints WHERE status = :status ORDER BY createdAt DESC")
    fun observeComplaintsByStatus(status: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE category = :category ORDER BY createdAt DESC")
    suspend fun getComplaintsByCategory(category: String): List<ComplaintEntity>

    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    suspend fun getAllComplaints(): List<ComplaintEntity>

    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun observeAllComplaints(): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaints(complaints: List<ComplaintEntity>)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)

    @Delete
    suspend fun deleteComplaint(complaint: ComplaintEntity)

    @Query("DELETE FROM complaints")
    suspend fun deleteAll()
}

