package com.sajalweb.pocketsaathi.domain

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightEngine @Inject constructor() {

    fun getHealthScore(
        todaySpent: Double,
        dailyBudget: Double,
        avgDailySpend: Double
    ): Int {
        if (dailyBudget <= 0) return 50
        val budgetScore = ((1.0 - (todaySpent / dailyBudget).coerceIn(0.0, 1.5)) * 60).toInt()
        val avgScore = if (avgDailySpend > 0) {
            ((1.0 - ((todaySpent - avgDailySpend) / avgDailySpend).coerceIn(-0.5, 1.0)) * 40).toInt()
        } else 40
        return (budgetScore + avgScore).coerceIn(0, 100)
    }

    fun getInsights(
        todaySpent: Double,
        dailyBudget: Double,
        avgDailySpend: Double,
        weekTotal: Double
    ): List<Insight> {
        val insights = mutableListOf<Insight>()

        when {
            todaySpent > dailyBudget * 1.5 ->
                insights.add(Insight("⚠️ You've exceeded today's budget by ${formatAmount(todaySpent - dailyBudget)}", InsightType.WARNING))
            todaySpent > dailyBudget ->
                insights.add(Insight("📊 Slightly over today's limit. Be mindful now.", InsightType.CAUTION))
            todaySpent == 0.0 ->
                insights.add(Insight("✨ No spending yet today. Great start!", InsightType.POSITIVE))
            todaySpent < dailyBudget * 0.5 ->
                insights.add(Insight("🎯 You're well within budget today!", InsightType.POSITIVE))
        }

        if (avgDailySpend > 0 && todaySpent > avgDailySpend * 1.3) {
            insights.add(Insight("📈 You spent more than usual today (avg ₹${formatAmount(avgDailySpend)})", InsightType.CAUTION))
        }

        return insights
    }

    private fun formatAmount(amount: Double) = "%.0f".format(amount)
}

data class Insight(val message: String, val type: InsightType)
enum class InsightType { POSITIVE, CAUTION, WARNING }