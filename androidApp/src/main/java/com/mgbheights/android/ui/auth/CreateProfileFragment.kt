package com.mgbheights.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.databinding.FragmentCreateProfileBinding
import com.mgbheights.android.ui.admin.AdminActivity
import com.mgbheights.android.ui.guard.GuardActivity
import com.mgbheights.android.ui.resident.ResidentActivity
import com.mgbheights.android.ui.tenant.TenantActivity
import com.mgbheights.android.ui.worker.WorkerActivity

class CreateProfileFragment : Fragment() {

    private var _binding: FragmentCreateProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val roles = listOf("Resident", "Tenant", "Guard", "Worker")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter

        binding.btnCreateAccount.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val flat = binding.etFlatNumber.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString()

            if (name.isEmpty() || flat.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createProfile(name, flat, role)
        }
    }

    private fun createProfile(name: String, flat: String, role: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCreateAccount.isEnabled = false

        val userData = hashMapOf(
            "id" to user.uid,
            "name" to name,
            "email" to (user.email ?: ""),
            "flatNumber" to flat,
            "userType" to role.lowercase(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(user.uid).set(userData)
            .addOnSuccessListener {
                navigateToHome(role.lowercase())
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnCreateAccount.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHome(role: String) {
        val activityClass = when (role) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
