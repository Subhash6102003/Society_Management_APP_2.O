package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateProfileFragment : Fragment(R.layout.fragment_create_profile) {

    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = arguments?.getString("email").orEmpty()
        val role = arguments?.getString("role").orEmpty()

        // Store role in ViewModel for later use
        authViewModel.selectedRole = when (role.lowercase()) {
            "resident" -> UserRole.RESIDENT
            "tenant" -> UserRole.TENANT
            "guard" -> UserRole.SECURITY_GUARD
            "worker", "maid" -> UserRole.WORKER
            else -> UserRole.RESIDENT
        }

        view.findViewById<View>(R.id.btnSubmitProfile)?.setOnClickListener {
            submitProfile(view, email, role)
        }

        authViewModel.signUpState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    findNavController().navigate(R.id.pendingApprovalFragment)
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun submitProfile(view: View, email: String, role: String) {
        val name = view.findViewById<android.widget.EditText>(R.id.etName)?.text?.toString()?.trim().orEmpty()
        val phone = view.findViewById<android.widget.EditText>(R.id.etPhone)?.text?.toString()?.trim().orEmpty()
        val flatNumber = view.findViewById<android.widget.EditText>(R.id.etFlatNumber)?.text?.toString()?.trim().orEmpty()
        val buildingNumber = view.findViewById<android.widget.EditText>(R.id.etBuildingNumber)?.text?.toString()?.trim().orEmpty()
        val password = view.findViewById<android.widget.EditText>(R.id.etPassword)?.text?.toString()?.trim().orEmpty()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        authViewModel.signUpName = name
        authViewModel.signUpPhone = phone
        authViewModel.signUpFlatNumber = flatNumber
        authViewModel.signUpTowerBlock = buildingNumber

        if (email.isNotBlank() && password.isNotBlank()) {
            authViewModel.signUpWithEmail(email, password, authViewModel.selectedRole, name)
        } else {
            // Profile completion for already-signed-up user
            authViewModel.completeSignUpProfile(name, flatNumber, buildingNumber)
        }
    }
}
