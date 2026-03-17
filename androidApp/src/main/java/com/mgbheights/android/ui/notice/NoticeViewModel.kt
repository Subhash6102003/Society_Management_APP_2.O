package com.mgbheights.android.ui.notice

import androidx.lifecycle.*
import com.mgbheights.shared.domain.model.Notice
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.domain.usecase.notice.CreateNoticeUseCase
import com.mgbheights.shared.domain.usecase.notice.GetNoticesUseCase
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val getNoticesUseCase: GetNoticesUseCase,
    private val createNoticeUseCase: CreateNoticeUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _notices = MutableLiveData<Resource<List<Notice>>>()
    val notices: LiveData<Resource<List<Notice>>> = _notices

    private val _noticeDetail = MutableLiveData<Resource<Notice>>()
    val noticeDetail: LiveData<Resource<Notice>> = _noticeDetail

    private val _createState = MutableLiveData<Resource<Notice>>()
    val createState: LiveData<Resource<Notice>> = _createState

    private val _isAdmin = MutableLiveData(false)
    val isAdmin: LiveData<Boolean> = _isAdmin

    init { loadNotices() }

    fun loadNotices() {
        viewModelScope.launch {
            _notices.value = Resource.Loading
            val userResult = getCurrentUserUseCase()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!
                _isAdmin.value = user.role == UserRole.ADMIN
                _notices.value = getNoticesUseCase.byRole(user.role)
            } else {
                _notices.value = getNoticesUseCase.all()
            }
        }
    }

    fun loadNotice(noticeId: String) {
        viewModelScope.launch {
            _noticeDetail.value = Resource.Loading
            _noticeDetail.value = getNoticesUseCase.byId(noticeId)
        }
    }

    fun createNotice(notice: Notice) {
        viewModelScope.launch {
            _createState.value = Resource.Loading
            _createState.value = createNoticeUseCase(notice)
        }
    }

    fun markRead(noticeId: String) {
        viewModelScope.launch {
            val userResult = getCurrentUserUseCase()
            if (userResult.isSuccess) {
                getNoticesUseCase.markRead(noticeId, userResult.getOrNull()!!.id)
            }
        }
    }
}
