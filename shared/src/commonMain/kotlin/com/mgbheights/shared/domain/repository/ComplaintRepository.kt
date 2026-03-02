package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.domain.model.Complaint
import com.mgbheights.shared.domain.model.ComplaintCategory
import com.mgbheights.shared.domain.model.ComplaintStatus
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

interface ComplaintRepository {
    suspend fun getComplaintById(complaintId: String): Resource<Complaint>
    fun observeComplaint(complaintId: String): Flow<Resource<Complaint>>
    suspend fun getComplaintsByFlat(flatId: String): Resource<List<Complaint>>
    fun observeComplaintsByFlat(flatId: String): Flow<Resource<List<Complaint>>>
    suspend fun getComplaintsByUser(userId: String): Resource<List<Complaint>>
    suspend fun getComplaintsByStatus(status: ComplaintStatus): Resource<List<Complaint>>
    fun observeComplaintsByStatus(status: ComplaintStatus): Flow<Resource<List<Complaint>>>
    suspend fun getComplaintsByCategory(category: ComplaintCategory): Resource<List<Complaint>>
    suspend fun getAllComplaints(): Resource<List<Complaint>>
    fun observeAllComplaints(): Flow<Resource<List<Complaint>>>
    suspend fun createComplaint(complaint: Complaint): Resource<Complaint>
    suspend fun updateComplaint(complaint: Complaint): Resource<Complaint>
    suspend fun updateStatus(complaintId: String, status: ComplaintStatus, resolution: String = ""): Resource<Unit>
    suspend fun assignWorker(complaintId: String, workerId: String, workerName: String): Resource<Unit>
    suspend fun deleteComplaint(complaintId: String): Resource<Unit>
}

