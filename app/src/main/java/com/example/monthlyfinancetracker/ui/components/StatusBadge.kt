package com.example.monthlyfinancetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monthlyfinancetracker.domain.model.PaymentStatus
import com.example.monthlyfinancetracker.ui.theme.StatusPaidBg
import com.example.monthlyfinancetracker.ui.theme.StatusPaidText
import com.example.monthlyfinancetracker.ui.theme.StatusPartialBg
import com.example.monthlyfinancetracker.ui.theme.StatusPartialText
import com.example.monthlyfinancetracker.ui.theme.StatusPendingBg
import com.example.monthlyfinancetracker.ui.theme.StatusPendingText

@Composable
fun StatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (text, bgColor, textColor) = when (status) {
        PaymentStatus.PAID -> Triple("Paid", StatusPaidBg, StatusPaidText)
        PaymentStatus.PARTIAL -> Triple("Partial", StatusPartialBg, StatusPartialText)
        PaymentStatus.PENDING -> Triple("Pending", StatusPendingBg, StatusPendingText)
    }

    Text(
        text = text,
        color = textColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
