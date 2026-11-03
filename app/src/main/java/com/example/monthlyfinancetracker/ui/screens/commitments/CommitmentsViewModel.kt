package com.example.monthlyfinancetracker.ui.screens.commitments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import com.example.monthlyfinancetracker.domain.model.CommitmentItem
import com.example.monthlyfinancetracker.domain.model.PaymentStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class CommitmentFilter {
    ALL,
    PAID,
    PARTIAL,
    PENDING
}

data class CommitmentsUiState(
    val selectedYear: Int = 2026,
    val selectedMonth: Int = 10,
    val activeFilter: CommitmentFilter = CommitmentFilter.ALL,
    val searchQuery: String = "",
    val isSearchOpen: Boolean = false
)

class CommitmentsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val dao = AppDatabase.getDatabase(application).financeDao()
        repository = FinanceRepository(dao)
    }

    private val _uiState = MutableStateFlow(
        CommitmentsUiState(
            selectedYear = Calendar.getInstance().get(Calendar.YEAR),
            selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        )
    )
    val uiState: StateFlow<CommitmentsUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredCommitments: StateFlow<List<CommitmentItem>> = combine(
        _uiState.flatMapLatest { state ->
            repository.getCommitmentItemsForMonth(state.selectedYear, state.selectedMonth)
        },
        _uiState
    ) { items, state ->
        items.filter { item ->
            val matchesFilter = when (state.activeFilter) {
                CommitmentFilter.ALL -> true
                CommitmentFilter.PAID -> item.status == PaymentStatus.PAID
                CommitmentFilter.PARTIAL -> item.status == PaymentStatus.PARTIAL
                CommitmentFilter.PENDING -> item.status == PaymentStatus.PENDING
            }

            val query = state.searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                    item.commitment.name.lowercase().contains(query) ||
                    item.category.name.lowercase().contains(query) ||
                    item.commitment.notes.lowercase().contains(query)

            matchesFilter && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
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

    fun setFilter(filter: CommitmentFilter) {
        _uiState.value = _uiState.value.copy(activeFilter = filter)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleSearch() {
        val current = _uiState.value
        _uiState.value = current.copy(
            isSearchOpen = !current.isSearchOpen,
            searchQuery = if (current.isSearchOpen) "" else current.searchQuery
        )
    }
}
