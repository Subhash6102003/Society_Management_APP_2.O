package com.mgbheights.shared.domain.usecase.complaint

import com.mgbheights.shared.domain.model.ComplaintStatus
import com.mgbheights.shared.domain.repository.ComplaintRepository
import com.mgbheights.shared.util.Resource

class UpdateComplaintStatusUseCase(private val complaintRepository: ComplaintRepository) {
    suspend operator fun invoke(
        complaintId: String,
        status: ComplaintStatus,
        resolution: String = ""
    ): Resource<Unit> {
        if (complaintId.isBlank()) return Resource.error("Complaint ID is required")
        if (status == ComplaintStatus.RESOLVED && resolution.isBlank()) {
            return Resource.error("Resolution notes are required when resolving a complaint")
        }
        return complaintRepository.updateStatus(complaintId, status, resolution)
    }

    suspend fun assignWorker(complaintId: String, workerId: String, workerName: String): Resource<Unit> {
        if (complaintId.isBlank()) return Resource.error("Complaint ID is required")
        if (workerId.isBlank()) return Resource.error("Worker must be selected")
        return complaintRepository.assignWorker(complaintId, workerId, workerName)
    }
}

