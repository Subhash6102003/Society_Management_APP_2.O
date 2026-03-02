package com.mgbheights.shared.domain.usecase.auth

import com.mgbheights.shared.domain.repository.AuthRepository
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators

class LoginWithPhoneUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(phoneNumber: String): Resource<String> {
        if (!Validators.isValidPhoneNumber(phoneNumber)) {
            return Resource.error("Please enter a valid 10-digit phone number")
        }
        return authRepository.sendOtp("+91$phoneNumber")
    }
}

