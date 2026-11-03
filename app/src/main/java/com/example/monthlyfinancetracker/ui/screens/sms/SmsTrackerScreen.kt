package com.example.monthlyfinancetracker.ui.screens.sms

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
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
import com.example.monthlyfinancetracker.domain.model.SmsMessageItem
import com.example.monthlyfinancetracker.ui.components.CompactTopBar
import com.example.monthlyfinancetracker.ui.theme.DarkGreenPrimary
import com.example.monthlyfinancetracker.ui.theme.StatusPaidText
import com.example.monthlyfinancetracker.utils.CurrencyUtils
import com.example.monthlyfinancetracker.utils.DateUtils

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
            // Info Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
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
                        text = "Reading SMS messages from Current Month (${DateUtils.formatMonthYear(uiState.selectedYear, uiState.selectedMonth)}) filtered by text 'UPI Mandate: Sent'.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkGreenPrimary,
                        lineHeight = 16.sp
                    )
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

            // Log Payment from SMS Dialog
            if (selectedSmsForLog != null) {
                val sms = selectedSmsForLog!!
                var selectedCommitment by remember { mutableStateOf<CommitmentItem?>(commitments.firstOrNull()) }
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
                                OutlinedTextField(
                                    value = selectedCommitment?.commitment?.name ?: "Select Commitment",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = false,
                                    shape = RoundedCornerShape(10.dp)
                                )

                                DropdownMenu(
                                    expanded = commitmentDropdownExpanded,
                                    onDismissRequest = { commitmentDropdownExpanded = false }
                                ) {
                                    commitments.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item.commitment.name) },
                                            onClick = {
                                                selectedCommitment = item
                                                commitmentDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                selectedCommitment?.let { target ->
                                    viewModel.logPaymentFromSms(
                                        commitmentId = target.commitment.id,
                                        amount = sms.amount ?: target.totalAmount,
                                        timestamp = sms.timestamp
                                    ) {
                                        selectedSmsForLog = null
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
        }
    }
}
