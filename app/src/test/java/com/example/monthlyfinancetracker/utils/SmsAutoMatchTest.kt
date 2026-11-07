package com.example.monthlyfinancetracker.utils

import com.example.monthlyfinancetracker.data.local.entity.CategoryEntity
import com.example.monthlyfinancetracker.data.local.entity.CommitmentEntity
import com.example.monthlyfinancetracker.domain.model.CommitmentItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlin.math.abs

class SmsAutoMatchTest {

    @Test
    fun testLoanEmiAmountMatching() {
        val loanCategory = CategoryEntity(id = 2, name = "Loan EMI", iconName = "bank", colorHex = "#1976D2")

        val phonePeLoan = CommitmentItem(
            commitment = CommitmentEntity(id = 10, name = "phonepe loan", categoryId = 2, monthlyAmount = 12438.0, dueDay = 3),
            category = loanCategory,
            year = 2026,
            month = 11,
            totalAmount = 12438.0,
            amountPaid = 0.0
        )

        val credLoan = CommitmentItem(
            commitment = CommitmentEntity(id = 11, name = "cred loan", categoryId = 2, monthlyAmount = 6192.0, dueDay = 3),
            category = loanCategory,
            year = 2026,
            month = 11,
            totalAmount = 6192.0,
            amountPaid = 0.0
        )

        val commitments = listOf(phonePeLoan, credLoan)

        val smsText = "UPI Mandate: Sent Rs 12,438 to PhonePe Loan Account ref 982193."
        val parsed = SmsParser.parseSms(smsText)

        assertNotNull(parsed.amount)
        assertEquals(12438.0, parsed.amount!!, 0.01)

        val matchedCommitment = commitments.find { c ->
            c.category.name.contains("Loan", ignoreCase = true) &&
                    abs(c.totalAmount - parsed.amount!!) < 1.0
        }

        assertNotNull(matchedCommitment)
        assertEquals("phonepe loan", matchedCommitment?.commitment?.name)
    }
}
