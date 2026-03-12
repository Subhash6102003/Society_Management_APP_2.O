package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.mgbheights.android.databinding.FragmentForgotPasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.tvBackToLogin.setOnClickListener { findNavController().navigateUp() }

        binding.btnSendReset.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tvError.text = "Please enter a valid email address"
                binding.tvError.isVisible = true
                binding.tvSuccess.isVisible = false
                return@setOnClickListener
            }

            binding.tvError.isVisible = false
            binding.tvSuccess.isVisible = false
            binding.progressLoading.isVisible = true
            binding.btnSendReset.isEnabled = false

            firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    binding.progressLoading.isVisible = false
                    binding.tvSuccess.text = "Password reset email sent! Check your inbox."
                    binding.tvSuccess.isVisible = true
                    binding.btnSendReset.isEnabled = true
                }
                .addOnFailureListener { e ->
                    binding.progressLoading.isVisible = false
                    binding.tvError.text = e.localizedMessage ?: "Failed to send reset email"
                    binding.tvError.isVisible = true
                    binding.btnSendReset.isEnabled = true
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

