package com.mgbheights.shared.domain.usecase.notice

import com.mgbheights.shared.domain.model.Notice
import com.mgbheights.shared.domain.repository.NoticeRepository
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators

class CreateNoticeUseCase(private val noticeRepository: NoticeRepository) {
    suspend operator fun invoke(notice: Notice): Resource<Notice> {
        if (notice.title.isBlank()) return Resource.error("Title is required")
        if (notice.body.isBlank()) return Resource.error("Body is required")
        if (!Validators.isValidDescription(notice.body, 5000)) {
            return Resource.error("Notice body is too long (max 5000 characters)")
        }
        if (notice.targetRoles.isEmpty()) return Resource.error("At least one target role is required")
        return noticeRepository.createNotice(notice)
    }

    suspend fun update(notice: Notice): Resource<Notice> {
        return noticeRepository.updateNotice(notice)
    }

    suspend fun delete(noticeId: String): Resource<Unit> {
        return noticeRepository.deleteNotice(noticeId)
    }
}

