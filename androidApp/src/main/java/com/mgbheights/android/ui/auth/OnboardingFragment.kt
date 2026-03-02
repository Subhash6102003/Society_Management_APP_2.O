package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentOnboardingBinding
import com.mgbheights.android.ui.profile.ProfileViewModel
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Show role info for non-resident roles
        val role = authViewModel.selectedRole
        if (role != UserRole.RESIDENT) {
            binding.cardRoleInfo.isVisible = true
            binding.tvRoleInfo.text = "Registering as ${role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}"
        }

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnSubmitProfile.setOnClickListener {
            val name = binding.etName.text?.toString()?.trim() ?: ""
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val flat = binding.etFlatNumber.text?.toString()?.trim() ?: ""
            val tower = binding.etTowerBlock.text?.toString()?.trim() ?: ""

            if (name.isBlank()) {
                showError("Please enter your name")
                return@setOnClickListener
            }
            if (flat.isBlank() && authViewModel.selectedRole == UserRole.RESIDENT) {
                showError("Please enter your flat number")
                return@setOnClickListener
            }

            setLoading(true)
            profileViewModel.completeProfile(
                name = name,
                email = email,
                flatNumber = flat,
                towerBlock = tower,
                role = authViewModel.selectedRole
            )
        }

        binding.btnChangePhoto.setOnClickListener {
            // Photo picker - simplified for now
        }
    }

    private fun observeViewModel() {
        profileViewModel.updateState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success -> {
                    setLoading(false)
                    // Navigate to awaiting approval screen
                    findNavController().navigate(R.id.action_onboarding_to_awaiting)
                }
                is Resource.Error -> {
                    setLoading(false)
                    showError(state.message)
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressLoading.isVisible = loading
        binding.btnSubmitProfile.isEnabled = !loading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

