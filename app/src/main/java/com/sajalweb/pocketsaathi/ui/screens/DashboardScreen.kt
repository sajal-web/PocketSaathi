package com.sajalweb.pocketsaathi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajalweb.pocketsaathi.ui.components.*
import com.sajalweb.pocketsaathi.ui.viewmodel.ExpenseViewModel

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onAddExpense: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit = {}
) {
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()
    var showSetupDialog by remember { mutableStateOf(!state.isSetupDone) }

    if (!state.isSetupDone) {
        IncomeSetupDialog { limit, percentage ->
            viewModel.saveBudgetConfig(limit, percentage)
            showSetupDialog = false
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 80.dp // extra space for FAB
        ),
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
                    Text(
                        "Today",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text("See all")
                    }
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