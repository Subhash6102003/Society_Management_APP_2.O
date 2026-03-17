package com.mgbheights.android.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentAdminDashboardBinding

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefresh.setOnRefreshListener {
            loadStats()
        }

        binding.cardResidents.setOnClickListener {
            findNavController().navigate(R.id.adminResidentsFragment)
        }
        
        binding.cardTenants.setOnClickListener {
            findNavController().navigate(R.id.adminTenantsFragment)
        }
        
        binding.cardGuards.setOnClickListener {
            findNavController().navigate(R.id.adminGuardsFragment)
        }
        
        binding.cardWorkers.setOnClickListener {
            findNavController().navigate(R.id.adminWorkersFragment)
        }

        binding.cardPendingComplaints.setOnClickListener {
            findNavController().navigate(R.id.adminComplaintsFragment)
        }

        loadStats()
    }

    private fun loadStats() {
        binding.swipeRefresh.isRefreshing = true
        
        val collections = mapOf(
            "residents" to binding.tvResidentCount,
            "tenants" to binding.tvTenantCount,
            "guards" to binding.tvGuardCount,
            "workers" to binding.tvWorkerCount
        )

        var loadedCount = 0
        for ((col, textView) in collections) {
            db.collection(col).get().addOnSuccessListener { 
                textView.text = it.size().toString()
                loadedCount++
                if (loadedCount == collections.size + 1) binding.swipeRefresh.isRefreshing = false
            }
        }

        db.collection("complaints").whereEqualTo("status", "pending").get().addOnSuccessListener {
            binding.tvPendingComplaintsCount.text = it.size().toString()
            loadedCount++
            if (loadedCount == collections.size + 1) binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
