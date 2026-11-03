package com.example.monthlyfinancetracker.ui.screens.pastmonths

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import com.example.monthlyfinancetracker.domain.model.MonthSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class PastMonthsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val dao = AppDatabase.getDatabase(application).financeDao()
        repository = FinanceRepository(dao)
    }

    // Returns historical summaries for past 12 months starting from current month
    val pastMonthsSummaries: StateFlow<List<MonthSummary>> = flow {
        val monthsList = mutableListOf<Pair<Int, Int>>()
        var y = 2026
        var m = 9 // Sep 2026
        for (i in 0 until 12) {
            monthsList.add(Pair(y, m))
            m--
            if (m < 1) {
                m = 12
                y--
            }
        }

        val flows = monthsList.map { (yr, mn) -> repository.getMonthSummary(yr, mn) }
        combine(flows) { summaries ->
            summaries.toList()
        }.collect { emit(it) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
