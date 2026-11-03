package com.example.monthlyfinancetracker.ui.screens.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import com.example.monthlyfinancetracker.domain.model.CommitmentItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class CalendarUiState(
    val selectedYear: Int = 2026,
    val selectedMonth: Int = 10,
    val selectedDay: Int? = null // null means showing all for month
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val dao = AppDatabase.getDatabase(application).financeDao()
        repository = FinanceRepository(dao)
    }

    private val _uiState = MutableStateFlow(
        CalendarUiState(
            selectedYear = Calendar.getInstance().get(Calendar.YEAR),
            selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        )
    )
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthCommitments: StateFlow<List<CommitmentItem>> = _uiState.flatMapLatest { state ->
        repository.getCommitmentItemsForMonth(state.selectedYear, state.selectedMonth)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val selectedDayCommitments: StateFlow<List<CommitmentItem>> = combine(
        monthCommitments,
        _uiState
    ) { items, state ->
        if (state.selectedDay == null) {
            items
        } else {
            items.filter { it.commitment.dueDay == state.selectedDay }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onPreviousMonth() {
        val current = _uiState.value
        val (newYear, newMonth) = if (current.selectedMonth == 1) Pair(current.selectedYear - 1, 12) else Pair(current.selectedYear, current.selectedMonth - 1)
        _uiState.value = current.copy(selectedYear = newYear, selectedMonth = newMonth, selectedDay = null)
    }

    fun onNextMonth() {
        val current = _uiState.value
        val (newYear, newMonth) = if (current.selectedMonth == 12) Pair(current.selectedYear + 1, 1) else Pair(current.selectedYear, current.selectedMonth + 1)
        _uiState.value = current.copy(selectedYear = newYear, selectedMonth = newMonth, selectedDay = null)
    }

    fun selectDay(day: Int) {
        val current = _uiState.value
        if (current.selectedDay == day) {
            _uiState.value = current.copy(selectedDay = null) // toggle clear
        } else {
            _uiState.value = current.copy(selectedDay = day)
        }
    }
}
