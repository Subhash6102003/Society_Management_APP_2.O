package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentSignupBinding
import com.mgbheights.android.util.CameraHelper
import com.mgbheights.android.util.PhotoCompressor
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Constants
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()
    private var accountCreated = false

    // Camera helpers for profile photo and ID proof
    private lateinit var profilePhotoCameraHelper: CameraHelper
    private lateinit var idProofCameraHelper: CameraHelper

    // Temporary Base64 strings
    private var profilePhotoBase64: String = ""
    private var idProofBase64: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)

        // Must register ActivityResult launchers BEFORE onViewCreated
        profilePhotoCameraHelper = CameraHelper(this) { uri ->
            val base64 = PhotoCompressor.compressProfilePhoto(requireContext(), uri)
            if (base64 != null) {
                profilePhotoBase64 = base64
                PhotoCompressor.loadPhotoIntoView(binding.ivProfilePhoto, base64, R.drawable.ic_profile)
                binding.btnCaptureProfile.text = "Change Photo"
                Toast.makeText(requireContext(), "Profile photo added!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to process photo", Toast.LENGTH_SHORT).show()
            }
        }

        idProofCameraHelper = CameraHelper(this) { uri ->
            val base64 = PhotoCompressor.compressIdProof(requireContext(), uri)
            if (base64 != null) {
                idProofBase64 = base64
                PhotoCompressor.loadPhotoIntoView(binding.ivIdProof, base64, R.drawable.ic_notices)
                binding.btnCaptureIdProof.text = "Change ID Proof"
                Toast.makeText(requireContext(), "ID proof added!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to process photo", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val roleName = viewModel.selectedRole.name.replace("_", " ").lowercase()
            .replaceFirstChar { it.uppercase() }
        binding.tvRoleBadge.text = "Registering as: $roleName"

        // Determine role type
        val isStaffRole = viewModel.selectedRole in listOf(
            UserRole.SECURITY_GUARD, UserRole.WORKER, UserRole.SECURITY_GUARD_WORKER
        )
        val needsIdProof = viewModel.selectedRole in listOf(
            UserRole.TENANT, UserRole.SECURITY_GUARD, UserRole.WORKER, UserRole.SECURITY_GUARD_WORKER
        )

        // Show/hide sections based on role
        binding.cardHouseDetails.isVisible = !isStaffRole
        binding.cardProfilePhoto.isVisible = true  // All roles can add profile photo
        binding.cardIdProof.isVisible = needsIdProof  // ID proof for non-resident roles

        // Wire up camera buttons
        binding.btnCaptureProfile.setOnClickListener {
            profilePhotoCameraHelper.showPhotoPicker()
        }
        binding.btnCaptureIdProof.setOnClickListener {
            idProofCameraHelper.showPhotoPicker()
        }

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnRegister.setOnClickListener { handleRegistration(isStaffRole) }
        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signUp_to_login)
        }
        observeViewModel()
    }

    private fun handleRegistration(isStaffRole: Boolean) {
        val name = binding.etName.text?.toString()?.trim() ?: ""
        val phone = binding.etPhone.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""
        val flat = binding.etFlatNumber.text?.toString()?.trim() ?: ""
        val tower = binding.etTowerBlock.text?.toString()?.trim() ?: ""

        if (name.isBlank()) { showError("Please enter your name"); return }
        if (!Validators.isValidPhoneNumber(phone)) { showError("Please enter a valid 10-digit phone number"); return }
        if (!Validators.isValidEmailRequired(email)) { showError("Please enter a valid email"); return }
        if (!Validators.isValidPassword(password)) { showError("Password must be at least 6 characters"); return }
        if (!isStaffRole && flat.isBlank()) { showError("Please enter your flat number"); return }

        // Store signup form data in ViewModel for use after email verification
        viewModel.signUpName = name
        viewModel.signUpPhone = phone
        viewModel.signUpFlatNumber = flat
        viewModel.signUpTowerBlock = tower
        viewModel.signUpProfilePhoto = profilePhotoBase64
        viewModel.signUpIdProof = idProofBase64
        viewModel.isFromSignUp = true

        // Create the Firebase Auth account (this also sends verification email)
        viewModel.signUpWithEmail(email, password)
    }

    private fun observeViewModel() {
        viewModel.signUpState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success -> {
                    setLoading(false)
                    val email = binding.etEmail.text?.toString()?.trim() ?: ""
                    val isAdminEmail = email.equals(Constants.ADMIN_DEFAULT_EMAIL, ignoreCase = true)

                    if (isAdminEmail) {
                        if (!accountCreated) {
                            accountCreated = true
                            viewModel.completeSignUpProfile(
                                viewModel.signUpName,
                                viewModel.signUpFlatNumber,
                                viewModel.signUpTowerBlock
                            )
                        } else {
                            findNavController().navigate(R.id.action_signUp_to_dashboard)
                        }
                    } else {
                        findNavController().navigate(R.id.action_signUp_to_emailVerification)
                    }
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
        binding.btnRegister.isEnabled = !loading
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
