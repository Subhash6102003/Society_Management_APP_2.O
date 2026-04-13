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
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.databinding.FragmentEnterEmailBinding
import java.util.Date
import kotlin.random.Random

class EnterEmailFragment : Fragment() {

    private var _binding: FragmentEnterEmailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEnterEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSendOtp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (validateEmail(email)) {
                sendOtp(email)
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            true
        } else {
            binding.tilEmail.error = "Invalid email address"
            false
        }
    }

    private fun sendOtp(email: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSendOtp.isEnabled = false

        val otp = (100000 + Random.nextInt(900000)).toString()
        val expiresAt = Date(System.currentTimeMillis() + 5 * 60 * 1000) // 5 minutes

        val otpData = hashMapOf(
            "code" to otp,
            "expiresAt" to Timestamp(expiresAt),
            "verified" to false
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("otpCodes").document(email).set(otpData)
            .addOnSuccessListener {
                // In a real scenario, the Firebase Trigger Email extension would pick this up
                // or you'd call an API here. For now, we assume it's sent.
                binding.progressBar.visibility = View.GONE
                binding.btnSendOtp.isEnabled = true
                
                val action = EnterEmailFragmentDirections.actionEnterEmailFragmentToOtpVerifyFragment(email)
                findNavController().navigate(action)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnSendOtp.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
