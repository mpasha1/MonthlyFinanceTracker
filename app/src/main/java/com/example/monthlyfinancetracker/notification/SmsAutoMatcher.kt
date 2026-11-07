package com.example.monthlyfinancetracker.notification

import android.content.Context
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.local.entity.PaymentRecordEntity
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import com.example.monthlyfinancetracker.utils.SmsReader
import kotlinx.coroutines.flow.first
import java.util.Calendar
import kotlin.math.abs

object SmsAutoMatcher {

    suspend fun performAutoMatchForCurrentMonth(context: Context): Int {
        val dao = AppDatabase.getDatabase(context).financeDao()
        val repository = FinanceRepository(dao)

        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1

        val messages = SmsReader.readUpiMandateSmsForCurrentMonth(context, year, month)
        if (messages.isEmpty()) return 0

        val currentItems = repository.getCommitmentItemsForMonth(year, month).first()
        val pendingItems = currentItems.filter { it.remainingAmount > 0 }

        var matchCount = 0

        for (msg in messages) {
            val smsAmount = msg.amount ?: continue
            if (!msg.isMandateSent) continue

            // Match Loan EMI commitment or any commitment matching the extracted amount
            val targetCommitment = pendingItems.find { item ->
                (item.category.name.contains("Loan", ignoreCase = true) || item.category.name.contains("EMI", ignoreCase = true)) &&
                        (abs(item.totalAmount - smsAmount) < 1.0 || abs(item.remainingAmount - smsAmount) < 1.0)
            } ?: pendingItems.find { item ->
                abs(item.totalAmount - smsAmount) < 1.0 || abs(item.remainingAmount - smsAmount) < 1.0
            }

            if (targetCommitment != null) {
                val existingLogs = targetCommitment.paymentRecords
                val alreadyLogged = existingLogs.any { abs(it.amountPaid - smsAmount) < 1.0 }

                if (!alreadyLogged) {
                    val record = PaymentRecordEntity(
                        commitmentId = targetCommitment.commitment.id,
                        year = year,
                        month = month,
                        amountPaid = smsAmount,
                        paymentDate = msg.timestamp,
                        paymentMethod = "UPI Mandate",
                        notes = "Auto-matched from SMS (UPI Mandate: Sent)"
                    )
                    repository.logPayment(record)
                    matchCount++
                }
            }
        }

        return matchCount
    }
}
