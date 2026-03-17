package com.mgbheights.android.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mgbheights.android.databinding.ActivityAdminRegisterBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminRegisterBinding
    private var profileImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImageUri = it
            binding.ivProfile.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivProfile.setOnClickListener { pickImage.launch("image/*") }

        binding.btnRegister.setOnClickListener { attemptRegistration() }
    }

    private fun attemptRegistration() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val adminCode = binding.etAdminCode.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || adminCode.isEmpty()) {
            showError("Please fill all fields")
            return
        }

        if (password != confirmPassword) {
            showError("Passwords do not match")
            return
        }

        if (adminCode != "MGBAdmin2025") {
            showError("Invalid Admin Code")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            try {
                val authResult = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user!!

                val photoUrl = profileImageUri?.let {
                    uploadPhoto(it, "admins/${user.uid}/profile.jpg")
                } ?: ""

                val userData = hashMapOf<String, Any>(
                    "name" to name,
                    "email" to email,
                    "phoneNumber" to phone,
                    "photoUrl" to photoUrl,
                    "role" to "admin",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                FirebaseFirestore.getInstance().collection("admins").document(user.uid).set(userData).await()
                user.sendEmailVerification().await()

                startActivity(Intent(this@AdminRegisterActivity, VerifyEmailActivity::class.java))
                finishAffinity()
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnRegister.isEnabled = true
                showError(e.message ?: "Registration failed")
            }
        }
    }

    private suspend fun uploadPhoto(uri: Uri, path: String): String {
        val ref = FirebaseStorage.getInstance().reference.child(path)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    private fun showError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
