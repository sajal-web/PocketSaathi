package com.sajalweb.pocketsaathi.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajalweb.pocketsaathi.ui.components.*
import com.sajalweb.pocketsaathi.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onAddExpense: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit = {}
) {
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()
    var showEditBudgetDialog by remember { mutableStateOf(false) }

    if (!state.isSetupDone) {
        IncomeSetupDialog { limit, percentage ->
            viewModel.saveBudgetConfig(limit, percentage)
        }
        return
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    "Pocket Saathi",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { showEditBudgetDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Budget")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BudgetCard(
                    todaySpent = state.todayTotal,
                    dailyBudget = state.dailyBudget,
                    remaining = state.remaining,
                    percentUsed = state.budgetPercentUsed
                )
            }

            item {
                HealthScoreCard(score = state.healthScore)
            }

            if (state.insights.isNotEmpty()) {
                item { InsightCards(insights = state.insights) }
            }

            item {
                WeeklySummaryCard(
                    weekTotal = state.weekTotal,
                    monthTotal = state.monthTotal,
                    breakdown = state.weeklyBreakdown
                )
            }

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

    if (showEditBudgetDialog) {
        EditBudgetDialog(
            currentLimit = state.monthlyLimit,
            currentPercentage = state.budgetPercentage,
            onSave = { limit, percentage ->
                viewModel.saveBudgetConfig(limit, percentage)
                showEditBudgetDialog = false
            },
            onDismiss = { showEditBudgetDialog = false }
        )
    }
}