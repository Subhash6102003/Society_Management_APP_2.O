package com.mgbheights.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.mgbheights.android.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnSendReset.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                showError("Please enter your email address.")
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("Please enter a valid email address.")
                return@setOnClickListener
            }
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSendReset.isEnabled = false

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnSendReset.isEnabled = true
                    if (task.isSuccessful) {
                        val intent = Intent(this, ResetEmailSentActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        val msg = when (task.exception) {
                            is FirebaseAuthInvalidUserException ->
                                "No account found with this email."
                            else -> task.exception?.message ?: "Failed. Try again."
                        }
                        showError(msg)
                    }
                }
        }
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
