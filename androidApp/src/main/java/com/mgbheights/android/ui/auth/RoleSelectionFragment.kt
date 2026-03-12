package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentRoleSelectionBinding
import com.mgbheights.shared.domain.model.UserRole
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoleSelectionFragment : Fragment() {

    private var _binding: FragmentRoleSelectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()
    private var selectedRole: UserRole? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRoleSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.cardResident.setOnClickListener { selectRole(UserRole.RESIDENT) }
        binding.cardTenant.setOnClickListener { selectRole(UserRole.TENANT) }
        binding.cardSecurityGuard.setOnClickListener { selectRole(UserRole.SECURITY_GUARD) }
        binding.cardWorker.setOnClickListener { selectRole(UserRole.WORKER) }

        binding.btnContinue.setOnClickListener {
            selectedRole?.let { role ->
                viewModel.selectedRole = role
                findNavController().navigate(R.id.action_roleSelection_to_signUp)
            }
        }
    }

    private fun selectRole(role: UserRole) {
        selectedRole = role
        binding.btnContinue.isEnabled = true

        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)

        // Reset all cards
        resetCard(binding.cardResident)
        resetCard(binding.cardTenant)
        resetCard(binding.cardSecurityGuard)
        resetCard(binding.cardWorker)

        // Highlight selected card
        val selected: MaterialCardView? = when (role) {
            UserRole.RESIDENT -> binding.cardResident
            UserRole.TENANT -> binding.cardTenant
            UserRole.SECURITY_GUARD -> binding.cardSecurityGuard
            UserRole.WORKER -> binding.cardWorker
            else -> null
        }
        selected?.let {
            it.strokeWidth = 3
            it.strokeColor = primaryColor
        }
    }

    private fun resetCard(card: MaterialCardView) {
        card.strokeWidth = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
