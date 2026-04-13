package com.mgbheights.android.data.repository

import com.mgbheights.android.data.remote.dto.ComplaintDto
import com.mgbheights.android.data.remote.dto.toComplaint
import com.mgbheights.android.data.remote.dto.toDto
import com.mgbheights.shared.domain.model.Complaint
import com.mgbheights.shared.domain.model.ComplaintStatus
import com.mgbheights.shared.domain.repository.ComplaintRepository
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
class ComplaintRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : ComplaintRepository {

    private val table = Constants.COLLECTION_COMPLAINTS

    override suspend fun getComplaintById(complaintId: String): Resource<Complaint> = try {
        val dto = supabase.from(table).select { filter { eq("id", complaintId) } }
            .decodeSingleOrNull<ComplaintDto>() ?: return Resource.error("Complaint not found")
        Resource.success(dto.toComplaint())
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeComplaint(complaintId: String): Flow<Resource<Complaint>> = flow {
        emit(getComplaintById(complaintId))
    }

    override suspend fun getComplaintsByUser(userId: String): Resource<List<Complaint>> = try {
        val dtos = supabase.from(table).select { filter { eq("user_id", userId) } }
            .decodeList<ComplaintDto>()
        Resource.success(dtos.map { it.toComplaint() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeComplaintsByUser(userId: String): Flow<Resource<List<Complaint>>> = flow {
        emit(getComplaintsByUser(userId))
    }

    override suspend fun getAllComplaints(): Resource<List<Complaint>> = try {
        val dtos = supabase.from(table).select().decodeList<ComplaintDto>()
        Resource.success(dtos.map { it.toComplaint() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeAllComplaints(): Flow<Resource<List<Complaint>>> = flow {
        emit(getAllComplaints())
    }

    override suspend fun createComplaint(complaint: Complaint): Resource<Complaint> = try {
        val newComplaint = if (complaint.id.isBlank()) complaint.copy(id = UUID.randomUUID().toString()) else complaint
        supabase.from(table).insert(newComplaint.toDto())
        Resource.success(newComplaint)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateComplaintStatus(complaintId: String, status: ComplaintStatus, adminResponse: String): Resource<Unit> = try {
        supabase.from(table).update(
            mapOf("status" to status.name, "admin_response" to adminResponse, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", complaintId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun deleteComplaint(complaintId: String): Resource<Unit> = try {
        supabase.from(table).delete { filter { eq("id", complaintId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }
}
