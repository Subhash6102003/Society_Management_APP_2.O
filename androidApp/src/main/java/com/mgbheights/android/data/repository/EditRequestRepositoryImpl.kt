package com.mgbheights.android.data.repository

import com.mgbheights.android.data.remote.dto.EditRequestDto
import com.mgbheights.android.data.remote.dto.toDto
import com.mgbheights.android.data.remote.dto.toEditRequest
import com.mgbheights.shared.domain.model.EditRequest
import com.mgbheights.shared.domain.model.EditRequestStatus
import com.mgbheights.shared.domain.repository.EditRequestRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditRequestRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : EditRequestRepository {

    private val table = Constants.COLLECTION_EDIT_REQUESTS

    override suspend fun submitEditRequest(editRequest: EditRequest): Resource<EditRequest> = try {
        val request = editRequest.copy(
            id = if (editRequest.id.isBlank()) UUID.randomUUID().toString() else editRequest.id,
            createdAt = System.currentTimeMillis()
        )
        supabase.from(table).insert(request.toDto())
        Resource.success(request)
    } catch (e: Exception) { Resource.error(e.message ?: "Failed to submit edit request", e) }

    override suspend fun getEditRequestsByUser(userId: String): Resource<List<EditRequest>> = try {
        val dtos = supabase.from(table).select { filter { eq("user_id", userId) } }
            .decodeList<EditRequestDto>()
        Resource.success(dtos.map { it.toEditRequest() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeEditRequestsByUser(userId: String): Flow<Resource<List<EditRequest>>> = flow {
        emit(getEditRequestsByUser(userId))
    }

    override suspend fun getPendingEditRequests(): Resource<List<EditRequest>> = try {
        val dtos = supabase.from(table).select { filter { eq("status", EditRequestStatus.PENDING.name) } }
            .decodeList<EditRequestDto>()
        Resource.success(dtos.map { it.toEditRequest() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observePendingEditRequests(): Flow<Resource<List<EditRequest>>> = flow {
        emit(getPendingEditRequests())
    }

    override suspend fun approveEditRequest(requestId: String): Resource<Unit> = try {
        supabase.from(table).update(
            mapOf("status" to EditRequestStatus.APPROVED.name, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", requestId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun rejectEditRequest(requestId: String, notes: String): Resource<Unit> = try {
        supabase.from(table).update(
            mapOf("status" to EditRequestStatus.REJECTED.name, "admin_notes" to notes, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", requestId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }
}
