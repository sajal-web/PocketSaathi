package com.sajalweb.pocketsaathi.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sajalweb.pocketsaathi.data.db.DayTotal
import com.sajalweb.pocketsaathi.ui.theme.CardBg
import com.sajalweb.pocketsaathi.ui.theme.Primary
import com.sajalweb.pocketsaathi.ui.theme.TextPrimary
import com.sajalweb.pocketsaathi.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun WeeklySummaryCard(
    weekTotal: Double,
    monthTotal: Double,
    breakdown: List<DayTotal>,
    selectedDayIndex: Int? = null,
    onDaySelected: (Int) -> Unit = {}
) {
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1

    val dailyAmounts = remember(breakdown) {
        val map = breakdown.associate {
            val day = it.dayOfWeek.toIntOrNull() ?: 0
            day to it.total
        }
        List(7) { index -> map[index] ?: 0.0 }
    }

    val maxAmount = dailyAmounts.maxOrNull()?.takeIf { it > 0 } ?: 1.0

    val cardBg = MaterialTheme.colorScheme.surfaceVariant
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("This Week", color = textSecondary)
                    Text(
                        "₹${"%.0f".format(weekTotal)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("This Month", color = textSecondary)
                    Text(
                        "₹${"%.0f".format(monthTotal)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor
                    )
                }
            }

            selectedDayIndex?.let { index ->
                val amount = dailyAmounts[index]
                Text(
                    text = "${dayLabels[index]}: ₹${"%.0f".format(amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyAmounts.forEachIndexed { index, amount ->
                    val isSelected = selectedDayIndex == index
                    val isToday = index == today
                    val fraction = if (maxAmount == 0.0) 0f else (amount / maxAmount).toFloat()
                    val animatedHeight by animateFloatAsState(
                        targetValue = fraction,
                        animationSpec = tween(600),
                        label = "bar_anim"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onDaySelected(index) }
                    ) {
                        if ((isSelected || isToday) && amount > 0) {
                            Text(
                                text = "₹${"%.0f".format(amount)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(4.dp))

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height((animatedHeight * 90).dp)
                        ) {
                            drawRoundRect(
                                color = when {
                                    isSelected -> primaryColor
                                    isToday -> primaryColor.copy(alpha = 0.6f)
                                    else -> primaryColor.copy(alpha = 0.25f)
                                },
                                size = size,
                                cornerRadius = CornerRadius(6.dp.toPx())
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = dayLabels[index].take(1),
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isSelected -> primaryColor
                                isToday -> primaryColor.copy(alpha = 0.7f)
                                else -> textSecondary
                            },
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}