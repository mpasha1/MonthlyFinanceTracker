package com.example.monthlyfinancetracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.monthlyfinancetracker.ui.screens.add.AddEditCommitmentScreen
import com.example.monthlyfinancetracker.ui.screens.add.AddEditViewModel
import com.example.monthlyfinancetracker.ui.screens.calendar.CalendarScreen
import com.example.monthlyfinancetracker.ui.screens.calendar.CalendarViewModel
import com.example.monthlyfinancetracker.ui.screens.commitments.CommitmentsListScreen
import com.example.monthlyfinancetracker.ui.screens.commitments.CommitmentsViewModel
import com.example.monthlyfinancetracker.ui.screens.details.PaymentDetailsScreen
import com.example.monthlyfinancetracker.ui.screens.details.PaymentDetailsViewModel
import com.example.monthlyfinancetracker.ui.screens.home.HomeScreen
import com.example.monthlyfinancetracker.ui.screens.home.HomeViewModel
import com.example.monthlyfinancetracker.ui.screens.pastmonths.PastMonthsScreen
import com.example.monthlyfinancetracker.ui.screens.pastmonths.PastMonthsViewModel
import com.example.monthlyfinancetracker.ui.screens.reminders.RemindersScreen
import com.example.monthlyfinancetracker.ui.screens.reminders.RemindersViewModel
import com.example.monthlyfinancetracker.ui.screens.reports.ReportsScreen
import com.example.monthlyfinancetracker.ui.screens.reports.ReportsViewModel
import com.example.monthlyfinancetracker.ui.screens.settings.CategoriesScreen
import com.example.monthlyfinancetracker.ui.screens.settings.SettingsScreen
import com.example.monthlyfinancetracker.ui.screens.splash.SplashScreen
import com.example.monthlyfinancetracker.ui.theme.DarkGreenPrimary

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun MainAppScaffold() {
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Default.Home),
        BottomNavItem("Calendar", Screen.Calendar.route, Icons.Default.CalendarMonth),
        BottomNavItem("Add", Screen.AddCommitment.createRoute(0L), Icons.Default.AddCircle),
        BottomNavItem("Reports", Screen.Reports.route, Icons.Default.BarChart),
        BottomNavItem("More", Screen.Settings.route, Icons.Default.Menu)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Calendar.route,
        Screen.Reports.route,
        Screen.Settings.route
    ) || (currentRoute != null && currentRoute.startsWith("add_commitment"))

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route ||
                                (item.route.startsWith("add_commitment") && currentRoute?.startsWith("add_commitment") == true)

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkGreenPrimary,
                                selectedTextColor = DarkGreenPrimary,
                                unselectedIconColor = Color(0xFF667085),
                                unselectedTextColor = Color(0xFF667085),
                                indicatorColor = DarkGreenPrimary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onGetStarted = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onCommitmentClick = { id, yr, mn ->
                        navController.navigate(Screen.PaymentDetails.createRoute(id, yr, mn))
                    },
                    onCalendarClick = {
                        navController.navigate(Screen.Calendar.route)
                    }
                )
            }

            composable(Screen.Calendar.route) {
                val calendarViewModel: CalendarViewModel = viewModel()
                CalendarScreen(
                    viewModel = calendarViewModel,
                    onCommitmentClick = { id, yr, mn ->
                        navController.navigate(Screen.PaymentDetails.createRoute(id, yr, mn))
                    }
                )
            }

            composable(
                route = Screen.AddCommitment.route,
                arguments = listOf(navArgument("commitmentId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val commitmentId = backStackEntry.arguments?.getLong("commitmentId") ?: 0L
                val addViewModel: AddEditViewModel = viewModel()
                AddEditCommitmentScreen(
                    commitmentId = commitmentId,
                    viewModel = addViewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.popBackStack()
                        navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.CommitmentsList.route) {
                val commitmentsViewModel: CommitmentsViewModel = viewModel()
                CommitmentsListScreen(
                    viewModel = commitmentsViewModel,
                    onBack = { navController.popBackStack() },
                    onCommitmentClick = { id, yr, mn ->
                        navController.navigate(Screen.PaymentDetails.createRoute(id, yr, mn))
                    }
                )
            }

            composable(
                route = Screen.PaymentDetails.route,
                arguments = listOf(
                    navArgument("commitmentId") { type = NavType.LongType },
                    navArgument("year") { type = NavType.IntType },
                    navArgument("month") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("commitmentId") ?: 0L
                val yr = backStackEntry.arguments?.getInt("year") ?: 2026
                val mn = backStackEntry.arguments?.getInt("month") ?: 10

                val detailsViewModel: PaymentDetailsViewModel = viewModel()
                PaymentDetailsScreen(
                    commitmentId = id,
                    year = yr,
                    month = mn,
                    viewModel = detailsViewModel,
                    onBack = { navController.popBackStack() },
                    onEditCommitment = { editId ->
                        navController.navigate(Screen.AddCommitment.createRoute(editId))
                    }
                )
            }

            composable(Screen.Reports.route) {
                val reportsViewModel: ReportsViewModel = viewModel()
                ReportsScreen(
                    viewModel = reportsViewModel
                )
            }

            composable(Screen.PastMonths.route) {
                val pastMonthsViewModel: PastMonthsViewModel = viewModel()
                PastMonthsScreen(
                    viewModel = pastMonthsViewModel,
                    onBack = { navController.popBackStack() },
                    onSelectMonth = { yr, mn ->
                        navController.navigate(Screen.Home.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                    onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                    onNavigateToPastMonths = { navController.navigate(Screen.PastMonths.route) },
                    onNavigateToSmsTracker = { navController.navigate(Screen.SmsTracker.route) }
                )
            }

            composable(Screen.Categories.route) {
                CategoriesScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Reminders.route) {
                val remindersViewModel: RemindersViewModel = viewModel()
                RemindersScreen(
                    viewModel = remindersViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SmsTracker.route) {
                val smsViewModel: com.example.monthlyfinancetracker.ui.screens.sms.SmsViewModel = viewModel()
                com.example.monthlyfinancetracker.ui.screens.sms.SmsTrackerScreen(
                    viewModel = smsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
