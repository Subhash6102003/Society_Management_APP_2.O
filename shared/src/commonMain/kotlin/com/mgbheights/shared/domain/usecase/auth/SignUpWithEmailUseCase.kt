package com.mgbheights.shared.domain.usecase.auth

import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.AuthRepository
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators

class SignUpWithEmailUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, role: UserRole, name: String): Resource<User> {
        if (name.isBlank()) {
            return Resource.error("Please enter your name")
        }
        if (!Validators.isValidEmailRequired(email)) {
            return Resource.error("Please enter a valid email address")
        }
        if (!Validators.isValidPassword(password)) {
            return Resource.error("Password must be at least 6 characters")
        }
        return authRepository.signUpWithEmail(email, password, role, name)
    }
}
