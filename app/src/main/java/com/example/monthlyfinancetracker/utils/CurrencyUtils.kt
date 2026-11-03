package com.example.monthlyfinancetracker.utils

import java.text.DecimalFormat

object CurrencyUtils {

    fun formatRupees(amount: Double, includeSymbol: Boolean = true): String {
        val longVal = amount.toLong()
        val formattedNumber = formatIndianNumberSystem(longVal)
        return if (includeSymbol) "₹$formattedNumber" else formattedNumber
    }

    private fun formatIndianNumberSystem(number: Long): String {
        val str = number.toString()
        if (str.length <= 3) return str

        val lastThree = str.substring(str.length - 3)
        val remaining = str.substring(0, str.length - 3)

        val sb = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            sb.append(remaining[i])
            count++
            if (count % 2 == 0 && i != 0) {
                sb.append(',')
            }
        }

        return sb.reverse().append(',').append(lastThree).toString()
    }
}
