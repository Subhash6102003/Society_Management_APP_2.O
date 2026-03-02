package com.mgbheights.android.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentDashboardBinding
import com.mgbheights.android.ui.adapter.NoticeAdapter
import com.mgbheights.android.ui.adapter.QuickActionAdapter
import com.mgbheights.android.ui.adapter.QuickActionItem
import com.mgbheights.shared.domain.model.UserRole
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var noticeAdapter: NoticeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Quick actions grid
        binding.rvQuickActions.layoutManager = GridLayoutManager(requireContext(), 4)

        // Announcements
        noticeAdapter = NoticeAdapter { notice ->
            // Navigate to notice detail - would use safe args in production
        }
        binding.rvAnnouncements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAnnouncements.adapter = noticeAdapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state.isLoading

            state.user?.let { user ->
                binding.tvGreeting.text = getString(R.string.dashboard_greeting, user.name.ifBlank { "Resident" })
                binding.tvFlatInfo.text = if (user.flatNumber.isNotBlank()) {
                    getString(R.string.dashboard_flat_info, user.flatNumber, user.towerBlock)
                } else ""

                // Setup quick actions based on role
                setupQuickActions(user.role)
            }

            // Pending dues
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            binding.tvDuesAmount.text = formatter.format(state.pendingDues)
            binding.cardPendingDues.isVisible = state.pendingDues > 0

            // Notices
            noticeAdapter.submitList(state.recentNotices)
            binding.layoutEmpty.isVisible = state.recentNotices.isEmpty() && !state.isLoading

            binding.btnPayNow.setOnClickListener {
                findNavController().navigate(R.id.maintenanceListFragment)
            }

            binding.btnViewAllNotices.setOnClickListener {
                findNavController().navigate(R.id.noticeListFragment)
            }
        }
    }

    private fun setupQuickActions(role: UserRole) {
        val actions = when (role) {
            UserRole.ADMIN -> listOf(
                QuickActionItem("Users", R.drawable.ic_visitors),
                QuickActionItem("Bills", R.drawable.ic_maintenance),
                QuickActionItem("Notices", R.drawable.ic_notices),
                QuickActionItem("Reports", R.drawable.ic_dashboard)
            )
            UserRole.RESIDENT -> listOf(
                QuickActionItem("Pay", R.drawable.ic_maintenance),
                QuickActionItem("Visitors", R.drawable.ic_visitors),
                QuickActionItem("Complaints", R.drawable.ic_notices),
                QuickActionItem("Notices", R.drawable.ic_notices)
            )
            UserRole.TENANT -> listOf(
                QuickActionItem("Pay", R.drawable.ic_maintenance),
                QuickActionItem("Receipts", R.drawable.ic_dashboard),
                QuickActionItem("House", R.drawable.ic_profile),
                QuickActionItem("Dues", R.drawable.ic_maintenance)
            )
            UserRole.SECURITY_GUARD -> listOf(
                QuickActionItem("Register", R.drawable.ic_visitors),
                QuickActionItem("Active", R.drawable.ic_visitors),
                QuickActionItem("Logs", R.drawable.ic_dashboard),
                QuickActionItem("Alert", R.drawable.ic_notices)
            )
            UserRole.WORKER -> listOf(
                QuickActionItem("Jobs", R.drawable.ic_dashboard),
                QuickActionItem("Earnings", R.drawable.ic_maintenance),
                QuickActionItem("History", R.drawable.ic_dashboard),
                QuickActionItem("Duty", R.drawable.ic_profile)
            )
        }

        binding.rvQuickActions.adapter = QuickActionAdapter(actions) { action ->
            when (action.label) {
                "Pay", "Bills", "Dues" -> findNavController().navigate(R.id.maintenanceListFragment)
                "Visitors", "Register", "Active" -> findNavController().navigate(R.id.visitorListFragment)
                "Notices", "Complaints" -> findNavController().navigate(R.id.noticeListFragment)
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

