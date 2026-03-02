package com.mgbheights.shared.domain.usecase.notice

import com.mgbheights.shared.domain.model.Notice
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.NoticeRepository
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

class GetNoticesUseCase(private val noticeRepository: NoticeRepository) {
    suspend fun byRole(role: UserRole): Resource<List<Notice>> {
        return noticeRepository.getNoticesByRole(role)
    }

    fun observeByRole(role: UserRole): Flow<Resource<List<Notice>>> {
        return noticeRepository.observeNoticesByRole(role)
    }

    suspend fun all(): Resource<List<Notice>> {
        return noticeRepository.getAllNotices()
    }

    fun observeAll(): Flow<Resource<List<Notice>>> {
        return noticeRepository.observeAllNotices()
    }

    suspend fun emergency(): Resource<List<Notice>> {
        return noticeRepository.getEmergencyNotices()
    }

    fun observeEmergency(): Flow<Resource<List<Notice>>> {
        return noticeRepository.observeEmergencyNotices()
    }

    suspend fun unreadCount(userId: String, role: UserRole): Resource<Int> {
        return noticeRepository.getUnreadCount(userId, role)
    }

    suspend fun markRead(noticeId: String, userId: String): Resource<Unit> {
        return noticeRepository.markAsRead(noticeId, userId)
    }
}

