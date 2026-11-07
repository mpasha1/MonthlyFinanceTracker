package com.example.monthlyfinancetracker.ui.screens.sms

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.monthlyfinancetracker.domain.model.CommitmentItem
import com.example.monthlyfinancetracker.domain.model.PaymentStatus
import com.example.monthlyfinancetracker.domain.model.SmsMessageItem
import com.example.monthlyfinancetracker.ui.components.CategoryIcon
import com.example.monthlyfinancetracker.ui.components.CompactTopBar
import com.example.monthlyfinancetracker.ui.theme.DarkGreenPrimary
import com.example.monthlyfinancetracker.ui.theme.StatusPaidText
import com.example.monthlyfinancetracker.utils.CurrencyUtils
import com.example.monthlyfinancetracker.utils.DateUtils
import kotlin.math.abs

@Composable
fun SmsTrackerScreen(
    viewModel: SmsViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val commitments by viewModel.currentMonthCommitments.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setPermissionGranted(isGranted)
    }

    LaunchedEffect(Unit) {
        val check = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
        val isGranted = check == PackageManager.PERMISSION_GRANTED
        viewModel.setPermissionGranted(isGranted)
    }

    var selectedSmsForLog by remember { mutableStateOf<SmsMessageItem?>(null) }
    var autoMatchResultDialogText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CompactTopBar(
                title = "UPI Mandates (Current Month)",
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Info & Auto-Match Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = DarkGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Reading SMS messages for ${DateUtils.formatMonthYear(uiState.selectedYear, uiState.selectedMonth)} filtered by text 'UPI Mandate: Sent' or 'Sent Rs.'.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DarkGreenPrimary,
                            lineHeight = 16.sp
                        )
                    }

                    if (uiState.hasPermission && uiState.messages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.autoMatchAndPayLoanEmiMandates { count, names ->
                                    if (count > 0) {
                                        autoMatchResultDialogText = "Successfully auto-matched and updated $count Loan EMI commitment(s) to PAID:\n\n• " + names.joinToString("\n• ")
                                    } else {
                                        Toast.makeText(context, "No matching unpaid Loan EMI commitments found for current SMS amounts.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Auto-Match & Mark Loan EMIs as Paid", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (!uiState.hasPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            tint = DarkGreenPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "SMS Permission Required",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Grant SMS permission to read UPI Mandate messages for the current month.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.READ_SMS) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else if (uiState.messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No 'UPI Mandate: Sent' messages found for ${DateUtils.formatMonthYear(uiState.selectedYear, uiState.selectedMonth)}.",
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.messages) { msg ->
                        val isAlreadyLogged = msg.amount != null && commitments.any { item ->
                            item.paymentRecords.any { record -> abs(record.amountPaid - msg.amount) < 1.0 } ||
                                    (item.status == PaymentStatus.PAID && abs(item.totalAmount - msg.amount) < 1.0)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE3F2FD)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Sms,
                                                contentDescription = null,
                                                tint = Color(0xFF1565C0),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = msg.sender,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Text(
                                        text = DateUtils.formatDate(msg.timestamp),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = msg.body,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (msg.amount != null) {
                                        Text(
                                            text = "Amount: ${CurrencyUtils.formatRupees(msg.amount)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusPaidText
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    if (isAlreadyLogged) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFE8F5E9))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = StatusPaidText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Already Logged",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = StatusPaidText
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = { selectedSmsForLog = msg },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Log Payment", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Log Payment from SMS Dialog
            if (selectedSmsForLog != null) {
                val sms = selectedSmsForLog!!
                var selectedCommitment by remember { mutableStateOf<CommitmentItem?>(commitments.firstOrNull()) }
                var isOtherSelected by remember { mutableStateOf(false) }
                var commitmentDropdownExpanded by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { selectedSmsForLog = null },
                    title = { Text("Log Payment from SMS", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Amount: ${CurrencyUtils.formatRupees(sms.amount ?: 0.0)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = StatusPaidText
                            )

                            Text(
                                text = "Select Commitment:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Box {
                                val displayText = if (isOtherSelected) "Other" else (selectedCommitment?.commitment?.name ?: "Select Commitment")

                                OutlinedTextField(
                                    value = displayText,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            modifier = Modifier.clickable { commitmentDropdownExpanded = true }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { commitmentDropdownExpanded = true },
                                    enabled = false,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )

                                DropdownMenu(
                                    expanded = commitmentDropdownExpanded,
                                    onDismissRequest = { commitmentDropdownExpanded = false }
                                ) {
                                    commitments.filter { !it.commitment.name.equals("Other", ignoreCase = true) }.forEach { item ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    CategoryIcon(
                                                        iconName = item.category.iconName,
                                                        colorHex = item.category.colorHex,
                                                        size = 28.dp,
                                                        iconSize = 16.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${item.commitment.name} (${CurrencyUtils.formatRupees(item.totalAmount)})",
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedCommitment = item
                                                isOtherSelected = false
                                                commitmentDropdownExpanded = false
                                            }
                                        )
                                    }

                                    // "Other" Option (no amount label)
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CategoryIcon(
                                                    iconName = "more_horiz",
                                                    colorHex = "#7B1FA2",
                                                    size = 28.dp,
                                                    iconSize = 16.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Other",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DarkGreenPrimary
                                                )
                                            }
                                        },
                                        onClick = {
                                            isOtherSelected = true
                                            selectedCommitment = null
                                            commitmentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (isOtherSelected) {
                                    viewModel.logPaymentForOtherSms(
                                        amount = sms.amount ?: 0.0,
                                        timestamp = sms.timestamp
                                    ) {
                                        selectedSmsForLog = null
                                    }
                                } else {
                                    selectedCommitment?.let { target ->
                                        viewModel.logPaymentFromSms(
                                            commitmentId = target.commitment.id,
                                            amount = sms.amount ?: target.totalAmount,
                                            timestamp = sms.timestamp
                                        ) {
                                            selectedSmsForLog = null
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary)
                        ) {
                            Text("Log Payment")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedSmsForLog = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Auto-match Result Dialog
            if (autoMatchResultDialogText != null) {
                AlertDialog(
                    onDismissRequest = { autoMatchResultDialogText = null },
                    title = { Text("Auto-Match Success", fontWeight = FontWeight.Bold) },
                    text = { Text(autoMatchResultDialogText!!) },
                    confirmButton = {
                        TextButton(onClick = { autoMatchResultDialogText = null }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}
