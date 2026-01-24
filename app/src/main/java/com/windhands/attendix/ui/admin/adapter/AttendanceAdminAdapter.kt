package com.windhands.attendix.ui.admin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.windhands.attendix.R
import com.windhands.attendix.databinding.ItemAttendanceAdminBinding
import com.windhands.attendix.ui.admin.AttendanceAdminItem

class AttendanceAdminAdapter : ListAdapter<AttendanceAdminItem, AttendanceAdminAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceAdminBinding.inflate(
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
        private val binding: ItemAttendanceAdminBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AttendanceAdminItem) {
            val context = binding.root.context

            // Avatar
            binding.ivAvatar.visibility = View.GONE
            binding.tvAvatarInitial.text = item.initials

            // Employee name
            binding.tvEmployeeName.text = item.employeeName

            // Department
            binding.tvDepartment.text = item.department.ifEmpty { "N/A" }

            // Status chip
            when {
                item.isPresent -> {
                    binding.chipStatus.text = context.getString(R.string.attendance_filter_present)
                    binding.chipStatus.setTextColor(context.getColor(R.color.success))
                    binding.chipStatus.setChipBackgroundColorResource(R.color.success_light)
                }
                item.isAbsent -> {
                    binding.chipStatus.text = context.getString(R.string.attendance_filter_absent)
                    binding.chipStatus.setTextColor(context.getColor(R.color.error))
                    binding.chipStatus.setChipBackgroundColorResource(R.color.error_light)
                }
                item.isLate -> {
                    binding.chipStatus.text = context.getString(R.string.attendance_filter_late)
                    binding.chipStatus.setTextColor(context.getColor(R.color.warning))
                    binding.chipStatus.setChipBackgroundColorResource(R.color.warning_light)
                }
                item.isOnLeave -> {
                    binding.chipStatus.text = context.getString(R.string.admin_on_leave)
                    binding.chipStatus.setTextColor(context.getColor(R.color.info))
                    binding.chipStatus.setChipBackgroundColorResource(R.color.info_background)
                }
                else -> {
                    binding.chipStatus.text = item.status.replaceFirstChar { it.uppercase() }
                    binding.chipStatus.setTextColor(context.getColor(R.color.on_surface_variant))
                    binding.chipStatus.setChipBackgroundColorResource(R.color.surface_variant)
                }
            }

            // Check-in time
            binding.tvCheckIn.text = item.checkIn
            binding.tvCheckIn.setTextColor(
                context.getColor(if (item.checkIn != "--:--") R.color.success else R.color.on_surface_variant)
            )

            // Check-out time
            binding.tvCheckOut.text = item.checkOut
            binding.tvCheckOut.setTextColor(
                context.getColor(if (item.checkOut != "--:--") R.color.error else R.color.on_surface_variant)
            )

            // Working hours
            binding.tvTotalHours.text = item.formattedWorkingHours
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<AttendanceAdminItem>() {
        override fun areItemsTheSame(oldItem: AttendanceAdminItem, newItem: AttendanceAdminItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AttendanceAdminItem, newItem: AttendanceAdminItem): Boolean {
            return oldItem == newItem
        }
    }
}
