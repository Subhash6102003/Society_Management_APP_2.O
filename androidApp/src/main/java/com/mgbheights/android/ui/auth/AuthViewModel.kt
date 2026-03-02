package com.mgbheights.android.ui.auth

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.domain.usecase.auth.LoginWithPhoneUseCase
import com.mgbheights.shared.domain.usecase.auth.VerifyOtpUseCase
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val application: Application,
    private val loginWithPhoneUseCase: LoginWithPhoneUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _sendOtpState = MutableLiveData<Resource<String>>()
    val sendOtpState: LiveData<Resource<String>> = _sendOtpState

    private val _verifyOtpState = MutableLiveData<Resource<User>>()
    val verifyOtpState: LiveData<Resource<User>> = _verifyOtpState

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    var storedVerificationId: String = ""
    var phoneNumber: String = ""

    init {
        checkLoginStatus()
    }

    /**
     * Check if user is already logged in via Firebase Auth.
     * Firebase persists auth state automatically — user stays logged in
     * until they explicitly sign out or the app is uninstalled.
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            val currentFirebaseUser = firebaseAuth.currentUser
            if (currentFirebaseUser != null) {
                Timber.d("User already logged in: ${currentFirebaseUser.uid}")
                _isLoggedIn.value = true
                _currentUser.value = Resource.Loading
                _currentUser.value = getCurrentUserUseCase()
            } else {
                _isLoggedIn.value = false
            }
        }
    }

    fun sendOtp(phone: String) {
        phoneNumber = phone
        viewModelScope.launch {
            _sendOtpState.value = Resource.Loading
            _sendOtpState.value = loginWithPhoneUseCase(phone)
        }
    }

    /**
     * Verify the OTP code entered by the user.
     */
    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            _verifyOtpState.value = Resource.Loading
            val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
            signInWithCredential(credential)
        }
    }

    /**
     * Sign in with a PhoneAuthCredential (from manual OTP or auto-verification).
     */
    fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _verifyOtpState.value = Resource.Loading
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user
                        Timber.d("Sign in successful: ${firebaseUser?.uid}")
                        _isLoggedIn.value = true

                        // Fetch or create user profile from Firestore
                        viewModelScope.launch {
                            _currentUser.value = getCurrentUserUseCase()
                        }
                    } else {
                        Timber.e(task.exception, "Sign in failed")
                        _verifyOtpState.value = Resource.error(
                            task.exception?.message ?: "Verification failed"
                        )
                    }
                }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = Resource.Loading
            _currentUser.value = getCurrentUserUseCase()
        }
    }

    /**
     * Sign out and clear Firebase auth state.
     */
    fun signOut() {
        firebaseAuth.signOut()
        _isLoggedIn.value = false
        _currentUser.value = null
    }
}

