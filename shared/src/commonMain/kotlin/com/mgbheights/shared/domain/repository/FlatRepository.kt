package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.domain.model.Flat
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

interface FlatRepository {
    suspend fun getFlatById(flatId: String): Resource<Flat>
    suspend fun getFlatByNumber(flatNumber: String, towerBlock: String): Resource<Flat>
    fun observeFlat(flatId: String): Flow<Resource<Flat>>
    suspend fun createFlat(flat: Flat): Resource<Flat>
    suspend fun updateFlat(flat: Flat): Resource<Flat>
    suspend fun getAllFlats(): Resource<List<Flat>>
    fun observeAllFlats(): Flow<Resource<List<Flat>>>
    suspend fun getFlatsByTower(towerBlock: String): Resource<List<Flat>>
    suspend fun getFlatsByOwner(ownerId: String): Resource<List<Flat>>
    suspend fun assignTenant(flatId: String, tenantId: String, tenantName: String, tenantPhone: String): Resource<Unit>
    suspend fun removeTenant(flatId: String): Resource<Unit>
    suspend fun assignWorker(flatId: String, workerId: String): Resource<Unit>
    suspend fun removeWorker(flatId: String, workerId: String): Resource<Unit>
}

