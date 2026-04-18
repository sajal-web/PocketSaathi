package com.sajalweb.pocketsaathi.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sajalweb.pocketsaathi.domain.Insight
import com.sajalweb.pocketsaathi.ui.theme.TextSecondary
import com.sajalweb.pocketsaathi.domain.InsightType
import com.sajalweb.pocketsaathi.ui.theme.AmberCaution
import com.sajalweb.pocketsaathi.ui.theme.GreenPositive
import com.sajalweb.pocketsaathi.ui.theme.RedWarning
import com.sajalweb.pocketsaathi.ui.theme.TextPrimary

@Composable
fun InsightCards(insights: List<Insight>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Insights",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        insights.forEach { insight ->
            InsightRow(insight = insight)
        }
    }
}

@Composable
private fun InsightRow(insight: Insight) {
    val (bgColor, textColor, borderColor) = when (insight.type) {
        InsightType.POSITIVE -> Triple(
            GreenPositive.copy(alpha = 0.08f),
            GreenPositive,
            GreenPositive.copy(alpha = 0.3f)
        )
        InsightType.CAUTION -> Triple(
            AmberCaution.copy(alpha = 0.10f),
            AmberCaution,
            AmberCaution.copy(alpha = 0.4f)
        )
        InsightType.WARNING -> Triple(
            RedWarning.copy(alpha = 0.08f),
            RedWarning,
            RedWarning.copy(alpha = 0.3f)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = insight.message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                lineHeight = androidx.compose.ui.unit.TextUnit(
                    20f, androidx.compose.ui.unit.TextUnitType.Sp
                )
            )
        }
    }
}