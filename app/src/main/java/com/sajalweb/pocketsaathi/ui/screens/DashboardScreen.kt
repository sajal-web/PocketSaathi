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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajalweb.pocketsaathi.data.model.Expense
import com.sajalweb.pocketsaathi.ui.components.*
import com.sajalweb.pocketsaathi.ui.viewmodel.ExpenseViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onAddExpense: () -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit = {}
) {
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()
    var selectedExpenseForEdit by remember { mutableStateOf<Expense?>(null) }
    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    var showLegalDialog by remember { mutableStateOf(false) }
    val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
    var showResetDialog by remember { mutableStateOf(false) }


    val displayTodaySpent = remember(state.weeklyBreakdown, selectedDayIndex, state.todayTotal) {
        if (selectedDayIndex == null) {
            state.todayTotal
        } else {
            state.weeklyBreakdown
                .find { it.dayOfWeek.toIntOrNull() == selectedDayIndex }
                ?.total ?: 0.0
        }
    }

    val displayRemaining = (state.dailyBudget - displayTodaySpent).coerceAtLeast(0.0)
    val displayPercentUsed = if (state.dailyBudget > 0) {
        (displayTodaySpent / state.dailyBudget).toFloat().coerceIn(0f, 1f)
    } else 0f

    if (!state.isDataLoaded) {
        return
    }

    if (!state.isSetupDone) {
        IncomeSetupDialog { limit, percentage, startDate, endDate ->
            viewModel.saveBudgetConfig(limit, percentage, startDate, endDate)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    "Pocket Saathi",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { showLegalDialog = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Legal")
                }
                IconButton(onClick = { showEditBudgetDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Budget")
                }
                // Add overflow menu
                var showMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Reset All Data") },
                        onClick = {
                            showMenu = false
                            showResetDialog = true
                        }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
            windowInsets = WindowInsets(0, 0, 0, 0)
        )

        selectedExpenseForEdit?.let { expense ->
            EditExpenseDialog(
                expense = expense,
                onDismiss = { selectedExpenseForEdit = null },
                onUpdate = { updated -> viewModel.updateExpense(updated) }
            )
        }

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
                    todaySpent = displayTodaySpent,
                    dailyBudget = state.dailyBudget,
                    remaining = displayRemaining,
                    percentUsed = displayPercentUsed
                )
            }

            item {
                WeeklySummaryCard(
                    weekTotal = state.weekTotal,
                    monthTotal = state.monthTotal,
                    breakdown = state.weeklyBreakdown,
                    selectedDayIndex = selectedDayIndex,
                    onDaySelected = { index ->
                        selectedDayIndex = if (selectedDayIndex == index) null else index
                    }
                )
            }

            item {
                HealthScoreCard(score = state.healthScore)
            }

            if (state.insights.isNotEmpty()) {
                item { InsightCards(insights = state.insights) }
            }

            item {
                ReportChartCard(
                    report = state.report,
                    onToggleChange = { viewModel.selectReportType(it) }
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
                        onDelete = { viewModel.deleteExpense(expense) },
                        onEdit = { selectedExpenseForEdit = expense }
                    )
                }
            }
        }
    }

    // Show confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Data?") },
            text = { Text("This will permanently delete all expenses and budget settings. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllData()
                        showResetDialog = false
                        // The UI will automatically show IncomeSetupDialog because isSetupDone becomes false
                    }
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLegalDialog) {
        LegalDialog(
            onDismiss = { showLegalDialog = false }
        )
    }

    if (showEditBudgetDialog) {

        val today = System.currentTimeMillis()

        val endOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }.timeInMillis

        EditBudgetDialog(
            currentLimit = state.monthlyLimit,
            currentPercentage = state.budgetPercentage,
            currentStartDate = today,
            currentEndDate = endOfMonth,
            onSave = { limit, percentage, startDate, endDate ->
                viewModel.saveBudgetConfig(limit, percentage, startDate, endDate)
                showEditBudgetDialog = false
            },
            onDismiss = { showEditBudgetDialog = false }
        )
    }
}