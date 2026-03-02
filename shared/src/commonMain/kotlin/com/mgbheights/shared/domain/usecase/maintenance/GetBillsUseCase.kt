package com.mgbheights.shared.domain.usecase.maintenance

import com.mgbheights.shared.domain.model.MaintenanceBill
import com.mgbheights.shared.domain.repository.MaintenanceRepository
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

class GetBillsUseCase(private val maintenanceRepository: MaintenanceRepository) {
    suspend fun byFlat(flatId: String): Resource<List<MaintenanceBill>> {
        return maintenanceRepository.getBillsByFlat(flatId)
    }

    fun observeByFlat(flatId: String): Flow<Resource<List<MaintenanceBill>>> {
        return maintenanceRepository.observeBillsByFlat(flatId)
    }

    suspend fun all(): Resource<List<MaintenanceBill>> {
        return maintenanceRepository.getAllBills()
    }

    fun observeAll(): Flow<Resource<List<MaintenanceBill>>> {
        return maintenanceRepository.observeAllBills()
    }

    suspend fun byMonth(month: String): Resource<List<MaintenanceBill>> {
        return maintenanceRepository.getBillsByMonth(month)
    }

    suspend fun overdue(): Resource<List<MaintenanceBill>> {
        return maintenanceRepository.getOverdueBills()
    }

    suspend fun ledger(flatId: String): Resource<List<MaintenanceBill>> {
        return maintenanceRepository.getLedgerByFlat(flatId)
    }
}

