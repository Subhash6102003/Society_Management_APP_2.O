package com.mgbheights.shared.domain.usecase.maintenance

import com.mgbheights.shared.domain.model.MaintenanceBill
import com.mgbheights.shared.domain.repository.MaintenanceRepository
import com.mgbheights.shared.util.Resource

class GenerateBillUseCase(private val maintenanceRepository: MaintenanceRepository) {
    suspend fun single(bill: MaintenanceBill): Resource<MaintenanceBill> {
        if (bill.amount <= 0) return Resource.error("Amount must be greater than 0")
        if (bill.flatId.isBlank()) return Resource.error("Flat must be selected")
        if (bill.month.isBlank()) return Resource.error("Month must be specified")
        return maintenanceRepository.createBill(bill)
    }

    suspend fun monthly(month: String, baseAmount: Double): Resource<List<MaintenanceBill>> {
        if (baseAmount <= 0) return Resource.error("Base amount must be greater than 0")
        if (month.isBlank()) return Resource.error("Month must be specified")
        return maintenanceRepository.generateMonthlyBills(month, baseAmount)
    }
}

