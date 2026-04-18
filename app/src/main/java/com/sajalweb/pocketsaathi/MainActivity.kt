package com.sajalweb.pocketsaathi

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sajalweb.pocketsaathi.ui.screens.AddExpenseSheet
import com.sajalweb.pocketsaathi.ui.screens.DashboardScreen
import com.sajalweb.pocketsaathi.ui.screens.HistoryScreen
import com.sajalweb.pocketsaathi.ui.theme.SpendSenseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendSenseTheme {
                SpendSenseApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendSenseApp() {
    val navController = rememberNavController()
    var showAddExpenseSheet by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val items = listOf(
                    Screen.Dashboard to Icons.Filled.Dashboard,
                    Screen.History to Icons.Filled.History
                )

                items.forEach { (screen, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            if (currentDestination?.route != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Dashboard.route)
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddExpenseSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("Add Expense") }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Log.d(paddingValues.toString(), "DashboardScreen")
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    modifier = Modifier,
                    onAddExpense = { showAddExpenseSheet = true },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    modifier = Modifier,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showAddExpenseSheet) {
        AddExpenseSheet(
            onDismiss = { showAddExpenseSheet = false }
        )
    }
}

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object History : Screen("history", "History")
}