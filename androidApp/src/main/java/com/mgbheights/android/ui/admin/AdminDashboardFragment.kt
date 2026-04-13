package com.mgbheights.android.ui.admin

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentAdminDashboardBinding

class AdminDashboardFragment : Fragment(R.layout.fragment_admin_dashboard) {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAdminDashboardBinding.bind(view)

        setupClickListeners()
        loadStats()

        binding.swipeRefresh.setOnRefreshListener {
            loadStats()
        }
    }

    private fun setupClickListeners() {
        binding.btnManageUsers.setOnClickListener {
            findNavController().navigate(R.id.adminUserManagementFragment)
        }
        binding.btnManageApprovals.setOnClickListener {
            findNavController().navigate(R.id.adminUserManagementFragment)
        }
        // ... other buttons as needed ...
    }

    private fun loadStats() {
        binding.swipeRefresh.isRefreshing = true
        val db = FirebaseFirestore.getInstance()

        db.collection("users").get().addOnSuccessListener { snapshot ->
            binding.tvTotalUsers.text = snapshot.size().toString()
            val pending = snapshot.documents.count { it.getBoolean("isApproved") == false }
            binding.tvPendingApprovals.text = pending.toString()
        }

        // FIX 1: Load pending complaints count
        db.collection("complaints")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                binding.tvPendingComplaints.text = snapshot.size().toString()
                binding.swipeRefresh.isRefreshing = false
            }
            .addOnFailureListener {
                binding.tvPendingComplaints.text = "0"
                binding.swipeRefresh.isRefreshing = false
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
