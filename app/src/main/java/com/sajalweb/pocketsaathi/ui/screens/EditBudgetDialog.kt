package com.sajalweb.pocketsaathi.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sajalweb.pocketsaathi.ui.theme.Primary
import com.sajalweb.pocketsaathi.ui.theme.TextPrimary
import com.sajalweb.pocketsaathi.ui.theme.TextSecondary
import com.sajalweb.pocketsaathi.utils.formatAmount
import com.sajalweb.pocketsaathi.utils.getTodayStartMillis
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetDialog(
    currentLimit: Double,
    currentPercentage: Int,
    currentStartDate: Long,
    currentEndDate: Long,
    onSave: (limit: Double, percentage: Int, startDate: Long, endDate: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var expenseText by remember { mutableStateOf(formatAmount(currentLimit)) }
    var percentage by remember { mutableStateOf(currentPercentage) }
    var errorMsg by remember { mutableStateOf("") }

    // Date range state
    val todayStart = getTodayStartMillis()

    // Ensure start date is not in the past
    var startDate by remember {
        mutableStateOf(if (currentStartDate < todayStart) todayStart else currentStartDate)
    }
    var endDate by remember {
        mutableStateOf(if (currentEndDate < todayStart) todayStart else currentEndDate)
    }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val monthlyLimit = expenseText.toDoubleOrNull()
    val isValidLimit = monthlyLimit != null && monthlyLimit >= 100.0
    val daysInPeriod = ((endDate - startDate) / (24 * 60 * 60 * 1000) + 1).toInt().coerceAtLeast(1)

    val effectiveMonthly = (monthlyLimit ?: 0.0) * (percentage / 100.0)
    val dailyBudget = if (isValidLimit) effectiveMonthly / daysInPeriod else 0.0

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis in todayStart..endDate
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            startDate = it
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val minEnd = maxOf(todayStart, startDate)
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= minEnd
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            endDate = it
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // When start date changes, adjust end date if needed
    LaunchedEffect(startDate) {
        if (endDate < startDate) {
            endDate = startDate
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("✏️", fontSize = 40.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Edit Monthly Budget",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = expenseText,
                    onValueChange = {
                        expenseText = it.filter { c -> c.isDigit() || c == '.' }
                        errorMsg = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Monthly Limit (₹)") },
                    placeholder = { Text("e.g. 30000") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMsg.isNotEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                if (errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Date range selection
                Text(
                    text = "Budget period",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                val primaryColor = MaterialTheme.colorScheme.primary
                val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = primaryColor.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStartDatePicker = true }
                        ) {
                            Text(
                                text = dateFormatter.format(Date(startDate)),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("End", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = primaryColor.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEndDatePicker = true }
                        ) {
                            Text(
                                text = dateFormatter.format(Date(endDate)),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = primaryColor
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Daily spending percentage",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("0%", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                    Slider(
                        value = percentage.toFloat(),
                        onValueChange = { percentage = it.toInt() },
                        valueRange = 1f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text("100%", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = primaryColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "$percentage%",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                AnimatedVisibility(visible = isValidLimit) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                primaryColor.copy(alpha = 0.08f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your new daily budget will be",
                            style = MaterialTheme.typography.labelMedium,
                            color = onSurfaceVariant
                        )
                        Text(
                            text = "₹${"%.0f".format(dailyBudget)} / day",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = "($percentage% of ₹${"%.0f".format(monthlyLimit ?: 0.0)} over $daysInPeriod days)",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            when {
                                expenseText.isBlank() -> errorMsg = "Please enter a limit"
                                !isValidLimit -> errorMsg = "Minimum limit is ₹100"
                                else -> {
                                    // Ensure proper timestamp boundaries
                                    val cal = Calendar.getInstance()
                                    cal.timeInMillis = startDate
                                    cal.set(Calendar.HOUR_OF_DAY, 0)
                                    cal.set(Calendar.MINUTE, 0)
                                    cal.set(Calendar.SECOND, 0)
                                    cal.set(Calendar.MILLISECOND, 0)
                                    val start = cal.timeInMillis

                                    cal.timeInMillis = endDate
                                    cal.set(Calendar.HOUR_OF_DAY, 23)
                                    cal.set(Calendar.MINUTE, 59)
                                    cal.set(Calendar.SECOND, 59)
                                    cal.set(Calendar.MILLISECOND, 999)
                                    val end = cal.timeInMillis

                                    onSave(monthlyLimit!!, percentage, start, end)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}