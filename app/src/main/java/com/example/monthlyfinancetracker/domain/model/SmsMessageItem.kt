package com.example.monthlyfinancetracker.domain.model

data class SmsMessageItem(
    val id: Long,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val amount: Double?,
    val merchant: String?,
    val isMandateSent: Boolean
)
