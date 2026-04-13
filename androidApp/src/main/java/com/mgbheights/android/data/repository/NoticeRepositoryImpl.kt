package com.mgbheights.android.data.repository

import com.mgbheights.android.data.remote.dto.NoticeDto
import com.mgbheights.android.data.remote.dto.toDto
import com.mgbheights.android.data.remote.dto.toNotice
import com.mgbheights.shared.domain.model.Notice
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.NoticeRepository
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
class NoticeRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : NoticeRepository {

    private val table = Constants.COLLECTION_NOTICES

    override suspend fun getNoticeById(noticeId: String): Resource<Notice> = try {
        val dto = supabase.from(table).select { filter { eq("id", noticeId) } }
            .decodeSingleOrNull<NoticeDto>() ?: return Resource.error("Notice not found")
        Resource.success(dto.toNotice())
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeNotice(noticeId: String): Flow<Resource<Notice>> = flow {
        emit(getNoticeById(noticeId))
    }

    override suspend fun getNoticesByRole(role: UserRole): Resource<List<Notice>> = try {
        // Supabase: filter notices where target_roles array contains the role
        val dtos = supabase.from(table).select { filter { contains("target_roles", listOf(role.name)) } }
            .decodeList<NoticeDto>()
        Resource.success(dtos.map { it.toNotice() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeNoticesByRole(role: UserRole): Flow<Resource<List<Notice>>> = flow {
        emit(getNoticesByRole(role))
    }

    override suspend fun getAllNotices(): Resource<List<Notice>> = try {
        val dtos = supabase.from(table).select().decodeList<NoticeDto>()
        Resource.success(dtos.map { it.toNotice() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeAllNotices(): Flow<Resource<List<Notice>>> = flow {
        emit(getAllNotices())
    }

    override suspend fun createNotice(notice: Notice): Resource<Notice> = try {
        val newNotice = if (notice.id.isBlank()) notice.copy(id = UUID.randomUUID().toString()) else notice
        supabase.from(table).insert(newNotice.toDto())
        Resource.success(newNotice)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun updateNotice(notice: Notice): Resource<Notice> = try {
        val updated = notice.copy(updatedAt = System.currentTimeMillis())
        supabase.from(table).update(updated.toDto()) { filter { eq("id", notice.id) } }
        Resource.success(updated)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun deleteNotice(noticeId: String): Resource<Unit> = try {
        supabase.from(table).delete { filter { eq("id", noticeId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }
}
