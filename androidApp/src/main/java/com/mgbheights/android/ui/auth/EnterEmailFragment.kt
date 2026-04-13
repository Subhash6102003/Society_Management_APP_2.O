package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import com.mgbheights.shared.domain.model.ApprovalStatus
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterEmailFragment : Fragment(R.layout.fragment_enter_email) {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            authViewModel.loginWithEmail(email, password)
        }

        authViewModel.loginState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnLogin.isEnabled = false
                }
                is Resource.Success -> {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    routeByApprovalAndRole(
                        resource.data.approvalStatus,
                        resource.data.role
                    )
                }
                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    // Translate internal codes to user-friendly messages
                    val message = when {
                        resource.message.startsWith("ACCESS_DENIED:") ->
                            "Access denied. Your account has been rejected. Contact admin."
                        else -> resource.message
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun routeByApprovalAndRole(status: ApprovalStatus, role: UserRole) {
        val options = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph_main, true)
            .build()
        val destination = when (status) {
            ApprovalStatus.PENDING -> R.id.pendingApprovalFragment
            ApprovalStatus.REJECTED -> R.id.rejectedFragment
            ApprovalStatus.APPROVED -> when (role) {
                UserRole.ADMIN -> R.id.adminDashboardFragment
                UserRole.RESIDENT -> R.id.residentHomeFragment
                UserRole.TENANT -> R.id.tenantHomeFragment
                UserRole.SECURITY_GUARD, UserRole.SECURITY_GUARD_WORKER -> R.id.guardDashboardFragment
                UserRole.WORKER -> R.id.workerDashboardFragment
            }
        }
        findNavController().navigate(destination, null, options)
    }
}
