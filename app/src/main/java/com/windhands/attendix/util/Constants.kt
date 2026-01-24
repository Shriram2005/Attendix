package com.windhands.attendix.util

/**
 * Application-wide constants
 */
object Constants {

    // Network
    const val NETWORK_TIMEOUT = 30L // seconds
    const val IP_RESTRICTED_CODE = "IP_RESTRICTED"

    // DataStore
    const val DATASTORE_NAME = "attendix_preferences"

    // Session Keys
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_EMPLOYEE_ID = "employee_id"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_FIRST_NAME = "first_name"
    const val KEY_LAST_NAME = "last_name"
    const val KEY_USER_ROLE = "user_role"
    const val KEY_DEPARTMENT = "department"
    const val KEY_DESIGNATION = "designation"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_REMEMBER_ME = "remember_me"
    const val KEY_PROFILE_PHOTO_URL = "profile_photo_url"

    // User Roles
    const val ROLE_ADMIN = "admin"
    const val ROLE_HR = "hr"
    const val ROLE_EMPLOYEE = "employee"

    // Attendance Status
    const val STATUS_PRESENT = "present"
    const val STATUS_ABSENT = "absent"
    const val STATUS_LATE = "late"
    const val STATUS_HALF_DAY = "half-day"
    const val STATUS_ON_LEAVE = "on-leave"

    // Leave Status
    const val LEAVE_PENDING = "pending"
    const val LEAVE_APPROVED = "approved"
    const val LEAVE_REJECTED = "rejected"

    // Leave Types
    const val LEAVE_TYPE_PAID = "paid"
    const val LEAVE_TYPE_UNPAID = "unpaid"

    // Date Formats
    const val DATE_FORMAT_API = "yyyy-MM-dd"
    const val DATE_FORMAT_DISPLAY = "MMM dd, yyyy"
    const val DATE_FORMAT_DISPLAY_SHORT = "MMM dd"
    const val TIME_FORMAT_API = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    const val TIME_FORMAT_DISPLAY = "hh:mm a"
    const val DATE_TIME_FORMAT_DISPLAY = "MMM dd, yyyy 'at' hh:mm a"

    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val FIRST_PAGE = 1
}
