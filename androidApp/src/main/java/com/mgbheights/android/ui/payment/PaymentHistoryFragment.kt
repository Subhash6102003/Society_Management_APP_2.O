package com.mgbheights.android.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentPaymentHistoryBinding
import com.mgbheights.android.ui.adapter.PaymentHistoryAdapter
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentHistoryFragment : Fragment() {

    private var _binding: FragmentPaymentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PaymentHistoryViewModel by viewModels()
    private lateinit var adapter: PaymentHistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        adapter = PaymentHistoryAdapter()
        binding.rvPayments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPayments.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        binding.progressLoading.isVisible = true
        FirebaseFirestore.getInstance()
            .collection("bills")
            .whereEqualTo("residentId", uid)
            .whereEqualTo("status", "PAID")
            .orderBy("paidAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                binding.progressLoading.isVisible = false
                if (snapshot.isEmpty) {
                    binding.layoutEmpty.isVisible = true
                    binding.rvPayments.isVisible = false
                    return@addOnSuccessListener
                }
                val bills = snapshot.toObjects(com.mgbheights.shared.domain.model.MaintenanceBill::class.java)
                adapter.submitList(bills)
                binding.layoutEmpty.isVisible = false
                binding.rvPayments.isVisible = true
            }
            .addOnFailureListener {
                binding.progressLoading.isVisible = false
                binding.layoutEmpty.isVisible = true
                binding.rvPayments.isVisible = false
                android.widget.Toast.makeText(requireContext(), "Failed to load payment history.", android.widget.Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
