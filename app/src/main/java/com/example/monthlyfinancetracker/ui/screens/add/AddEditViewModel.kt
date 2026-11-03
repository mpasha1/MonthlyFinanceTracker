package com.example.monthlyfinancetracker.ui.screens.add

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.local.entity.CategoryEntity
import com.example.monthlyfinancetracker.data.local.entity.CommitmentEntity
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class AddEditFormState(
    val id: Long = 0L,
    val name: String = "",
    val selectedCategory: CategoryEntity? = null,
    val monthlyAmount: String = "",
    val dueDay: Int = 1,
    val frequency: String = "Every Month",
    val durationType: String = "Ongoing / Indefinite",
    val customDurationMonths: String = "",
    val startYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val startMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val paymentMethod: String = "UPI",
    val notes: String = "",
    val nameError: String? = null,
    val amountError: String? = null,
    val durationError: String? = null
)

class AddEditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val dao = AppDatabase.getDatabase(application).financeDao()
        repository = FinanceRepository(dao)
    }

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _formState = MutableStateFlow(AddEditFormState())
    val formState: StateFlow<AddEditFormState> = _formState.asStateFlow()

    fun loadCommitment(commitmentId: Long) {
        if (commitmentId <= 0L) return
        viewModelScope.launch {
            val cwc = repository.getCommitmentById(commitmentId)
            if (cwc != null) {
                val c = cwc.commitment
                val (durType, customStr) = when (c.durationMonths) {
                    0 -> Pair("Ongoing / Indefinite", "")
                    3 -> Pair("3 Months", "")
                    6 -> Pair("6 Months", "")
                    12 -> Pair("12 Months (1 Year)", "")
                    24 -> Pair("24 Months (2 Years)", "")
                    36 -> Pair("36 Months (3 Years)", "")
                    60 -> Pair("60 Months (5 Years)", "")
                    else -> Pair("Custom Months", c.durationMonths.toString())
                }

                _formState.value = AddEditFormState(
                    id = c.id,
                    name = c.name,
                    selectedCategory = cwc.category,
                    monthlyAmount = if (c.monthlyAmount % 1.0 == 0.0) c.monthlyAmount.toLong().toString() else c.monthlyAmount.toString(),
                    dueDay = c.dueDay,
                    frequency = if (c.frequency == "EVERY_MONTH") "Every Month" else "One Time",
                    durationType = durType,
                    customDurationMonths = customStr,
                    startYear = c.startYear,
                    startMonth = c.startMonth,
                    paymentMethod = c.paymentMethod,
                    notes = c.notes
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _formState.value = _formState.value.copy(name = name, nameError = null)
    }

    fun onCategoryChange(category: CategoryEntity) {
        _formState.value = _formState.value.copy(selectedCategory = category)
    }

    fun onAmountChange(amount: String) {
        _formState.value = _formState.value.copy(monthlyAmount = amount, amountError = null)
    }

    fun onDueDayChange(day: Int) {
        _formState.value = _formState.value.copy(dueDay = day)
    }

    fun onFrequencyChange(freq: String) {
        _formState.value = _formState.value.copy(frequency = freq)
    }

    fun onDurationTypeChange(type: String) {
        _formState.value = _formState.value.copy(durationType = type, durationError = null)
    }

    fun onCustomDurationChange(custom: String) {
        _formState.value = _formState.value.copy(customDurationMonths = custom, durationError = null)
    }

    fun onStartMonthYearChange(year: Int, month: Int) {
        _formState.value = _formState.value.copy(startYear = year, startMonth = month)
    }

    fun onPaymentMethodChange(method: String) {
        _formState.value = _formState.value.copy(paymentMethod = method)
    }

    fun onNotesChange(notes: String) {
        _formState.value = _formState.value.copy(notes = notes)
    }

    fun saveCommitment(onSaved: () -> Unit) {
        val state = _formState.value
        val nameTrimmed = state.name.trim()
        val amountParsed = state.monthlyAmount.toDoubleOrNull()

        var hasError = false
        if (nameTrimmed.isEmpty()) {
            _formState.value = _formState.value.copy(nameError = "Name is required")
            hasError = true
        }

        if (amountParsed == null || amountParsed <= 0) {
            _formState.value = _formState.value.copy(amountError = "Enter a valid amount")
            hasError = true
        }

        var resolvedDurationMonths = 0
        if (state.frequency == "Every Month") {
            resolvedDurationMonths = when (state.durationType) {
                "Ongoing / Indefinite" -> 0
                "3 Months" -> 3
                "6 Months" -> 6
                "12 Months (1 Year)" -> 12
                "24 Months (2 Years)" -> 24
                "36 Months (3 Years)" -> 36
                "60 Months (5 Years)" -> 60
                "Custom Months" -> {
                    val parsed = state.customDurationMonths.toIntOrNull()
                    if (parsed == null || parsed <= 0) {
                        _formState.value = _formState.value.copy(durationError = "Enter a valid number of months")
                        hasError = true
                        0
                    } else parsed
                }
                else -> 0
            }
        } else {
            resolvedDurationMonths = 1
        }

        val categoryId = state.selectedCategory?.id ?: categories.value.firstOrNull()?.id ?: 1L

        if (!hasError) {
            viewModelScope.launch {
                val entity = CommitmentEntity(
                    id = state.id,
                    name = nameTrimmed,
                    categoryId = categoryId,
                    monthlyAmount = amountParsed!!,
                    dueDay = state.dueDay,
                    frequency = if (state.frequency == "Every Month") "EVERY_MONTH" else "ONE_TIME",
                    durationMonths = resolvedDurationMonths,
                    startYear = state.startYear,
                    startMonth = state.startMonth,
                    paymentMethod = state.paymentMethod,
                    notes = state.notes
                )
                repository.saveCommitment(entity)
                onSaved()
            }
        }
    }
}
