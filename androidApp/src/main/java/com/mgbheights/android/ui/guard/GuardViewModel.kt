package com.mgbheights.android.ui.guard

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
class GuardViewModel @Inject constructor(
    private val getVisitorsUseCase: GetVisitorsUseCase,
    private val addVisitorUseCase: AddVisitorUseCase,
    private val approveVisitorUseCase: ApproveVisitorUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _todaysVisitors = MutableLiveData<Resource<List<Visitor>>>()
    val todaysVisitors: LiveData<Resource<List<Visitor>>> = _todaysVisitors

    private val _activeVisitors = MutableLiveData<Resource<List<Visitor>>>()
    val activeVisitors: LiveData<Resource<List<Visitor>>> = _activeVisitors

    private val _registerState = MutableLiveData<Resource<Visitor>>()
    val registerState: LiveData<Resource<Visitor>> = _registerState

    init {
        loadTodaysVisitors()
        loadActiveVisitors()
    }

    fun loadTodaysVisitors() {
        viewModelScope.launch {
            _todaysVisitors.value = Resource.Loading
            _todaysVisitors.value = getVisitorsUseCase.today()
        }
    }

    fun loadActiveVisitors() {
        viewModelScope.launch {
            _activeVisitors.value = Resource.Loading
            _activeVisitors.value = getVisitorsUseCase.active()
        }
    }

    fun registerVisitor(visitor: Visitor) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading
            val userResult = getCurrentUserUseCase()
            if (userResult.isSuccess) {
                val guard = userResult.getOrNull()!!
                val visitorWithGuard = visitor.copy(
                    guardId = guard.id,
                    guardName = guard.name
                )
                _registerState.value = addVisitorUseCase(visitorWithGuard)
                loadTodaysVisitors()
            } else {
                _registerState.value = Resource.error("Guard not authenticated")
            }
        }
    }

    fun checkInVisitor(visitorId: String) {
        viewModelScope.launch {
            approveVisitorUseCase.checkIn(visitorId)
            loadTodaysVisitors()
            loadActiveVisitors()
        }
    }

    fun checkOutVisitor(visitorId: String) {
        viewModelScope.launch {
            approveVisitorUseCase.checkOut(visitorId)
            loadTodaysVisitors()
            loadActiveVisitors()
        }
    }
}

