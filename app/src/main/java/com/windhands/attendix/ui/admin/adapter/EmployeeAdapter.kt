package com.windhands.attendix.ui.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.windhands.attendix.R
import com.windhands.attendix.databinding.ItemEmployeeBinding
import com.windhands.attendix.ui.admin.EmployeeItem

class EmployeeAdapter(
    private val onItemClick: (EmployeeItem) -> Unit,
    private val onEditClick: (EmployeeItem) -> Unit,
    private val onViewAttendanceClick: (EmployeeItem) -> Unit,
    private val onDeactivateClick: (EmployeeItem) -> Unit,
    private val onActivateClick: (EmployeeItem) -> Unit,
    private val onDeleteClick: (EmployeeItem) -> Unit
) : ListAdapter<EmployeeItem, EmployeeAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEmployeeBinding.inflate(
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
        private val binding: ItemEmployeeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EmployeeItem) {
            val context = binding.root.context

            // Avatar and initials
            binding.tvAvatarInitial.text = item.initials
            binding.ivAvatar.visibility = View.GONE
            
            // Status indicator
            binding.viewStatusIndicator.setBackgroundResource(
                if (item.isActive) R.drawable.bg_status_indicator
                else R.drawable.bg_status_indicator_inactive
            )

            // Name
            binding.tvName.text = item.fullName

            // Employee info
            val department = item.department ?: "No Department"
            binding.tvEmployeeInfo.text = "${item.employeeId} • $department"

            // Role/Designation
            binding.tvRole.text = item.designation ?: item.role.replaceFirstChar { it.uppercase() }

            // Email
            binding.tvEmail.text = item.email

            // Status chip
            if (item.isActive) {
                binding.chipStatus.text = context.getString(R.string.employee_active)
                binding.chipStatus.setTextColor(context.getColor(R.color.success))
                binding.chipStatus.setChipBackgroundColorResource(R.color.success_light)
            } else {
                binding.chipStatus.text = context.getString(R.string.employee_inactive)
                binding.chipStatus.setTextColor(context.getColor(R.color.error))
                binding.chipStatus.setChipBackgroundColorResource(R.color.error_light)
            }

            // Click listeners
            binding.root.setOnClickListener {
                onItemClick(item)
            }

            binding.btnMore.setOnClickListener { view ->
                showPopupMenu(view, item)
            }
        }

        private fun showPopupMenu(anchor: View, item: EmployeeItem) {
            val popup = PopupMenu(anchor.context, anchor)
            popup.menuInflater.inflate(R.menu.menu_employee_options, popup.menu)
            
            // Show/hide activate/deactivate based on status
            popup.menu.findItem(R.id.action_activate).isVisible = !item.isActive
            popup.menu.findItem(R.id.action_deactivate).isVisible = item.isActive
            
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_edit -> {
                        onEditClick(item)
                        true
                    }
                    R.id.action_view_attendance -> {
                        onViewAttendanceClick(item)
                        true
                    }
                    R.id.action_activate -> {
                        onActivateClick(item)
                        true
                    }
                    R.id.action_deactivate -> {
                        onDeactivateClick(item)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(item)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<EmployeeItem>() {
        override fun areItemsTheSame(oldItem: EmployeeItem, newItem: EmployeeItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EmployeeItem, newItem: EmployeeItem): Boolean {
            return oldItem == newItem
        }
    }
}
