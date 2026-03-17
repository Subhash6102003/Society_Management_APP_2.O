package com.mgbheights.android.ui.complaint

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
import com.mgbheights.android.databinding.FragmentComplaintListBinding
import com.mgbheights.android.ui.adapter.ComplaintAdapter
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComplaintListFragment : Fragment() {
    private var _binding: FragmentComplaintListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ComplaintViewModel by viewModels()
    private lateinit var adapter: ComplaintAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComplaintListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ComplaintAdapter { complaint ->
            val action = ComplaintListFragmentDirections.actionComplaintListToComplaintDetail(complaint.id)
            findNavController().navigate(action)
        }
        binding.rvComplaints.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComplaints.adapter = adapter

        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadComplaints() }

        binding.fabCreateComplaint.setOnClickListener {
            findNavController().navigate(R.id.action_complaintList_to_createComplaint)
        }

        val tabs = listOf("All", "Open", "In Progress", "Resolved")
        tabs.forEach { binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it)) }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.filterByStatus(tab?.text?.toString() ?: "All")
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        viewModel.complaints.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state is Resource.Loading
            binding.progressLoading.isVisible = state is Resource.Loading
            when (state) {
                is Resource.Success -> {
                    adapter.submitList(state.data)
                    binding.layoutEmpty.isVisible = state.data.isEmpty()
                }
                is Resource.Error -> {
                    binding.layoutEmpty.isVisible = true
                }
                is Resource.Loading -> {}
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
