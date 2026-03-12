package com.mgbheights.android.ui.maintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mgbheights.android.R
import com.mgbheights.android.databinding.FragmentBillDetailBinding
import com.mgbheights.shared.domain.model.BillStatus
import com.mgbheights.shared.domain.model.MaintenanceBill
import com.mgbheights.shared.domain.model.UserRole
import com.mgbheights.shared.domain.repository.MaintenanceRepository
import com.mgbheights.shared.domain.usecase.auth.GetCurrentUserUseCase
import com.mgbheights.shared.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BillDetailFragment : Fragment() {

    private var _binding: FragmentBillDetailBinding? = null
    private val binding get() = _binding!!
    private val args: BillDetailFragmentArgs by navArgs()
    private val viewModel: BillDetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBillDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        viewModel.loadBill(args.billId)

        viewModel.bill.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> { /* show loading */ }
                is Resource.Success -> displayBill(state.data)
                is Resource.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.markPaidResult.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> binding.btnPay.isEnabled = false
                is Resource.Success -> {
                    Toast.makeText(requireContext(), "Bill marked as paid!", Toast.LENGTH_SHORT).show()
                    viewModel.loadBill(args.billId) // Refresh
                }
                is Resource.Error -> {
                    binding.btnPay.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayBill(bill: MaintenanceBill) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        binding.tvTotalAmount.text = formatter.format(bill.totalAmount)
        binding.tvDueDate.text = if (bill.dueDate > 0) "Due: ${dateFormat.format(Date(bill.dueDate))}" else "Month: ${bill.month}"
        binding.tvFlatNumber.text = "Flat: ${bill.flatNumber} ${bill.towerBlock}"
        binding.tvResidentName.text = bill.residentName.ifBlank { "Resident" }
        binding.tvMonth.text = "Bill for: ${bill.month}"

        // Status chip
        binding.chipStatus.text = bill.status.name
        val statusColor = when (bill.status) {
            BillStatus.PAID -> R.color.primary
            BillStatus.OVERDUE -> R.color.error
            BillStatus.PENDING -> R.color.status_warning
            else -> R.color.outline
        }
        binding.chipStatus.setChipBackgroundColorResource(statusColor)

        // Late fee
        if (bill.lateFee > 0) {
            binding.layoutLateFee.isVisible = true
            binding.tvLateFee.text = formatter.format(bill.lateFee)
        }

        // Show/hide pay button based on status and role
        val isPending = bill.status == BillStatus.PENDING || bill.status == BillStatus.OVERDUE
        binding.btnPay.isVisible = isPending
        binding.btnPay.text = if (isPending) "Mark as Paid" else "Paid"
        binding.btnDownloadReceipt.isVisible = bill.status == BillStatus.PAID

        binding.btnPay.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Payment")
                .setMessage("Mark this bill of ${formatter.format(bill.totalAmount)} as paid?")
                .setPositiveButton("Mark Paid") { _, _ ->
                    viewModel.markAsPaid(bill.id)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

@HiltViewModel
class BillDetailViewModel @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _bill = MutableLiveData<Resource<MaintenanceBill>>()
    val bill: LiveData<Resource<MaintenanceBill>> = _bill

    private val _markPaidResult = MutableLiveData<Resource<Unit>>()
    val markPaidResult: LiveData<Resource<Unit>> = _markPaidResult

    fun loadBill(billId: String) {
        viewModelScope.launch {
            _bill.value = Resource.Loading
            _bill.value = maintenanceRepository.getBillById(billId)
        }
    }

    fun markAsPaid(billId: String) {
        viewModelScope.launch {
            _markPaidResult.value = Resource.Loading
            val paymentId = "PAY_${System.currentTimeMillis()}"
            _markPaidResult.value = maintenanceRepository.markBillPaid(billId, paymentId)
        }
    }
}
