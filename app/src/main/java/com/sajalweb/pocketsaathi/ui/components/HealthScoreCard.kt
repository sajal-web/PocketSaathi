package com.sajalweb.pocketsaathi.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajalweb.pocketsaathi.ui.theme.AmberCaution
import com.sajalweb.pocketsaathi.ui.theme.CardBg
import com.sajalweb.pocketsaathi.ui.theme.GreenPositive
import com.sajalweb.pocketsaathi.ui.theme.RedWarning
import com.sajalweb.pocketsaathi.ui.theme.TextSecondary

@Composable
fun HealthScoreCard(score: Int) {
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "score_anim"
    )

    val scoreColor = when {
        score >= 75 -> GreenPositive
        score >= 45 -> AmberCaution
        else -> RedWarning
    }

    val scoreLabel = when {
        score >= 75 -> "Great"
        score >= 45 -> "Okay"
        else -> "Over Budget"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Money Health",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$animatedScore",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor
                    )
                    Text(
                        text = " / 100",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = scoreColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = scoreLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = scoreColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Score arc
            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 10.dp.toPx()
                    val sweep = (animatedScore / 100f) * 270f

                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = scoreColor,
                        startAngle = 135f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Progress bar row
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Poor", style = MaterialTheme.typography.labelSmall, color = RedWarning)
                Text("Okay", style = MaterialTheme.typography.labelSmall, color = AmberCaution)
                Text("Great", style = MaterialTheme.typography.labelSmall, color = GreenPositive)
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { animatedScore / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = scoreColor,
                trackColor = Color.LightGray.copy(alpha = 0.25f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}