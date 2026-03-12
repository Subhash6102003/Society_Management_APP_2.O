package com.mgbheights.android.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentProfileBinding
import com.mgbheights.android.util.CameraHelper
import com.mgbheights.android.util.PhotoCompressor
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var cameraHelper: CameraHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Register camera launcher before onViewCreated
        cameraHelper = CameraHelper(this) { uri ->
            val base64 = PhotoCompressor.compressProfilePhoto(requireContext(), uri)
            if (base64 != null) {
                PhotoCompressor.loadPhotoIntoView(binding.ivProfilePhoto, base64, R.drawable.ic_profile)
                viewModel.updateProfilePhoto(base64)
                Toast.makeText(requireContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to process photo", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> { /* optionally show shimmer */ }
                is Resource.Success -> {
                    val user = state.data
                    binding.tvUserName.text = user.name.ifBlank { "User" }
                    binding.tvUserRole.text = user.role.name.replace("_", " ")
                    binding.tvPhone.text = user.phoneNumber.ifBlank { "Not set" }
                    binding.tvEmail.text = user.email.ifBlank { "Not set" }

                    // Display profile photo (Base64 or placeholder)
                    PhotoCompressor.loadPhotoIntoView(
                        binding.ivProfilePhoto,
                        user.profilePhotoUrl,
                        R.drawable.ic_profile
                    )

                    // Hide house details for Worker/Security Guard roles
                    val isStaffRole = user.role == UserRole.WORKER ||
                            user.role == UserRole.SECURITY_GUARD ||
                            user.role == UserRole.SECURITY_GUARD_WORKER
                    binding.cardHouseDetails.isVisible = !isStaffRole
                    if (!isStaffRole) {
                        binding.tvFlatNumber.text = user.flatNumber.ifBlank { "N/A" }
                        binding.tvTowerBlock.text = user.towerBlock.ifBlank { "N/A" }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Change photo FAB
        binding.fabChangePhoto.isVisible = true
        binding.fabChangePhoto.setOnClickListener {
            cameraHelper.showPhotoPicker()
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_editProfile)
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
