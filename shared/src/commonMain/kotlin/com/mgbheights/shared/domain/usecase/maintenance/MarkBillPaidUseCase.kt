package com.mgbheights.shared.domain.usecase.maintenance

import com.mgbheights.shared.domain.repository.MaintenanceRepository
import com.mgbheights.shared.util.Resource

class MarkBillPaidUseCase(private val maintenanceRepository: MaintenanceRepository) {
    suspend operator fun invoke(billId: String, paymentId: String): Resource<Unit> {
        if (billId.isBlank()) return Resource.error("Bill ID is required")
        if (paymentId.isBlank()) return Resource.error("Payment ID is required")
        return maintenanceRepository.markBillPaid(billId, paymentId)
    }
}

