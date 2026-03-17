package com.mgbheights.android.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mgbheights.android.databinding.FragmentPaymentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    // Use standard lazy initialization for arguments to avoid SafeArgs generation issues
    private val billId: String by lazy {
        arguments?.getString("billId") ?: ""
    }
    
    private val amount: Float by lazy {
        arguments?.getFloat("amount") ?: 0f
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (billId.isBlank()) {
            Toast.makeText(requireContext(), "Invalid payment data", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        
        binding.tvAmount.text = "₹${amount}"
        
        binding.btnPayNow.setOnClickListener {
            // Integration with Razorpay SDK would go here
            Toast.makeText(requireContext(), "Processing payment of ₹${amount}...", Toast.LENGTH_LONG).show()
            
            // Simulating successful payment for now
            binding.layoutSuccess.visibility = View.VISIBLE
            binding.btnPayNow.isEnabled = false
        }
        
        binding.btnDone.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
