package com.mgbheights.android.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mgbheights.android.databinding.FragmentAdminUserManagementBinding
import com.mgbheights.android.ui.adapter.PendingApprovalAdapter
import com.mgbheights.shared.domain.model.User
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminUserManagementFragment : Fragment() {

    private var _binding: FragmentAdminUserManagementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adapter: PendingApprovalAdapter
    private var allUsers: List<User> = emptyList()
    private var currentTab = 0 // 0=All, 1=Pending, 2=Residents, 3=Workers, 4=Guards

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminUserManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupTabs()
        setupSearch()
        setupFab()
        observeViewModel()
        // Load ALL users so all tabs work (All, Pending, Residents, Workers, Guards)
        viewModel.loadAllUsers()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupRecyclerView() {
        adapter = PendingApprovalAdapter(
            onApprove = { user -> viewModel.approveUser(user.id) },
            onReject = { user ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Reject User")
                    .setMessage("Reject ${user.name.ifBlank { "this user" }}? Their account will be deleted.")
                    .setPositiveButton("Reject") { _, _ -> viewModel.rejectUser(user.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onDelete = { user ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete User")
                    .setMessage("Permanently delete ${user.name.ifBlank { "this user" }}? This action cannot be undone.")
                    .setPositiveButton("Delete") { _, _ -> viewModel.rejectUser(user.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvUsers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUsers.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                filterUsers()
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun setupSearch() {
        binding.etSearch.doAfterTextChanged { filterUsers() }
    }

    private fun setupFab() {
        binding.fabAddUser.setOnClickListener {
            // Navigate to role selection to add new staff
            findNavController().navigate(com.mgbheights.android.R.id.roleSelectionFragment)
        }
        binding.fabAddUser.visibility = View.GONE
    }

    private fun filterUsers() {
        val query = binding.etSearch.text?.toString()?.trim()?.lowercase() ?: ""
        val filtered = allUsers.filter { user ->
            val matchesTab = when (currentTab) {
                0 -> true // All
                1 -> !user.isApproved // Pending
                2 -> user.role.name == "RESIDENT" || user.role.name == "TENANT"
                3 -> user.role.name == "WORKER" || user.role.name == "SECURITY_GUARD_WORKER"
                4 -> user.role.name == "SECURITY_GUARD" || user.role.name == "SECURITY_GUARD_WORKER"
                else -> true
            }
            val matchesSearch = query.isBlank() ||
                user.name.lowercase().contains(query) ||
                user.email.lowercase().contains(query) ||
                user.flatNumber.lowercase().contains(query) ||
                user.phoneNumber.contains(query)
            matchesTab && matchesSearch
        }
        adapter.submitList(filtered)
        binding.rvUsers.isVisible = filtered.isNotEmpty()
        binding.layoutEmpty.isVisible = filtered.isEmpty()
    }

    private fun observeViewModel() {
        viewModel.allUsersState.observe(viewLifecycleOwner) { state ->
            binding.progressLoading.isVisible = state is Resource.Loading
            when (state) {
                is Resource.Loading -> { }
                is Resource.Success -> {
                    allUsers = state.data
                    filterUsers()
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    binding.layoutEmpty.isVisible = true
                    binding.rvUsers.isVisible = false
                }
            }
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { state ->
            if (state is Resource.Success) {
                Toast.makeText(requireContext(), "Action completed", Toast.LENGTH_SHORT).show()
            } else if (state is Resource.Error) {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

