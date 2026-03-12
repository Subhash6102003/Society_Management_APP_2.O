package com.mgbheights.android.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mgbheights.android.data.local.dao.UserDao
import com.mgbheights.android.data.mapper.toDomain
import com.mgbheights.android.data.mapper.toEntity
import com.mgbheights.android.data.mapper.toFirestoreMap
import com.mgbheights.android.data.mapper.toUser
import com.mgbheights.shared.domain.model.User
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

    override suspend fun loginWithEmail(email: String, password: String): Resource<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Resource.error("Authentication failed")

            val isAdminEmail = (firebaseUser.email ?: email).equals(Constants.ADMIN_DEFAULT_EMAIL, ignoreCase = true)

            // Fetch user from Firestore
            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.uid)
                .get()
                .await()

            if (doc.exists()) {
                val data = doc.data ?: return Resource.error("User data is null")
                var user = data.toUser().copy(id = firebaseUser.uid)

                // Auto-promote admin email to ADMIN role + approved
                if (isAdminEmail && (user.role != com.mgbheights.shared.domain.model.UserRole.ADMIN || !user.isApproved)) {
                    user = user.copy(
                        role = com.mgbheights.shared.domain.model.UserRole.ADMIN,
                        isApproved = true,
                        isProfileComplete = true
                    )
                    try {
                        firestore.collection(Constants.COLLECTION_USERS)
                            .document(firebaseUser.uid)
                            .set(
                                mapOf(
                                    "role" to "ADMIN",
                                    "isApproved" to true,
                                    "isProfileComplete" to true,
                                    "updatedAt" to System.currentTimeMillis()
                                ),
                                SetOptions.merge()
                            )
                            .await()
                    } catch (_: Exception) {
                        // Silently handle — local state is already correct
                    }
                }
                // Also auto-approve any other ADMIN role user
                else if (user.role == com.mgbheights.shared.domain.model.UserRole.ADMIN && !user.isApproved) {
                    user = user.copy(isApproved = true)
                    try {
                        firestore.collection(Constants.COLLECTION_USERS)
                            .document(firebaseUser.uid)
                            .set(mapOf("isApproved" to true, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())
                            .await()
                    } catch (_: Exception) { }
                }

                userDao.insertUser(user.toEntity())
                Resource.success(user)
            } else {
                // User exists in Auth but not Firestore — create Firestore doc
                val role = if (isAdminEmail) com.mgbheights.shared.domain.model.UserRole.ADMIN
                           else com.mgbheights.shared.domain.model.UserRole.RESIDENT
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    role = role,
                    isApproved = isAdminEmail,
                    isProfileComplete = isAdminEmail,
                    isOnboarded = isAdminEmail,
                    name = if (isAdminEmail) "Admin" else "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                firestore.collection(Constants.COLLECTION_USERS)
                    .document(firebaseUser.uid)
                    .set(newUser.toFirestoreMap())
                    .await()
                userDao.insertUser(newUser.toEntity())
                Resource.success(newUser)
            }
        } catch (e: Exception) {
            Resource.error(e.message ?: "Login failed", e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Resource<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Resource.error("Account creation failed")

            val isAdminEmail = (firebaseUser.email ?: email).equals(Constants.ADMIN_DEFAULT_EMAIL, ignoreCase = true)

            // Create user doc — admin email gets ADMIN role immediately
            val newUser = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                role = if (isAdminEmail) com.mgbheights.shared.domain.model.UserRole.ADMIN
                       else com.mgbheights.shared.domain.model.UserRole.RESIDENT,
                isApproved = isAdminEmail,
                isProfileComplete = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            firestore.collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.uid)
                .set(newUser.toFirestoreMap())
                .await()
            userDao.insertUser(newUser.toEntity())
            Resource.success(newUser)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Sign up failed", e)
        }
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Resource.error("Not logged in")
            val isAdminEmail = firebaseUser.email?.equals(Constants.ADMIN_DEFAULT_EMAIL, ignoreCase = true) == true

            // Try local cache first
            val cachedUser = userDao.getUserById(firebaseUser.uid)
            if (cachedUser != null) {
                var domainUser = cachedUser.toDomain()
                // Auto-promote admin email from cache
                if (isAdminEmail && (domainUser.role != com.mgbheights.shared.domain.model.UserRole.ADMIN || !domainUser.isApproved)) {
                    domainUser = domainUser.copy(
                        role = com.mgbheights.shared.domain.model.UserRole.ADMIN,
                        isApproved = true,
                        isProfileComplete = true
                    )
                    userDao.updateUser(domainUser.toEntity())
                    return Resource.success(domainUser)
                }
                // Auto-approve any admin role
                if (domainUser.role == com.mgbheights.shared.domain.model.UserRole.ADMIN && !domainUser.isApproved) {
                    domainUser = domainUser.copy(isApproved = true)
                    userDao.updateUser(domainUser.toEntity())
                    return Resource.success(domainUser)
                }
                return Resource.success(domainUser)
            }

            // Fetch from Firestore
            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.uid)
                .get()
                .await()

            if (doc.exists()) {
                val data = doc.data ?: return Resource.error("User data is null")
                var user = data.toUser().copy(id = firebaseUser.uid)
                // Auto-promote admin email
                if (isAdminEmail && (user.role != com.mgbheights.shared.domain.model.UserRole.ADMIN || !user.isApproved)) {
                    user = user.copy(
                        role = com.mgbheights.shared.domain.model.UserRole.ADMIN,
                        isApproved = true,
                        isProfileComplete = true
                    )
                    try {
                        firestore.collection(Constants.COLLECTION_USERS)
                            .document(firebaseUser.uid)
                            .set(mapOf("role" to "ADMIN", "isApproved" to true, "isProfileComplete" to true, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())
                            .await()
                    } catch (_: Exception) { }
                }
                // Auto-approve any admin role
                else if (user.role == com.mgbheights.shared.domain.model.UserRole.ADMIN && !user.isApproved) {
                    user = user.copy(isApproved = true)
                    try {
                        firestore.collection(Constants.COLLECTION_USERS)
                            .document(firebaseUser.uid)
                            .set(mapOf("isApproved" to true, "updatedAt" to System.currentTimeMillis()), SetOptions.merge())
                            .await()
                    } catch (_: Exception) { }
                }
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
            trySend(null)
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

    override suspend fun deleteAccount(): Resource<Unit> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Resource.error("Not logged in")
            val userId = firebaseUser.uid

            // Delete Firestore doc
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .delete()
                .await()

            // Delete local cache
            userDao.deleteAll()

            // Delete Firebase Auth account
            firebaseUser.delete().await()

            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to delete account", e)
        }
    }
}
