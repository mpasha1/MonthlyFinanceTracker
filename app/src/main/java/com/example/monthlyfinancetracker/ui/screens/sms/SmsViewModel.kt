package com.example.monthlyfinancetracker.ui.screens.sms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.local.entity.PaymentRecordEntity
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import com.example.monthlyfinancetracker.domain.model.CommitmentItem
import com.example.monthlyfinancetracker.domain.model.SmsMessageItem
import com.example.monthlyfinancetracker.utils.SmsReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class SmsUiState(
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val hasPermission: Boolean = false,
    val messages: List<SmsMessageItem> = emptyList(),
    val isLoading: Boolean = false
)

class SmsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val dao = AppDatabase.getDatabase(application).financeDao()
        repository = FinanceRepository(dao)
    }

    private val _uiState = MutableStateFlow(SmsUiState())
    val uiState: StateFlow<SmsUiState> = _uiState.asStateFlow()

    val currentMonthCommitments: StateFlow<List<CommitmentItem>> = repository.getCommitmentItemsForMonth(
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH) + 1
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setPermissionGranted(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasPermission = granted)
        if (granted) {
            loadCurrentMonthMandateSms()
        }
    }

    fun loadCurrentMonthMandateSms() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val state = _uiState.value
            val list = SmsReader.readUpiMandateSmsForCurrentMonth(
                getApplication(),
                state.selectedYear,
                state.selectedMonth
            )
            _uiState.value = _uiState.value.copy(messages = list, isLoading = false)
        }
    }

    fun logPaymentFromSms(commitmentId: Long, amount: Double, timestamp: Long, onLogged: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val record = PaymentRecordEntity(
                commitmentId = commitmentId,
                year = state.selectedYear,
                month = state.selectedMonth,
                amountPaid = amount,
                paymentDate = timestamp,
                paymentMethod = "UPI Mandate",
                notes = "Auto-logged from SMS (UPI Mandate: Sent)"
            )
            repository.logPayment(record)
            onLogged()
        }
    }
}
