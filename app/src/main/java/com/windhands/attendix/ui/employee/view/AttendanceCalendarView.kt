package com.windhands.attendix.ui.employee.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.windhands.attendix.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Custom calendar view for displaying attendance records
 * Each day is colored based on attendance status
 */
class AttendanceCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Current month being displayed
    private var currentMonth: YearMonth = YearMonth.now()
    
    // Map of date to attendance status
    private var attendanceMap: Map<LocalDate, AttendanceStatus> = emptyMap()
    
    // Selected date
    private var selectedDate: LocalDate? = null
    
    // Listener for date selection
    var onDateSelectedListener: ((LocalDate) -> Unit)? = null
    
    // Paint objects
    private val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val todayCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // Layout dimensions
    private var cellWidth = 0f
    private var cellHeight = 0f
    private val headerHeight = 100f
    private val dayOfWeekHeight = 80f
    private val padding = 8f
    private val cellPadding = 4f
    private val cornerRadius = 12f
    
    // Day of week labels
    private val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    // Store cell bounds for touch detection
    private val cellBounds = mutableListOf<CellInfo>()
    
    init {
        setupPaints()
        isClickable = true
        isFocusable = true
    }
    
    private fun setupPaints() {
        // Text paint for day numbers
        textPaint.textSize = 36f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = ContextCompat.getColor(context, R.color.text_primary)
        
        // Header text paint
        headerTextPaint.textSize = 48f
        headerTextPaint.textAlign = Paint.Align.CENTER
        headerTextPaint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        headerTextPaint.color = ContextCompat.getColor(context, R.color.primary)
        
        // Today circle paint
        todayCirclePaint.style = Paint.Style.STROKE
        todayCirclePaint.strokeWidth = 4f
        todayCirclePaint.color = ContextCompat.getColor(context, R.color.primary)
        
        // Selected circle paint
        selectedCirclePaint.style = Paint.Style.STROKE
        selectedCirclePaint.strokeWidth = 6f
        selectedCirclePaint.color = ContextCompat.getColor(context, R.color.primary)
    }
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        
        // Calculate cell dimensions
        cellWidth = (width - (padding * 2)) / 7f
        cellHeight = cellWidth // Square cells
        
        // Calculate total height: header + day labels + 6 weeks of days
        val totalHeight = (headerHeight + dayOfWeekHeight + (cellHeight * 6) + padding * 2).toInt()
        
        setMeasuredDimension(width, totalHeight)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Clear cell bounds for new layout
        cellBounds.clear()
        
        // Draw month/year header
        drawHeader(canvas)
        
        // Draw day of week labels
        drawDayOfWeekLabels(canvas)
        
        // Draw calendar days
        drawCalendarDays(canvas)
    }
    
    private fun drawHeader(canvas: Canvas) {
        val monthYear = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
        val y = headerHeight / 2 + headerTextPaint.textSize / 3
        canvas.drawText(monthYear, width / 2f, y, headerTextPaint)
    }
    
    private fun drawDayOfWeekLabels(canvas: Canvas) {
        val startY = headerHeight
        val textY = startY + dayOfWeekHeight / 2 + textPaint.textSize / 3
        
        val dayTextPaint = Paint(textPaint).apply {
            textSize = 32f
            color = ContextCompat.getColor(context, R.color.text_secondary)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        
        daysOfWeek.forEachIndexed { index, day ->
            val x = padding + cellWidth * index + cellWidth / 2
            canvas.drawText(day, x, textY, dayTextPaint)
        }
    }
    
    private fun drawCalendarDays(canvas: Canvas) {
        val startY = headerHeight + dayOfWeekHeight
        val firstDayOfMonth = currentMonth.atDay(1)
        val lastDayOfMonth = currentMonth.atEndOfMonth()
        
        // Get day of week for first day (0 = Sunday, 6 = Saturday)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
        
        // Calculate total days to display (including previous/next month)
        var currentDate = firstDayOfMonth.minusDays(firstDayOfWeek.toLong())
        
        // Draw 6 weeks
        for (week in 0 until 6) {
            for (day in 0 until 7) {
                val x = padding + cellWidth * day
                val y = startY + cellHeight * week
                
                drawDay(canvas, currentDate, x, y)
                currentDate = currentDate.plusDays(1)
            }
        }
    }
    
    private fun drawDay(canvas: Canvas, date: LocalDate, x: Float, y: Float) {
        val isCurrentMonth = date.month == currentMonth.month && date.year == currentMonth.year
        val isToday = date == LocalDate.now()
        val isSelected = date == selectedDate
        val status = attendanceMap[date]
        
        // Create cell bounds for touch detection
        val cellRect = RectF(
            x + cellPadding,
            y + cellPadding,
            x + cellWidth - cellPadding,
            y + cellHeight - cellPadding
        )
        
        if (isCurrentMonth) {
            cellBounds.add(CellInfo(date, cellRect))
        }
        
        // Draw background based on attendance status
        if (isCurrentMonth && status != null) {
            dayPaint.color = getStatusColor(status)
            dayPaint.style = Paint.Style.FILL
            canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, dayPaint)
        } else {
            // Empty background for days without attendance
            dayPaint.color = ContextCompat.getColor(
                context,
                if (isCurrentMonth) R.color.surface_variant else R.color.surface_overlay
            )
            dayPaint.style = Paint.Style.FILL
            dayPaint.alpha = if (isCurrentMonth) 30 else 15
            canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, dayPaint)
            dayPaint.alpha = 255
        }
        
        // Draw today indicator
        if (isToday) {
            canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, todayCirclePaint)
        }
        
        // Draw selected indicator
        if (isSelected) {
            canvas.drawRoundRect(cellRect, cornerRadius, cornerRadius, selectedCirclePaint)
        }
        
        // Draw day number
        val dayText = date.dayOfMonth.toString()
        val textX = x + cellWidth / 2
        val textY = y + cellHeight / 2 + textPaint.textSize / 3
        
        // Determine text color
        val textColor = when {
            !isCurrentMonth -> ContextCompat.getColor(context, R.color.text_disabled)
            status != null -> getStatusTextColor(status)
            else -> ContextCompat.getColor(context, R.color.text_primary)
        }
        
        textPaint.color = textColor
        canvas.drawText(dayText, textX, textY, textPaint)
    }
    
    private fun getStatusColor(status: AttendanceStatus): Int {
        return when (status) {
            AttendanceStatus.PRESENT -> ContextCompat.getColor(context, R.color.status_present)
            AttendanceStatus.ABSENT -> ContextCompat.getColor(context, R.color.status_absent)
            AttendanceStatus.LATE -> ContextCompat.getColor(context, R.color.status_late)
            AttendanceStatus.ON_LEAVE -> ContextCompat.getColor(context, R.color.status_on_leave)
            AttendanceStatus.HALF_DAY -> ContextCompat.getColor(context, R.color.status_half_day)
            AttendanceStatus.WORKING -> ContextCompat.getColor(context, R.color.status_working)
        }
    }
    
    private fun getStatusTextColor(status: AttendanceStatus): Int {
        // Return white for all status colors for better visibility
        return ContextCompat.getColor(context, R.color.white)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                // Find which cell was touched
                android.util.Log.d("CalendarView", "Touch event at: ${event.x}, ${event.y}")
                android.util.Log.d("CalendarView", "Cell bounds count: ${cellBounds.size}")

                for (cellInfo in cellBounds) {
                    if (cellInfo.bounds.contains(event.x, event.y)) {
                        selectedDate = cellInfo.date
                        android.util.Log.d("CalendarView", "Cell touched! Date: ${cellInfo.date}")
                        android.util.Log.d("CalendarView", "Listener is null: ${onDateSelectedListener == null}")
                        onDateSelectedListener?.invoke(cellInfo.date)
                        invalidate()
                        parent?.requestDisallowInterceptTouchEvent(false)
                        return true
                    }
                }
                android.util.Log.d("CalendarView", "No cell found at touch position")
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                return false
            }
        }
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
    
    /**
     * Update the calendar with new attendance data
     */
    fun setAttendanceData(data: Map<LocalDate, AttendanceStatus>) {
        attendanceMap = data
        invalidate()
    }
    
    /**
     * Navigate to previous month
     */
    fun previousMonth() {
        currentMonth = currentMonth.minusMonths(1)
        selectedDate = null
        invalidate()
    }
    
    /**
     * Navigate to next month
     */
    fun nextMonth() {
        currentMonth = currentMonth.plusMonths(1)
        selectedDate = null
        invalidate()
    }
    
    /**
     * Go to today's date
     */
    fun goToToday() {
        currentMonth = YearMonth.now()
        selectedDate = LocalDate.now()
        invalidate()
        onDateSelectedListener?.invoke(LocalDate.now())
    }
    
    /**
     * Get current month
     */
    fun getCurrentMonth(): YearMonth = currentMonth
    
    /**
     * Get selected date
     */
    fun getSelectedDate(): LocalDate? = selectedDate
    
    /**
     * Clear selection
     */
    fun clearSelection() {
        selectedDate = null
        invalidate()
    }
    
    private data class CellInfo(
        val date: LocalDate,
        val bounds: RectF
    )
}

/**
 * Attendance status enum
 */
enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    ON_LEAVE,
    HALF_DAY,
    WORKING;
    
    companion object {
        fun fromString(status: String): AttendanceStatus {
            return when (status.lowercase().replace("-", "_").replace(" ", "_")) {
                "present" -> PRESENT
                "absent" -> ABSENT
                "late" -> LATE
                "on_leave", "leave", "onleave" -> ON_LEAVE
                "half_day", "halfday" -> HALF_DAY
                "working" -> WORKING
                else -> PRESENT
            }
        }
    }
}
