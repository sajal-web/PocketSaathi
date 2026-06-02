package com.sajalweb.pocketsaathi.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajalweb.pocketsaathi.data.model.Category
import com.sajalweb.pocketsaathi.data.model.ParsedExpense
import com.sajalweb.pocketsaathi.ui.theme.Primary
import com.sajalweb.pocketsaathi.ui.theme.TextSecondary
import com.sajalweb.pocketsaathi.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseSheet(
    viewModel: ExpenseViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val state by viewModel.addState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    val focusRequester = remember { FocusRequester() }

    // 👇 FALLBACK: treat plain number as amount
    val fallbackAmount = remember(state.inputText) {
        state.inputText.trim().toDoubleOrNull()
    }
    val effectiveAmount = state.parsed?.amount ?: fallbackAmount
    val canSave = effectiveAmount != null

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onDismiss()
            viewModel.resetAddState()
        }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).navigationBarsPadding()) {
            Text("Add Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            // Smart text input
            OutlinedTextField(
                value = state.inputText,
                onValueChange = viewModel::onInputChanged,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                placeholder = { Text("e.g. 250 or paid 250 for food", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                label = { Text("Amount or description") },
                shape = RoundedCornerShape(16.dp),
                singleLine = false,
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Preview: show parsed OR fallback
            AnimatedVisibility(visible = state.parsed != null || fallbackAmount != null) {
                val displayAmount = effectiveAmount
                val displayCategory = selectedCategory ?: state.parsed?.category ?: Category.OTHERS
                val displayDescription = state.parsed?.description?.ifEmpty { "Expense" } ?: "Expense"
                val primaryColor = MaterialTheme.colorScheme.primary
                val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(displayCategory.emoji, fontSize = 28.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(displayDescription, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(displayCategory.label, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                        }
                        if (displayAmount != null) {
                            Text("₹${displayAmount.toInt()}", style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold, color = primaryColor)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Category quick-select chips
            Text("Category", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Category.entries.toTypedArray()) { cat ->
                    val isSelected = (selectedCategory ?: state.parsed?.category) == cat
                    val primaryColor = MaterialTheme.colorScheme.primary

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text("${cat.emoji} ${cat.label}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor.copy(alpha = 0.15f),
                            selectedLabelColor = primaryColor
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Save button (enabled if we have an amount)
            Button(
                onClick = {
                    val amount = effectiveAmount ?: return@Button
                    val category = selectedCategory ?: state.parsed?.category ?: Category.OTHERS
                    val desc = state.parsed?.description?.ifEmpty { "Expense" } ?: "Expense"
                    viewModel.saveExpense(amount, desc, category)
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save ₹${effectiveAmount?.toInt() ?: ""}", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}