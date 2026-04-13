package com.mgbheights.android.data.repository

import com.mgbheights.android.data.remote.dto.VisitorDto
import com.mgbheights.android.data.remote.dto.toDto
import com.mgbheights.android.data.remote.dto.toVisitor
import com.mgbheights.shared.domain.model.Visitor
import com.mgbheights.shared.domain.model.VisitorStatus
import com.mgbheights.shared.domain.repository.VisitorRepository
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
class VisitorRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : VisitorRepository {

    private val table = Constants.COLLECTION_VISITORS

    override suspend fun getVisitorById(visitorId: String): Resource<Visitor> = try {
        val dto = supabase.from(table).select { filter { eq("id", visitorId) } }
            .decodeSingleOrNull<VisitorDto>() ?: return Resource.error("Visitor not found")
        Resource.success(dto.toVisitor())
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeVisitor(visitorId: String): Flow<Resource<Visitor>> = flow {
        emit(getVisitorById(visitorId))
    }

    override suspend fun getVisitorsByFlat(flatId: String): Resource<List<Visitor>> = try {
        val dtos = supabase.from(table).select { filter { eq("flat_number", flatId) } }
            .decodeList<VisitorDto>()
        Resource.success(dtos.map { it.toVisitor() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeVisitorsByFlat(flatId: String): Flow<Resource<List<Visitor>>> = flow {
        emit(getVisitorsByFlat(flatId))
    }

    override suspend fun getVisitorsByResident(residentId: String): Resource<List<Visitor>> = try {
        val dtos = supabase.from(table).select { filter { eq("resident_id", residentId) } }
            .decodeList<VisitorDto>()
        Resource.success(dtos.map { it.toVisitor() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeVisitorsByResident(residentId: String): Flow<Resource<List<Visitor>>> = flow {
        emit(getVisitorsByResident(residentId))
    }

    override suspend fun getAllVisitors(): Resource<List<Visitor>> = try {
        val dtos = supabase.from(table).select().decodeList<VisitorDto>()
        Resource.success(dtos.map { it.toVisitor() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeAllVisitors(): Flow<Resource<List<Visitor>>> = flow {
        emit(getAllVisitors())
    }

    override suspend fun getPendingVisitors(): Resource<List<Visitor>> = try {
        val dtos = supabase.from(table).select { filter { eq("status", VisitorStatus.PENDING.name) } }
            .decodeList<VisitorDto>()
        Resource.success(dtos.map { it.toVisitor() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observePendingVisitors(): Flow<Resource<List<Visitor>>> = flow {
        emit(getPendingVisitors())
    }

    override suspend fun addVisitor(visitor: Visitor): Resource<Visitor> = try {
        val newVisitor = if (visitor.id.isBlank()) visitor.copy(id = UUID.randomUUID().toString()) else visitor
        supabase.from(table).insert(newVisitor.toDto())
        Resource.success(newVisitor)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun approveVisitor(visitorId: String): Resource<Unit> = try {
        supabase.from(table).update(
            mapOf("status" to VisitorStatus.APPROVED.name, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", visitorId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun denyVisitor(visitorId: String, reason: String): Resource<Unit> = try {
        supabase.from(table).update(
            mapOf("status" to VisitorStatus.DENIED.name, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", visitorId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun checkOutVisitor(visitorId: String): Resource<Unit> = try {
        supabase.from(table).update(
            mapOf("exit_time" to System.currentTimeMillis(), "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", visitorId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }
}
