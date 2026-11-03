package com.example.monthlyfinancetracker.ui.screens.reports

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monthlyfinancetracker.ui.components.CompactTopBar
import com.example.monthlyfinancetracker.ui.components.DonutChart
import com.example.monthlyfinancetracker.ui.components.MonthPicker
import com.example.monthlyfinancetracker.ui.theme.DarkGreenPrimary
import com.example.monthlyfinancetracker.ui.theme.StatusPaidText
import com.example.monthlyfinancetracker.ui.theme.StatusPendingText
import com.example.monthlyfinancetracker.utils.CurrencyUtils

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary by viewModel.monthSummary.collectAsState()
    val breakdowns by viewModel.categoryBreakdowns.collectAsState()

    Scaffold(
        topBar = {
            CompactTopBar(
                title = "Reports"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Segmented Button Bar (Monthly Summary | Category Wise)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE0E0E0))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (uiState.activeTab == ReportTab.MONTHLY_SUMMARY) DarkGreenPrimary else Color.Transparent)
                            .clickable { viewModel.setTab(ReportTab.MONTHLY_SUMMARY) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Monthly Summary",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.activeTab == ReportTab.MONTHLY_SUMMARY) Color.White else Color(0xFF424242)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (uiState.activeTab == ReportTab.CATEGORY_WISE) DarkGreenPrimary else Color.Transparent)
                            .clickable { viewModel.setTab(ReportTab.CATEGORY_WISE) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Category Wise",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (uiState.activeTab == ReportTab.CATEGORY_WISE) Color.White else Color(0xFF424242)
                        )
                    }
                }
            }

            MonthPicker(
                year = uiState.selectedYear,
                month = uiState.selectedMonth,
                onPreviousMonth = { viewModel.onPreviousMonth() },
                onNextMonth = { viewModel.onNextMonth() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.activeTab == ReportTab.CATEGORY_WISE) {
                // Category Wise View
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    DonutChart(
                        breakdowns = breakdowns,
                        totalAmount = summary.totalCommitment
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        breakdowns.forEach { b ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(b.category.colorHex))
                            } catch (e: Exception) {
                                DarkGreenPrimary
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = b.category.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = String.format("%.1f%%", b.percentage),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            } else {
                // Monthly Summary View
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Monthly Breakdown",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            SummaryRow(
                                label = "Total Commitment",
                                value = CurrencyUtils.formatRupees(summary.totalCommitment),
                                isBold = true
                            )

                            SummaryRow(
                                label = "Already Paid",
                                value = CurrencyUtils.formatRupees(summary.totalPaid),
                                valueColor = StatusPaidText
                            )

                            SummaryRow(
                                label = "Balance to Pay",
                                value = CurrencyUtils.formatRupees(summary.balance),
                                valueColor = StatusPendingText
                            )

                            SummaryRow(
                                label = "Paid Percentage",
                                value = String.format("%.1f%%", summary.paidPercentage),
                                valueColor = StatusPaidText,
                                isBold = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF667085),
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}
