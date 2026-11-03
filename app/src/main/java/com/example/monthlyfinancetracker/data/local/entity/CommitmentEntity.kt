package com.example.monthlyfinancetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "commitments",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class CommitmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val categoryId: Long,
    val monthlyAmount: Double,
    val dueDay: Int, // 1..31
    val frequency: String = "EVERY_MONTH", // EVERY_MONTH, ONE_TIME
    val durationMonths: Int = 0, // 0 = Ongoing / Indefinite, >0 = Number of months
    val startYear: Int = 2026,
    val startMonth: Int = 10,
    val paymentMethod: String = "UPI",
    val notes: String = "",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
