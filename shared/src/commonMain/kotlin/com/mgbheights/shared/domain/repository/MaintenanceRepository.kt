package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.domain.model.BillStatus
import com.mgbheights.shared.domain.model.MaintenanceBill
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    suspend fun getBillById(billId: String): Resource<MaintenanceBill>
    fun observeBill(billId: String): Flow<Resource<MaintenanceBill>>
    suspend fun getBillsByFlat(flatId: String): Resource<List<MaintenanceBill>>
    fun observeBillsByFlat(flatId: String): Flow<Resource<List<MaintenanceBill>>>
    suspend fun getBillsByStatus(status: BillStatus): Resource<List<MaintenanceBill>>
    fun observeBillsByStatus(status: BillStatus): Flow<Resource<List<MaintenanceBill>>>
    suspend fun getBillsByMonth(month: String): Resource<List<MaintenanceBill>>
    suspend fun getAllBills(): Resource<List<MaintenanceBill>>
    fun observeAllBills(): Flow<Resource<List<MaintenanceBill>>>
    suspend fun createBill(bill: MaintenanceBill): Resource<MaintenanceBill>
    suspend fun updateBill(bill: MaintenanceBill): Resource<MaintenanceBill>
    suspend fun markBillPaid(billId: String, paymentId: String): Resource<Unit>
    suspend fun generateMonthlyBills(month: String, baseAmount: Double): Resource<List<MaintenanceBill>>
    suspend fun applyLateFees(): Resource<Int>
    suspend fun waiveBill(billId: String): Resource<Unit>
    suspend fun getOverdueBills(): Resource<List<MaintenanceBill>>
    suspend fun getLedgerByFlat(flatId: String): Resource<List<MaintenanceBill>>
}

