package com.mgbheights.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mgbheights.android.R
import com.mgbheights.android.databinding.ItemComplaintBinding
import com.mgbheights.shared.domain.model.Complaint
import com.mgbheights.shared.domain.model.ComplaintStatus
import com.mgbheights.shared.util.DateTimeUtil

class ComplaintAdapter(
    private val onItemClick: (Complaint) -> Unit
) : ListAdapter<Complaint, ComplaintAdapter.ViewHolder>(ComplaintDiffCallback()) {

    inner class ViewHolder(private val binding: ItemComplaintBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(complaint: Complaint) {
            binding.tvTitle.text = complaint.title
            binding.tvCategory.text = complaint.category.name
            binding.tvDescription.text = complaint.description
            binding.tvFlatInfo.text = "${complaint.flatNumber}, ${complaint.towerBlock}"
            binding.tvTimestamp.text = DateTimeUtil.getRelativeTime(complaint.createdAt)

            val ctx = binding.root.context
            when (complaint.status) {
                ComplaintStatus.OPEN -> {
                    binding.chipStatus.text = "Open"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_warning)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                }
                ComplaintStatus.IN_PROGRESS -> {
                    binding.chipStatus.text = "In Progress"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_info)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                }
                ComplaintStatus.RESOLVED -> {
                    binding.chipStatus.text = "Resolved"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_success)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                }
                ComplaintStatus.CLOSED -> {
                    binding.chipStatus.text = "Closed"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.outline)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                }
                ComplaintStatus.REJECTED -> {
                    binding.chipStatus.text = "Rejected"
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_overdue)
                    binding.chipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.on_primary))
                }
            }

            binding.root.setOnClickListener { onItemClick(complaint) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemComplaintBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ComplaintDiffCallback : DiffUtil.ItemCallback<Complaint>() {
    override fun areItemsTheSame(oldItem: Complaint, newItem: Complaint) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Complaint, newItem: Complaint) = oldItem == newItem
}

