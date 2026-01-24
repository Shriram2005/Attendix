package com.windhands.attendix.ui.employee.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.windhands.attendix.R
import com.windhands.attendix.databinding.ItemLeaveBalanceCardBinding
import com.windhands.attendix.ui.employee.model.LeaveBalanceItem

class LeaveBalanceCardAdapter : ListAdapter<LeaveBalanceItem, LeaveBalanceCardAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaveBalanceCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemLeaveBalanceCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LeaveBalanceItem) {
            val context = binding.root.context

            binding.tvLeaveType.text = item.displayName
            binding.tvDaysLeft.text = context.getString(R.string.leave_days_left, item.remaining)
            binding.progressBar.progress = item.progress
            binding.progressBar.max = 100

            // Set icon and colors based on leave type
            val (iconRes, colorRes) = when (item.type.lowercase()) {
                "annual" -> Pair(R.drawable.ic_calendar, R.color.primary)
                "sick" -> Pair(R.drawable.ic_medical, R.color.warning)
                "personal" -> Pair(R.drawable.ic_person, R.color.info)
                "unpaid" -> Pair(R.drawable.ic_timer, R.color.error)
                else -> Pair(R.drawable.ic_calendar, R.color.primary)
            }

            binding.ivIcon.setImageResource(iconRes)
            val color = context.getColor(colorRes)
            binding.ivIcon.setColorFilter(color)
            binding.tvDaysLeft.setTextColor(color)
            binding.progressBar.setIndicatorColor(color)
            
            // Set icon container background tint
            val containerColor = when (item.type.lowercase()) {
                "annual" -> R.color.primary_container
                "sick" -> R.color.warning_container
                "personal" -> R.color.info_container
                "unpaid" -> R.color.error_container
                else -> R.color.primary_container
            }
            binding.iconContainer.backgroundTintList = android.content.res.ColorStateList.valueOf(
                context.getColor(containerColor)
            )
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<LeaveBalanceItem>() {
        override fun areItemsTheSame(oldItem: LeaveBalanceItem, newItem: LeaveBalanceItem): Boolean {
            return oldItem.type == newItem.type
        }

        override fun areContentsTheSame(oldItem: LeaveBalanceItem, newItem: LeaveBalanceItem): Boolean {
            return oldItem == newItem
        }
    }
}
