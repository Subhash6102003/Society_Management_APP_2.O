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
        setupRecyclerViews()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        binding.rvQuickActions.layoutManager = GridLayoutManager(requireContext(), 4)
        noticeAdapter = NoticeAdapter { _ ->
            findNavController().navigate(R.id.noticeListFragment)
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
            if (_binding == null) return@observe

            binding.swipeRefresh.isRefreshing = state.isLoading

            if (state.needsApproval && !hasRedirected) {
                if (findNavController().currentDestination?.id == R.id.dashboardFragment) {
                    hasRedirected = true
                    findNavController().navigate(R.id.awaitingApprovalFragment)
                }
                return@observe
            }

            state.user?.let { user ->
                if (!hasRedirected && findNavController().currentDestination?.id == R.id.dashboardFragment) {
                    val redirectAction = when (user.role) {
                        UserRole.ADMIN -> R.id.adminDashboardFragment
                        UserRole.SECURITY_GUARD, UserRole.SECURITY_GUARD_WORKER -> R.id.guardDashboardFragment
                        UserRole.WORKER -> R.id.workerDashboardFragment
                        else -> null
                    }
                    if (redirectAction != null) {
                        hasRedirected = true
                        findNavController().navigate(redirectAction)
                        return@observe
                    }
                }

                binding.tvGreeting.text = "Hello, ${user.name.ifBlank { "Resident" }}"
                binding.tvFlatInfo.text = "${user.flatNumber} ${user.towerBlock}"
                setupQuickActions(user.role)

                val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                binding.tvDuesAmount.text = formatter.format(state.pendingDues)
            }

            noticeAdapter.submitList(state.recentNotices)
            binding.rvAnnouncements.isVisible = state.recentNotices.isNotEmpty()
        }
    }

    private fun setupQuickActions(role: UserRole) {
        val actions = when (role) {
            UserRole.RESIDENT -> listOf(
                QuickActionItem("Pay", R.drawable.ic_maintenance),
                QuickActionItem("Visitors", R.drawable.ic_visitors),
                QuickActionItem("Complaints", R.drawable.ic_notices),
                QuickActionItem("Notices", R.drawable.ic_notices)
            )
            UserRole.TENANT -> listOf(
                QuickActionItem("Pay", R.drawable.ic_maintenance),
                QuickActionItem("History", R.drawable.ic_dashboard),
                QuickActionItem("Complaints", R.drawable.ic_notices),
                QuickActionItem("Notices", R.drawable.ic_notices)
            )
            else -> emptyList()
        }

        binding.rvQuickActions.adapter = QuickActionAdapter(actions) { action ->
            when (action.label) {
                "Pay", "History" -> findNavController().navigate(R.id.maintenanceListFragment)
                "Visitors" -> findNavController().navigate(R.id.visitorListFragment)
                "Notices" -> findNavController().navigate(R.id.noticeListFragment)
                "Complaints" -> findNavController().navigate(R.id.complaintListFragment)
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
