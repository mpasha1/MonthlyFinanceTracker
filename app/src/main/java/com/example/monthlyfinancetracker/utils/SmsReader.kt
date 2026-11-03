package com.example.monthlyfinancetracker.utils

import android.content.Context
import android.net.Uri
import com.example.monthlyfinancetracker.domain.model.SmsMessageItem
import java.util.Calendar

object SmsReader {

    fun readUpiMandateSmsForCurrentMonth(
        context: Context,
        year: Int = Calendar.getInstance().get(Calendar.YEAR),
        month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    ): List<SmsMessageItem> {
        val messagesList = mutableListOf<SmsMessageItem>()

        // Current month bounds
        val startCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = startCal.timeInMillis

        val endCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endMillis = endCal.timeInMillis

        val inboxUri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")
        val selection = "date >= ? AND date <= ?"
        val selectionArgs = arrayOf(startMillis.toString(), endMillis.toString())
        val sortOrder = "date DESC"

        try {
            val cursor = context.contentResolver.query(
                inboxUri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            cursor?.use { c ->
                val idIdx = c.getColumnIndexOrThrow("_id")
                val addressIdx = c.getColumnIndexOrThrow("address")
                val bodyIdx = c.getColumnIndexOrThrow("body")
                val dateIdx = c.getColumnIndexOrThrow("date")

                while (c.moveToNext()) {
                    val id = c.getLong(idIdx)
                    val address = c.getString(addressIdx) ?: ""
                    val body = c.getString(bodyIdx) ?: ""
                    val date = c.getLong(dateIdx)

                    // Filter by text "UPI Mandate: Sent" or "UPI Mandate"
                    if (body.contains("UPI Mandate: Sent", ignoreCase = true) ||
                        body.contains("UPI Mandate", ignoreCase = true)
                    ) {
                        val parsed = SmsParser.parseSms(body)
                        messagesList.add(
                            SmsMessageItem(
                                id = id,
                                sender = address,
                                body = body,
                                timestamp = date,
                                amount = parsed.amount,
                                merchant = parsed.merchantOrBeneficiary,
                                isMandateSent = parsed.isUpiMandateSent
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return messagesList
    }
}
