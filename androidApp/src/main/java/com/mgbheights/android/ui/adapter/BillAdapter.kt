package com.mgbheights.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mgbheights.android.R
import com.mgbheights.android.databinding.ItemBillBinding
import com.mgbheights.shared.domain.model.BillStatus
import com.mgbheights.shared.domain.model.MaintenanceBill
import com.mgbheights.shared.util.DateTimeUtil
import java.text.NumberFormat
import java.util.Locale

class BillAdapter(
    private val onPayClick: (MaintenanceBill) -> Unit,
    private val onItemClick: (MaintenanceBill) -> Unit
) : ListAdapter<MaintenanceBill, BillAdapter.ViewHolder>(BillDiffCallback()) {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    inner class ViewHolder(private val binding: ItemBillBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bill: MaintenanceBill) {
            binding.tvMonth.text = DateTimeUtil.formatMonthYear(bill.createdAt)
            binding.tvFlatInfo.text = "${bill.flatNumber}, ${bill.towerBlock}"
            binding.tvAmount.text = formatter.format(bill.totalAmount)
            binding.tvDueDate.text = "Due: ${DateTimeUtil.formatDate(bill.dueDate)}"

            val ctx = binding.root.context
            when (bill.status) {
                BillStatus.PAID -> {
                    binding.chipStatus.text = ctx.getString(R.string.status_paid)
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_success)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                    binding.btnPay.isVisible = false
                }
                BillStatus.OVERDUE -> {
                    binding.chipStatus.text = ctx.getString(R.string.status_overdue)
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_overdue)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                    binding.btnPay.isVisible = true
                }
                BillStatus.PENDING -> {
                    binding.chipStatus.text = ctx.getString(R.string.status_pending)
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_pending)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                    binding.btnPay.isVisible = true
                }
                else -> {
                    binding.chipStatus.text = bill.status.name
                    binding.btnPay.isVisible = false
                }
            }

            binding.btnPay.setOnClickListener { onPayClick(bill) }
            binding.root.setOnClickListener { onItemClick(bill) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class BillDiffCallback : DiffUtil.ItemCallback<MaintenanceBill>() {
    override fun areItemsTheSame(oldItem: MaintenanceBill, newItem: MaintenanceBill) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: MaintenanceBill, newItem: MaintenanceBill) = oldItem == newItem
}

