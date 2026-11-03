package com.example.monthlyfinancetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.monthlyfinancetracker.data.local.entity.CategoryEntity
import com.example.monthlyfinancetracker.data.local.entity.CommitmentEntity
import com.example.monthlyfinancetracker.data.local.entity.CommitmentWithCategory
import com.example.monthlyfinancetracker.data.local.entity.PaymentRecordEntity
import com.example.monthlyfinancetracker.data.local.entity.ReminderPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun updateCategory(category: CategoryEntity): Int

    @Delete
    suspend fun deleteCategory(category: CategoryEntity): Int

    // --- Commitments ---
    @Transaction
    @Query("SELECT * FROM commitments WHERE isArchived = 0 ORDER BY dueDay ASC")
    fun getAllCommitmentsWithCategory(): Flow<List<CommitmentWithCategory>>

    @Transaction
    @Query("SELECT * FROM commitments WHERE id = :id")
    suspend fun getCommitmentWithCategoryById(id: Long): CommitmentWithCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(commitment: CommitmentEntity): Long

    @Update
    suspend fun updateCommitment(commitment: CommitmentEntity): Int

    @Delete
    suspend fun deleteCommitment(commitment: CommitmentEntity): Int

    @Query("UPDATE commitments SET isArchived = 1 WHERE id = :id")
    suspend fun archiveCommitment(id: Long): Int

    // --- Payment Records ---
    @Query("SELECT * FROM payment_records WHERE year = :year AND month = :month")
    fun getPaymentRecordsForMonth(year: Int, month: Int): Flow<List<PaymentRecordEntity>>

    @Query("SELECT * FROM payment_records WHERE commitmentId = :commitmentId AND year = :year AND month = :month ORDER BY paymentDate DESC")
    fun getPaymentsForCommitmentAndMonth(commitmentId: Long, year: Int, month: Int): Flow<List<PaymentRecordEntity>>

    @Query("SELECT * FROM payment_records ORDER BY paymentDate DESC")
    fun getAllPaymentRecords(): Flow<List<PaymentRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentRecord(record: PaymentRecordEntity): Long

    @Delete
    suspend fun deletePaymentRecord(record: PaymentRecordEntity): Int

    // --- Reminder Preferences ---
    @Query("SELECT * FROM reminder_preferences")
    fun getAllReminderPreferences(): Flow<List<ReminderPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setReminderPreference(pref: ReminderPreferenceEntity): Long

    @Query("SELECT isReminderEnabled FROM reminder_preferences WHERE commitmentId = :commitmentId")
    suspend fun isReminderEnabled(commitmentId: Long): Boolean?
}
