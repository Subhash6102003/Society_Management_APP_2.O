package com.mgbheights.android.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentAdminDashboardBinding
import com.mgbheights.android.ui.adapter.PendingApprovalAdapter
import com.mgbheights.android.ui.adapter.QuickActionAdapter
import com.mgbheights.android.ui.adapter.QuickActionItem
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var approvalAdapter: PendingApprovalAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupButtons()
        setupSwipeRefresh()
        observeViewModel()
        viewModel.loadPendingApprovals()
    }

    private fun setupRecyclerViews() {
        // Quick actions grid
        val actions = listOf(
            QuickActionItem("Users", R.drawable.ic_visitors),
            QuickActionItem("Approvals", R.drawable.ic_profile),
            QuickActionItem("Bills", R.drawable.ic_maintenance),
            QuickActionItem("Notices", R.drawable.ic_notices),
            QuickActionItem("Complaints", R.drawable.ic_notices),
            QuickActionItem("Visitors", R.drawable.ic_visitors),
            QuickActionItem("Reports", R.drawable.ic_dashboard),
            QuickActionItem("Settings", R.drawable.ic_profile)
        )
        binding.rvAdminActions.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvAdminActions.adapter = QuickActionAdapter(actions) { action ->
            when (action.label) {
                "Users", "Approvals" -> findNavController().navigate(R.id.adminUserManagementFragment)
                "Bills" -> findNavController().navigate(R.id.maintenanceListFragment)
                "Notices" -> findNavController().navigate(R.id.noticeListFragment)
                "Complaints" -> findNavController().navigate(R.id.complaintListFragment)
                "Visitors" -> findNavController().navigate(R.id.visitorListFragment)
                "Settings" -> findNavController().navigate(R.id.profileFragment)
                else -> Toast.makeText(requireContext(), "${action.label} coming soon", Toast.LENGTH_SHORT).show()
            }
        }

        // Pending approvals list
        approvalAdapter = PendingApprovalAdapter(
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
                    .setMessage("Permanently delete ${user.name.ifBlank { "this user" }}?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.rejectUser(user.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvPendingApprovals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPendingApprovals.adapter = approvalAdapter
        binding.rvPendingApprovals.isNestedScrollingEnabled = false
    }

    private fun setupButtons() {
        binding.cardPendingApprovals.setOnClickListener {
            findNavController().navigate(R.id.adminUserManagementFragment)
        }

        binding.btnViewAllApprovals.setOnClickListener {
            findNavController().navigate(R.id.adminUserManagementFragment)
        }

        binding.btnGenerateBills.setOnClickListener {
            showGenerateBillsDialog()
        }

        binding.btnApplyLateFees.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Apply Late Fees")
                .setMessage("Apply 2% late fee to all overdue bills?")
                .setPositiveButton("Apply") { _, _ ->
                    viewModel.applyLateFees()
                    Toast.makeText(requireContext(), "Late fees applied", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnEmergencyBroadcast.setOnClickListener {
            val bundle = Bundle().apply {
                putString("prefillTitle", "⚠️ VERY IMPORTANT")
            }
            findNavController().navigate(R.id.createNoticeFragment, bundle)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadAdminDashboard()
            viewModel.loadPendingApprovals()
        }
    }

    private fun observeViewModel() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state.isLoading
            binding.tvTotalUsers.text = state.totalUsers.toString()
            binding.tvPendingApprovals.text = state.pendingApprovals.toString()
            binding.tvTotalCollected.text = formatter.format(state.totalCollected)
            binding.tvTotalPending.text = formatter.format(state.totalPending)
        }

        viewModel.pendingUsers.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> { /* handled by swipeRefresh */ }
                is Resource.Success -> {
                    val users = state.data
                    approvalAdapter.submitList(users)
                    binding.rvPendingApprovals.isVisible = users.isNotEmpty()
                    binding.cardNoApprovals.isVisible = users.isEmpty()
                }
                is Resource.Error -> {
                    binding.cardNoApprovals.isVisible = true
                    binding.rvPendingApprovals.isVisible = false
                }
            }
        }
    }

    private fun showGenerateBillsDialog() {
        val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
        
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null) // placeholder parent
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(60, 40, 60, 20)
        }
        
        val tvLabel1 = android.widget.TextView(requireContext()).apply { text = "Month (e.g., $currentMonth)" }
        val etMonth = android.widget.EditText(requireContext()).apply {
            setText(currentMonth)
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        val tvLabel2 = android.widget.TextView(requireContext()).apply { 
            text = "Monthly Amount (₹)"
            setPadding(0, 20, 0, 0)
        }
        val etAmount = android.widget.EditText(requireContext()).apply {
            hint = "e.g., 2000"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        
        layout.addView(tvLabel1)
        layout.addView(etMonth)
        layout.addView(tvLabel2)
        layout.addView(etAmount)
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Generate Monthly Bills")
            .setMessage("This will create a bill for every flat/resident.")
            .setView(layout)
            .setPositiveButton("Generate") { _, _ ->
                val month = etMonth.text.toString().trim()
                val amount = etAmount.text.toString().toDoubleOrNull()
                if (month.isBlank() || amount == null || amount <= 0) {
                    Toast.makeText(requireContext(), "Please enter valid month and amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.generateMonthlyBills(month, amount)
                Toast.makeText(requireContext(), "Generating bills for $month...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

