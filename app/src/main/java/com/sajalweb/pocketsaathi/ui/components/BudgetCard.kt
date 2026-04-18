package com.sajalweb.pocketsaathi.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sajalweb.pocketsaathi.ui.theme.AmberCaution
import com.sajalweb.pocketsaathi.ui.theme.CardBg
import com.sajalweb.pocketsaathi.ui.theme.GreenPositive
import com.sajalweb.pocketsaathi.ui.theme.Primary
import com.sajalweb.pocketsaathi.ui.theme.RedWarning
import com.sajalweb.pocketsaathi.ui.theme.TextSecondary

@Composable
fun BudgetCard(
    todaySpent: Double,
    dailyBudget: Double,
    remaining: Double,
    percentUsed: Float
) {
    val color = when {
        percentUsed >= 1f -> RedWarning
        percentUsed >= 0.8f -> AmberCaution
        else -> GreenPositive
    }
    val animatedPercent by animateFloatAsState(
        targetValue = percentUsed,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "budget_arc"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("Daily Budget", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            Spacer(Modifier.height(16.dp))

            // Circular arc progress
            Box(Modifier.size(160.dp).align(Alignment.CenterHorizontally)) {
                Canvas(Modifier.fillMaxSize()) {
                    val strokeWidth = 18.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Track
                    drawArc(color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 135f, sweepAngle = 270f, useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress
                    drawArc(color = color,
                        startAngle = 135f, sweepAngle = 270f * animatedPercent, useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round))
                }
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("₹${remaining.toInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
                    Text("remaining", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Spent today", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text("₹${todaySpent.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Daily limit", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text("₹${dailyBudget.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Primary)
                }
            }
        }
    }
}