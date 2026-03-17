package com.mgbheights.shared.domain.usecase.complaint

import com.mgbheights.shared.domain.model.Complaint
import com.mgbheights.shared.domain.model.ComplaintStatus
import com.mgbheights.shared.domain.repository.ComplaintRepository
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

class GetComplaintsUseCase(private val complaintRepository: ComplaintRepository) {
    suspend fun byId(complaintId: String): Resource<Complaint> {
        return complaintRepository.getComplaintById(complaintId)
    }

    suspend fun byFlat(flatId: String): Resource<List<Complaint>> {
        return complaintRepository.getComplaintsByFlat(flatId)
    }

    fun observeByFlat(flatId: String): Flow<Resource<List<Complaint>>> {
        return complaintRepository.observeComplaintsByFlat(flatId)
    }

    suspend fun byUser(userId: String): Resource<List<Complaint>> {
        return complaintRepository.getComplaintsByUser(userId)
    }

    suspend fun byStatus(status: ComplaintStatus): Resource<List<Complaint>> {
        return complaintRepository.getComplaintsByStatus(status)
    }

    fun observeByStatus(status: ComplaintStatus): Flow<Resource<List<Complaint>>> {
        return complaintRepository.observeComplaintsByStatus(status)
    }

    suspend fun all(): Resource<List<Complaint>> {
        return complaintRepository.getAllComplaints()
    }

    fun observeAll(): Flow<Resource<List<Complaint>>> {
        return complaintRepository.observeAllComplaints()
    }
}
