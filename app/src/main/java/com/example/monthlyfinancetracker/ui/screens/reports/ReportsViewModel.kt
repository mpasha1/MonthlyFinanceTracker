package com.example.monthlyfinancetracker.ui.screens.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import com.example.monthlyfinancetracker.domain.model.CategoryBreakdown
import com.example.monthlyfinancetracker.domain.model.MonthSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class ReportTab {
    MONTHLY_SUMMARY,
    CATEGORY_WISE
}

data class ReportsUiState(
    val selectedYear: Int = 2026,
    val selectedMonth: Int = 10,
    val activeTab: ReportTab = ReportTab.MONTHLY_SUMMARY
)

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val dao = AppDatabase.getDatabase(application).financeDao()
        repository = FinanceRepository(dao)
    }

    private val _uiState = MutableStateFlow(
        ReportsUiState(
            selectedYear = Calendar.getInstance().get(Calendar.YEAR),
            selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        )
    )
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthSummary: StateFlow<MonthSummary> = _uiState.flatMapLatest { state ->
        repository.getMonthSummary(state.selectedYear, state.selectedMonth)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthSummary(_uiState.value.selectedYear, _uiState.value.selectedMonth, 0.0, 0.0)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val categoryBreakdowns: StateFlow<List<CategoryBreakdown>> = _uiState.flatMapLatest { state ->
        repository.getCategoryBreakdownsForMonth(state.selectedYear, state.selectedMonth)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onPreviousMonth() {
        val current = _uiState.value
        val (newYear, newMonth) = if (current.selectedMonth == 1) Pair(current.selectedYear - 1, 12) else Pair(current.selectedYear, current.selectedMonth - 1)
        _uiState.value = current.copy(selectedYear = newYear, selectedMonth = newMonth)
    }

    fun onNextMonth() {
        val current = _uiState.value
        val (newYear, newMonth) = if (current.selectedMonth == 12) Pair(current.selectedYear + 1, 1) else Pair(current.selectedYear, current.selectedMonth + 1)
        _uiState.value = current.copy(selectedYear = newYear, selectedMonth = newMonth)
    }

    fun setTab(tab: ReportTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }
}
