package com.example.monthlyfinancetracker.ui.screens.pastmonths

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monthlyfinancetracker.ui.components.CompactTopBar
import com.example.monthlyfinancetracker.ui.theme.StatusPaidText
import com.example.monthlyfinancetracker.utils.CurrencyUtils
import com.example.monthlyfinancetracker.utils.DateUtils

@Composable
fun PastMonthsScreen(
    viewModel: PastMonthsViewModel,
    onBack: () -> Unit,
    onSelectMonth: (year: Int, month: Int) -> Unit
) {
    val pastSummaries by viewModel.pastMonthsSummaries.collectAsState()

    Scaffold(
        topBar = {
            CompactTopBar(
                title = "Past Months",
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
        ) {
            // Header table titles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Month", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF667085), modifier = Modifier.weight(1.2f))
                Text(text = "Total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF667085), modifier = Modifier.weight(1f))
                Text(text = "Paid", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF667085), modifier = Modifier.weight(1f))
                Text(text = "Balance", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF667085), modifier = Modifier.weight(1f))
                Text(text = "%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF667085), modifier = Modifier.weight(0.8f))
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pastSummaries) { summary ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMonth(summary.year, summary.month) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateUtils.formatMonthYear(summary.year, summary.month),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1.2f)
                            )

                            Text(
                                text = CurrencyUtils.formatRupees(summary.totalCommitment, false),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = CurrencyUtils.formatRupees(summary.totalPaid, false),
                                fontSize = 12.sp,
                                color = StatusPaidText,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = CurrencyUtils.formatRupees(summary.balance, false),
                                fontSize = 12.sp,
                                color = Color(0xFF667085),
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = String.format("%.0f%%", summary.paidPercentage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusPaidText,
                                modifier = Modifier.weight(0.8f)
                            )

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
