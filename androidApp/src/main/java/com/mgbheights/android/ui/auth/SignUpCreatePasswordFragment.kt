package com.mgbheights.android.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mgbheights.android.databinding.FragmentSignupCreatePasswordBinding
import com.mgbheights.android.ui.admin.AdminActivity
import com.mgbheights.android.ui.guard.GuardActivity
import com.mgbheights.android.ui.resident.ResidentActivity
import com.mgbheights.android.ui.tenant.TenantActivity
import com.mgbheights.android.ui.worker.WorkerActivity
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SignUpCreatePasswordFragment : Fragment() {

    private var _binding: FragmentSignupCreatePasswordBinding? = null
    private val binding get() = _binding!!
    private val args: SignUpCreatePasswordFragmentArgs by navArgs()
    private var profilePhotoUri: Uri? = null
    private var idPhotoUri: Uri? = null

    private val pickProfileImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profilePhotoUri = it
            binding.ivProfile.setImageURI(it)
        }
    }

    private val pickIdImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            idPhotoUri = it
            binding.ivIdPhoto.setImageURI(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignupCreatePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()

        binding.btnUploadProfile.setOnClickListener { pickProfileImage.launch("image/*") }
        binding.btnUploadId.setOnClickListener { pickIdImage.launch("image/*") }

        binding.btnCreateAccount.setOnClickListener { attemptAccountCreation() }
    }

    private fun setupSpinners() {
        val roles = arrayOf("Resident", "Tenant", "Security Guard", "Worker", "Admin")
        val roleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roles)
        binding.actRole.setAdapter(roleAdapter)

        binding.actRole.setOnItemClickListener { _, _, position, _ ->
            val selectedRole = roles[position]
            binding.tilFlat.visibility = if (selectedRole == "Resident" || selectedRole == "Tenant") View.VISIBLE else View.GONE
            binding.tilOwner.visibility = if (selectedRole == "Tenant") View.VISIBLE else View.GONE
            binding.tilJobType.visibility = if (selectedRole == "Worker") View.VISIBLE else View.GONE
            binding.tilAdminCode.visibility = if (selectedRole == "Admin") View.VISIBLE else View.GONE
            binding.cardIdPhoto.visibility = if (selectedRole == "Security Guard" || selectedRole == "Worker") View.VISIBLE else View.GONE
        }

        val jobTypes = arrayOf("Plumber", "Electrician", "Cleaner", "Gardener", "Other")
        val jobAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, jobTypes)
        binding.actJobType.setAdapter(jobAdapter)
    }

    private fun attemptAccountCreation() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirm = binding.etConfirm.text.toString().trim()
        val role = binding.actRole.text.toString()

        if (name.isEmpty() || phone.isEmpty() || password.isEmpty() || role.isEmpty()) {
            Toast.makeText(requireContext(), "Fill in all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirm) {
            Toast.makeText(requireContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreateAccount.isEnabled = false

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(args.email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserToFirestore(task.result.user!!, role, name, phone)
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCreateAccount.isEnabled = true
                    Toast.makeText(requireContext(), task.exception?.message ?: "Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirestore(user: FirebaseUser, role: String, name: String, phone: String) {
        lifecycleScope.launch {
            val roleKey = role.lowercase()
            val collection = when (roleKey) {
                "admin" -> "admins"
                "resident" -> "residents"
                "tenant" -> "tenants"
                "security guard" -> "guards"
                "worker" -> "workers"
                else -> "residents"
            }

            val finalRoleKey = if (roleKey == "security guard") "guard" else roleKey

            val userData = hashMapOf<String, Any>(
                "name" to name,
                "email" to args.email,
                "phone" to phone,
                "role" to finalRoleKey,
                "createdAt" to FieldValue.serverTimestamp()
            )

            profilePhotoUri?.let {
                val url = uploadPhoto(it, "$collection/${user.uid}/profile.jpg")
                userData["photoUrl"] = url
            }

            idPhotoUri?.let {
                val url = uploadPhoto(it, "$collection/${user.uid}/id_photo.jpg")
                userData["idPhotoUrl"] = url
            }

            if (roleKey == "resident" || roleKey == "tenant") {
                userData["flatNumber"] = binding.etFlat.text.toString().trim()
            }
            if (roleKey == "tenant") {
                userData["ownerName"] = binding.etOwner.text.toString().trim()
            }
            if (roleKey == "worker") {
                userData["jobType"] = binding.actJobType.text.toString()
            }

            FirebaseFirestore.getInstance().collection(collection).document(user.uid).set(userData).await()
            user.sendEmailVerification().await()

            val intent = when (finalRoleKey) {
                "admin" -> Intent(requireContext(), AdminActivity::class.java)
                "resident" -> Intent(requireContext(), ResidentActivity::class.java)
                "tenant" -> Intent(requireContext(), TenantActivity::class.java)
                "guard" -> Intent(requireContext(), GuardActivity::class.java)
                "worker" -> Intent(requireContext(), WorkerActivity::class.java)
                else -> Intent(requireContext(), ResidentActivity::class.java)
            }
            startActivity(intent)
            requireActivity().finishAffinity()
        }
    }

    private suspend fun uploadPhoto(uri: Uri, path: String): String {
        val ref = FirebaseStorage.getInstance().reference.child(path)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
