package com.mgbheights.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mgbheights.android.databinding.ItemPaymentHistoryBinding
import com.mgbheights.shared.domain.model.MaintenanceBill
import com.mgbheights.shared.util.DateTimeUtil
import java.text.NumberFormat
import java.util.Locale

class PaymentHistoryAdapter : ListAdapter<MaintenanceBill, PaymentHistoryAdapter.ViewHolder>(PaymentDiffCallback()) {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    inner class ViewHolder(private val binding: ItemPaymentHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bill: MaintenanceBill) {
            binding.tvMonth.text = if (bill.month.isNotBlank()) bill.month else DateTimeUtil.formatMonthYear(bill.createdAt)
            binding.tvFlatInfo.text = "${bill.flatNumber}, ${bill.towerBlock}"
            binding.tvAmount.text = formatter.format(bill.totalAmount)
            binding.tvPaidDate.text = if (bill.paidAt > 0) "Paid: ${DateTimeUtil.formatDateTime(bill.paidAt)}" else "Paid"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaymentHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PaymentDiffCallback : DiffUtil.ItemCallback<MaintenanceBill>() {
    override fun areItemsTheSame(oldItem: MaintenanceBill, newItem: MaintenanceBill) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: MaintenanceBill, newItem: MaintenanceBill) = oldItem == newItem
}

