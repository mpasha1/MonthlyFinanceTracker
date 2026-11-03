package com.example.monthlyfinancetracker.ui.screens.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monthlyfinancetracker.ui.components.CategoryIcon
import com.example.monthlyfinancetracker.ui.components.CompactTopBar
import com.example.monthlyfinancetracker.ui.theme.DarkGreenPrimary

@Composable
fun AddEditCommitmentScreen(
    commitmentId: Long,
    viewModel: AddEditViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()
    val categories by viewModel.categories.collectAsState()

    LaunchedEffect(commitmentId) {
        if (commitmentId > 0) {
            viewModel.loadCommitment(commitmentId)
        }
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var dayDropdownExpanded by remember { mutableStateOf(false) }
    var frequencyDropdownExpanded by remember { mutableStateOf(false) }
    var durationDropdownExpanded by remember { mutableStateOf(false) }
    var paymentMethodDropdownExpanded by remember { mutableStateOf(false) }

    val paymentMethods = listOf("UPI", "Bank Transfer", "Credit Card", "Debit Card", "Cash", "Auto-Debit", "Other")
    val frequencies = listOf("Every Month", "One Time")
    val durationOptions = listOf(
        "Ongoing / Indefinite",
        "3 Months",
        "6 Months",
        "12 Months (1 Year)",
        "24 Months (2 Years)",
        "36 Months (3 Years)",
        "60 Months (5 Years)",
        "Custom Months"
    )

    val isEditing = formState.id > 0

    Scaffold(
        topBar = {
            CompactTopBar(
                title = if (isEditing) "Edit Commitment" else "Add Commitment",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Field 1: Name *
            Column {
                Text(
                    text = "Name *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { viewModel.onNameChange(it) },
                    placeholder = { Text("Credit Card") },
                    isError = formState.nameError != null,
                    supportingText = formState.nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Field 2: Category *
            Column {
                Text(
                    text = "Category *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))

                val currentCategory = formState.selectedCategory ?: categories.firstOrNull()

                Box {
                    OutlinedTextField(
                        value = currentCategory?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            currentCategory?.let {
                                CategoryIcon(
                                    iconName = it.iconName,
                                    colorHex = it.colorHex,
                                    size = 32.dp,
                                    iconSize = 18.dp
                                )
                            }
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { categoryDropdownExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryDropdownExpanded = true },
                        enabled = false,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    DropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CategoryIcon(
                                            iconName = cat.iconName,
                                            colorHex = cat.colorHex,
                                            size = 28.dp,
                                            iconSize = 16.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = cat.name, fontSize = 15.sp)
                                    }
                                },
                                onClick = {
                                    viewModel.onCategoryChange(cat)
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Field 3: Monthly Amount *
            Column {
                Text(
                    text = "Monthly Amount *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = formState.monthlyAmount,
                    onValueChange = { viewModel.onAmountChange(it) },
                    placeholder = { Text("18500") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.CurrencyRupee, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = formState.amountError != null,
                    supportingText = formState.amountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Field 4: Due Date * (Day number & Frequency)
            Column {
                Text(
                    text = "Due Date *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Day Selector (1..31)
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = "${formState.dueDay}",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { dayDropdownExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dayDropdownExpanded = true },
                            enabled = false,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        DropdownMenu(
                            expanded = dayDropdownExpanded,
                            onDismissRequest = { dayDropdownExpanded = false }
                        ) {
                            (1..31).forEach { day ->
                                DropdownMenuItem(
                                    text = { Text("Day $day", fontSize = 15.sp) },
                                    onClick = {
                                        viewModel.onDueDayChange(day)
                                        dayDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Frequency Selector
                    Box(modifier = Modifier.weight(1.2f)) {
                        OutlinedTextField(
                            value = formState.frequency,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { frequencyDropdownExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { frequencyDropdownExpanded = true },
                            enabled = false,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        DropdownMenu(
                            expanded = frequencyDropdownExpanded,
                            onDismissRequest = { frequencyDropdownExpanded = false }
                        ) {
                            frequencies.forEach { freq ->
                                DropdownMenuItem(
                                    text = { Text(freq, fontSize = 15.sp) },
                                    onClick = {
                                        viewModel.onFrequencyChange(freq)
                                        frequencyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Field 5: Duration / How Many Months? (Shown when "Every Month" selected)
            if (formState.frequency == "Every Month") {
                Column {
                    Text(
                        text = "Duration / How Many Months?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box {
                        OutlinedTextField(
                            value = formState.durationType,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { durationDropdownExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { durationDropdownExpanded = true },
                            enabled = false,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        DropdownMenu(
                            expanded = durationDropdownExpanded,
                            onDismissRequest = { durationDropdownExpanded = false }
                        ) {
                            durationOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt, fontSize = 15.sp) },
                                    onClick = {
                                        viewModel.onDurationTypeChange(opt)
                                        durationDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (formState.durationType == "Custom Months") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = formState.customDurationMonths,
                            onValueChange = { viewModel.onCustomDurationChange(it) },
                            label = { Text("Enter Number of Months") },
                            placeholder = { Text("e.g. 18") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = formState.durationError != null,
                            supportingText = formState.durationError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Field 6: Payment Method
            Column {
                Text(
                    text = "Payment Method",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    OutlinedTextField(
                        value = formState.paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { paymentMethodDropdownExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { paymentMethodDropdownExpanded = true },
                        enabled = false,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    DropdownMenu(
                        expanded = paymentMethodDropdownExpanded,
                        onDismissRequest = { paymentMethodDropdownExpanded = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method, fontSize = 15.sp) },
                                onClick = {
                                    viewModel.onPaymentMethodChange(method)
                                    paymentMethodDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Field 7: Notes (Optional)
            Column {
                Text(
                    text = "Notes (Optional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = formState.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
                    placeholder = { Text("e.g. Use HDFC credit card") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.saveCommitment(onSaved)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
            ) {
                Text(
                    text = "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
