package com.example.monthlyfinancetracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monthlyfinancetracker.domain.model.CategoryBreakdown
import com.example.monthlyfinancetracker.utils.CurrencyUtils

@Composable
fun DonutChart(
    breakdowns: List<CategoryBreakdown>,
    totalAmount: Double,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    strokeWidth: Dp = 26.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            var startAngle = -90f

            if (breakdowns.isEmpty()) {
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidthPx)
                )
            } else {
                breakdowns.forEach { breakdown ->
                    val sweepAngle = (breakdown.percentage / 100f) * 360f
                    val color = try {
                        Color(android.graphics.Color.parseColor(breakdown.category.colorHex))
                    } catch (e: Exception) {
                        Color(0xFF0F684B)
                    }

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = CurrencyUtils.formatRupees(totalAmount),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101828)
            )
            Text(
                text = "Total Commitment",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF667085)
            )
        }
    }
}
