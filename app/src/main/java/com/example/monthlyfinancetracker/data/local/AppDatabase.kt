package com.example.monthlyfinancetracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.monthlyfinancetracker.data.local.dao.FinanceDao
import com.example.monthlyfinancetracker.data.local.entity.CategoryEntity
import com.example.monthlyfinancetracker.data.local.entity.CommitmentEntity
import com.example.monthlyfinancetracker.data.local.entity.PaymentRecordEntity
import com.example.monthlyfinancetracker.data.local.entity.ReminderPreferenceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        CommitmentEntity::class,
        PaymentRecordEntity::class,
        ReminderPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monthly_finance_tracker_v3.db"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultCategoriesAndSampleData(database.financeDao())
                }
            }
        }

        private suspend fun populateDefaultCategoriesAndSampleData(dao: FinanceDao) {
            val defaultCategories = listOf(
                CategoryEntity(id = 1, name = "Rent", iconName = "home", colorHex = "#2E7D32", isDefault = true),
                CategoryEntity(id = 2, name = "Loan EMI", iconName = "bank", colorHex = "#1976D2", isDefault = true),
                CategoryEntity(id = 3, name = "Credit Card", iconName = "credit_card", colorHex = "#F57C00", isDefault = true),
                CategoryEntity(id = 4, name = "SIP", iconName = "trending_up", colorHex = "#E91E63", isDefault = true),
                CategoryEntity(id = 5, name = "Investment", iconName = "savings", colorHex = "#388E3C", isDefault = true),
                CategoryEntity(id = 6, name = "Bills", iconName = "receipt", colorHex = "#0097A7", isDefault = true),
                CategoryEntity(id = 7, name = "Other", iconName = "more_horiz", colorHex = "#7B1FA2", isDefault = true)
            )
            dao.insertCategories(defaultCategories)
        }
    }
}
