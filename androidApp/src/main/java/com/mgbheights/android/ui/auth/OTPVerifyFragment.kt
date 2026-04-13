package com.mgbheights.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentOtpVerifyBinding
import com.mgbheights.android.ui.admin.AdminActivity
import com.mgbheights.android.ui.guard.GuardActivity
import com.mgbheights.android.ui.resident.ResidentActivity
import com.mgbheights.android.ui.tenant.TenantActivity
import com.mgbheights.android.ui.worker.WorkerActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OTPVerifyFragment : Fragment() {

    private var _binding: FragmentOtpVerifyBinding? = null
    private val binding get() = _binding!!
    private val args: OTPVerifyFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOtpVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSubtitle.text = "Enter the 6-digit code sent to ${args.email}"

        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()
            if (otp.length == 6) {
                verifyOtp(otp)
            } else {
                Toast.makeText(requireContext(), "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvResend.setOnClickListener {
            // Resend logic: navigate back or trigger resend in EnterEmailFragment
            findNavController().navigateUp()
        }
    }

    private fun verifyOtp(otp: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnVerify.isEnabled = false

        val db = FirebaseFirestore.getInstance()
        db.collection("otpCodes").document(args.email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val savedOtp = document.getString("code")
                    val expiresAt = document.getTimestamp("expiresAt")
                    val verified = document.getBoolean("verified") ?: false

                    if (verified) {
                        Toast.makeText(requireContext(), "OTP already used", Toast.LENGTH_SHORT).show()
                        resetUI()
                        return@addOnSuccessListener
                    }

                    if (expiresAt != null && expiresAt.toDate().before(java.util.Date())) {
                        Toast.makeText(requireContext(), "OTP expired", Toast.LENGTH_SHORT).show()
                        resetUI()
                        return@addOnSuccessListener
                    }

                    if (savedOtp == otp) {
                        // Success! Mark as verified
                        db.collection("otpCodes").document(args.email).update("verified", true)
                        
                        // Sign in anonymously for now to get a UID, then link or just use UID for Firestore
                        FirebaseAuth.getInstance().signInAnonymously()
                            .addOnSuccessListener { authResult ->
                                checkUserExists(authResult.user?.uid ?: "")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Auth failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                resetUI()
                            }
                    } else {
                        Toast.makeText(requireContext(), "Invalid OTP", Toast.LENGTH_SHORT).show()
                        resetUI()
                    }
                } else {
                    Toast.makeText(requireContext(), "OTP not found", Toast.LENGTH_SHORT).show()
                    resetUI()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                resetUI()
            }
    }

    private fun checkUserExists(uid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("userType") ?: "resident"
                    navigateToHome(role)
                } else {
                    // New User -> Create Profile
                    val action = OTPVerifyFragmentDirections.actionOtpVerifyFragmentToCreateProfileFragment()
                    findNavController().navigate(action)
                }
            }
            .addOnFailureListener {
                resetUI()
            }
    }

    private fun navigateToHome(role: String) {
        val activityClass = when (role.lowercase()) {
            "admin" -> AdminActivity::class.java
            "guard" -> GuardActivity::class.java
            "tenant" -> TenantActivity::class.java
            "worker" -> WorkerActivity::class.java
            else -> ResidentActivity::class.java
        }
        val intent = Intent(requireContext(), activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun resetUI() {
        binding.progressBar.visibility = View.GONE
        binding.btnVerify.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
