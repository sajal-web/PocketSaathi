package com.sajalweb.pocketsaathi.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sajalweb.pocketsaathi.ui.theme.Primary
import com.sajalweb.pocketsaathi.ui.theme.TextPrimary
import com.sajalweb.pocketsaathi.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun IncomeSetupDialog(
    onSave: (monthlyLimit: Double, budgetPercentage: Int) -> Unit
) {
    var expenseText by remember { mutableStateOf("") }
    var percentage by remember { mutableStateOf(70) } // default 70%
    var errorMsg by remember { mutableStateOf("") }

    val monthlyLimit = expenseText.toDoubleOrNull()
    val isValidLimit = monthlyLimit != null && monthlyLimit >= 100.0

    val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
    val effectiveMonthly = (monthlyLimit ?: 0.0) * (percentage / 100.0)
    val dailyBudget = effectiveMonthly / daysInMonth

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎯", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Set Monthly Spending Limit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "How much can you spend in a month? We'll split it into daily budget.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = expenseText,
                    onValueChange = {
                        expenseText = it.filter { c -> c.isDigit() || c == '.' }
                        errorMsg = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Monthly Limit (₹)") },
                    placeholder = { Text("e.g. 30000") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.SemiBold, color = Primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    isError = errorMsg.isNotEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        focusedLabelColor = Primary
                    )
                )

                AnimatedVisibility(visible = errorMsg.isNotEmpty()) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Percentage selector – now moves 1 by 1
                Text(
                    text = "What percentage of this limit should be your daily spending budget?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("0%", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Slider(
                        value = percentage.toFloat(),
                        onValueChange = { percentage = it.toInt() },
                        valueRange = 1f..100f,   // 👈 continuous 1-100
                        colors = SliderDefaults.colors(
                            thumbColor = Primary,
                            activeTrackColor = Primary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text("100%", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "$percentage%",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                AnimatedVisibility(visible = isValidLimit) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Primary.copy(alpha = 0.08f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your daily budget will be",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "₹${"%.0f".format(dailyBudget)} / day",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = "($percentage% of ₹${"%.0f".format(monthlyLimit ?: 0.0)} ÷ $daysInMonth days)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        when {
                            expenseText.isBlank() -> errorMsg = "Please enter your monthly limit"
                            !isValidLimit -> errorMsg = "Minimum limit is ₹100"
                            else -> onSave(monthlyLimit!!, percentage)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Let's Go →", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}