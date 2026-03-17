package com.mgbheights.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.mgbheights.android.databinding.ActivityResetEmailSentBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ResetEmailSentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetEmailSentBinding
    private var countdownJob: Job? = null
    private val email by lazy { intent.getStringExtra("email") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetEmailSentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvEmail.text = email

        binding.btnOpenEmail.setOnClickListener {
            val i = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_EMAIL)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try { startActivity(i) }
            catch (e: Exception) { showError("No email app found.") }
        }

        binding.btnResend.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset link resent.", Toast.LENGTH_SHORT).show()
                    startCooldown()
                }
                .addOnFailureListener { showError(it.message ?: "Failed.") }
        }

        binding.tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun startCooldown() {
        binding.btnResend.isEnabled = false
        countdownJob?.cancel()
        countdownJob = lifecycleScope.launch {
            for (i in 60 downTo 1) {
                binding.btnResend.text = "Resend in ${i}s"
                delay(1000L)
            }
            binding.btnResend.text = "Resend Link"
            binding.btnResend.isEnabled = true
        }
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
