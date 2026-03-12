package com.mgbheights.shared.domain.usecase.auth

import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.repository.AuthRepository
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators

class LoginWithEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<User> {
        if (!Validators.isValidEmailRequired(email)) {
            return Resource.error("Please enter a valid email address")
        }
        if (!Validators.isValidPassword(password)) {
            return Resource.error("Password must be at least 6 characters")
        }
        return authRepository.loginWithEmail(email, password)
    }
}

