package com.example.monthlyfinancetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_preferences")
data class ReminderPreferenceEntity(
    @PrimaryKey val commitmentId: Long,
    val isReminderEnabled: Boolean = true
)
