package com.mgbheights.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mgbheights.android.databinding.ItemNoticeBinding
import com.mgbheights.shared.domain.model.Notice
import com.mgbheights.shared.domain.model.NoticePriority
import com.mgbheights.shared.util.DateTimeUtil

class NoticeAdapter(
    private val onClick: (Notice) -> Unit
) : ListAdapter<Notice, NoticeAdapter.ViewHolder>(NoticeDiffCallback()) {

    inner class ViewHolder(private val binding: ItemNoticeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notice: Notice) {
            binding.tvTitle.text = notice.title
            binding.tvBody.text = notice.body
            binding.tvTimestamp.text = DateTimeUtil.getRelativeTime(notice.createdAt)
            binding.tvCreatedBy.text = "By ${notice.createdByName}"

            // Priority chip
            binding.chipPriority.text = notice.priority.name
            binding.chipPriority.isVisible = notice.priority != NoticePriority.NORMAL

            // Category chip
            binding.chipCategory.text = notice.category.name
            binding.chipCategory.isVisible = true

            // Image
            binding.ivImage.isVisible = notice.imageUrl.isNotBlank()

            binding.root.setOnClickListener { onClick(notice) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoticeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class NoticeDiffCallback : DiffUtil.ItemCallback<Notice>() {
    override fun areItemsTheSame(oldItem: Notice, newItem: Notice) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Notice, newItem: Notice) = oldItem == newItem
}

