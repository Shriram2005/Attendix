package com.windhands.attendix.ui.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.windhands.attendix.R
import com.windhands.attendix.databinding.ItemLeaveApprovalBinding
import com.windhands.attendix.ui.admin.LeaveApprovalItem

class LeaveApprovalAdapter(
    private val onApproveClick: (LeaveApprovalItem) -> Unit,
    private val onRejectClick: (LeaveApprovalItem) -> Unit
) : ListAdapter<LeaveApprovalItem, LeaveApprovalAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaveApprovalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemLeaveApprovalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LeaveApprovalItem) {
            val context = binding.root.context

            // Avatar
            binding.ivAvatar.visibility = View.GONE
            binding.tvAvatarInitial.text = item.initials

            // Employee name
            binding.tvEmployeeName.text = item.employeeName

            // Leave info
            val daysText = if (item.days == 1) "1 Day" else "${item.days} Days"
            binding.tvLeaveInfo.text = "${item.leaveType} • $daysText"

            // Status chip
            when {
                item.isPending -> {
                    binding.chipStatus.text = context.getString(R.string.status_pending)
                    binding.chipStatus.setTextColor(context.getColor(R.color.warning))
                    binding.chipStatus.setChipBackgroundColorResource(R.color.warning_light)
                }
                item.isApproved -> {
                    binding.chipStatus.text = context.getString(R.string.status_approved)
                    binding.chipStatus.setTextColor(context.getColor(R.color.success))
                    binding.chipStatus.setChipBackgroundColorResource(R.color.success_light)
                }
                item.isRejected -> {
                    binding.chipStatus.text = context.getString(R.string.status_rejected)
                    binding.chipStatus.setTextColor(context.getColor(R.color.error))
                    binding.chipStatus.setChipBackgroundColorResource(R.color.error_light)
                }
                else -> {
                    binding.chipStatus.text = item.status.replaceFirstChar { it.uppercase() }
                }
            }

            // Date range
            binding.tvDateRange.text = "${item.startDate} - ${item.endDate}"

            // Reason
            if (item.reason.isNotBlank()) {
                binding.tvReason.text = item.reason
                binding.tvReason.isVisible = true
            } else {
                binding.tvReason.isVisible = false
            }

            // Action buttons vs reviewer info
            if (item.isPending) {
                binding.layoutActions.isVisible = true
                binding.tvReviewerInfo.isVisible = false
                
                binding.btnApprove.setOnClickListener {
                    onApproveClick(item)
                }
                binding.btnReject.setOnClickListener {
                    onRejectClick(item)
                }
            } else {
                binding.layoutActions.isVisible = false
                
                if (!item.reviewerRemarks.isNullOrBlank()) {
                    binding.tvReviewerInfo.text = "Remarks: ${item.reviewerRemarks}"
                    binding.tvReviewerInfo.isVisible = true
                } else {
                    binding.tvReviewerInfo.isVisible = false
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<LeaveApprovalItem>() {
        override fun areItemsTheSame(oldItem: LeaveApprovalItem, newItem: LeaveApprovalItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LeaveApprovalItem, newItem: LeaveApprovalItem): Boolean {
            return oldItem == newItem
        }
    }
}
