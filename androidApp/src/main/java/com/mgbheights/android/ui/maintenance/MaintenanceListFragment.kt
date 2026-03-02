package com.mgbheights.android.ui.maintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentMaintenanceListBinding
import com.mgbheights.android.ui.adapter.BillAdapter
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MaintenanceListFragment : Fragment() {

    private var _binding: FragmentMaintenanceListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MaintenanceViewModel by viewModels()
    private lateinit var billAdapter: BillAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMaintenanceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupTabs() {
        val filters = listOf("All", "Pending", "Overdue", "Paid")
        filters.forEach { binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it)) }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { viewModel.setFilter(tab?.text?.toString() ?: "All") }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        billAdapter = BillAdapter(
            onPayClick = { bill ->
                val action = MaintenanceListFragmentDirections.actionMaintenanceToBillDetail(bill.id)
                findNavController().navigate(action)
            },
            onItemClick = { bill ->
                val action = MaintenanceListFragmentDirections.actionMaintenanceToBillDetail(bill.id)
                findNavController().navigate(action)
            }
        )
        binding.rvBills.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBills.adapter = billAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadBills() }
    }

    private fun observeViewModel() {
        viewModel.bills.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressLoading.isVisible = true
                    binding.layoutEmpty.isVisible = false
                }
                is Resource.Success -> {
                    binding.progressLoading.isVisible = false
                    binding.swipeRefresh.isRefreshing = false
                    val bills = state.data
                    billAdapter.submitList(bills)
                    binding.layoutEmpty.isVisible = bills.isEmpty()
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

