package com.mgbheights.android.ui.complaint

import androidx.lifecycle.*
import com.mgbheights.shared.domain.model.Complaint
import com.mgbheights.shared.domain.model.ComplaintStatus
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.domain.usecase.complaint.CreateComplaintUseCase
import com.mgbheights.shared.domain.usecase.complaint.GetComplaintsUseCase
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComplaintViewModel @Inject constructor(
    private val getComplaintsUseCase: GetComplaintsUseCase,
    private val createComplaintUseCase: CreateComplaintUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _complaints = MutableLiveData<Resource<List<Complaint>>>()
    val complaints: LiveData<Resource<List<Complaint>>> = _complaints

    private val _complaintDetail = MutableLiveData<Resource<Complaint>>()
    val complaintDetail: LiveData<Resource<Complaint>> = _complaintDetail

    private val _createState = MutableLiveData<Resource<Complaint>>()
    val createState: LiveData<Resource<Complaint>> = _createState

    private var allComplaints: List<Complaint> = emptyList()

    init { loadComplaints() }

    fun loadComplaints() {
        viewModelScope.launch {
            _complaints.value = Resource.Loading
            val userResult = getCurrentUserUseCase()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()!!
                val result = if (user.role.name == "ADMIN") {
                    getComplaintsUseCase.all()
                } else {
                    getComplaintsUseCase.byUser(user.id)
                }
                if (result.isSuccess) {
                    allComplaints = result.getOrNull() ?: emptyList()
                }
                _complaints.value = result
            } else {
                _complaints.value = Resource.error("Failed to load user")
            }
        }
    }

    fun loadComplaint(complaintId: String) {
        viewModelScope.launch {
            _complaintDetail.value = Resource.Loading
            _complaintDetail.value = getComplaintsUseCase.byId(complaintId)
        }
    }

    fun filterByStatus(status: String) {
        val filtered = if (status == "All") {
            allComplaints
        } else {
            val statusEnum = try { ComplaintStatus.valueOf(status.uppercase().replace(" ", "_")) } catch (_: Exception) { return }
            allComplaints.filter { it.status == statusEnum }
        }
        _complaints.value = Resource.success(filtered)
    }

    fun createComplaint(complaint: Complaint) {
        viewModelScope.launch {
            _createState.value = Resource.Loading
            _createState.value = createComplaintUseCase(complaint)
        }
    }
}
