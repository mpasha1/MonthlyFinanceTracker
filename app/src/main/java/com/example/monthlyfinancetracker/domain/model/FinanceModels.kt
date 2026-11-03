package com.example.monthlyfinancetracker.domain.model

import com.example.monthlyfinancetracker.data.local.entity.CategoryEntity
import com.example.monthlyfinancetracker.data.local.entity.CommitmentEntity
import com.example.monthlyfinancetracker.data.local.entity.PaymentRecordEntity

enum class PaymentStatus {
    PAID,
    PARTIAL,
    PENDING
}

data class CommitmentItem(
    val commitment: CommitmentEntity,
    val category: CategoryEntity,
    val year: Int,
    val month: Int,
    val totalAmount: Double,
    val amountPaid: Double,
    val isReminderEnabled: Boolean = true,
    val paymentRecords: List<PaymentRecordEntity> = emptyList()
) {
    val remainingAmount: Double
        get() = (totalAmount - amountPaid).coerceAtLeast(0.0)

    val completionPercentage: Float
        get() = if (totalAmount > 0) ((amountPaid / totalAmount) * 100).coerceIn(0.0, 100.0).toFloat() else 0f

    val status: PaymentStatus
        get() = when {
            amountPaid >= totalAmount && totalAmount > 0 -> PaymentStatus.PAID
            amountPaid > 0 -> PaymentStatus.PARTIAL
            else -> PaymentStatus.PENDING
        }
}

data class MonthSummary(
    val year: Int,
    val month: Int,
    val totalCommitment: Double,
    val totalPaid: Double
) {
    val balance: Double
        get() = (totalCommitment - totalPaid).coerceAtLeast(0.0)

    val paidPercentage: Float
        get() = if (totalCommitment > 0) ((totalPaid / totalCommitment) * 100).coerceIn(0.0, 100.0).toFloat() else 0f
}

data class CategoryBreakdown(
    val category: CategoryEntity,
    val totalAmount: Double,
    val percentage: Float
)
