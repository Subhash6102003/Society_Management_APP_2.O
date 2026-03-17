package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.databinding.FragmentVerifyOtpBinding

class ForgotPasswordVerifyOtpFragment : Fragment() {

    private var _binding: FragmentVerifyOtpBinding? = null
    private val binding get() = _binding!!
    private val args: ForgotPasswordVerifyOtpFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVerifyOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvEmail.text = args.email
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.btnVerify.setOnClickListener {
            val enteredOtp = binding.etOtp.text.toString().trim()
            if (enteredOtp.length != 6) {
                Toast.makeText(requireContext(), "Enter the 6-digit code.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnVerify.isEnabled = false

            FirebaseFirestore.getInstance()
                .collection("otps")
                .document(args.email)
                .get()
                .addOnSuccessListener { doc ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnVerify.isEnabled = true

                    if (!doc.exists()) {
                        Toast.makeText(requireContext(), "Code expired. Please request a new one.", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val storedOtp = doc.getString("otp") ?: ""
                    val expiresAt = doc.getTimestamp("expiresAt")
                    val used = doc.getBoolean("used") ?: false
                    val now = Timestamp.now()

                    when {
                        used -> Toast.makeText(requireContext(), "This code has already been used.", Toast.LENGTH_SHORT).show()
                        expiresAt != null && now > expiresAt -> Toast.makeText(requireContext(), "Code expired.", Toast.LENGTH_SHORT).show()
                        enteredOtp != storedOtp -> Toast.makeText(requireContext(), "Incorrect code.", Toast.LENGTH_SHORT).show()
                        else -> {
                            doc.reference.update("used", true)
                            val action = ForgotPasswordVerifyOtpFragmentDirections.actionToForgotNewPass(args.email)
                            findNavController().navigate(action)
                        }
                    }
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.btnVerify.isEnabled = true
                    Toast.makeText(requireContext(), "Verification failed.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
