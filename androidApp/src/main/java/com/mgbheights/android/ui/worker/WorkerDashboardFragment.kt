package com.mgbheights.android.ui.worker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentWorkerDashboardBinding
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class WorkerDashboardFragment : Fragment() {

    private var _binding: FragmentWorkerDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WorkerViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWorkerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.rvWorkOrders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWorkOrders.isNestedScrollingEnabled = false

        binding.switchDuty.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleDuty()
            binding.tvDutyStatus.text = if (isChecked) "On Duty — Ready for jobs" else "Off Duty — Toggle to go on duty"
        }

        binding.swipeRefresh.setColorSchemeResources(R.color.primary)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadWorkOrders()
            viewModel.loadEarnings()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        // Fix #5: Show setup message when no worker document exists for this user
        viewModel.workerSetupNeeded.observe(viewLifecycleOwner) { needed ->
            if (_binding == null) return@observe
            if (needed) {
                binding.rvWorkOrders.isVisible = false
                binding.layoutEmpty.isVisible = true
                binding.tvEmptyMessage.text =
                    "Your worker profile is being set up.\nPlease contact the admin."
            }
        }

        viewModel.isDutyOn.observe(viewLifecycleOwner) { isOn ->
            if (_binding == null) return@observe
            binding.switchDuty.isChecked = isOn
            binding.tvDutyStatus.text = if (isOn) "On Duty — Ready for jobs" else "Off Duty — Toggle to go on duty"
        }

        viewModel.earnings.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            if (state is Resource.Success) {
                binding.tvEarnings.text = formatter.format(state.data)
            }
        }

        viewModel.workOrders.observe(viewLifecycleOwner) { state ->
            if (_binding == null) return@observe
            when (state) {
                is Resource.Success -> {
                    val orders = state.data
                    binding.rvWorkOrders.isVisible = orders.isNotEmpty()
                    binding.layoutEmpty.isVisible = orders.isEmpty()
                    if (orders.isEmpty()) {
                        binding.tvEmptyMessage.text = "No jobs assigned yet"
                    }
                }
                is Resource.Loading -> { /* handled by swipeRefresh */ }
                is Resource.Error -> {
                    // Fix #6: Use context safely — fragment may be detached when error arrives
                    Toast.makeText(context ?: return@observe, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
