package com.mgbheights.shared.domain.usecase.visitor

import com.mgbheights.shared.domain.repository.VisitorRepository
import com.mgbheights.shared.util.Resource

class ApproveVisitorUseCase(private val visitorRepository: VisitorRepository) {
    suspend fun approve(visitorId: String, approvedBy: String): Resource<Unit> {
        if (visitorId.isBlank()) return Resource.error("Visitor ID is required")
        return visitorRepository.approveVisitor(visitorId, approvedBy)
    }

    suspend fun deny(visitorId: String, reason: String): Resource<Unit> {
        if (visitorId.isBlank()) return Resource.error("Visitor ID is required")
        return visitorRepository.denyVisitor(visitorId, reason)
    }

    suspend fun checkIn(visitorId: String): Resource<Unit> {
        return visitorRepository.checkInVisitor(visitorId)
    }

    suspend fun checkOut(visitorId: String): Resource<Unit> {
        return visitorRepository.checkOutVisitor(visitorId)
    }

    suspend fun markFrequent(visitorId: String): Resource<Unit> {
        return visitorRepository.markAsFrequent(visitorId)
    }

    suspend fun blacklist(visitorId: String): Resource<Unit> {
        return visitorRepository.blacklistVisitor(visitorId)
    }

    suspend fun unblacklist(visitorId: String): Resource<Unit> {
        return visitorRepository.unblacklistVisitor(visitorId)
    }
}

