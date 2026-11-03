package com.example.monthlyfinancetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.monthlyfinancetracker.ui.navigation.MainAppScaffold
import com.example.monthlyfinancetracker.ui.theme.MonthlyFinanceTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonthlyFinanceTrackerTheme {
                MainAppScaffold()
            }
        }
    }
}
