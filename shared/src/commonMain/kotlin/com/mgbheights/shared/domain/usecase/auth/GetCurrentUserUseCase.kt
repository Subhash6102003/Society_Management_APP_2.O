package com.mgbheights.shared.domain.usecase.auth

import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.repository.AuthRepository
import com.mgbheights.shared.util.Resource
import kotlinx.coroutines.flow.Flow

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Resource<User> {
        return authRepository.getCurrentUser()
    }

    fun observeAuthState(): Flow<User?> {
        return authRepository.observeAuthState()
    }

    suspend fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
}

