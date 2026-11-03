package com.example.monthlyfinancetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CategoryIcon(
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    iconSize: Dp = 22.dp
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF0F684B)
    }

    val icon: ImageVector = when (iconName.lowercase()) {
        "home", "rent" -> Icons.Default.Home
        "bank", "loan" -> Icons.Default.AccountBalance
        "credit_card", "card" -> Icons.Default.CreditCard
        "trending_up", "sip" -> Icons.Default.TrendingUp
        "savings", "investment" -> Icons.Default.Savings
        "receipt", "bills" -> Icons.Default.Receipt
        else -> Icons.Default.MoreHoriz
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(parsedColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = iconName,
            tint = parsedColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
