package com.windhands.attendix.ui.employee.model

/**
 * Shared model for leave balance display across different screens
 */
data class LeaveBalanceItem(
    val type: String,
    val displayName: String,
    val used: Int = 0,
    val total: Int,
    val remaining: Int,
    val iconType: LeaveIconType = LeaveIconType.OTHER
) {
    val progress: Int
        get() = if (total > 0) ((total - remaining) * 100 / total) else 0
}

enum class LeaveIconType {
    ANNUAL, SICK, CASUAL, UNPAID, OTHER
}
