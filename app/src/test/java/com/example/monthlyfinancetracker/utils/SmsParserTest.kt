package com.example.monthlyfinancetracker.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParserTest {

    @Test
    fun testUpiMandateSentSmsParsing() {
        val sampleSms = "UPI Mandate: Sent Rs 15,000 to HDFC Mutual Fund for SIP ref 98213812."
        val result = SmsParser.parseSms(sampleSms)

        assertTrue(result.isUpiMandateSent)
        assertEquals(15000.0, result.amount!!, 0.01)
    }

    @Test
    fun testRupeeSymbolAmountExtraction() {
        val sampleSms = "UPI Mandate: Sent ₹18,500 for Credit Card payment."
        val result = SmsParser.parseSms(sampleSms)

        assertTrue(result.isUpiMandateSent)
        assertEquals(18500.0, result.amount!!, 0.01)
    }

    @Test
    fun testSentRsSmsParsing() {
        val sampleSms = "Sent Rs. 12,438 to PhonePe Loan Account on 03-11-2026."
        val result = SmsParser.parseSms(sampleSms)

        assertTrue(result.isUpiMandateSent)
        assertEquals(12438.0, result.amount!!, 0.01)
    }
}
