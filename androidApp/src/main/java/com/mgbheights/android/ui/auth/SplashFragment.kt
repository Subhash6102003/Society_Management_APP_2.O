package com.mgbheights.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.ui.admin.AdminActivity
import com.mgbheights.android.ui.guard.GuardActivity
import com.mgbheights.android.ui.resident.ResidentActivity
import com.mgbheights.android.ui.tenant.TenantActivity
import com.mgbheights.android.ui.worker.WorkerActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            delay(2000)
            checkAuthAndNavigate()
        }
    }

    private fun checkAuthAndNavigate() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            findNavController().navigate(R.id.action_splashFragment_to_enterEmailFragment)
        } else {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("userType") ?: "resident"
                        navigateToHome(role)
                    } else {
                        // Profile not created yet
                        findNavController().navigate(R.id.action_splashFragment_to_enterEmailFragment)
                    }
                }
                .addOnFailureListener {
                    findNavController().navigate(R.id.action_splashFragment_to_enterEmailFragment)
                }
        }
    }

    private fun navigateToHome(role: String) {
        val activityClass = when (role.lowercase()) {
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
}
