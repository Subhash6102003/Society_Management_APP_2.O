package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentAwaitingApprovalBinding
import com.mgbheights.android.ui.profile.ProfileViewModel
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AwaitingApprovalFragment : Fragment() {

    private var _binding: FragmentAwaitingApprovalBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAwaitingApprovalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
        profileViewModel.loadProfile()
    }

    private fun setupUI() {
        binding.btnCheckStatus.setOnClickListener {
            binding.progressLoading.isVisible = true
            profileViewModel.loadProfile()
        }

        binding.btnLogout.setOnClickListener {
            profileViewModel.logout()
            findNavController().navigate(R.id.action_awaiting_to_login)
        }
    }

    private fun observeViewModel() {
        profileViewModel.profile.observe(viewLifecycleOwner) { state ->
            binding.progressLoading.isVisible = false
            when (state) {
                is Resource.Success -> {
                    val user = state.data
                    binding.tvUserInfo.text = "${user.name} • ${user.flatNumber} ${user.towerBlock}"
                    binding.tvRoleInfo.text = "Role: ${user.role.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}"

                    if (user.isApproved) {
                        // Approved! Navigate to dashboard
                        findNavController().navigate(R.id.action_awaiting_to_dashboard)
                    }
                }
                is Resource.Error -> {
                    binding.tvUserInfo.text = "Could not load status"
                }
                is Resource.Loading -> {
                    binding.progressLoading.isVisible = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

