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

        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.payments.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressLoading.isVisible = true
                    binding.layoutEmpty.isVisible = false
                }
                is Resource.Success -> {
                    binding.progressLoading.isVisible = false
                    binding.swipeRefresh.isRefreshing = false
                    val payments = state.data
                    adapter.submitList(payments)
                    binding.rvPayments.isVisible = payments.isNotEmpty()
                    binding.layoutEmpty.isVisible = payments.isEmpty()
                }
                is Resource.Error -> {
                    binding.progressLoading.isVisible = false
                    binding.swipeRefresh.isRefreshing = false
                    binding.layoutEmpty.isVisible = true
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
