package com.sajalweb.pocketsaathi.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajalweb.pocketsaathi.data.model.ReportType
import com.sajalweb.pocketsaathi.ui.theme.GreenPositive
import com.sajalweb.pocketsaathi.ui.theme.Primary
import com.sajalweb.pocketsaathi.ui.theme.RedWarning
import com.sajalweb.pocketsaathi.ui.theme.TextPrimary
import com.sajalweb.pocketsaathi.ui.theme.TextSecondary
import com.sajalweb.pocketsaathi.ui.viewmodel.ReportUiState

@Composable
fun ReportChartCard(
    report: ReportUiState,
    onToggleChange: (ReportType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F3FC)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (report.isSaved)
                        GreenPositive.copy(alpha = 0.12f)
                    else
                        RedWarning.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (report.isSaved) "✓ On track" else "⚠ Over budget",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (report.isSaved) GreenPositive else RedWarning
                    )
                }
            }

            // Segmented toggle
            ReportTypeToggle(
                selected = report.reportType,
                onSelect = onToggleChange
            )

            // Animated bar chart
            ReportBarChart(report = report)

            // Stat row
            ReportStatRow(report = report)

            // Insight message
            if (report.insightMessage.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (report.isSaved)
                        GreenPositive.copy(alpha = 0.08f)
                    else
                        RedWarning.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = report.insightMessage,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (report.isSaved) GreenPositive else RedWarning,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportTypeToggle(
    selected: ReportType,
    onSelect: (ReportType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.7f)),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ReportType.values().forEach { type ->
            val isSelected = type == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) Primary else Color.Transparent
                    ),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { onSelect(type) },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(
                        text = type.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportBarChart(report: ReportUiState) {
    val total = report.budgetLimit.coerceAtLeast(report.totalSpending).coerceAtLeast(1.0)

    // Animate bars when report type or values change
    val spendFraction = (report.totalSpending / total).toFloat().coerceIn(0f, 1f)
    val budgetFraction = (report.budgetLimit / total).toFloat().coerceIn(0f, 1f)

    val animatedSpend by animateFloatAsState(
        targetValue = spendFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "spend_bar"
    )
    val animatedBudget by animateFloatAsState(
        targetValue = budgetFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "budget_bar"
    )

    val spendColor  = if (report.isSaved) GreenPositive else RedWarning
    val budgetColor = Primary

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Spending bar
        BarRow(
            label     = "Spending",
            value     = "₹${"%.0f".format(report.totalSpending)}",
            fraction  = animatedSpend,
            barColor  = spendColor,
            trackColor = spendColor.copy(alpha = 0.15f)
        )
        // Budget bar
        BarRow(
            label     = "Budget",
            value     = "₹${"%.0f".format(report.budgetLimit)}",
            fraction  = animatedBudget,
            barColor  = budgetColor,
            trackColor = budgetColor.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun BarRow(
    label: String,
    value: String,
    fraction: Float,
    barColor: Color,
    trackColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceAtLeast(0.03f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun ReportStatRow(report: ReportUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Spending stat
        ReportStatChip(
            label = "Spent",
            value = "₹${"%.0f".format(report.totalSpending)}",
            valueColor = if (report.isSaved) GreenPositive else RedWarning,
            modifier = Modifier.weight(1f)
        )
        // Saved or overspent
        if (report.isSaved) {
            ReportStatChip(
                label = "Saved",
                value = "₹${"%.0f".format(report.savedAmount)}",
                valueColor = GreenPositive,
                modifier = Modifier.weight(1f)
            )
        } else {
            ReportStatChip(
                label = "Overspent",
                value = "₹${"%.0f".format(report.overspentAmount)}",
                valueColor = RedWarning,
                modifier = Modifier.weight(1f)
            )
        }
        // Budget limit stat
        ReportStatChip(
            label = "Budget",
            value = "₹${"%.0f".format(report.budgetLimit)}",
            valueColor = Primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReportStatChip(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.8f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}