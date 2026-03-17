package com.mgbheights.android.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentEditProfileBinding
import com.mgbheights.android.util.CameraHelper
import com.mgbheights.android.util.PhotoCompressor
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var cameraHelper: CameraHelper
    private var newProfilePhotoBase64: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        
        cameraHelper = CameraHelper(this) { uri ->
            val base64 = PhotoCompressor.compressProfilePhoto(requireContext(), uri)
            if (base64 != null) {
                newProfilePhotoBase64 = base64
                PhotoCompressor.loadPhotoIntoView(binding.ivProfilePhoto, base64, R.drawable.ic_profile)
            }
        }
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        viewModel.user.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                val user = state.data
                binding.etName.setText(user.name)
                binding.etEmail.setText(user.email)
                binding.etFlatNumber.setText(user.flatNumber)
                binding.etTowerBlock.setText(user.towerBlock)
                
                if (newProfilePhotoBase64 == null) {
                    PhotoCompressor.loadPhotoIntoView(binding.ivProfilePhoto, user.profilePhotoUrl, R.drawable.ic_profile)
                }

                val isStaff = user.role == UserRole.WORKER || user.role == UserRole.SECURITY_GUARD || user.role == UserRole.SECURITY_GUARD_WORKER
                binding.tilFlatNumber.visibility = if (isStaff) View.GONE else View.VISIBLE
                binding.tilTowerBlock.visibility = if (isStaff) View.GONE else View.VISIBLE
            }
        }

        binding.layoutProfilePhoto.setOnClickListener {
            cameraHelper.showPhotoPicker()
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val flat = binding.etFlatNumber.text.toString().trim()
            val tower = binding.etTowerBlock.text.toString().trim()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If photo was changed, update it first or along with profile
            newProfilePhotoBase64?.let { viewModel.updateProfilePhoto(it) }
            
            viewModel.updateProfile(name, email, flat, tower)
        }

        viewModel.updateState.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else if (state is Resource.Error) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
