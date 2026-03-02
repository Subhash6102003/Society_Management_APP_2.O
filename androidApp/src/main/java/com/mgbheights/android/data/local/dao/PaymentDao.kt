package com.mgbheights.android.data.local.dao

import androidx.room.*
import com.mgbheights.android.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE id = :paymentId")
    suspend fun getPaymentById(paymentId: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE id = :paymentId")
    fun observePayment(paymentId: String): Flow<PaymentEntity?>

    @Query("SELECT * FROM payments WHERE flatId = :flatId ORDER BY createdAt DESC")
    suspend fun getPaymentsByFlat(flatId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE flatId = :flatId ORDER BY createdAt DESC")
    fun observePaymentsByFlat(flatId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getPaymentsByUser(userId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY createdAt DESC")
    fun observePaymentsByUser(userId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE status = :status ORDER BY createdAt DESC")
    suspend fun getPaymentsByStatus(status: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE type = :type ORDER BY createdAt DESC")
    suspend fun getPaymentsByType(type: String): List<PaymentEntity>

    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    suspend fun getAllPayments(): List<PaymentEntity>

    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun observeAllPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments")
    suspend fun deleteAll()
}

