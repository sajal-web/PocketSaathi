package com.sajalweb.pocketsaathi.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajalweb.pocketsaathi.domain.Insight
import com.sajalweb.pocketsaathi.ui.theme.TextSecondary
import com.sajalweb.pocketsaathi.domain.InsightType
import com.sajalweb.pocketsaathi.ui.theme.AmberCaution
import com.sajalweb.pocketsaathi.ui.theme.GreenPositive
import com.sajalweb.pocketsaathi.ui.theme.Primary
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
    val (bgColor, titleColor, borderColor) = when (insight.type) {
        InsightType.POSITIVE -> Triple(
            GreenPositive.copy(alpha = 0.07f), GreenPositive, GreenPositive.copy(alpha = 0.25f)
        )
        InsightType.CAUTION -> Triple(
            AmberCaution.copy(alpha = 0.08f), AmberCaution, AmberCaution.copy(alpha = 0.3f)
        )
        InsightType.WARNING -> Triple(
            RedWarning.copy(alpha = 0.07f), RedWarning, RedWarning.copy(alpha = 0.25f)
        )
        InsightType.NEUTRAL -> Triple(
            Primary.copy(alpha = 0.06f), Primary, Primary.copy(alpha = 0.2f)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Emoji bubble
            Surface(
                shape = CircleShape,
                color = titleColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(insight.emoji, fontSize = 16.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.75f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}