package com.example.monthlyfinancetracker.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monthlyfinancetracker.ui.components.CategoryIcon
import com.example.monthlyfinancetracker.ui.components.CompactTopBar
import com.example.monthlyfinancetracker.ui.components.StatusBadge
import com.example.monthlyfinancetracker.ui.theme.DarkGreenPrimary
import com.example.monthlyfinancetracker.ui.theme.MintGreenAccent
import com.example.monthlyfinancetracker.ui.theme.StatusPaidText
import com.example.monthlyfinancetracker.utils.CurrencyUtils
import com.example.monthlyfinancetracker.utils.DateUtils

@Composable
fun PaymentDetailsScreen(
    commitmentId: Long,
    year: Int,
    month: Int,
    viewModel: PaymentDetailsViewModel,
    onBack: () -> Unit,
    onEditCommitment: (commitmentId: Long) -> Unit
) {
    LaunchedEffect(commitmentId, year, month) {
        viewModel.initArgs(commitmentId, year, month)
    }

    val item by viewModel.commitmentItem.collectAsState()
    val history by viewModel.paymentHistory.collectAsState()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CompactTopBar(
                title = "Payment Details",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    item?.let { currItem ->
                        IconButton(onClick = { onEditCommitment(currItem.commitment.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Commitment",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Commitment",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            item?.let { currItem ->
                if (currItem.remainingAmount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
                        ) {
                            Text(
                                text = "Mark as Paid",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (item == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading commitment details...")
            }
        } else {
            val currItem = item!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Details Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryIcon(
                                    iconName = currItem.category.iconName,
                                    colorHex = currItem.category.colorHex,
                                    size = 48.dp,
                                    iconSize = 26.dp
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currItem.commitment.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${CurrencyUtils.formatRupees(currItem.totalAmount)} / month",
                                        fontSize = 14.sp,
                                        color = Color(0xFF667085)
                                    )
                                    Text(
                                        text = DateUtils.formatFullDueDate(currItem.commitment.dueDay, month, year),
                                        fontSize = 13.sp,
                                        color = Color(0xFF667085)
                                    )
                                }

                                StatusBadge(status = currItem.status)
                            }

                            if (currItem.commitment.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Notes: ${currItem.commitment.notes}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF55605C)
                                )
                            }
                        }
                    }
                }

                // Payment Progress Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Payment Progress",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = String.format("%.1f%%", currItem.completionPercentage),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPaidText
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { currItem.completionPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = MintGreenAccent,
                                trackColor = MintGreenAccent.copy(alpha = 0.2f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${CurrencyUtils.formatRupees(currItem.amountPaid)} paid",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF344054)
                                )
                                Text(
                                    text = "${CurrencyUtils.formatRupees(currItem.remainingAmount)} remaining",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF667085)
                                )
                            }
                        }
                    }
                }

                // Payment History Section
                item {
                    Text(
                        text = "Payment History",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (history.isEmpty()) {
                    item {
                        Text(
                            text = "No payments logged for this month yet.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(history) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = StatusPaidText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = DateUtils.formatDate(record.paymentDate),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = record.paymentMethod,
                                        fontSize = 12.sp,
                                        color = Color(0xFF667085)
                                    )
                                }

                                Text(
                                    text = CurrencyUtils.formatRupees(record.amountPaid),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusPaidText
                                )

                                IconButton(onClick = { viewModel.deletePaymentRecord(record) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete record",
                                        tint = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Log Payment Dialog
            if (showPaymentDialog) {
                var amountText by remember { mutableStateOf(currItem.remainingAmount.toInt().toString()) }
                var methodSelected by remember { mutableStateOf(currItem.commitment.paymentMethod) }
                var noteText by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showPaymentDialog = false },
                    title = { Text("Log Payment", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it },
                                label = { Text("Amount Paid (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = methodSelected,
                                onValueChange = { methodSelected = it },
                                label = { Text("Payment Method") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text("Notes (Optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    viewModel.logPayment(amt, methodSelected, noteText) {
                                        showPaymentDialog = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
                        ) {
                            Text("Confirm Payment")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPaymentDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Delete Confirmation Dialog
            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Delete Commitment?", fontWeight = FontWeight.Bold) },
                    text = { Text("Are you sure you want to delete '${currItem.commitment.name}'? This action cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteCommitment {
                                    showDeleteConfirmDialog = false
                                    onBack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
