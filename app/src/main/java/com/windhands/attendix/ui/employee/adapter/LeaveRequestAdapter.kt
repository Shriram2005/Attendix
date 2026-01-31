package com.windhands.attendix.ui.employee.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.windhands.attendix.R
import com.windhands.attendix.databinding.ItemLeaveRequestBinding
import com.windhands.attendix.ui.employee.LeaveRequestItem

class LeaveRequestAdapter(
    private val onItemClick: (LeaveRequestItem) -> Unit,
    private val onCancelClick: (LeaveRequestItem) -> Unit
) : ListAdapter<LeaveRequestItem, LeaveRequestAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaveRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick, onCancelClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemLeaveRequestBinding,
        private val onItemClick: (LeaveRequestItem) -> Unit,
        private val onCancelClick: (LeaveRequestItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LeaveRequestItem) {
            val context = binding.root.context

            binding.tvLeaveType.text = item.displayType
            binding.tvDateRange.text = item.formattedDateRange
            binding.tvDaysCount.text = context.resources.getQuantityString(
                R.plurals.leave_days_count, item.days, item.days
            )
            binding.tvStatus.text = item.status.replaceFirstChar { it.uppercase() }

            // Set status badge styling
            val (bgRes, textColorRes) = when (item.status.lowercase()) {
                "approved" -> Pair(R.drawable.bg_chip_success, R.color.success)
                "pending" -> Pair(R.drawable.bg_chip_warning, R.color.warning)
                "rejected" -> Pair(R.drawable.bg_chip_error, R.color.error)
                "cancelled" -> Pair(R.drawable.bg_chip_info, R.color.info)
                else -> Pair(R.drawable.bg_chip_info, R.color.info)
            }
            binding.tvStatus.setBackgroundResource(bgRes)
            binding.tvStatus.setTextColor(context.getColor(textColorRes))

            // Set icon based on status
            val iconRes = when (item.status.lowercase()) {
                "approved" -> R.drawable.ic_event_available
                "pending" -> R.drawable.ic_pending
                "rejected" -> R.drawable.ic_event_busy
                else -> R.drawable.ic_calendar
            }
            binding.ivIcon.setImageResource(iconRes)
            
            // Set icon container color based on status
            val containerColorRes = when (item.status.lowercase()) {
                "approved" -> R.color.success_container
                "pending" -> R.color.warning_container
                "rejected" -> R.color.error_container
                else -> R.color.info_container
            }
            binding.iconContainer.backgroundTintList = android.content.res.ColorStateList.valueOf(
                context.getColor(containerColorRes)
            )
            
            val iconTintRes = when (item.status.lowercase()) {
                "approved" -> R.color.success
                "pending" -> R.color.warning
                "rejected" -> R.color.error
                else -> R.color.info
            }
            binding.ivIcon.setColorFilter(context.getColor(iconTintRes))

            binding.root.setOnClickListener { onItemClick(item) }
            
            // Show cancel button for pending requests
            if (item.canCancel) {
                binding.btnCancelRequest.visibility = android.view.View.VISIBLE
                binding.btnCancelRequest.setOnClickListener {
                    onCancelClick(item)
                }
            } else {
                binding.btnCancelRequest.visibility = android.view.View.GONE
                binding.btnCancelRequest.setOnClickListener(null)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<LeaveRequestItem>() {
        override fun areItemsTheSame(oldItem: LeaveRequestItem, newItem: LeaveRequestItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LeaveRequestItem, newItem: LeaveRequestItem): Boolean {
            return oldItem == newItem
        }
    }
}
