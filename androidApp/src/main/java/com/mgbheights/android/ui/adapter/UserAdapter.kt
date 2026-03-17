package com.mgbheights.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mgbheights.android.R
import com.mgbheights.android.databinding.ItemUserBinding
import com.mgbheights.shared.domain.model.User

class UserAdapter(private val onItemClick: (User) -> Unit) :
    ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvUserName.text = user.name.ifBlank { "Unnamed User" }
            binding.tvUserDetail.text = when {
                user.flatNumber.isNotBlank() -> "Flat: ${user.flatNumber}"
                user.email.isNotBlank() -> user.email
                else -> user.phoneNumber
            }

            Glide.with(binding.ivUserPhoto.context)
                .load(user.profilePhotoUrl)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(binding.ivUserPhoto)

            binding.root.setOnClickListener { onItemClick(user) }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
}
