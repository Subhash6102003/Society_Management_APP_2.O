package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserById(userId: String): Resource<User>
    suspend fun getUserByPhone(phoneNumber: String): Resource<User>
    suspend fun getUserByEmail(email: String): Resource<User>
    fun observeUser(userId: String): Flow<Resource<User>>
    suspend fun createUser(user: User): Resource<User>
    suspend fun updateUser(user: User): Resource<User>
    suspend fun updateProfilePhoto(userId: String, photoUrl: String): Resource<Unit>
    suspend fun approveUser(userId: String): Resource<Unit>
    suspend fun rejectUser(userId: String): Resource<Unit>
    suspend fun blockUser(userId: String): Resource<Unit>
    suspend fun unblockUser(userId: String): Resource<Unit>
    suspend fun deleteUser(userId: String): Resource<Unit>
    suspend fun getUsersByRole(role: UserRole): Resource<List<User>>
    fun observeUsersByRole(role: UserRole): Flow<Resource<List<User>>>
    suspend fun getPendingApprovals(): Resource<List<User>>
    fun observePendingApprovals(): Flow<Resource<List<User>>>
    suspend fun getTenantsByResident(residentId: String): Resource<List<User>>
    suspend fun getAllUsers(): Resource<List<User>>
    fun observeAllUsers(): Flow<Resource<List<User>>>
}
