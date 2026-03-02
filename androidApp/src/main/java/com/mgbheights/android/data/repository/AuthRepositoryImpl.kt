package com.mgbheights.android.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.data.local.dao.UserDao
import com.mgbheights.android.data.mapper.toDomain
import com.mgbheights.android.data.mapper.toEntity
import com.mgbheights.android.data.mapper.toFirestoreMap
import com.mgbheights.android.data.mapper.toUser
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.AuthRepository
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    override suspend fun sendOtp(phoneNumber: String): Resource<String> {
        return try {
            // The actual OTP sending is handled in the Fragment via PhoneAuthProvider
            // This returns a placeholder - real verification ID comes from the callback
            Resource.success(phoneNumber)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to send OTP", e)
        }
    }

    override suspend fun verifyOtp(verificationId: String, otp: String): Resource<User> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: return Resource.error("Authentication failed")

            // Check if user exists in Firestore
            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = if (doc.exists()) {
                val data = doc.data ?: return Resource.error("User data is null")
                data.toUser().copy(id = firebaseUser.uid)
            } else {
                // Create new user
                val newUser = User(
                    id = firebaseUser.uid,
                    phoneNumber = firebaseUser.phoneNumber ?: "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                firestore.collection(Constants.COLLECTION_USERS)
                    .document(firebaseUser.uid)
                    .set(newUser.toFirestoreMap())
                    .await()
                newUser
            }

            // Cache locally
            userDao.insertUser(user.toEntity())
            Resource.success(user)
        } catch (e: Exception) {
            Resource.error(e.message ?: "OTP verification failed", e)
        }
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Resource.error("Not logged in")

            // Try local cache first
            val cachedUser = userDao.getUserById(firebaseUser.uid)
            if (cachedUser != null) {
                return Resource.success(cachedUser.toDomain())
            }

            // Fetch from Firestore
            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.uid)
                .get()
                .await()

            if (doc.exists()) {
                val data = doc.data ?: return Resource.error("User data is null")
                val user = data.toUser().copy(id = firebaseUser.uid)
                userDao.insertUser(user.toEntity())
                Resource.success(user)
            } else {
                Resource.error("User not found")
            }
        } catch (e: Exception) {
            // Fallback to local cache
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val cachedUser = userDao.getUserById(firebaseUser.uid)
                if (cachedUser != null) {
                    return Resource.success(cachedUser.toDomain())
                }
            }
            Resource.error(e.message ?: "Failed to get user", e)
        }
    }

    override fun observeAuthState(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(null) // Simplified; in production, fetch user data
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signOut(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()
            userDao.deleteAll()
            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Sign out failed", e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override suspend fun updateFcmToken(token: String): Resource<Unit> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Resource.error("Not logged in")
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update("fcmToken", token)
                .await()
            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to update FCM token", e)
        }
    }
}

