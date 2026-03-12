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
import com.mgbheights.android.databinding.FragmentEditProfileBinding
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.util.Resource
import com.mgbheights.shared.util.Validators
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        // Pre-fill current values
        viewModel.user.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                val user = state.data
                binding.etName.setText(user.name)
                binding.etEmail.setText(user.email)
                binding.etFlatNumber.setText(user.flatNumber)
                binding.etTowerBlock.setText(user.towerBlock)
                // Admin sees different note
                if (user.role == UserRole.ADMIN) {
                    binding.tvEditNote.text = "As admin, your changes will be applied immediately."
                }
            }
        }
        binding.btnSubmit.setOnClickListener {
            val name = binding.etName.text?.toString()?.trim() ?: ""
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val flat = binding.etFlatNumber.text?.toString()?.trim() ?: ""
            val tower = binding.etTowerBlock.text?.toString()?.trim() ?: ""
            if (name.isBlank()) {
                showError("Name cannot be empty")
                return@setOnClickListener
            }
            if (email.isNotBlank() && !Validators.isValidEmailRequired(email)) {
                showError("Please enter a valid email address")
                return@setOnClickListener
            }
            viewModel.submitEditRequest(name, email, flat, tower)
        }
        viewModel.editRequestState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressLoading.isVisible = true
                    binding.btnSubmit.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressLoading.isVisible = false
                    val user = viewModel.user.value
                    val isAdmin = (user as? Resource.Success)?.data?.role == UserRole.ADMIN
                    val msg = if (isAdmin) "Profile updated successfully!" else "Edit request submitted for admin approval."
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    binding.progressLoading.isVisible = false
                    binding.btnSubmit.isEnabled = true
                    showError(state.message)
                }
            }
        }
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
