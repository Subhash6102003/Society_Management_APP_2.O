package com.mgbheights.shared.domain.usecase.visitor

import com.mgbheights.shared.domain.model.Visitor
import com.mgbheights.shared.domain.repository.VisitorRepository
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

class GetVisitorsUseCase(private val visitorRepository: VisitorRepository) {
    suspend fun byFlat(flatId: String): Resource<List<Visitor>> {
        return visitorRepository.getVisitorsByFlat(flatId)
    }

    fun observeByFlat(flatId: String): Flow<Resource<List<Visitor>>> {
        return visitorRepository.observeVisitorsByFlat(flatId)
    }

    suspend fun active(): Resource<List<Visitor>> {
        return visitorRepository.getActiveVisitors()
    }

    fun observeActive(): Flow<Resource<List<Visitor>>> {
        return visitorRepository.observeActiveVisitors()
    }

    suspend fun today(): Resource<List<Visitor>> {
        return visitorRepository.getTodaysVisitors()
    }

    fun observeToday(): Flow<Resource<List<Visitor>>> {
        return visitorRepository.observeTodaysVisitors()
    }

    suspend fun all(): Resource<List<Visitor>> {
        return visitorRepository.getAllVisitors()
    }

    suspend fun history(flatId: String): Resource<List<Visitor>> {
        return visitorRepository.getVisitorHistory(flatId)
    }

    suspend fun frequent(flatId: String): Resource<List<Visitor>> {
        return visitorRepository.getFrequentVisitors(flatId)
    }

    suspend fun blacklisted(): Resource<List<Visitor>> {
        return visitorRepository.getBlacklistedVisitors()
    }

    fun observePendingForResident(residentId: String): Flow<Resource<List<Visitor>>> {
        return visitorRepository.observePendingApprovals(residentId)
    }
}

