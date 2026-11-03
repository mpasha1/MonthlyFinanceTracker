package com.example.monthlyfinancetracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_records",
    foreignKeys = [
        ForeignKey(
            entity = CommitmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["commitmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["commitmentId"]), Index(value = ["year", "month"])]
)
data class PaymentRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commitmentId: Long,
    val year: Int,
    val month: Int, // 1..12
    val amountPaid: Double,
    val paymentDate: Long, // Epoch timestamp in ms
    val paymentMethod: String,
    val notes: String = ""
)
