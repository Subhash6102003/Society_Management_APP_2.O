package com.mgbheights.shared.domain.usecase.auth

import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.repository.AuthRepository
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators

class VerifyOtpUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(verificationId: String, otp: String): Resource<User> {
        if (!Validators.isValidOtp(otp)) {
            return Resource.error("Please enter a valid 6-digit OTP")
        }
        return authRepository.verifyOtp(verificationId, otp)
    }
}

