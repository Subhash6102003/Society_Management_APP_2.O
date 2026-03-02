package com.mgbheights.shared.domain.repository

import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun sendOtp(phoneNumber: String): Resource<String>
    suspend fun verifyOtp(verificationId: String, otp: String): Resource<User>
    suspend fun getCurrentUser(): Resource<User>
    fun observeAuthState(): Flow<User?>
    suspend fun signOut(): Resource<Unit>
    suspend fun isLoggedIn(): Boolean
    suspend fun updateFcmToken(token: String): Resource<Unit>
}

