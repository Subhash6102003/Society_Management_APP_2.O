package com.mgbheights.android.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mgbheights.android.databinding.ActivityGuardRegisterBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class GuardRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuardRegisterBinding
    private var profileImageUri: Uri? = null
    private var idImageUri: Uri? = null

    private val pickProfileImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImageUri = it
            binding.ivProfile.setImageURI(it)
        }
    }

    private val pickIdImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            idImageUri = it
            binding.ivIdPhoto.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuardRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivProfile.setOnClickListener { pickProfileImage.launch("image/*") }
        binding.btnUploadId.setOnClickListener { pickIdImage.launch("image/*") }

        binding.btnRegister.setOnClickListener { attemptRegistration() }
    }

    private fun attemptRegistration() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            showError("Please fill all fields")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Invalid email address")
            return
        }

        if (password != confirmPassword) {
            showError("Passwords do not match")
            return
        }

        if (idImageUri == null) {
            showError("ID Photo is required")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        lifecycleScope.launch {
            try {
                val authResult = FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user!!

                val profilePhotoUrl = profileImageUri?.let {
                    uploadPhoto(it, "guards/${user.uid}/profile.jpg")
                } ?: ""

                val idPhotoUrl = uploadPhoto(idImageUri!!, "guards/${user.uid}/id_photo.jpg")

                val userData = hashMapOf<String, Any>(
                    "name" to name,
                    "email" to email,
                    "phoneNumber" to phone,
                    "photoUrl" to profilePhotoUrl,
                    "idPhotoUrl" to idPhotoUrl,
                    "role" to "guard",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                FirebaseFirestore.getInstance().collection("guards").document(user.uid).set(userData).await()
                user.sendEmailVerification().await()

                startActivity(Intent(this@GuardRegisterActivity, VerifyEmailActivity::class.java))
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
