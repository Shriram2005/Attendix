package com.windhands.attendix.ui.employee.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.windhands.attendix.R
import com.windhands.attendix.databinding.ItemLeaveBalanceMiniBinding
import com.windhands.attendix.ui.employee.model.LeaveBalanceItem

class LeaveBalanceMiniAdapter : ListAdapter<LeaveBalanceItem, LeaveBalanceMiniAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaveBalanceMiniBinding.inflate(
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
        private val binding: ItemLeaveBalanceMiniBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LeaveBalanceItem) {
            val context = binding.root.context

            binding.tvLeaveType.text = "${item.displayName}:"
            binding.tvDaysLeft.text = item.remaining.toString()

            // Set color based on leave type
            val colorRes = when (item.type.lowercase()) {
                "annual" -> R.color.primary
                "sick" -> R.color.warning
                "personal" -> R.color.info
                "unpaid" -> R.color.error
                else -> R.color.primary
            }
            binding.tvDaysLeft.setTextColor(context.getColor(colorRes))
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
