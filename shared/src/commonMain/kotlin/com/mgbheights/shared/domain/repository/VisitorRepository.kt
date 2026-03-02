package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.domain.model.Visitor
import com.mgbheights.shared.domain.model.VisitorStatus
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

interface VisitorRepository {
    suspend fun getVisitorById(visitorId: String): Resource<Visitor>
    fun observeVisitor(visitorId: String): Flow<Resource<Visitor>>
    suspend fun getVisitorsByFlat(flatId: String): Resource<List<Visitor>>
    fun observeVisitorsByFlat(flatId: String): Flow<Resource<List<Visitor>>>
    suspend fun getVisitorsByGuard(guardId: String): Resource<List<Visitor>>
    suspend fun getActiveVisitors(): Resource<List<Visitor>>
    fun observeActiveVisitors(): Flow<Resource<List<Visitor>>>
    suspend fun getAllVisitors(): Resource<List<Visitor>>
    fun observeAllVisitors(): Flow<Resource<List<Visitor>>>
    suspend fun getTodaysVisitors(): Resource<List<Visitor>>
    fun observeTodaysVisitors(): Flow<Resource<List<Visitor>>>
    suspend fun createVisitor(visitor: Visitor): Resource<Visitor>
    suspend fun updateVisitor(visitor: Visitor): Resource<Visitor>
    suspend fun approveVisitor(visitorId: String, approvedBy: String): Resource<Unit>
    suspend fun denyVisitor(visitorId: String, reason: String): Resource<Unit>
    suspend fun checkInVisitor(visitorId: String): Resource<Unit>
    suspend fun checkOutVisitor(visitorId: String): Resource<Unit>
    suspend fun markAsFrequent(visitorId: String): Resource<Unit>
    suspend fun blacklistVisitor(visitorId: String): Resource<Unit>
    suspend fun unblacklistVisitor(visitorId: String): Resource<Unit>
    suspend fun getFrequentVisitors(flatId: String): Resource<List<Visitor>>
    suspend fun getBlacklistedVisitors(): Resource<List<Visitor>>
    suspend fun getVisitorHistory(flatId: String): Resource<List<Visitor>>
    fun observePendingApprovals(residentId: String): Flow<Resource<List<Visitor>>>
}

