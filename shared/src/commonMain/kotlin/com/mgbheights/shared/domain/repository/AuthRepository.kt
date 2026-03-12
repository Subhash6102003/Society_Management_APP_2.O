package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun loginWithEmail(email: String, password: String): Resource<User>
    suspend fun signUpWithEmail(email: String, password: String): Resource<User>
    suspend fun getCurrentUser(): Resource<User>
    fun observeAuthState(): Flow<User?>
    suspend fun signOut(): Resource<Unit>
    suspend fun isLoggedIn(): Boolean
    suspend fun updateFcmToken(token: String): Resource<Unit>
    suspend fun deleteAccount(): Resource<Unit>
}
