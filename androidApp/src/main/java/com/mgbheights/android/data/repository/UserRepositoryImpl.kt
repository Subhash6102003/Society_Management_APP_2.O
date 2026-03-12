package com.mgbheights.android.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.data.local.dao.UserDao
import com.mgbheights.android.data.mapper.*
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.UserRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : UserRepository {

    private val usersRef = firestore.collection(Constants.COLLECTION_USERS)

    override suspend fun getUserById(userId: String): Resource<User> = try {
        val doc = usersRef.document(userId).get().await()
        if (doc.exists()) {
            val user = doc.data!!.toUser().copy(id = doc.id)
            userDao.insertUser(user.toEntity())
            Resource.success(user)
        } else Resource.error("User not found")
    } catch (e: Exception) {
        val cached = userDao.getUserById(userId)
        if (cached != null) Resource.success(cached.toDomain())
        else Resource.error(e.message ?: "Failed to fetch user", e)
    }

    override suspend fun getUserByPhone(phoneNumber: String): Resource<User> = try {
        val snap = usersRef.whereEqualTo("phoneNumber", phoneNumber).limit(1).get().await()
        if (snap.documents.isNotEmpty()) {
            val doc = snap.documents[0]
            Resource.success(doc.data!!.toUser().copy(id = doc.id))
        } else Resource.error("User not found")
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getUserByEmail(email: String): Resource<User> = try {
        val snap = usersRef.whereEqualTo("email", email).limit(1).get().await()
        if (snap.documents.isNotEmpty()) {
            val doc = snap.documents[0]
            Resource.success(doc.data!!.toUser().copy(id = doc.id))
        } else Resource.error("User not found")
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observeUser(userId: String): Flow<Resource<User>> = callbackFlow {
        val listener = usersRef.document(userId).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            if (snap != null && snap.exists()) {
                val user = snap.data!!.toUser().copy(id = snap.id)
                trySend(Resource.success(user))
            } else trySend(Resource.error("User not found"))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun createUser(user: User): Resource<User> = try {
        val data = user.toFirestoreMap()
        usersRef.document(user.id).set(data).await()
        userDao.insertUser(user.toEntity())
        Resource.success(user)
    } catch (e: Exception) { Resource.error(e.message ?: "Failed to create user", e) }

    override suspend fun updateUser(user: User): Resource<User> = try {
        val updated = user.copy(updatedAt = System.currentTimeMillis())
        usersRef.document(user.id).set(updated.toFirestoreMap()).await()
        userDao.updateUser(updated.toEntity())
        Resource.success(updated)
    } catch (e: Exception) { Resource.error(e.message ?: "Failed to update user", e) }

    override suspend fun updateProfilePhoto(userId: String, photoUrl: String): Resource<Unit> = try {
        usersRef.document(userId).update("profilePhotoUrl", photoUrl, "updatedAt", System.currentTimeMillis()).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun approveUser(userId: String): Resource<Unit> = try {
        usersRef.document(userId).update("isApproved", true, "updatedAt", System.currentTimeMillis()).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun blockUser(userId: String): Resource<Unit> = try {
        usersRef.document(userId).update("isBlocked", true, "updatedAt", System.currentTimeMillis()).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun unblockUser(userId: String): Resource<Unit> = try {
        usersRef.document(userId).update("isBlocked", false, "updatedAt", System.currentTimeMillis()).await()
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun deleteUser(userId: String): Resource<Unit> = try {
        usersRef.document(userId).delete().await()
        userDao.getUserById(userId)?.let { userDao.deleteAll() }
        Resource.success(Unit)
    } catch (e: Exception) { Resource.error(e.message ?: "Failed to delete user", e) }

    override suspend fun getUsersByRole(role: UserRole): Resource<List<User>> = try {
        val snap = usersRef.whereEqualTo("role", role.name).get().await()
        val users = snap.documents.mapNotNull { it.data?.toUser()?.copy(id = it.id) }
        userDao.insertUsers(users.map { it.toEntity() })
        Resource.success(users)
    } catch (e: Exception) {
        val cached = userDao.getUsersByRole(role.name)
        if (cached.isNotEmpty()) Resource.success(cached.map { it.toDomain() })
        else Resource.error(e.message ?: "Error", e)
    }

    override fun observeUsersByRole(role: UserRole): Flow<Resource<List<User>>> = callbackFlow {
        val listener = usersRef.whereEqualTo("role", role.name).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            val users = snap?.documents?.mapNotNull { it.data?.toUser()?.copy(id = it.id) } ?: emptyList()
            trySend(Resource.success(users))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getPendingApprovals(): Resource<List<User>> = try {
        val snap = usersRef.whereEqualTo("isApproved", false).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toUser()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override fun observePendingApprovals(): Flow<Resource<List<User>>> = callbackFlow {
        val listener = usersRef.whereEqualTo("isApproved", false).addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            val users = snap?.documents?.mapNotNull { it.data?.toUser()?.copy(id = it.id) } ?: emptyList()
            trySend(Resource.success(users))
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getTenantsByResident(residentId: String): Resource<List<User>> = try {
        val snap = usersRef.whereEqualTo("tenantOf", residentId).get().await()
        Resource.success(snap.documents.mapNotNull { it.data?.toUser()?.copy(id = it.id) })
    } catch (e: Exception) { Resource.error(e.message ?: "Error", e) }

    override suspend fun getAllUsers(): Resource<List<User>> = try {
        val snap = usersRef.get().await()
        val users = snap.documents.mapNotNull { it.data?.toUser()?.copy(id = it.id) }
        userDao.insertUsers(users.map { it.toEntity() })
        Resource.success(users)
    } catch (e: Exception) {
        val cached = userDao.getAllUsers()
        if (cached.isNotEmpty()) Resource.success(cached.map { it.toDomain() })
        else Resource.error(e.message ?: "Error", e)
    }

    override fun observeAllUsers(): Flow<Resource<List<User>>> = callbackFlow {
        val listener = usersRef.addSnapshotListener { snap, error ->
            if (error != null) { trySend(Resource.error(error.message ?: "Error")); return@addSnapshotListener }
            val users = snap?.documents?.mapNotNull { it.data?.toUser()?.copy(id = it.id) } ?: emptyList()
            trySend(Resource.success(users))
        }
        awaitClose { listener.remove() }
    }
}

