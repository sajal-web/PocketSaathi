package com.sajalweb.pocketsaathi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajalweb.pocketsaathi.ui.components.BudgetCard
import com.sajalweb.pocketsaathi.ui.components.ExpenseItem
import com.sajalweb.pocketsaathi.ui.components.HealthScoreCard
import com.sajalweb.pocketsaathi.ui.components.InsightCards
import com.sajalweb.pocketsaathi.ui.components.WeeklySummaryCard
import com.sajalweb.pocketsaathi.ui.theme.Primary
import com.sajalweb.pocketsaathi.ui.viewmodel.ExpenseViewModel

// ui/screens/DashboardScreen.kt
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit
) {
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }
    var showSetupDialog by remember { mutableStateOf(!state.isSetupDone) }

    if (!state.isSetupDone) {
        IncomeSetupDialog(onSave = { income ->
            viewModel.saveMonthlyIncome(income)
            showSetupDialog = false
        })
        return
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = Primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("Add Expense") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Budget Card
            item {
                BudgetCard(
                    todaySpent = state.todayTotal,
                    dailyBudget = state.dailyBudget,
                    remaining = state.remaining,
                    percentUsed = state.budgetPercentUsed
                )
            }

            // Health Score
            item {
                HealthScoreCard(score = state.healthScore)
            }

            // Insights
            if (state.insights.isNotEmpty()) {
                item { InsightCards(insights = state.insights) }
            }

            // Weekly Summary
            item {
                WeeklySummaryCard(
                    weekTotal = state.weekTotal,
                    monthTotal = state.monthTotal,
                    breakdown = state.weeklyBreakdown
                )
            }

            // Today's Transactions
            if (state.todayExpenses.isNotEmpty()) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = onNavigateToHistory) { Text("See all") }
                    }
                }
                items(state.todayExpenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        AddExpenseSheet(
            viewModel = viewModel,
            onDismiss = { showAddSheet = false }
        )
    }
}