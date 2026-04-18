package com.sajalweb.pocketsaathi.ui.components

// ui/components/WeeklySummaryCard.kt
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    breakdown: List<DayTotal>
) {
    val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed

    // Build a 7-slot array for the week
    val dailyAmounts = remember(breakdown) {
        val map = breakdown.associate { it.dayOfWeek.toInt() to it.total }
        (0..6).map { map[it] ?: 0.0 }
    }
    val maxAmount = dailyAmounts.maxOrNull()?.takeIf { it > 0 } ?: 1.0

    // Animate bars in
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "bar_anim"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "This Week",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                    Text(
                        text = "₹${"%.0f".format(weekTotal)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "This Month",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "₹${"%.0f".format(monthTotal)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyAmounts.forEachIndexed { index, amount ->
                    val isToday = index == today
                    val fraction = ((amount / maxAmount) * animatedProgress).toFloat()
                        .coerceIn(0.01f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Amount label (only for today or highest bar)
                        if (isToday && amount > 0) {
                            Text(
                                text = "₹${"%.0f".format(amount)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(2.dp))

                        // Bar
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .height((fraction * 72).dp)
                        ) {
                            drawRoundRect(
                                color = if (isToday) Primary else Primary.copy(alpha = 0.25f),
                                size = size,
                                cornerRadius = CornerRadius(6.dp.toPx())
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = dayLabels[index].take(1),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) Primary else TextSecondary,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}