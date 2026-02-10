package com.windhands.attendix.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

// View Extensions
fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.visibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

fun View.enable() {
    isEnabled = true
    alpha = 1.0f
}

fun View.disable() {
    isEnabled = false
    alpha = 0.5f
}

// Keyboard Extensions
fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.hideKeyboard() {
    currentFocus?.let { view ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun FragmentActivity.hideKeyboard() {
    currentFocus?.let { view ->
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

// Toast & Snackbar Extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, duration)
}

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    duration: Int = Snackbar.LENGTH_INDEFINITE,
    action: () -> Unit
) {
    Snackbar.make(this, message, duration)
        .setAction(actionText) { action() }
        .show()
}

// Date Extensions
fun String.toDisplayDate(): String {
    return try {
        val inputFormat = SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.getDefault())
        val outputFormat = SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault())
        val date = inputFormat.parse(this)
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this
    }
}

fun String.toDisplayTime(): String {
    val timeFormatter = DateTimeFormatter.ofPattern(Constants.TIME_FORMAT_DISPLAY, Locale.getDefault())
    val parsedDateTime = parseApiDateTime(this)
    if (parsedDateTime != null) {
        return parsedDateTime.format(timeFormatter)
    }

    val localTime = parseApiLocalTime(this)
    return localTime?.format(timeFormatter) ?: this
}

fun String.toDisplayDateTime(): String {
    val dateTimeFormatter =
        DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT_DISPLAY, Locale.getDefault())
    return parseApiDateTime(this)?.format(dateTimeFormatter) ?: this
}

private fun parseApiDateTime(value: String): ZonedDateTime? {
    val normalized = value.trim()
    if (normalized.isBlank()) return null

    val isoValue = if (normalized.contains(' ') && !normalized.contains('T')) {
        normalized.replace(' ', 'T')
    } else {
        normalized
    }

    val parsed = runCatching {
        DateTimeFormatter.ISO_DATE_TIME.parseBest(
            isoValue,
            ZonedDateTime::from,
            OffsetDateTime::from,
            LocalDateTime::from
        )
    }.getOrNull() ?: return null

    return when (parsed) {
        is ZonedDateTime -> parsed.withZoneSameInstant(ZoneId.systemDefault())
        is OffsetDateTime -> parsed.atZoneSameInstant(ZoneId.systemDefault())
        is LocalDateTime -> parsed.atZone(ZoneId.systemDefault())
        else -> null
    }
}

private fun parseApiLocalTime(value: String): LocalTime? {
    val normalized = value.trim()
    if (normalized.isBlank()) return null
    return runCatching { LocalTime.parse(normalized, DateTimeFormatter.ISO_LOCAL_TIME) }.getOrNull()
}

fun Date.toApiDateString(): String {
    val format = SimpleDateFormat(Constants.DATE_FORMAT_API, Locale.getDefault())
    return format.format(this)
}

fun Date.toDisplayDateString(): String {
    val format = SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault())
    return format.format(this)
}

// String Extensions
fun String?.orEmpty(): String = this ?: ""

fun String?.isNotNullOrEmpty(): Boolean = !this.isNullOrEmpty()

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}

// Number Extensions
fun Double?.formatHours(): String {
    if (this == null) return "0h 0m"
    val hours = this.toInt()
    val minutes = ((this - hours) * 60).toInt()
    return "${hours}h ${minutes}m"
}

fun Int?.orZero(): Int = this ?: 0
fun Double?.orZero(): Double = this ?: 0.0
