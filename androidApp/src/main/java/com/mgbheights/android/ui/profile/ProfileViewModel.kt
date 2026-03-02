package com.mgbheights.android.ui.profile

import android.app.Application
import androidx.lifecycle.*
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.AuthRepository
import com.mgbheights.shared.domain.repository.UserRepository
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val application: Application,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profile = MutableLiveData<Resource<User>>()
    val profile: LiveData<Resource<User>> = _profile

    // Alias for backward compat
    val user: LiveData<Resource<User>> get() = _profile

    private val _updateState = MutableLiveData<Resource<User>>()
    val updateState: LiveData<Resource<User>> = _updateState

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = Resource.Loading
            _profile.value = getCurrentUserUseCase()
        }
    }

    fun completeProfile(name: String, email: String, flatNumber: String, towerBlock: String, role: UserRole) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val currentResult = getCurrentUserUseCase()
            if (currentResult.isSuccess) {
                val current = currentResult.getOrNull()!!
                val updated = current.copy(
                    name = name,
                    email = email,
                    flatNumber = flatNumber,
                    towerBlock = towerBlock,
                    houseNumber = flatNumber,
                    role = role,
                    isProfileComplete = true,
                    isApproved = role == UserRole.ADMIN,
                    updatedAt = System.currentTimeMillis()
                )
                _updateState.value = userRepository.updateUser(updated)
            } else {
                _updateState.value = Resource.error(currentResult.errorMessageOrNull() ?: "Failed to load profile")
            }
        }
    }

    fun updateProfile(name: String, email: String, flatNumber: String, towerBlock: String) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val currentResult = getCurrentUserUseCase()
            if (currentResult.isSuccess) {
                val current = currentResult.getOrNull()!!
                val updated = current.copy(
                    name = name,
                    email = email,
                    flatNumber = flatNumber,
                    towerBlock = towerBlock,
                    updatedAt = System.currentTimeMillis()
                )
                _updateState.value = userRepository.updateUser(updated)
                _profile.value = Resource.Success(updated)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
