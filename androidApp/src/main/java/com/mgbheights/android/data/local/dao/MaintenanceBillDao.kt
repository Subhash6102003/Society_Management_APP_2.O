package com.mgbheights.android.data.local.dao

import androidx.room.*
import com.mgbheights.android.data.local.entity.MaintenanceBillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceBillDao {
    @Query("SELECT * FROM maintenance_bills WHERE id = :billId")
    suspend fun getBillById(billId: String): MaintenanceBillEntity?

    @Query("SELECT * FROM maintenance_bills WHERE id = :billId")
    fun observeBill(billId: String): Flow<MaintenanceBillEntity?>

    @Query("SELECT * FROM maintenance_bills WHERE flatId = :flatId ORDER BY createdAt DESC")
    suspend fun getBillsByFlat(flatId: String): List<MaintenanceBillEntity>

    @Query("SELECT * FROM maintenance_bills WHERE flatId = :flatId ORDER BY createdAt DESC")
    fun observeBillsByFlat(flatId: String): Flow<List<MaintenanceBillEntity>>

    @Query("SELECT * FROM maintenance_bills WHERE status = :status ORDER BY dueDate ASC")
    suspend fun getBillsByStatus(status: String): List<MaintenanceBillEntity>

    @Query("SELECT * FROM maintenance_bills WHERE status = :status ORDER BY dueDate ASC")
    fun observeBillsByStatus(status: String): Flow<List<MaintenanceBillEntity>>

    @Query("SELECT * FROM maintenance_bills WHERE month = :month ORDER BY flatNumber ASC")
    suspend fun getBillsByMonth(month: String): List<MaintenanceBillEntity>

    @Query("SELECT * FROM maintenance_bills ORDER BY createdAt DESC")
    suspend fun getAllBills(): List<MaintenanceBillEntity>

    @Query("SELECT * FROM maintenance_bills ORDER BY createdAt DESC")
    fun observeAllBills(): Flow<List<MaintenanceBillEntity>>

    @Query("SELECT * FROM maintenance_bills WHERE status = 'OVERDUE' ORDER BY dueDate ASC")
    suspend fun getOverdueBills(): List<MaintenanceBillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: MaintenanceBillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<MaintenanceBillEntity>)

    @Update
    suspend fun updateBill(bill: MaintenanceBillEntity)

    @Query("DELETE FROM maintenance_bills")
    suspend fun deleteAll()
}

