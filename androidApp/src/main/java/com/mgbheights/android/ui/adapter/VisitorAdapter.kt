package com.mgbheights.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mgbheights.android.R
import com.mgbheights.android.databinding.ItemVisitorBinding
import com.mgbheights.shared.domain.model.Visitor
import com.mgbheights.shared.domain.model.VisitorStatus
import com.mgbheights.shared.util.DateTimeUtil

class VisitorAdapter(
    private val onApprove: ((Visitor) -> Unit)? = null,
    private val onDeny: ((Visitor) -> Unit)? = null,
    private val onItemClick: (Visitor) -> Unit
) : ListAdapter<Visitor, VisitorAdapter.ViewHolder>(VisitorDiffCallback()) {

    inner class ViewHolder(private val binding: ItemVisitorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(visitor: Visitor) {
            binding.tvName.text = visitor.name
            binding.tvPurpose.text = visitor.purpose
            binding.tvFlatInfo.text = "${visitor.flatNumber}, ${visitor.towerBlock}"

            val ctx = binding.root.context
            when (visitor.status) {
                VisitorStatus.PENDING -> {
                    binding.chipStatus.text = "Pending"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_pending)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                    binding.layoutActions.isVisible = onApprove != null
                }
                VisitorStatus.APPROVED -> {
                    binding.chipStatus.text = "Approved"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_success)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                    binding.layoutActions.isVisible = false
                }
                VisitorStatus.DENIED -> {
                    binding.chipStatus.text = "Denied"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_overdue)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                    binding.layoutActions.isVisible = false
                }
                VisitorStatus.CHECKED_IN -> {
                    binding.chipStatus.text = "Inside"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_info)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                    binding.layoutActions.isVisible = false
                }
                VisitorStatus.CHECKED_OUT -> {
                    binding.chipStatus.text = "Left"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.outline)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                    binding.layoutActions.isVisible = false
                }
                else -> {
                    binding.chipStatus.text = visitor.status.name
                    binding.layoutActions.isVisible = false
                }
            }

            // Time
            binding.tvTime.text = if (visitor.entryTime > 0) {
                "In: ${DateTimeUtil.formatTime(visitor.entryTime)}"
            } else {
                DateTimeUtil.getRelativeTime(visitor.createdAt)
            }

            // Vehicle
            binding.tvVehicle.isVisible = visitor.vehicleNumber.isNotBlank()
            binding.tvVehicle.text = visitor.vehicleNumber

            // Actions
            binding.btnApprove.setOnClickListener { onApprove?.invoke(visitor) }
            binding.btnDeny.setOnClickListener { onDeny?.invoke(visitor) }
            binding.root.setOnClickListener { onItemClick(visitor) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVisitorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class VisitorDiffCallback : DiffUtil.ItemCallback<Visitor>() {
    override fun areItemsTheSame(oldItem: Visitor, newItem: Visitor) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Visitor, newItem: Visitor) = oldItem == newItem
}

