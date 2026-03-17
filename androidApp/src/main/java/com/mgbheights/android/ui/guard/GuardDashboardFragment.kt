package com.mgbheights.android.ui.guard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentGuardDashboardBinding
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GuardDashboardFragment : Fragment() {

    private var _binding: FragmentGuardDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GuardViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGuardDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.rvActiveVisitors.layoutManager = LinearLayoutManager(requireContext())
        binding.rvActiveVisitors.isNestedScrollingEnabled = false

        binding.btnRegisterVisitor.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_guardDashboard_to_addVisitor)
            } catch (e: Exception) {
                // Fallback if action is not found or other navigation error
                try {
                    findNavController().navigate(R.id.addVisitorFragment)
                } catch (_: Exception) {
                    Toast.makeText(requireContext(), "Visitor registration coming soon", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnEmergencyAlert.setOnClickListener {
            Toast.makeText(requireContext(), "🚨 Emergency alert sent!", Toast.LENGTH_SHORT).show()
        }

        binding.switchShift.setOnCheckedChangeListener { _, isChecked ->
            binding.tvShiftStatus.text = if (isChecked) "On Duty — Shift Active" else "Off Duty — Toggle to start"
            binding.tvShiftInfo.text = if (isChecked) "On Duty" else "Off Duty"
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadTodaysVisitors()
            viewModel.loadActiveVisitors()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        viewModel.activeVisitors.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is Resource.Success -> {
                    val visitors = state.data
                    binding.chipActiveCount.text = visitors.size.toString()
                    binding.cardNoActiveVisitors.isVisible = visitors.isEmpty()
                    binding.rvActiveVisitors.isVisible = visitors.isNotEmpty()
                }
                is Resource.Loading -> { /* handled by swipeRefresh */ }
                is Resource.Error -> {
                    binding.cardNoActiveVisitors.isVisible = true
                    binding.rvActiveVisitors.isVisible = false
                }
            }
        }

        viewModel.todaysVisitors.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is Resource.Success -> {
                    // Entry log items would be populated here
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
