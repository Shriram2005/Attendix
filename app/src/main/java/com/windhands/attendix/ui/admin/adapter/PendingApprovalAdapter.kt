package com.windhands.attendix.ui.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.windhands.attendix.databinding.ItemPendingApprovalBinding
import com.windhands.attendix.ui.admin.PendingApprovalItem

class PendingApprovalAdapter(
    private val onApproveClick: (PendingApprovalItem) -> Unit,
    private val onRejectClick: (PendingApprovalItem) -> Unit
) : ListAdapter<PendingApprovalItem, PendingApprovalAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPendingApprovalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onApproveClick, onRejectClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPendingApprovalBinding,
        private val onApproveClick: (PendingApprovalItem) -> Unit,
        private val onRejectClick: (PendingApprovalItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PendingApprovalItem) {
            binding.tvEmployeeName.text = item.employeeName
            
            val daysText = if (item.days == 1) "1 Day" else "${item.days} Days"
            binding.tvLeaveInfo.text = "${item.leaveType} • $daysText"

            binding.btnApprove.setOnClickListener {
                onApproveClick(item)
            }

            binding.btnReject.setOnClickListener {
                onRejectClick(item)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PendingApprovalItem>() {
        override fun areItemsTheSame(oldItem: PendingApprovalItem, newItem: PendingApprovalItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PendingApprovalItem, newItem: PendingApprovalItem): Boolean {
            return oldItem == newItem
        }
    }
}
