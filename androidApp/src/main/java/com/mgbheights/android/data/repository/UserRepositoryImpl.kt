package com.mgbheights.android.data.repository

import com.mgbheights.android.data.remote.dto.UserDto
import com.mgbheights.android.data.remote.dto.toDto
import com.mgbheights.android.data.remote.dto.toUser
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.UserRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : UserRepository {

    override suspend fun getUserById(userId: String): Resource<User> = try {
        val dto = supabase.from(Constants.COLLECTION_USERS)
            .select { filter { eq("id", userId) } }
            .decodeSingleOrNull<UserDto>()
            ?: return Resource.error("User not found")
        Resource.success(dto.toUser())
    } catch (e: Exception) { Resource.error(e.message ?: "Failed to fetch user", e) }

    override suspend fun getUserByPhone(phoneNumber: String): Resource<User> = try {
        val dto = supabase.from(Constants.COLLECTION_USERS)
            .select { filter { eq("phone_number", phoneNumber) } }
            .decodeSingleOrNull<UserDto>()
            ?: return Resource.error("User not found")
        Resource.success(dto.toUser())
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getUserByEmail(email: String): Resource<User> = try {
        val dto = supabase.from(Constants.COLLECTION_USERS)
            .select { filter { eq("email", email) } }
            .decodeSingleOrNull<UserDto>()
            ?: return Resource.error("User not found")
        Resource.success(dto.toUser())
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeUser(userId: String): Flow<Resource<User>> = flow {
        emit(getUserById(userId))
    }

    override suspend fun createUser(user: User): Resource<User> = try {
        supabase.from(Constants.COLLECTION_USERS).insert(user.toDto())
        Resource.success(user)
    } catch (e: Exception) { Resource.error(e.message ?: "Failed to create user", e) }

    override suspend fun updateUser(user: User): Resource<User> = try {
        val updated = user.copy(updatedAt = System.currentTimeMillis())
        supabase.from(Constants.COLLECTION_USERS).update(updated.toDto()) {
            filter { eq("id", user.id) }
        }
        Resource.success(updated)
    } catch (e: Exception) { Resource.error(e.message ?: "Failed to update user", e) }

    override suspend fun updateProfilePhoto(userId: String, photoUrl: String): Resource<Unit> = try {
        supabase.from(Constants.COLLECTION_USERS).update(
            mapOf("profile_photo_url" to photoUrl, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", userId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun approveUser(userId: String): Resource<Unit> = try {
        supabase.from(Constants.COLLECTION_USERS).update(
            mapOf("approval_status" to "APPROVED", "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", userId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun rejectUser(userId: String): Resource<Unit> = try {
        supabase.from(Constants.COLLECTION_USERS).update(
            mapOf("approval_status" to "REJECTED", "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", userId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun blockUser(userId: String): Resource<Unit> = try {
        supabase.from(Constants.COLLECTION_USERS).update(
            mapOf("is_blocked" to true, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", userId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun unblockUser(userId: String): Resource<Unit> = try {
        supabase.from(Constants.COLLECTION_USERS).update(
            mapOf("is_blocked" to false, "updated_at" to System.currentTimeMillis())
        ) { filter { eq("id", userId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun deleteUser(userId: String): Resource<Unit> = try {
        supabase.from(Constants.COLLECTION_USERS).delete { filter { eq("id", userId) } }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Failed to delete user", e) }

    override suspend fun getUsersByRole(role: UserRole): Resource<List<User>> = try {
        val dtos = supabase.from(Constants.COLLECTION_USERS)
            .select { filter { eq("role", role.name) } }
            .decodeList<UserDto>()
        Resource.success(dtos.map { it.toUser() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeUsersByRole(role: UserRole): Flow<Resource<List<User>>> = flow {
        emit(getUsersByRole(role))
    }

    override suspend fun getPendingApprovals(): Resource<List<User>> = try {
        val dtos = supabase.from(Constants.COLLECTION_USERS)
            .select { filter { eq("approval_status", "PENDING") } }
            .decodeList<UserDto>()
        Resource.success(dtos.map { it.toUser() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observePendingApprovals(): Flow<Resource<List<User>>> = flow {
        emit(getPendingApprovals())
    }

    override suspend fun getTenantsByResident(residentId: String): Resource<List<User>> = try {
        val dtos = supabase.from(Constants.COLLECTION_USERS)
            .select { filter { eq("tenant_of", residentId) } }
            .decodeList<UserDto>()
        Resource.success(dtos.map { it.toUser() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getAllUsers(): Resource<List<User>> = try {
        val dtos = supabase.from(Constants.COLLECTION_USERS).select().decodeList<UserDto>()
        Resource.success(dtos.map { it.toUser() })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeAllUsers(): Flow<Resource<List<User>>> = flow {
        emit(getAllUsers())
    }
}
