package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentForgotNewPasswordBinding

class ForgotPasswordNewFragment : Fragment() {

    private var _binding: FragmentForgotNewPasswordBinding? = null
    private val binding get() = _binding!!
    private val args: ForgotPasswordNewFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentForgotNewPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvEmail.text = args.email
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.btnSave.setOnClickListener {
            val newPass = binding.etNewPassword.text.toString().trim()
            val confirm = binding.etConfirm.text.toString().trim()

            if (newPass.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass != confirm) {
                Toast.makeText(requireContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false

            // We don't have user's old password, so we use sendPasswordResetEmail
            // After verification of OTP, user can reset via email link
            FirebaseAuth.getInstance()
                .sendPasswordResetEmail(args.email)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    AlertDialog.Builder(requireContext())
                        .setTitle("Password Reset Email Sent")
                        .setMessage(
                            "Your identity has been verified. " +
                            "We have sent a password reset link to ${args.email}. " +
                            "Open it to set your new password, then return and login."
                        )
                        .setPositiveButton("Go to Login") { _, _ ->
                            findNavController().navigate(
                                R.id.loginFragment,
                                null,
                                NavOptions.Builder()
                                    .setPopUpTo(R.id.nav_graph_auth, true)
                                    .build()
                            )
                        }
                        .setCancelable(false)
                        .show()
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), it.message ?: "Failed. Try again.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
