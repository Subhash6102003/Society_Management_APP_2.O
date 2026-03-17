package com.mgbheights.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.databinding.ActivityVerifyEmailBinding
import com.mgbheights.android.ui.admin.AdminActivity
import com.mgbheights.android.ui.guard.GuardActivity
import com.mgbheights.android.ui.resident.ResidentActivity
import com.mgbheights.android.ui.tenant.TenantActivity
import com.mgbheights.android.ui.worker.WorkerActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VerifyEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerifyEmailBinding
    private var resendJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                goToLogin()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            user.reload().addOnCompleteListener {
                binding.progressBar.visibility = View.GONE
                if (user.isEmailVerified) {
                    routeVerifiedUser(user.uid)
                } else {
                    showError("Email not verified yet. Please check your inbox and click the link.")
                }
            }
        }

        binding.btnResend.setOnClickListener {
            FirebaseAuth.getInstance().currentUser
                ?.sendEmailVerification()
                ?.addOnSuccessListener {
                    Toast.makeText(this, "Verification email resent.", Toast.LENGTH_SHORT).show()
                    startResendCooldown()
                }
                ?.addOnFailureListener { showError(it.message ?: "Failed.") }
        }

        binding.tvLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            goToLogin()
        }
    }

    private fun routeVerifiedUser(uid: String) {
        val db = FirebaseFirestore.getInstance()
        val collections = listOf("admins", "residents", "tenants", "guards", "workers")
        var index = 0

        fun checkNext() {
            if (index >= collections.size) {
                FirebaseAuth.getInstance().signOut()
                goToLogin()
                return
            }
            db.collection(collections[index]).document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val intent = when (doc.getString("role")) {
                            "admin" -> Intent(this, AdminActivity::class.java)
                            "resident" -> Intent(this, ResidentActivity::class.java)
                            "tenant" -> Intent(this, TenantActivity::class.java)
                            "guard" -> Intent(this, GuardActivity::class.java)
                            "worker" -> Intent(this, WorkerActivity::class.java)
                            else -> null
                        }
                        if (intent != null) {
                            startActivity(intent)
                            finishAffinity()
                        } else {
                            index++
                            checkNext()
                        }
                    } else {
                        index++
                        checkNext()
                    }
                }
                .addOnFailureListener {
                    index++
                    checkNext()
                }
        }
        checkNext()
    }

    private fun startResendCooldown() {
        binding.btnResend.isEnabled = false
        resendJob?.cancel()
        resendJob = lifecycleScope.launch {
            for (i in 60 downTo 1) {
                binding.btnResend.text = "Resend in ${i}s"
                delay(1000L)
            }
            binding.btnResend.text = "Resend Verification Email"
            binding.btnResend.isEnabled = true
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finishAffinity()
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
