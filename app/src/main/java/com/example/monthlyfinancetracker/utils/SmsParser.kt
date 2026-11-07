package com.example.monthlyfinancetracker.utils

data class ParsedUpiMandate(
    val amount: Double?,
    val rawText: String,
    val isUpiMandateSent: Boolean,
    val merchantOrBeneficiary: String?
)

object SmsParser {

    fun parseSms(messageBody: String): ParsedUpiMandate {
        val isMandateSent = messageBody.contains("UPI Mandate: Sent", ignoreCase = true) ||
                messageBody.contains("UPI Mandate", ignoreCase = true) ||
                messageBody.contains("Mandate executed", ignoreCase = true) ||
                messageBody.contains("Sent Rs.", ignoreCase = true) ||
                messageBody.contains("Sent Rs", ignoreCase = true) ||
                messageBody.contains("Sent INR", ignoreCase = true) ||
                messageBody.contains("Sent ₹", ignoreCase = true)

        val amount = extractAmount(messageBody)
        val merchant = extractMerchant(messageBody)

        return ParsedUpiMandate(
            amount = amount,
            rawText = messageBody,
            isUpiMandateSent = isMandateSent,
            merchantOrBeneficiary = merchant
        )
    }

    fun extractAmount(text: String): Double? {
        val regex = Regex("""(?i)(?:rs\.?|inr|₹)\s*([\d,]+(?:\.\d{1,2})?)""")
        val match = regex.find(text) ?: return null
        val amountStr = match.groupValues[1].replace(",", "")
        return amountStr.toDoubleOrNull()
    }

    fun extractMerchant(text: String): String? {
        val regex = Regex("""(?i)(?:to|at|vpa|for)\s+([A-Za-z0-9\s]+?)(?:\.|,|\son\s|\sref|\sdate|$)""")
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.trim()
    }
}
