package com.example.monthlyfinancetracker.ui.screens.sms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.local.entity.CommitmentEntity
import com.example.monthlyfinancetracker.data.local.entity.PaymentRecordEntity
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import com.example.monthlyfinancetracker.domain.model.CommitmentItem
import com.example.monthlyfinancetracker.domain.model.SmsMessageItem
import com.example.monthlyfinancetracker.utils.SmsReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs

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

    fun autoMatchAndPayLoanEmiMandates(onCompleted: (matchCount: Int, matchedNames: List<String>) -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            val messages = state.messages
            val commitments = currentMonthCommitments.value

            val matchedNames = mutableListOf<String>()
            var matchCount = 0

            val loanEmiCommitments = commitments.filter { item ->
                (item.category.name.contains("Loan", ignoreCase = true) || item.category.name.contains("EMI", ignoreCase = true)) &&
                        item.remainingAmount > 0
            }

            for (msg in messages) {
                val smsAmount = msg.amount ?: continue
                if (!msg.isMandateSent) continue

                val matchingCommitment = loanEmiCommitments.find { c ->
                    abs(c.totalAmount - smsAmount) < 1.0 || abs(c.remainingAmount - smsAmount) < 1.0
                }

                if (matchingCommitment != null) {
                    val record = PaymentRecordEntity(
                        commitmentId = matchingCommitment.commitment.id,
                        year = state.selectedYear,
                        month = state.selectedMonth,
                        amountPaid = smsAmount,
                        paymentDate = msg.timestamp,
                        paymentMethod = "UPI Mandate",
                        notes = "Auto-matched from SMS (UPI Mandate: Sent)"
                    )
                    repository.logPayment(record)
                    matchCount++
                    matchedNames.add("${matchingCommitment.commitment.name} (₹${smsAmount.toInt()})")
                }
            }

            onCompleted(matchCount, matchedNames)
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

    fun logPaymentForOtherSms(amount: Double, timestamp: Long, onLogged: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value

            // 1. Check if a commitment named "Other" already exists
            val currentItems = currentMonthCommitments.value
            val existingOtherItem = currentItems.find { it.commitment.name.equals("Other", ignoreCase = true) }

            val targetCommitmentId: Long

            if (existingOtherItem != null) {
                targetCommitmentId = existingOtherItem.commitment.id
                // Update commitment monthly amount to include the new payment
                val updatedAmount = existingOtherItem.amountPaid + amount
                val updatedCommitment = existingOtherItem.commitment.copy(
                    monthlyAmount = maxOf(existingOtherItem.commitment.monthlyAmount, updatedAmount)
                )
                repository.saveCommitment(updatedCommitment)
            } else {
                val categories = repository.getAllCategories().first()
                val otherCategory = categories.find { it.name.equals("Other", ignoreCase = true) } ?: categories.lastOrNull()
                val categoryId = otherCategory?.id ?: 7L

                val newCommitment = CommitmentEntity(
                    name = "Other",
                    categoryId = categoryId,
                    monthlyAmount = amount,
                    dueDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                    frequency = "EVERY_MONTH",
                    durationMonths = 0,
                    startYear = state.selectedYear,
                    startMonth = state.selectedMonth,
                    paymentMethod = "UPI / SMS",
                    notes = "Single commitment tracking all Other SMS payments"
                )

                targetCommitmentId = repository.saveCommitment(newCommitment)
            }

            val record = PaymentRecordEntity(
                commitmentId = targetCommitmentId,
                year = state.selectedYear,
                month = state.selectedMonth,
                amountPaid = amount,
                paymentDate = timestamp,
                paymentMethod = "UPI / SMS",
                notes = "Auto-logged from SMS"
            )
            repository.logPayment(record)
            onLogged()
        }
    }
}
