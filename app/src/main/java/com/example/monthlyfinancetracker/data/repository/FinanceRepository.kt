package com.example.monthlyfinancetracker.data.repository

import com.example.monthlyfinancetracker.data.local.dao.FinanceDao
import com.example.monthlyfinancetracker.data.local.entity.CategoryEntity
import com.example.monthlyfinancetracker.data.local.entity.CommitmentEntity
import com.example.monthlyfinancetracker.data.local.entity.PaymentRecordEntity
import com.example.monthlyfinancetracker.data.local.entity.ReminderPreferenceEntity
import com.example.monthlyfinancetracker.domain.model.CategoryBreakdown
import com.example.monthlyfinancetracker.domain.model.CommitmentItem
import com.example.monthlyfinancetracker.domain.model.MonthSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class FinanceRepository(
    private val dao: FinanceDao
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = dao.getAllCategories()

    suspend fun getCategoryById(id: Long) = dao.getCategoryById(id)

    suspend fun insertCategory(category: CategoryEntity) = dao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) = dao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = dao.deleteCategory(category)

    fun getCommitmentItemsForMonth(year: Int, month: Int): Flow<List<CommitmentItem>> {
        return combine(
            dao.getAllCommitmentsWithCategory(),
            dao.getPaymentRecordsForMonth(year, month),
            dao.getAllReminderPreferences()
        ) { commitmentsWithCategory, paymentRecords, reminderPrefs ->
            val reminderMap = reminderPrefs.associate { it.commitmentId to it.isReminderEnabled }

            val viewingMonthIndex = year * 12 + (month - 1)

            commitmentsWithCategory
                .filter { cwc ->
                    val c = cwc.commitment
                    val startMonthIndex = c.startYear * 12 + (c.startMonth - 1)

                    if (viewingMonthIndex < startMonthIndex) {
                        return@filter false
                    }

                    if (c.frequency == "ONE_TIME") {
                        viewingMonthIndex == startMonthIndex
                    } else {
                        // EVERY_MONTH
                        if (c.durationMonths <= 0) {
                            true // Ongoing / Indefinite
                        } else {
                            val endMonthIndex = startMonthIndex + c.durationMonths - 1
                            viewingMonthIndex <= endMonthIndex
                        }
                    }
                }
                .map { cwc ->
                    val commitment = cwc.commitment
                    val category = cwc.category
                    val recordsForCommitment = paymentRecords.filter { it.commitmentId == commitment.id }
                    val paidSum = recordsForCommitment.sumOf { it.amountPaid }
                    val isReminder = reminderMap[commitment.id] ?: true

                    CommitmentItem(
                        commitment = commitment,
                        category = category,
                        year = year,
                        month = month,
                        totalAmount = commitment.monthlyAmount,
                        amountPaid = paidSum,
                        isReminderEnabled = isReminder,
                        paymentRecords = recordsForCommitment
                    )
                }
        }
    }

    fun getMonthSummary(year: Int, month: Int): Flow<MonthSummary> {
        return getCommitmentItemsForMonth(year, month).combine(dao.getAllCommitmentsWithCategory()) { items, _ ->
            val totalCommitment = items.sumOf { it.totalAmount }
            val totalPaid = items.sumOf { it.amountPaid }
            MonthSummary(
                year = year,
                month = month,
                totalCommitment = totalCommitment,
                totalPaid = totalPaid
            )
        }
    }

    fun getCategoryBreakdownsForMonth(year: Int, month: Int): Flow<List<CategoryBreakdown>> {
        return getCommitmentItemsForMonth(year, month).combine(dao.getAllCategories()) { items, categories ->
            val totalSum = items.sumOf { it.totalAmount }
            val categoryMap = items.groupBy { it.category.id }

            categories.mapNotNull { cat ->
                val itemsForCat = categoryMap[cat.id] ?: emptyList()
                val catTotal = itemsForCat.sumOf { it.totalAmount }
                if (catTotal > 0) {
                    val pct = if (totalSum > 0) ((catTotal / totalSum) * 100).toFloat() else 0f
                    CategoryBreakdown(
                        category = cat,
                        totalAmount = catTotal,
                        percentage = pct
                    )
                } else null
            }.sortedByDescending { it.totalAmount }
        }
    }

    suspend fun getCommitmentById(id: Long) = dao.getCommitmentWithCategoryById(id)

    suspend fun consolidateOtherCommitments() {
        val otherCommitments = dao.getOtherCommitments()
        if (otherCommitments.size <= 1) return

        val primary = otherCommitments.first()
        val secondaries = otherCommitments.drop(1)

        for (sec in secondaries) {
            dao.reassignPaymentRecords(sec.id, primary.id)
            dao.deleteCommitment(sec)
        }

        val totalPaidSum = dao.getTotalPaymentsForCommitment(primary.id) ?: primary.monthlyAmount
        val updatedPrimary = primary.copy(
            monthlyAmount = maxOf(primary.monthlyAmount, totalPaidSum)
        )
        dao.updateCommitment(updatedPrimary)
    }

    fun getPaymentsForCommitmentAndMonth(commitmentId: Long, year: Int, month: Int): Flow<List<PaymentRecordEntity>> {
        return dao.getPaymentsForCommitmentAndMonth(commitmentId, year, month)
    }

    suspend fun saveCommitment(commitment: CommitmentEntity): Long {
        return if (commitment.id == 0L) {
            dao.insertCommitment(commitment)
        } else {
            dao.updateCommitment(commitment)
            commitment.id
        }
    }

    suspend fun deleteCommitment(commitment: CommitmentEntity) {
        dao.deleteCommitment(commitment)
    }

    suspend fun logPayment(record: PaymentRecordEntity): Long {
        return dao.insertPaymentRecord(record)
    }

    suspend fun deletePaymentRecord(record: PaymentRecordEntity) {
        dao.deletePaymentRecord(record)
    }

    suspend fun toggleReminder(commitmentId: Long, enabled: Boolean) {
        dao.setReminderPreference(ReminderPreferenceEntity(commitmentId, enabled))
    }

    fun getAllPaymentRecords(): Flow<List<PaymentRecordEntity>> = dao.getAllPaymentRecords()
}
