package com.example.monthlyfinancetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyUtilsTest {

    @Test
    fun testFormatRupeesIndianSystem() {
        assertEquals("₹1,21,000", CurrencyUtils.formatRupees(121000.0))
        assertEquals("₹95,000", CurrencyUtils.formatRupees(95000.0))
        assertEquals("₹26,000", CurrencyUtils.formatRupees(26000.0))
        assertEquals("₹18,500", CurrencyUtils.formatRupees(18500.0))
        assertEquals("₹7,500", CurrencyUtils.formatRupees(7500.0))
        assertEquals("₹500", CurrencyUtils.formatRupees(500.0))
    }

    @Test
    fun testFormatMonthNameAndDueDate() {
        assertEquals("October 2026", DateUtils.formatMonthYear(2026, 10))
        assertEquals("Due 5 Oct", DateUtils.formatDueDate(5, 10))
        assertEquals("Due: 12 Oct 2026", DateUtils.formatFullDueDate(12, 10, 2026))
    }
}
