package com.sajalweb.pocketsaathi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajalweb.pocketsaathi.ui.components.ExpenseItem
import com.sajalweb.pocketsaathi.ui.theme.Primary
import com.sajalweb.pocketsaathi.ui.theme.TextPrimary
import com.sajalweb.pocketsaathi.ui.theme.TextSecondary
import com.sajalweb.pocketsaathi.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.dashboardState.collectAsStateWithLifecycle()

    // Group all expenses by date
    val grouped = remember(state.recentExpenses) {
        state.recentExpenses.groupBy { expense ->
            val cal = Calendar.getInstance().apply { timeInMillis = expense.timestamp }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
        }
    }

    val dateFormat = remember { SimpleDateFormat("EEEE, d MMM", Locale.getDefault()) }

    fun formatGroupKey(key: String): String {
        val parts = key.split("-").map { it.toInt() }
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0])
            set(Calendar.MONTH, parts[1])
            set(Calendar.DAY_OF_MONTH, parts[2])
        }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return when {
            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) &&
                    cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> "Today"
            cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) &&
                    cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) -> "Yesterday"
            else -> dateFormat.format(cal.time)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    "Transaction History",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFF5F5FF)
            ),
            modifier = Modifier.statusBarsPadding() // 👈 Add this to align with status bar
        )

        if (state.recentExpenses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No transactions yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        "Add your first expense from the dashboard",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 12.dp, bottom = 80.dp // bottom padding for FAB
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Summary chip at top
            item {
                SummaryRow(
                    totalCount = state.recentExpenses.size,
                    totalAmount = state.recentExpenses.sumOf { it.amount }
                )
                Spacer(Modifier.height(4.dp))
            }

            grouped.forEach { (dateKey, expenses) ->
                // Date header
                item(key = "header_$dateKey") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatGroupKey(dateKey),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Text(
                            text = "₹${"%.0f".format(expenses.sumOf { it.amount })}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                    }
                }

                // Expense items for this date
                items(expenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onDelete = { viewModel.deleteExpense(expense) }
                    )
                }

                item(key = "spacer_$dateKey") { Spacer(Modifier.height(4.dp)) }
            }
        }
    }
}

@Composable
private fun SummaryRow(totalCount: Int, totalAmount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryChip(
            label = "Total spent",
            value = "₹${"%.0f".format(totalAmount)}",
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Transactions",
            value = "$totalCount",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.08f)),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}