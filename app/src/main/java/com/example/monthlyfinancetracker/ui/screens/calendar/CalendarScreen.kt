package com.example.monthlyfinancetracker.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monthlyfinancetracker.domain.model.PaymentStatus
import com.example.monthlyfinancetracker.ui.components.CommitmentItemCard
import com.example.monthlyfinancetracker.ui.components.CompactTopBar
import com.example.monthlyfinancetracker.ui.components.MonthPicker
import com.example.monthlyfinancetracker.ui.theme.DarkGreenPrimary
import com.example.monthlyfinancetracker.ui.theme.StatusPaidBg
import com.example.monthlyfinancetracker.ui.theme.StatusPaidText
import com.example.monthlyfinancetracker.ui.theme.StatusPendingBg
import com.example.monthlyfinancetracker.ui.theme.StatusPendingText
import com.example.monthlyfinancetracker.utils.DateUtils

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onCommitmentClick: (commitmentId: Long, year: Int, month: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val monthCommitments by viewModel.monthCommitments.collectAsState()
    val selectedDayCommitments by viewModel.selectedDayCommitments.collectAsState()

    val totalDays = DateUtils.getDaysInMonth(uiState.selectedYear, uiState.selectedMonth)
    val firstDayOffset = DateUtils.getFirstDayOfWeek(uiState.selectedYear, uiState.selectedMonth) - 1

    val dueDayMap = monthCommitments.groupBy { it.commitment.dueDay }
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Scaffold(
        topBar = {
            CompactTopBar(
                title = "Due Date Calendar"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MonthPicker(
                year = uiState.selectedYear,
                month = uiState.selectedMonth,
                onPreviousMonth = { viewModel.onPreviousMonth() },
                onNextMonth = { viewModel.onNextMonth() }
            )

            // Days of week header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667085),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Calendar Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                userScrollEnabled = false
            ) {
                items(firstDayOffset) {
                    Box(modifier = Modifier.aspectRatio(1f))
                }

                items(totalDays) { index ->
                    val dayNum = index + 1
                    val commitmentsForDay = dueDayMap[dayNum] ?: emptyList()
                    val isSelected = uiState.selectedDay == dayNum

                    val hasPending = commitmentsForDay.any { it.status == PaymentStatus.PENDING || it.status == PaymentStatus.PARTIAL }
                    val hasPaid = commitmentsForDay.any { it.status == PaymentStatus.PAID }

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> DarkGreenPrimary
                                    hasPending -> StatusPendingBg
                                    hasPaid -> StatusPaidBg
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { viewModel.selectDay(dayNum) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$dayNum",
                                fontSize = 14.sp,
                                fontWeight = if (commitmentsForDay.isNotEmpty() || isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> Color.White
                                    hasPending -> StatusPendingText
                                    hasPaid -> StatusPaidText
                                    else -> MaterialTheme.colorScheme.onBackground
                                }
                            )

                            if (commitmentsForDay.isNotEmpty() && !isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (hasPending) StatusPendingText else StatusPaidText)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (uiState.selectedDay != null) "Payments Due on Day ${uiState.selectedDay}" else "Upcoming Payments in ${DateUtils.getMonthName(uiState.selectedMonth)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            if (selectedDayCommitments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No payments due for this date.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(selectedDayCommitments) { item ->
                        CommitmentItemCard(
                            item = item,
                            onClick = {
                                onCommitmentClick(item.commitment.id, item.year, item.month)
                            }
                        )
                    }
                }
            }
        }
    }
}
