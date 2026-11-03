package com.example.monthlyfinancetracker.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommitmentDurationTest {

    private fun isCommitmentActive(
        startYear: Int,
        startMonth: Int,
        durationMonths: Int,
        viewingYear: Int,
        viewingMonth: Int
    ): Boolean {
        val startIdx = startYear * 12 + (startMonth - 1)
        val viewingIdx = viewingYear * 12 + (viewingMonth - 1)

        if (viewingIdx < startIdx) return false
        if (durationMonths <= 0) return true // Ongoing

        val endIdx = startIdx + durationMonths - 1
        return viewingIdx <= endIdx
    }

    @Test
    fun testOngoingCommitment() {
        // Ongoing commitment starting Oct 2026
        assertTrue(isCommitmentActive(2026, 10, 0, 2026, 10))
        assertTrue(isCommitmentActive(2026, 10, 0, 2028, 5))
        assertFalse(isCommitmentActive(2026, 10, 0, 2026, 9)) // Before start date
    }

    @Test
    fun testFixed12MonthsCommitment() {
        // 12 months commitment starting Oct 2026 (Oct 2026 - Sep 2027)
        assertTrue(isCommitmentActive(2026, 10, 12, 2026, 10)) // Month 1
        assertTrue(isCommitmentActive(2026, 10, 12, 2027, 9))  // Month 12
        assertFalse(isCommitmentActive(2026, 10, 12, 2027, 10)) // Month 13 (Expired)
        assertFalse(isCommitmentActive(2026, 10, 12, 2028, 1))  // Month 16 (Expired)
    }
}
