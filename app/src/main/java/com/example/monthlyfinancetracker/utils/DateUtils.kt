package com.example.monthlyfinancetracker.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val monthNames = arrayOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    private val monthShortNames = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    fun getMonthName(month: Int): String {
        return if (month in 1..12) monthNames[month - 1] else ""
    }

    fun getMonthShortName(month: Int): String {
        return if (month in 1..12) monthShortNames[month - 1] else ""
    }

    fun formatMonthYear(year: Int, month: Int): String {
        return "${getMonthName(month)} $year"
    }

    fun formatDueDate(day: Int, month: Int): String {
        return "Due $day ${getMonthShortName(month)}"
    }

    fun formatFullDueDate(day: Int, month: Int, year: Int): String {
        return "Due: $day ${getMonthShortName(month)} $year"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        return calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, ...
    }
}
