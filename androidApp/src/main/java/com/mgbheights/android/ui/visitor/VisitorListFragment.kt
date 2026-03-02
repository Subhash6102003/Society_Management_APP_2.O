package com.mgbheights.android.ui.visitor

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
import com.mgbheights.android.databinding.FragmentVisitorListBinding
import com.mgbheights.android.ui.adapter.VisitorAdapter
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VisitorListFragment : Fragment() {

    private var _binding: FragmentVisitorListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VisitorViewModel by viewModels()
    private lateinit var visitorAdapter: VisitorAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVisitorListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupRecyclerView()
        setupUI()
        observeViewModel()
    }

    private fun setupTabs() {
        listOf("All", "Pending", "Active", "History").forEach {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it))
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) { viewModel.loadVisitors() }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        visitorAdapter = VisitorAdapter(
            onApprove = { viewModel.approveVisitor(it.id) },
            onDeny = { viewModel.denyVisitor(it.id) },
            onItemClick = { /* Navigate to detail */ }
        )
        binding.rvVisitors.layoutManager = LinearLayoutManager(requireContext())
        binding.rvVisitors.adapter = visitorAdapter
    }

    private fun setupUI() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadVisitors() }
        binding.fabAddVisitor.setOnClickListener {
            findNavController().navigate(R.id.action_visitorList_to_addVisitor)
        }
    }

    private fun observeViewModel() {
        viewModel.visitors.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> binding.progressLoading.isVisible = true
                is Resource.Success -> {
                    binding.progressLoading.isVisible = false
                    binding.swipeRefresh.isRefreshing = false
                    visitorAdapter.submitList(state.data)
                    binding.layoutEmpty.isVisible = state.data.isEmpty()
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

