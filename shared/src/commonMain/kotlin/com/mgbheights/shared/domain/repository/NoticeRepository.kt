package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.domain.model.Notice
import com.mgbheights.shared.domain.model.NoticeCategory
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

interface NoticeRepository {
    suspend fun getNoticeById(noticeId: String): Resource<Notice>
    fun observeNotice(noticeId: String): Flow<Resource<Notice>>
    suspend fun getNoticesByRole(role: UserRole): Resource<List<Notice>>
    fun observeNoticesByRole(role: UserRole): Flow<Resource<List<Notice>>>
    suspend fun getNoticesByCategory(category: NoticeCategory): Resource<List<Notice>>
    suspend fun getAllNotices(): Resource<List<Notice>>
    fun observeAllNotices(): Flow<Resource<List<Notice>>>
    suspend fun createNotice(notice: Notice): Resource<Notice>
    suspend fun updateNotice(notice: Notice): Resource<Notice>
    suspend fun deleteNotice(noticeId: String): Resource<Unit>
    suspend fun markAsRead(noticeId: String, userId: String): Resource<Unit>
    suspend fun getUnreadCount(userId: String, role: UserRole): Resource<Int>
    suspend fun getEmergencyNotices(): Resource<List<Notice>>
    fun observeEmergencyNotices(): Flow<Resource<List<Notice>>>
}

