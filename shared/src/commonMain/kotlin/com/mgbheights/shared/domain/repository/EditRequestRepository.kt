package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.domain.model.EditRequest
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

interface EditRequestRepository {
    suspend fun submitEditRequest(editRequest: EditRequest): Resource<EditRequest>
    suspend fun getEditRequestsByUser(userId: String): Resource<List<EditRequest>>
    suspend fun getPendingEditRequests(): Resource<List<EditRequest>>
    fun observePendingEditRequests(): Flow<Resource<List<EditRequest>>>
    suspend fun approveEditRequest(requestId: String, adminId: String): Resource<Unit>
    suspend fun rejectEditRequest(requestId: String, adminId: String, note: String): Resource<Unit>
}

