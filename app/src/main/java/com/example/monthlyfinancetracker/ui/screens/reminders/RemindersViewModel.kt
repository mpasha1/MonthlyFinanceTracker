package com.example.monthlyfinancetracker.ui.screens.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import com.example.monthlyfinancetracker.domain.model.CommitmentItem
import com.example.monthlyfinancetracker.notification.ReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val reminderManager: ReminderManager

    init {
        val dao = AppDatabase.getDatabase(application).financeDao()
        repository = FinanceRepository(dao)
        reminderManager = ReminderManager(application)
    }

    private val _isGlobalNotificationsEnabled = MutableStateFlow(true)
    val isGlobalNotificationsEnabled: StateFlow<Boolean> = _isGlobalNotificationsEnabled.asStateFlow()

    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    val upcomingCommitments: StateFlow<List<CommitmentItem>> = repository.getCommitmentItemsForMonth(2026, 10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleGlobalNotifications(enabled: Boolean) {
        _isGlobalNotificationsEnabled.value = enabled
        if (!enabled) {
            upcomingCommitments.value.forEach {
                reminderManager.cancelReminder(it.commitment.id)
            }
        } else {
            reminderManager.scheduleRemindersForMonth(upcomingCommitments.value)
        }
    }

    fun toggleCommitmentReminder(commitmentId: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(commitmentId, enabled)
            if (enabled && _isGlobalNotificationsEnabled.value) {
                upcomingCommitments.value.find { it.commitment.id == commitmentId }?.let { item ->
                    reminderManager.scheduleForCommitment(item)
                }
            } else {
                reminderManager.cancelReminder(commitmentId)
            }
        }
    }
}
