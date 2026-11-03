package com.example.monthlyfinancetracker.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object AddCommitment : Screen("add_commitment?commitmentId={commitmentId}") {
        fun createRoute(commitmentId: Long = 0L) = "add_commitment?commitmentId=$commitmentId"
    }
    object CommitmentsList : Screen("commitments_list")
    object PaymentDetails : Screen("payment_details/{commitmentId}/{year}/{month}") {
        fun createRoute(commitmentId: Long, year: Int, month: Int) = "payment_details/$commitmentId/$year/$month"
    }
    object Reports : Screen("reports")
    object PastMonths : Screen("past_months")
    object Settings : Screen("settings")
    object Categories : Screen("categories")
    object Reminders : Screen("reminders")
    object SmsTracker : Screen("sms_tracker")
}
