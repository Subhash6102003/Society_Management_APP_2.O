package com.mgbheights.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mgbheights.android.databinding.ItemUserApprovalBinding
import com.mgbheights.shared.domain.model.User

class PendingApprovalAdapter(
    private val onApprove: (User) -> Unit,
    private val onReject: (User) -> Unit,
    private val onDelete: ((User) -> Unit)? = null,
    private val onItemClick: ((User) -> Unit)? = null
) : ListAdapter<User, PendingApprovalAdapter.ViewHolder>(UserDiffCallback()) {

    inner class ViewHolder(private val binding: ItemUserApprovalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvName.text = user.name.ifBlank { "Unnamed User" }
            binding.tvDetails.text = buildString {
                if (user.flatNumber.isNotBlank()) append("Flat ${user.flatNumber}")
                if (user.towerBlock.isNotBlank()) append(" • ${user.towerBlock}")
                if (user.email.isNotBlank()) {
                    if (isNotBlank()) append("\n")
                    append(user.email)
                }
                if (user.phoneNumber.isNotBlank()) {
                    if (isNotBlank()) append(" • ")
                    append(user.phoneNumber)
                }
            }
            binding.chipRole.text = user.role.name.replace("_", " ")

            if (user.isApproved) {
                binding.layoutPendingActions.isVisible = false
                binding.btnDelete.isVisible = true
                binding.btnDelete.setOnClickListener { onDelete?.invoke(user) }
            } else {
                binding.layoutPendingActions.isVisible = true
                binding.btnDelete.isVisible = false
                binding.btnApprove.setOnClickListener { onApprove(user) }
                binding.btnReject.setOnClickListener { onReject(user) }
            }
            
            binding.root.setOnClickListener { onItemClick?.invoke(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserApprovalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
    }
}
