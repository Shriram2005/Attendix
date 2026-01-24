package com.windhands.attendix.ui.employee.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.windhands.attendix.R
import com.windhands.attendix.databinding.ItemAttendanceRecordBinding
import com.windhands.attendix.ui.employee.AttendanceRecordItem

class AttendanceRecordAdapter : ListAdapter<AttendanceRecordItem, AttendanceRecordAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceRecordBinding.inflate(
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
        private val binding: ItemAttendanceRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AttendanceRecordItem) {
            val context = binding.root.context

            // Date - show formatted date with day of week
            binding.tvDate.text = "${item.dayOfWeek}, ${item.date}"
            binding.tvTotalHours.text = "${item.formattedHours} Total"

            // Status badge (using TextView, not Chip)
            when {
                item.isPresent -> {
                    binding.tvStatus.text = context.getString(R.string.attendance_filter_present)
                    binding.tvStatus.setTextColor(context.getColor(R.color.success))
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_chip_success)
                }
                item.isAbsent -> {
                    binding.tvStatus.text = context.getString(R.string.attendance_filter_absent)
                    binding.tvStatus.setTextColor(context.getColor(R.color.error))
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_chip_error)
                }
                item.isLate -> {
                    binding.tvStatus.text = context.getString(R.string.attendance_filter_late)
                    binding.tvStatus.setTextColor(context.getColor(R.color.warning))
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_chip_warning)
                }
                item.isOnLeave -> {
                    binding.tvStatus.text = context.getString(R.string.admin_on_leave)
                    binding.tvStatus.setTextColor(context.getColor(R.color.info))
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_chip_info)
                }
                else -> {
                    binding.tvStatus.text = item.status.replaceFirstChar { it.uppercase() }
                    binding.tvStatus.setTextColor(context.getColor(R.color.on_surface_variant))
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_chip_neutral)
                }
            }

            // Times
            binding.tvCheckIn.text = item.checkIn
            binding.tvCheckOut.text = item.checkOut
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<AttendanceRecordItem>() {
        override fun areItemsTheSame(oldItem: AttendanceRecordItem, newItem: AttendanceRecordItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AttendanceRecordItem, newItem: AttendanceRecordItem): Boolean {
            return oldItem == newItem
        }
    }
}
