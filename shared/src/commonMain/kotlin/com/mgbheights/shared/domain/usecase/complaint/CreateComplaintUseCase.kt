package com.mgbheights.shared.domain.usecase.complaint

import com.mgbheights.shared.domain.model.Complaint
import com.mgbheights.shared.domain.repository.ComplaintRepository
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators

class CreateComplaintUseCase(private val complaintRepository: ComplaintRepository) {
    suspend operator fun invoke(complaint: Complaint): Resource<Complaint> {
        if (complaint.title.isBlank()) return Resource.error("Title is required")
        if (complaint.description.isBlank()) return Resource.error("Description is required")
        if (!Validators.isValidDescription(complaint.description)) {
            return Resource.error("Description is too long (max 1000 characters)")
        }
        if (complaint.flatId.isBlank()) return Resource.error("Flat must be selected")
        return complaintRepository.createComplaint(complaint)
    }
}

