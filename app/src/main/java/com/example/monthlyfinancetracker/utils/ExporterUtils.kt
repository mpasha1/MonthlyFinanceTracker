package com.example.monthlyfinancetracker.utils

import android.content.Context
import com.example.monthlyfinancetracker.data.local.entity.CommitmentWithCategory
import com.example.monthlyfinancetracker.data.local.entity.PaymentRecordEntity
import java.io.File

object ExporterUtils {

    fun generateCsvExport(
        commitments: List<CommitmentWithCategory>,
        payments: List<PaymentRecordEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("Commitment Name,Category,Monthly Amount,Due Day,Payment Method,Notes\n")
        commitments.forEach { cwc ->
            val c = cwc.commitment
            val cat = cwc.category
            sb.append("\"${c.name}\",\"${cat.name}\",${c.monthlyAmount},${c.dueDay},\"${c.paymentMethod}\",\"${c.notes}\"\n")
        }

        sb.append("\n\nPayment History\n")
        sb.append("Commitment ID,Year,Month,Amount Paid,Payment Date,Payment Method,Notes\n")
        payments.forEach { p ->
            sb.append("${p.commitmentId},${p.year},${p.month},${p.amountPaid},\"${DateUtils.formatDate(p.paymentDate)}\",\"${p.paymentMethod}\",\"${p.notes}\"\n")
        }

        return sb.toString()
    }

    fun saveCsvToFile(context: Context, csvData: String): File {
        val fileName = "monthly_finance_export_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(csvData)
        return file
    }
}
