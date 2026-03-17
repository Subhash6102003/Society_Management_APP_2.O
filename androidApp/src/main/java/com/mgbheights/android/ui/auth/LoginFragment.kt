package com.mgbheights.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentLoginBinding
import com.mgbheights.android.ui.admin.AdminActivity
import com.mgbheights.android.ui.guard.GuardActivity
import com.mgbheights.android.ui.resident.ResidentActivity
import com.mgbheights.android.ui.tenant.TenantActivity
import com.mgbheights.android.ui.worker.WorkerActivity

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordEmailFragment)
        }
        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpEmailFragment)
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val pass  = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            showError("Enter email and password.")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    showError(task.exception?.message ?: "Login failed.")
                    return@addOnCompleteListener
                }
                
                val user = FirebaseAuth.getInstance().currentUser!!
                user.reload().addOnCompleteListener {
                    if (!user.isEmailVerified) {
                        FirebaseAuth.getInstance().signOut()
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        showError("Please verify your email before logging in.")
                        // Navigate to verify email if needed, but OTP flow is preferred for signup
                    } else {
                        routeByRole(user.uid)
                    }
                }
            }
    }

    private fun routeByRole(uid: String) {
        val db   = FirebaseFirestore.getInstance()
        val cols = listOf("admins","residents","tenants","guards","workers")
        var i    = 0
        fun next() {
            if (i >= cols.size) {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                FirebaseAuth.getInstance().signOut()
                showErrorDialog("No account found. Contact your administrator.")
                return
            }
            db.collection(cols[i]).document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        binding.progressBar.visibility = View.GONE
                        val intent = when (doc.getString("role")) {
                            "admin"    -> Intent(requireContext(), AdminActivity::class.java)
                            "resident" -> Intent(requireContext(), ResidentActivity::class.java)
                            "tenant"   -> Intent(requireContext(), TenantActivity::class.java)
                            "guard"    -> Intent(requireContext(), GuardActivity::class.java)
                            "worker"   -> Intent(requireContext(), WorkerActivity::class.java)
                            else       -> null
                        }
                        if (intent != null) {
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finishAffinity()
                        } else { i++; next() }
                    } else { i++; next() }
                }
                .addOnFailureListener { i++; next() }
        }
        next()
    }

    private fun showError(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorDialog(msg: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
