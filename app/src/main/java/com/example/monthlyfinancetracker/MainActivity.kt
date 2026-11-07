package com.example.monthlyfinancetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.monthlyfinancetracker.data.local.AppDatabase
import com.example.monthlyfinancetracker.data.repository.FinanceRepository
import com.example.monthlyfinancetracker.notification.SmsAutoMatcher
import com.example.monthlyfinancetracker.ui.navigation.MainAppScaffold
import com.example.monthlyfinancetracker.ui.theme.MonthlyFinanceTrackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MonthlyFinanceTrackerTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        triggerAutoSmsMatch()
                    }
                }

                LaunchedEffect(Unit) {
                    val isPermissionGranted = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.READ_SMS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!isPermissionGranted) {
                        permissionLauncher.launch(Manifest.permission.READ_SMS)
                    } else {
                        triggerAutoSmsMatch()
                    }
                }

                MainAppScaffold()
            }
        }
    }

    private fun triggerAutoSmsMatch() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dao = AppDatabase.getDatabase(applicationContext).financeDao()
                val repository = FinanceRepository(dao)
                repository.consolidateOtherCommitments()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val matchesCount = SmsAutoMatcher.performAutoMatchForCurrentMonth(applicationContext)
            if (matchesCount > 0) {
                launch(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Auto-matched $matchesCount UPI Mandate payment(s) from SMS and updated status to Paid!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
