package com.example.monthlyfinancetracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.monthlyfinancetracker.domain.model.CommitmentItem
import com.example.monthlyfinancetracker.utils.CurrencyUtils
import java.util.Calendar

class ReminderManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleRemindersForMonth(commitments: List<CommitmentItem>) {
        commitments.forEach { item ->
            if (item.isReminderEnabled && item.remainingAmount > 0) {
                scheduleForCommitment(item)
            } else {
                cancelReminder(item.commitment.id)
            }
        }
    }

    fun scheduleForCommitment(item: CommitmentItem) {
        val dueDay = item.commitment.dueDay
        val amountStr = CurrencyUtils.formatRupees(item.totalAmount)

        // Schedule notification for 1 day before due date at 9:00 AM
        val calDayBefore = Calendar.getInstance().apply {
            set(Calendar.YEAR, item.year)
            set(Calendar.MONTH, item.month - 1)
            set(Calendar.DAY_OF_MONTH, dueDay)
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (calDayBefore.timeInMillis > System.currentTimeMillis()) {
            scheduleAlarm(
                requestCode = (item.commitment.id * 100 + 1).toInt(),
                triggerAtMillis = calDayBefore.timeInMillis,
                title = "Payment Due Tomorrow!",
                message = "${item.commitment.name} ($amountStr) is due tomorrow.",
                commitmentId = item.commitment.id
            )
        }

        // Schedule notification on due date at 9:00 AM
        val calDueDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, item.year)
            set(Calendar.MONTH, item.month - 1)
            set(Calendar.DAY_OF_MONTH, dueDay)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        if (calDueDate.timeInMillis > System.currentTimeMillis()) {
            scheduleAlarm(
                requestCode = (item.commitment.id * 100 + 2).toInt(),
                triggerAtMillis = calDueDate.timeInMillis,
                title = "Payment Due Today!",
                message = "${item.commitment.name} ($amountStr) is due today.",
                commitmentId = item.commitment.id
            )
        }
    }

    private fun scheduleAlarm(requestCode: Int, triggerAtMillis: Long, title: String, message: String, commitmentId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_MESSAGE", message)
            putExtra("EXTRA_COMMITMENT_ID", commitmentId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelReminder(commitmentId: Long) {
        listOf(1, 2).forEach { subCode ->
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                (commitmentId * 100 + subCode).toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }
}
