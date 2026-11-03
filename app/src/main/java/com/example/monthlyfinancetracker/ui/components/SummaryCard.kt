package com.example.monthlyfinancetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monthlyfinancetracker.domain.model.MonthSummary
import com.example.monthlyfinancetracker.ui.theme.BlueStatCardBg
import com.example.monthlyfinancetracker.ui.theme.BlueStatText
import com.example.monthlyfinancetracker.ui.theme.GreenStatCardBg
import com.example.monthlyfinancetracker.ui.theme.GreenStatText
import com.example.monthlyfinancetracker.ui.theme.RedStatCardBg
import com.example.monthlyfinancetracker.ui.theme.RedStatText
import com.example.monthlyfinancetracker.utils.CurrencyUtils

@Composable
fun DashboardSummaryGrid(
    summary: MonthSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Total Commitment
            SummaryCardItem(
                modifier = Modifier.weight(1f),
                title = "Total Commitment",
                value = CurrencyUtils.formatRupees(summary.totalCommitment),
                iconBgColor = BlueStatCardBg,
                iconColor = BlueStatText,
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BlueStatText),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            // Card 2: Paid
            SummaryCardItem(
                modifier = Modifier.weight(1f),
                title = "Paid",
                value = CurrencyUtils.formatRupees(summary.totalPaid),
                iconBgColor = GreenStatCardBg,
                iconColor = GreenStatText,
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GreenStatText),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 3: Balance / Remaining
            SummaryCardItem(
                modifier = Modifier.weight(1f),
                title = "Remaining",
                value = CurrencyUtils.formatRupees(summary.balance),
                iconBgColor = RedStatCardBg,
                iconColor = RedStatText,
                iconContent = {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(RedStatText),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            // Card 4: % Completed Progress Ring
            SummaryCardItem(
                modifier = Modifier.weight(1f),
                title = "Completed",
                value = String.format("%.1f%%", summary.paidPercentage),
                iconBgColor = GreenStatCardBg,
                iconColor = GreenStatText,
                iconContent = {
                    Box(
                        modifier = Modifier.size(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { summary.paidPercentage / 100f },
                            modifier = Modifier.size(32.dp),
                            color = GreenStatText,
                            trackColor = GreenStatText.copy(alpha = 0.2f),
                            strokeWidth = 3.5.dp
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun SummaryCardItem(
    title: String,
    value: String,
    iconBgColor: Color,
    iconColor: Color,
    iconContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = iconBgColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                iconContent()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = value,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2522)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5F6864)
            )
        }
    }
}
