package com.mgbheights.android.ui.visitor

import androidx.lifecycle.*
import com.mgbheights.shared.domain.model.Visitor
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.domain.usecase.visitor.AddVisitorUseCase
import com.mgbheights.shared.domain.usecase.visitor.ApproveVisitorUseCase
import com.mgbheights.shared.domain.usecase.visitor.GetVisitorsUseCase
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VisitorViewModel @Inject constructor(
    private val getVisitorsUseCase: GetVisitorsUseCase,
    private val addVisitorUseCase: AddVisitorUseCase,
    private val approveVisitorUseCase: ApproveVisitorUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _visitors = MutableLiveData<Resource<List<Visitor>>>()
    val visitors: LiveData<Resource<List<Visitor>>> = _visitors

    private val _addVisitorState = MutableLiveData<Resource<Visitor>>()
    val addVisitorState: LiveData<Resource<Visitor>> = _addVisitorState

    private val _actionState = MutableLiveData<Resource<Unit>>()
    val actionState: LiveData<Resource<Unit>> = _actionState

    init { loadVisitors() }

    fun loadVisitors() {
        viewModelScope.launch {
            _visitors.value = Resource.Loading
            val userResult = getCurrentUserUseCase()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!
                _visitors.value = when (user.role.name) {
                    "ADMIN" -> getVisitorsUseCase.all()
                    "SECURITY_GUARD" -> getVisitorsUseCase.today()
                    else -> getVisitorsUseCase.all().map { visitors ->
                        visitors.filter { it.residentId == user.id || it.flatNumber == user.flatNumber }
                    }
                }
            } else {
                _visitors.value = Resource.error(userResult.errorMessageOrNull() ?: "Error")
            }
        }
    }

    fun addVisitor(visitor: Visitor) {
        viewModelScope.launch {
            _addVisitorState.value = Resource.Loading
            _addVisitorState.value = addVisitorUseCase(visitor)
        }
    }

    fun approveVisitor(visitorId: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val userResult = getCurrentUserUseCase()
            if (userResult.isSuccess) {
                _actionState.value = approveVisitorUseCase.approve(visitorId, userResult.getOrNull()!!.id)
                loadVisitors()
            }
        }
    }

    fun denyVisitor(visitorId: String, reason: String = "Denied by resident") {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            _actionState.value = approveVisitorUseCase.deny(visitorId, reason)
            loadVisitors()
        }
    }

    fun checkInVisitor(visitorId: String) {
        viewModelScope.launch {
            _actionState.value = approveVisitorUseCase.checkIn(visitorId)
            loadVisitors()
        }
    }

    fun checkOutVisitor(visitorId: String) {
        viewModelScope.launch {
            _actionState.value = approveVisitorUseCase.checkOut(visitorId)
            loadVisitors()
        }
    }
}

