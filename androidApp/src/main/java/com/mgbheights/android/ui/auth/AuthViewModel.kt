package com.mgbheights.android.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mgbheights.shared.domain.model.ApprovalStatus
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.AuthRepository
import com.mgbheights.shared.domain.repository.UserRepository
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.domain.usecase.auth.LoginWithEmailUseCase
import com.mgbheights.shared.domain.usecase.auth.SignUpWithEmailUseCase
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
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
    var signUpName: String = ""
    var signUpPhone: String = ""
    var signUpFlatNumber: String = ""
    var signUpTowerBlock: String = ""
    var isFromSignUp: Boolean = false
    var signUpProfilePhoto: String = ""
    var signUpIdProof: String = ""

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val result = getCurrentUserUseCase()
            if (result.isSuccess) {
                Timber.d("User already logged in: ${result.getOrNull()?.id}")
                _isLoggedIn.value = true
                _currentUser.value = result
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
            }
        }
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
                    approvalStatus = if (selectedRole == UserRole.ADMIN) ApprovalStatus.APPROVED else ApprovalStatus.PENDING,
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
        viewModelScope.launch {
            authRepository.signOut()
            _isLoggedIn.value = false
            _currentUser.value = Resource.error("Signed out")
            isFromSignUp = false
            selectedRole = UserRole.RESIDENT
        }
    }
}
