package com.mgbheights.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.databinding.ActivityLoginBinding
import com.mgbheights.android.ui.admin.AdminActivity
import com.mgbheights.android.ui.guard.GuardActivity
import com.mgbheights.android.ui.resident.ResidentActivity
import com.mgbheights.android.ui.tenant.TenantActivity
import com.mgbheights.android.ui.worker.WorkerActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RoleSelectRegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter email and password.")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser!!
                    user.reload().addOnCompleteListener {
                        if (!user.isEmailVerified) {
                            FirebaseAuth.getInstance().signOut()
                            binding.progressBar.visibility = View.GONE
                            binding.btnLogin.isEnabled = true
                            showError("Please verify your email before logging in.")
                            startActivity(Intent(this, VerifyEmailActivity::class.java))
                        } else {
                            routeByRole(user.uid)
                        }
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    showError(task.exception?.message ?: "Login failed.")
                }
            }
    }

    private fun routeByRole(uid: String) {
        val db = FirebaseFirestore.getInstance()
        val collections = listOf("admins", "residents", "tenants", "guards", "workers")
        var index = 0

        fun checkNext() {
            if (index >= collections.size) {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                FirebaseAuth.getInstance().signOut()
                showError("No account found. Contact your administrator.")
                return
            }
            db.collection(collections[index]).document(uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        binding.progressBar.visibility = View.GONE
                        val intent = when (doc.getString("role")) {
                            "admin"    -> Intent(this, AdminActivity::class.java)
                            "resident" -> Intent(this, ResidentActivity::class.java)
                            "tenant"   -> Intent(this, TenantActivity::class.java)
                            "guard"    -> Intent(this, GuardActivity::class.java)
                            "worker"   -> Intent(this, WorkerActivity::class.java)
                            else       -> null
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

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
