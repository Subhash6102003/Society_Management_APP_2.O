package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentAwaitingApprovalBinding
import com.mgbheights.android.ui.profile.ProfileViewModel
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AwaitingApprovalFragment : Fragment() {

    private var _binding: FragmentAwaitingApprovalBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthViewModel by activityViewModels()
    private val handler = Handler(Looper.getMainLooper())
    private val pollRunnable = object : Runnable {
        override fun run() {
            profileViewModel.loadProfile()
            handler.postDelayed(this, 10_000) // Poll every 10 seconds
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAwaitingApprovalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
        profileViewModel.loadProfile()

        // Start auto-polling
        handler.postDelayed(pollRunnable, 10_000)
    }

    private fun setupUI() {
        binding.btnCheckStatus.setOnClickListener {
            binding.progressLoading.isVisible = true
            profileViewModel.loadProfile()
        }

        binding.btnLogout.setOnClickListener {
            handler.removeCallbacks(pollRunnable)
            authViewModel.signOut()
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

                    if (user.isApproved || user.role == UserRole.ADMIN) {
                        // Approved (or admin)! Navigate to dashboard
                        handler.removeCallbacks(pollRunnable)
                        findNavController().navigate(R.id.action_awaiting_to_dashboard)
                    }
                }
                is Resource.Error -> {
                    // If user doc not found, it means admin rejected (deleted the doc)
                    val msg = state.message
                    if (msg.contains("not found", ignoreCase = true) || msg.contains("Not logged in", ignoreCase = true)) {
                        handler.removeCallbacks(pollRunnable)
                        binding.tvUserInfo.text = "Your registration was rejected by the admin."
                        binding.tvRoleInfo.text = "Please sign up again with correct details."
                        binding.tvRoleInfo.setTextColor(resources.getColor(R.color.error, null))
                        binding.btnCheckStatus.isVisible = false
                        binding.btnLogout.text = "Back to Sign Up"
                    } else {
                        binding.tvUserInfo.text = "Could not load status"
                    }
                }
                is Resource.Loading -> {
                    binding.progressLoading.isVisible = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(pollRunnable)
        _binding = null
    }
}
