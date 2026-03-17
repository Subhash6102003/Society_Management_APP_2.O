package com.mgbheights.android.ui.profile

import android.app.Application
import androidx.lifecycle.*
import com.mgbheights.shared.domain.model.EditRequest
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.AuthRepository
import com.mgbheights.shared.domain.repository.EditRequestRepository
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
    private val userRepository: UserRepository,
    private val editRequestRepository: EditRequestRepository
) : ViewModel() {

    private val _profile = MutableLiveData<Resource<User>>()
    val profile: LiveData<Resource<User>> = _profile

    val user: LiveData<Resource<User>> get() = _profile

    private val _updateState = MutableLiveData<Resource<User>>()
    val updateState: LiveData<Resource<User>> = _updateState

    private val _editRequestState = MutableLiveData<Resource<EditRequest>>()
    val editRequestState: LiveData<Resource<EditRequest>> = _editRequestState

    private val _myEditRequests = MutableLiveData<Resource<List<EditRequest>>>()
    val myEditRequests: LiveData<Resource<List<EditRequest>>> = _myEditRequests

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
                // FIX: Don't reset isApproved if it's already true (e.g. Admin pre-approved this account)
                val isAlreadyApproved = current.isApproved
                val updated = current.copy(
                    name = name,
                    email = email,
                    flatNumber = flatNumber,
                    towerBlock = towerBlock,
                    houseNumber = flatNumber,
                    role = role,
                    isProfileComplete = true,
                    isApproved = isAlreadyApproved || role == UserRole.ADMIN,
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
                val result = userRepository.updateUser(updated)
                _updateState.value = result
                if (result.isSuccess) {
                    _profile.value = Resource.Success(updated)
                }
            }
        }
    }

    fun updateProfilePhoto(base64DataUri: String) {
        viewModelScope.launch {
            val currentResult = getCurrentUserUseCase()
            if (currentResult.isSuccess) {
                val current = currentResult.getOrNull()!!
                val updated = current.copy(
                    profilePhotoUrl = base64DataUri,
                    updatedAt = System.currentTimeMillis()
                )
                val result = userRepository.updateUser(updated)
                if (result.isSuccess) {
                    _profile.value = Resource.Success(updated)
                }
            }
        }
    }

    fun submitEditRequest(newName: String, newEmail: String, newFlatNumber: String, newTowerBlock: String) {
        viewModelScope.launch {
            _editRequestState.value = Resource.Loading
            val currentResult = getCurrentUserUseCase()
            if (currentResult.isSuccess) {
                val current = currentResult.getOrNull()!!

                if (current.role == UserRole.ADMIN) {
                    val updated = current.copy(
                        name = newName,
                        email = newEmail,
                        flatNumber = newFlatNumber,
                        towerBlock = newTowerBlock,
                        updatedAt = System.currentTimeMillis()
                    )
                    val result = userRepository.updateUser(updated)
                    if (result.isSuccess) {
                        _profile.value = Resource.Success(updated)
                        _editRequestState.value = Resource.Success(EditRequest())
                    } else {
                        _editRequestState.value = Resource.error("Failed to update profile")
                    }
                    return@launch
                }

                val requestedChanges = mutableMapOf<String, String>()
                val currentValues = mutableMapOf<String, String>()

                if (newName != current.name) {
                    requestedChanges["name"] = newName
                    currentValues["name"] = current.name
                }
                if (newEmail != current.email) {
                    requestedChanges["email"] = newEmail
                    currentValues["email"] = current.email
                }
                if (newFlatNumber != current.flatNumber) {
                    requestedChanges["flatNumber"] = newFlatNumber
                    currentValues["flatNumber"] = current.flatNumber
                }
                if (newTowerBlock != current.towerBlock) {
                    requestedChanges["towerBlock"] = newTowerBlock
                    currentValues["towerBlock"] = current.towerBlock
                }

                if (requestedChanges.isEmpty()) {
                    _editRequestState.value = Resource.error("No changes detected")
                    return@launch
                }

                val editRequest = EditRequest(
                    userId = current.id,
                    userName = current.name,
                    userRole = current.role.name,
                    requestedChanges = requestedChanges,
                    currentValues = currentValues
                )

                val result = editRequestRepository.submitEditRequest(editRequest)
                _editRequestState.value = result
            } else {
                _editRequestState.value = Resource.error("Failed to load profile")
            }
        }
    }

    fun loadMyEditRequests() {
        viewModelScope.launch {
            val currentResult = getCurrentUserUseCase()
            if (currentResult.isSuccess) {
                val userId = currentResult.getOrNull()!!.id
                _myEditRequests.value = Resource.Loading
                _myEditRequests.value = editRequestRepository.getEditRequestsByUser(userId)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
