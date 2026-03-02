package com.mgbheights.android.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentProfileBinding
import com.mgbheights.android.ui.auth.AuthViewModel
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                val user = state.data
                binding.tvUserName.text = user.name.ifBlank { "Resident" }
                binding.tvUserRole.text = user.role.name.replace("_", " ")
                binding.tvPhone.text = user.phoneNumber
                binding.tvFlatNumber.text = user.flatNumber
                binding.tvTowerBlock.text = user.towerBlock
                binding.tvEmail.text = user.email.ifBlank { "Not set" }
            }
        }

        binding.btnPaymentHistory.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_paymentHistory)
        }

        binding.btnMyComplaints.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_complaints)
        }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    viewModel.logout()
                    findNavController().navigate(R.id.action_profile_to_login)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

