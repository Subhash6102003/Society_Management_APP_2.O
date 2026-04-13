package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import com.mgbheights.shared.domain.model.ApprovalStatus
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment(R.layout.fragment_splash) {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.isLoggedIn.observe(viewLifecycleOwner) { loggedIn ->
            if (loggedIn == false) {
                goTo(R.id.landingFragment)
            }
        }

        authViewModel.currentUser.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> routeByApprovalAndRole(resource.data)
                is Resource.Error -> goTo(R.id.landingFragment)
                else -> { /* loading — wait */ }
            }
        }
    }

    private fun routeByApprovalAndRole(user: User) {
        val destination = when (user.approvalStatus) {
            ApprovalStatus.PENDING -> R.id.pendingApprovalFragment
            ApprovalStatus.REJECTED -> R.id.rejectedFragment
            ApprovalStatus.APPROVED -> when (user.role) {
                UserRole.ADMIN -> R.id.adminDashboardFragment
                UserRole.RESIDENT -> R.id.residentHomeFragment
                UserRole.TENANT -> R.id.tenantHomeFragment
                UserRole.SECURITY_GUARD, UserRole.SECURITY_GUARD_WORKER -> R.id.guardDashboardFragment
                UserRole.WORKER -> R.id.workerDashboardFragment
            }
        }
        goTo(destination)
    }

    private fun goTo(destination: Int) {
        val options = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph_main, true)
            .build()
        findNavController().navigate(destination, null, options)
    }
}
