package com.example.monthlyfinancetracker.ui.screens.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.local.entity.PaymentRecordEntity
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import com.example.monthlyfinancetracker.domain.model.CommitmentItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PaymentDetailsArgs(
    val commitmentId: Long,
    val year: Int,
    val month: Int
)

class PaymentDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val dao = AppDatabase.getDatabase(application).financeDao()
        repository = FinanceRepository(dao)
    }

    private val _args = MutableStateFlow<PaymentDetailsArgs?>(null)

    fun initArgs(commitmentId: Long, year: Int, month: Int) {
        _args.value = PaymentDetailsArgs(commitmentId, year, month)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val commitmentItem: StateFlow<CommitmentItem?> = _args.flatMapLatest { args ->
        if (args == null) flowOf(null)
        else repository.getCommitmentItemsForMonth(args.year, args.month).combine(flowOf(args)) { items, a ->
            items.find { it.commitment.id == a.commitmentId }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val paymentHistory: StateFlow<List<PaymentRecordEntity>> = _args.flatMapLatest { args ->
        if (args == null) flowOf(emptyList())
        else repository.getPaymentsForCommitmentAndMonth(args.commitmentId, args.year, args.month)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun logPayment(amount: Double, paymentMethod: String, notes: String, onLogged: () -> Unit) {
        val args = _args.value ?: return
        viewModelScope.launch {
            val record = PaymentRecordEntity(
                commitmentId = args.commitmentId,
                year = args.year,
                month = args.month,
                amountPaid = amount,
                paymentDate = System.currentTimeMillis(),
                paymentMethod = paymentMethod,
                notes = notes
            )
            repository.logPayment(record)
            onLogged()
        }
    }

    fun deletePaymentRecord(record: PaymentRecordEntity) {
        viewModelScope.launch {
            repository.deletePaymentRecord(record)
        }
    }

    fun deleteCommitment(onDeleted: () -> Unit) {
        val item = commitmentItem.value ?: return
        viewModelScope.launch {
            repository.deleteCommitment(item.commitment)
            onDeleted()
        }
    }
}
