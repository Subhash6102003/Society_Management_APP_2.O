package com.mgbheights.android.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun loginWithEmail(email: String, password: String): Resource<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Resource.error("Authentication failed")

            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.uid)
                .get()
                .await()

            if (doc.exists()) {
                val data = doc.data ?: return Resource.error("User data is null")
                var user = data.toUser().copy(id = firebaseUser.uid)

                val isAdminEmail = (firebaseUser.email ?: email).equals(Constants.ADMIN_DEFAULT_EMAIL, ignoreCase = true)

                if (isAdminEmail && (user.role != UserRole.ADMIN || !user.isApproved)) {
                    user = user.copy(
                        role = UserRole.ADMIN,
                        isApproved = true,
                        isProfileComplete = true
                    )
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
                }

                Resource.success(user)
            } else {
                firebaseAuth.signOut()
                Resource.error("Your account has been removed or not fully registered. Please contact the administrator.")
            }
        } catch (e: Exception) {
            Resource.error(e.message ?: "Login failed", e)
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, role: UserRole, name: String): Resource<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return Resource.error("Account creation failed")

            val isAdminEmail = (firebaseUser.email ?: email).equals(Constants.ADMIN_DEFAULT_EMAIL, ignoreCase = true)
            val finalRole = if (isAdminEmail) UserRole.ADMIN else role

            val newUser = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                name = name,
                role = finalRole,
                isApproved = isAdminEmail,
                isProfileComplete = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            firestore.collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.uid)
                .set(newUser.toFirestoreMap())
                .await()
            Resource.success(newUser)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Sign up failed", e)
        }
    }

    override suspend fun getCurrentUser(): Resource<User> {
        return try {
            val firebaseUser = firebaseAuth.currentUser ?: return Resource.error("Not logged in")
            
            val doc = firestore.collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.uid)
                .get()
                .await()

            if (doc.exists()) {
                val data = doc.data ?: return Resource.error("User data is null")
                val user = data.toUser().copy(id = firebaseUser.uid)
                Resource.success(user)
            } else {
                firebaseAuth.signOut()
                Resource.error("User profile not found")
            }
        } catch (e: Exception) {
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

            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .delete()
                .await()

            firebaseUser.delete().await()

            Resource.success(Unit)
        } catch (e: Exception) {
            Resource.error(e.message ?: "Failed to delete account", e)
        }
    }
}
