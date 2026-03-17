package com.mgbheights.android.ui.auth

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.domain.usecase.auth.LoginWithEmailUseCase
import com.mgbheights.shared.domain.usecase.auth.SignUpWithEmailUseCase
import com.mgbheights.shared.domain.repository.UserRepository
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val application: Application,
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _loginState = MutableLiveData<Resource<User>>()
    val loginState: LiveData<Resource<User>> = _loginState

    private val _signUpState = MutableLiveData<Resource<User>>()
    val signUpState: LiveData<Resource<User>> = _signUpState

    private val _currentUser = MutableLiveData<Resource<User>>()
    val currentUser: LiveData<Resource<User>> = _currentUser

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    // Sign-up flow state
    var selectedRole: UserRole = UserRole.RESIDENT
    var signUpEmail: String = ""
    var signUpPassword: String = ""

    // Sign-up form data (stored for use after email verification)
    var signUpName: String = ""
    var signUpPhone: String = ""
    var signUpFlatNumber: String = ""
    var signUpTowerBlock: String = ""
    var isFromSignUp: Boolean = false

    // Photo data (Base64 data URIs stored in Firestore)
    var signUpProfilePhoto: String = ""
    var signUpIdProof: String = ""

    init {
        checkLoginStatus()
    }

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

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading
            val result = loginWithEmailUseCase(email, password)
            _loginState.value = result
            if (result.isSuccess) {
                _isLoggedIn.value = true
                _currentUser.value = result
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, role: UserRole, name: String) {
        viewModelScope.launch {
            _signUpState.value = Resource.Loading
            val result = signUpWithEmailUseCase(email, password, role, name)
            _signUpState.value = result
            if (result.isSuccess) {
                _isLoggedIn.value = true
                signUpEmail = email
                signUpPassword = password
                sendEmailVerification()
            }
        }
    }

    fun sendEmailVerification() {
        val user = firebaseAuth.currentUser ?: return
        user.sendEmailVerification()
    }

    fun isEmailVerified(): Boolean {
        return firebaseAuth.currentUser?.isEmailVerified == true
    }

    fun completeSignUpProfile(name: String, flatNumber: String, towerBlock: String) {
        viewModelScope.launch {
            _signUpState.value = Resource.Loading
            val currentResult = getCurrentUserUseCase()
            if (currentResult.isSuccess) {
                val current = currentResult.getOrNull()!!
                val updated = current.copy(
                    name = name,
                    phoneNumber = signUpPhone,
                    flatNumber = if (selectedRole == UserRole.RESIDENT || selectedRole == UserRole.TENANT) flatNumber else "",
                    towerBlock = if (selectedRole == UserRole.RESIDENT || selectedRole == UserRole.TENANT) towerBlock else "",
                    houseNumber = if (selectedRole == UserRole.RESIDENT || selectedRole == UserRole.TENANT) flatNumber else "",
                    role = selectedRole,
                    profilePhotoUrl = signUpProfilePhoto,
                    idProofUrl = signUpIdProof,
                    isProfileComplete = true,
                    isApproved = selectedRole == UserRole.ADMIN,
                    updatedAt = System.currentTimeMillis()
                )
                val updateResult = userRepository.updateUser(updated)
                _signUpState.value = updateResult
                if (updateResult.isSuccess) {
                    _currentUser.value = updateResult
                }
            } else {
                _signUpState.value = Resource.error(
                    currentResult.errorMessageOrNull() ?: "Failed to load profile"
                )
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = Resource.Loading
            _currentUser.value = getCurrentUserUseCase()
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
        _isLoggedIn.value = false
        _currentUser.value = Resource.error("Signed out")
        isFromSignUp = false
        // Reset role to default for next user
        selectedRole = UserRole.RESIDENT
    }
}
