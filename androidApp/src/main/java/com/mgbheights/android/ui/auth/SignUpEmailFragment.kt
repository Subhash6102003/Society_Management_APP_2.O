package com.mgbheights.android.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.databinding.FragmentSignupEmailBinding
import java.util.Date

class SignUpEmailFragment : Fragment() {

    private var _binding: FragmentSignupEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignupEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        binding.btnSendCode.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Enter a valid email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnSendCode.isEnabled = false

            val otp = (100000..999999).random().toString()

            val otpData = hashMapOf(
                "otp" to otp,
                "email" to email,
                "createdAt" to FieldValue.serverTimestamp(),
                "expiresAt" to Timestamp(Date(System.currentTimeMillis() + 10 * 60 * 1000)),
                "used" to false
            )

            FirebaseFirestore.getInstance()
                .collection("otps")
                .document(email)
                .set(otpData)
                .addOnSuccessListener {
                    val action = SignUpEmailFragmentDirections.actionToSignupVerifyOtp(email, "signup")
                    findNavController().navigate(action)
                    binding.progressBar.visibility = View.GONE
                    binding.btnSendCode.isEnabled = true
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSendCode.isEnabled = true
                    Toast.makeText(requireContext(), it.message ?: "Failed to send code.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
