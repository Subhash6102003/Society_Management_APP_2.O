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

    private var hasRedirected = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Fix #2: Do NOT reset hasRedirected here. The flag lives for the lifetime of this
        // fragment instance. Because all redirect actions use popUpTo+inclusive, the fragment
        // is destroyed after every redirect — so a new instance (hasRedirected=false) is always
        // created when the Dashboard destination is re-entered.
        setupRecyclerViews()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Quick actions grid
        binding.rvQuickActions.layoutManager = GridLayoutManager(requireContext(), 4)

        // Announcements
        noticeAdapter = NoticeAdapter { notice ->
            // Navigate to notice detail
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
            // Fix #3: Guard against null binding — fragment view may be destroyed
            // while an async state update is still in flight.
            if (_binding == null) return@observe

            binding.swipeRefresh.isRefreshing = state.isLoading

            // Fix #7: User is not yet approved — navigate to awaiting approval screen
            // immediately without firing any further UI or Firestore work.
            if (state.needsApproval && !hasRedirected) {
                if (findNavController().currentDestination?.id == R.id.dashboardFragment) {
                    hasRedirected = true
                    findNavController().navigate(R.id.action_dashboard_to_awaiting)
                }
                return@observe
            }

            state.user?.let { user ->
                // Redirect to role-specific dashboards (once per fragment instance)
                if (!hasRedirected && findNavController().currentDestination?.id == R.id.dashboardFragment) {
                    val redirectAction = when (user.role) {
                        UserRole.ADMIN -> R.id.action_dashboard_to_adminDashboard
                        UserRole.SECURITY_GUARD, UserRole.SECURITY_GUARD_WORKER -> R.id.action_dashboard_to_guardDashboard
                        UserRole.WORKER -> R.id.action_dashboard_to_workerDashboard
                        else -> null
                    }
                    if (redirectAction != null) {
                        hasRedirected = true
                        findNavController().navigate(redirectAction)
                        return@observe  // Fix #3: Stop ALL further processing after navigation
                    }
                }
                // At this point only RESIDENT and TENANT reach the generic dashboard UI.
                // If a redirected role somehow ends up here, skip UI updates.
                if (user.role == UserRole.ADMIN || user.role == UserRole.SECURITY_GUARD ||
                    user.role == UserRole.SECURITY_GUARD_WORKER || user.role == UserRole.WORKER) return@observe

                // Fix #3: Re-check binding after the role guard (no async gap, but defensive)
                if (_binding == null) return@observe

                binding.tvGreeting.text = "Hello, ${user.name.ifBlank { "Resident" }}"
                setupQuickActions(user.role)

                // Update financial summary
                val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                binding.tvDuesAmount.text = formatter.format(state.pendingDues)
            }

            // Fix #3: Final null-check before touching list-related views
            if (_binding == null) return@observe
            // Notices
            noticeAdapter.submitList(state.recentNotices)
            binding.rvAnnouncements.isVisible = state.recentNotices.isNotEmpty()
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
                QuickActionItem("Profile", R.drawable.ic_profile)
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
            UserRole.SECURITY_GUARD_WORKER -> listOf(
                QuickActionItem("Register", R.drawable.ic_visitors),
                QuickActionItem("Active", R.drawable.ic_visitors),
                QuickActionItem("Jobs", R.drawable.ic_dashboard),
                QuickActionItem("Alert", R.drawable.ic_notices)
            )
        }

        binding.rvQuickActions.adapter = QuickActionAdapter(actions) { action ->
            when (action.label) {
                "Pay", "Bills", "Dues" -> findNavController().navigate(R.id.maintenanceListFragment)
                "Visitors", "Register", "Active" -> findNavController().navigate(R.id.visitorListFragment)
                "Complaints" -> findNavController().navigate(R.id.createComplaintFragment)
                "Notices" -> findNavController().navigate(R.id.noticeListFragment)
                "Users" -> findNavController().navigate(R.id.adminUserManagementFragment)
                "Profile", "Reports" -> findNavController().navigate(R.id.profileFragment)
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
