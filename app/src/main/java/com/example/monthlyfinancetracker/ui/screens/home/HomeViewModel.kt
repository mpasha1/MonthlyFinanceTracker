package com.example.monthlyfinancetracker.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import com.example.monthlyfinancetracker.domain.model.CommitmentItem
import com.example.monthlyfinancetracker.domain.model.MonthSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class HomeUiState(
    val selectedYear: Int = 2026,
    val selectedMonth: Int = 10 // Defaulting to October 2026 as per mockup
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val dao = AppDatabase.getDatabase(application).financeDao()
        repository = FinanceRepository(dao)
    }

    private val _uiState = MutableStateFlow(
        HomeUiState(
            selectedYear = Calendar.getInstance().get(Calendar.YEAR),
            selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val commitments: StateFlow<List<CommitmentItem>> = _uiState.flatMapLatest { state ->
        repository.getCommitmentItemsForMonth(state.selectedYear, state.selectedMonth)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthSummary: StateFlow<MonthSummary> = _uiState.flatMapLatest { state ->
        repository.getMonthSummary(state.selectedYear, state.selectedMonth)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthSummary(_uiState.value.selectedYear, _uiState.value.selectedMonth, 0.0, 0.0)
    )

    fun onPreviousMonth() {
        val current = _uiState.value
        if (current.selectedMonth == 1) {
            _uiState.value = current.copy(selectedYear = current.selectedYear - 1, selectedMonth = 12)
        } else {
            _uiState.value = current.copy(selectedMonth = current.selectedMonth - 1)
        }
    }

    fun onNextMonth() {
        val current = _uiState.value
        if (current.selectedMonth == 12) {
            _uiState.value = current.copy(selectedYear = current.selectedYear + 1, selectedMonth = 1)
        } else {
            _uiState.value = current.copy(selectedMonth = current.selectedMonth + 1)
        }
    }

    fun setMonth(year: Int, month: Int) {
        _uiState.value = _uiState.value.copy(selectedYear = year, selectedMonth = month)
    }
}
