// domain/InsightEngine.kt
package com.sajalweb.pocketsaathi.domain

import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightEngine @Inject constructor() {

    // ─────────────────────────────────────────
    // Health Score  (0–100)
    // Weighted across 3 signals:
    //   50 pts → today vs daily budget
    //   30 pts → today vs personal average
    //   20 pts → week pace vs weekly budget
    // ─────────────────────────────────────────
    fun getHealthScore(
        todaySpent: Double,
        dailyBudget: Double,
        avgDailySpend: Double,
        weekTotal: Double = 0.0,
        weekBudget: Double = 0.0
    ): Int {
        if (dailyBudget <= 0) return 50

        // Perfect day = 100 (you spent nothing)
        if (todaySpent == 0.0) {
            // Bonus: if you're also on track for the week, add a little extra?
            return 100
        }

        // Signal 1 — budget adherence today (50 pts)
        val budgetRatio = (todaySpent / dailyBudget).coerceIn(0.0, 2.0)
        val budgetScore = ((1.0 - budgetRatio / 2.0) * 50).toInt()

        // Signal 2 — vs personal average (30 pts)
        val avgScore = if (avgDailySpend > 0) {
            val diff = (todaySpent - avgDailySpend) / avgDailySpend
            ((1.0 - diff.coerceIn(-0.5, 1.0)) * 20 + 10).toInt()
        } else 20

        // Signal 3 — week pace (20 pts)
        val weekScore = if (weekBudget > 0) {
            val weekRatio = (weekTotal / weekBudget).coerceIn(0.0, 2.0)
            ((1.0 - weekRatio / 2.0) * 20).toInt()
        } else 10

        return (budgetScore + avgScore + weekScore).coerceIn(0, 100)
    }

    // ─────────────────────────────────────────
    // Main insight generator
    // Accepts richer context for smarter output
    // ─────────────────────────────────────────
    fun getInsights(
        todaySpent: Double,
        dailyBudget: Double,
        avgDailySpend: Double,
        weekTotal: Double,
        weekBudget: Double = 0.0,
        monthTotal: Double = 0.0,
        monthBudget: Double = 0.0,
        topCategoryName: String = "",
        topCategoryAmount: Double = 0.0,
        totalTransactionsToday: Int = 0,
        recentExpenses: List<RecentExpense> = emptyList()
    ): List<Insight> {
        val insights = mutableListOf<Insight>()
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val remaining = (dailyBudget - todaySpent).coerceAtLeast(0.0)
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY

        // ── 1. TODAY'S BUDGET STATUS (always shown) ──────────────────
        when {
            dailyBudget <= 0 -> {
                insights += Insight(
                    emoji = "⚙️",
                    title = "No budget set",
                    message = "Set your monthly income to unlock daily budget tracking.",
                    type = InsightType.NEUTRAL,
                    priority = 10
                )
            }
            todaySpent == 0.0 && hour >= 12 -> {
                insights += Insight(
                    emoji = "💤",
                    title = "No spending yet today",
                    message = "It's ${formatHour(hour)} and you haven't spent anything. Enjoying a low-spend day!",
                    type = InsightType.POSITIVE,
                    priority = 9
                )
            }
            todaySpent == 0.0 -> {
                insights += Insight(
                    emoji = "🌅",
                    title = "Fresh start today",
                    message = "Your daily budget is ₹${fmt(dailyBudget)}. Make it count!",
                    type = InsightType.POSITIVE,
                    priority = 9
                )
            }
            todaySpent >= dailyBudget * 2.0 -> {
                insights += Insight(
                    emoji = "🚨",
                    title = "Way over budget",
                    message = "You've spent ₹${fmt(todaySpent)} — that's ${pct(todaySpent, dailyBudget)}% of your daily limit. Tomorrow, aim for ₹${fmt(dailyBudget)}.",
                    type = InsightType.WARNING,
                    priority = 10
                )
            }
            todaySpent >= dailyBudget * 1.5 -> {
                insights += Insight(
                    emoji = "⚠️",
                    title = "Budget exceeded",
                    message = "You're ₹${fmt(todaySpent - dailyBudget)} over today's limit. Try to hold off on any more spending.",
                    type = InsightType.WARNING,
                    priority = 10
                )
            }
            todaySpent > dailyBudget -> {
                insights += Insight(
                    emoji = "📊",
                    title = "Slightly over limit",
                    message = "Just ₹${fmt(todaySpent - dailyBudget)} over today's budget. Pause before the next spend.",
                    type = InsightType.CAUTION,
                    priority = 8
                )
            }
            remaining <= dailyBudget * 0.15 && todaySpent > 0 -> {
                insights += Insight(
                    emoji = "🪙",
                    title = "Almost out of budget",
                    message = "Only ₹${fmt(remaining)} left for today. You've used ${pct(todaySpent, dailyBudget)}% already.",
                    type = InsightType.CAUTION,
                    priority = 8
                )
            }
            todaySpent <= dailyBudget * 0.3 -> {
                insights += Insight(
                    emoji = "🎯",
                    title = "Well within budget",
                    message = "Great control! ₹${fmt(remaining)} still available today.",
                    type = InsightType.POSITIVE,
                    priority = 6
                )
            }
            else -> {
                insights += Insight(
                    emoji = "✅",
                    title = "On track today",
                    message = "₹${fmt(remaining)} remaining for today. Keep it up!",
                    type = InsightType.POSITIVE,
                    priority = 5
                )
            }
        }

        // ── 2. VS PERSONAL AVERAGE ────────────────────────────────────
        if (avgDailySpend > 0 && todaySpent > 0) {
            val diff = todaySpent - avgDailySpend
            val diffPct = ((diff / avgDailySpend) * 100).toInt()
            when {
                diffPct >= 50 -> insights += Insight(
                    emoji = "📈",
                    title = "High-spend day for you",
                    message = "Today's ₹${fmt(todaySpent)} is ${diffPct}% more than your usual ₹${fmt(avgDailySpend)} daily average.",
                    type = InsightType.CAUTION,
                    priority = 7
                )
                diffPct in 20..49 -> insights += Insight(
                    emoji = "📉",
                    title = "Spending above average",
                    message = "You're spending ₹${fmt(diff)} more than your average day. Worth keeping an eye on.",
                    type = InsightType.CAUTION,
                    priority = 5
                )
                diffPct <= -30 -> insights += Insight(
                    emoji = "🏆",
                    title = "Below your average — nice!",
                    message = "You're spending ₹${fmt(-diff)} less than your usual daily average. Solid discipline!",
                    type = InsightType.POSITIVE,
                    priority = 6
                )
            }
        }

        // ── 3. WEEK PACE ──────────────────────────────────────────────
        if (weekBudget > 0 && weekTotal > 0) {
            val weekRatio = weekTotal / weekBudget
            val daysElapsed = daysElapsedThisWeek()
            val expectedRatio = daysElapsed / 7.0
            when {
                weekRatio > 0.9 && daysElapsed <= 5 -> insights += Insight(
                    emoji = "🗓️",
                    title = "Week budget nearly gone",
                    message = "You've used ${pct(weekTotal, weekBudget)}% of your weekly budget with ${7 - daysElapsed} days to go.",
                    type = InsightType.WARNING,
                    priority = 9
                )
                weekRatio > expectedRatio * 1.3 -> insights += Insight(
                    emoji = "⏩",
                    title = "Spending faster than planned",
                    message = "At this pace you'll exceed your weekly budget. ₹${fmt(weekBudget - weekTotal)} left for ${7 - daysElapsed} more days.",
                    type = InsightType.CAUTION,
                    priority = 7
                )
                weekRatio < expectedRatio * 0.6 && daysElapsed >= 2 -> insights += Insight(
                    emoji = "💚",
                    title = "Week is going well",
                    message = "You've only used ${pct(weekTotal, weekBudget)}% of your weekly budget so far. Great pace!",
                    type = InsightType.POSITIVE,
                    priority = 5
                )
            }
        }

        // ── 4. MONTH PACE ─────────────────────────────────────────────
        if (monthBudget > 0 && monthTotal > 0) {
            val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            val monthRatio = monthTotal / monthBudget
            val expectedMonthRatio = dayOfMonth.toDouble() / daysInMonth
            val daysLeft = daysInMonth - dayOfMonth

            when {
                monthRatio > 0.95 && daysLeft > 5 -> insights += Insight(
                    emoji = "🔴",
                    title = "Monthly budget almost gone",
                    message = "₹${fmt(monthBudget - monthTotal)} left for the month with $daysLeft days remaining. Spend carefully.",
                    type = InsightType.WARNING,
                    priority = 9
                )
                monthRatio > expectedMonthRatio * 1.25 -> insights += Insight(
                    emoji = "📆",
                    title = "Month spending ahead of pace",
                    message = "You've spent ₹${fmt(monthTotal)} — ${pct(monthTotal, monthBudget)}% of your monthly budget at day $dayOfMonth.",
                    type = InsightType.CAUTION,
                    priority = 6
                )
                monthRatio < expectedMonthRatio * 0.7 && dayOfMonth >= 10 -> insights += Insight(
                    emoji = "🌟",
                    title = "Excellent monthly pace",
                    message = "Only ${pct(monthTotal, monthBudget)}% of monthly budget used by day $dayOfMonth. You're doing great!",
                    type = InsightType.POSITIVE,
                    priority = 6
                )
            }
        }

        // ── 5. TOP SPENDING CATEGORY ──────────────────────────────────
        if (topCategoryName.isNotEmpty() && topCategoryAmount > 0 && todaySpent > 0) {
            val catPct = ((topCategoryAmount / todaySpent) * 100).toInt()
            if (catPct >= 60) {
                insights += Insight(
                    emoji = "🔍",
                    title = "$topCategoryName is your main spend",
                    message = "${catPct}% of today's expenses went to $topCategoryName (₹${fmt(topCategoryAmount)}).",
                    type = InsightType.NEUTRAL,
                    priority = 4
                )
            }
        }

        // ── 6. TRANSACTION FREQUENCY ──────────────────────────────────
        if (totalTransactionsToday >= 6) {
            insights += Insight(
                emoji = "🛒",
                title = "Lots of small spends today",
                message = "$totalTransactionsToday transactions today. Small purchases add up — review them.",
                type = InsightType.CAUTION,
                priority = 4
            )
        }

        // ── 7. TIME-AWARE TIPS ────────────────────────────────────────
        if (todaySpent > 0 && dailyBudget > 0) {
            when {
                hour in 11..13 && remaining < dailyBudget * 0.4 -> insights += Insight(
                    emoji = "🍽️",
                    title = "Lunch time — budget check",
                    message = "Only ₹${fmt(remaining)} left today and it's lunchtime. Plan your afternoon spend.",
                    type = InsightType.CAUTION,
                    priority = 5
                )
                hour >= 20 && todaySpent < dailyBudget * 0.5 -> insights += Insight(
                    emoji = "🌙",
                    title = "Low spend evening",
                    message = "Good evening! You've only spent ₹${fmt(todaySpent)} today. Finishing strong.",
                    type = InsightType.POSITIVE,
                    priority = 4
                )
                hour >= 22 && todaySpent > dailyBudget -> insights += Insight(
                    emoji = "💤",
                    title = "End of day recap",
                    message = "Today ended ₹${fmt(todaySpent - dailyBudget)} over budget. Start fresh tomorrow with ₹${fmt(dailyBudget)}.",
                    type = InsightType.NEUTRAL,
                    priority = 6
                )
            }
        }

        // ── 8. WEEKEND AWARENESS ─────────────────────────────────────
        if (isWeekend && avgDailySpend > 0 && todaySpent > avgDailySpend * 1.2) {
            insights += Insight(
                emoji = "🎉",
                title = "Weekend spending bump",
                message = "Weekends tend to cost more. You're ₹${fmt(todaySpent - avgDailySpend)} above your weekday average.",
                type = InsightType.NEUTRAL,
                priority = 3
            )
        }

        // ── 9. SAVINGS POTENTIAL ──────────────────────────────────────
        if (dailyBudget > 0 && avgDailySpend > 0 && avgDailySpend < dailyBudget * 0.8) {
            val monthlySavings = (dailyBudget - avgDailySpend) * 30
            if (monthlySavings > 500) {
                insights += Insight(
                    emoji = "💰",
                    title = "You could save ₹${fmt(monthlySavings)}/month",
                    message = "At your average daily spend of ₹${fmt(avgDailySpend)}, you're on track to save ₹${fmt(monthlySavings)} this month.",
                    type = InsightType.POSITIVE,
                    priority = 3
                )
            }
        }

        // ── 10. STREAK / MOTIVATION ───────────────────────────────────
        if (recentExpenses.isNotEmpty()) {
            val consecutiveGoodDays = countGoodDays(recentExpenses, dailyBudget)
            if (consecutiveGoodDays >= 3) {
                insights += Insight(
                    emoji = "🔥",
                    title = "$consecutiveGoodDays-day budget streak!",
                    message = "You've stayed within budget for $consecutiveGoodDays days in a row. Keep the streak alive!",
                    type = InsightType.POSITIVE,
                    priority = 7
                )
            }
        }

        // Return sorted by priority (highest first), max 4 insights shown
        return insights
            .sortedByDescending { it.priority }
            .take(4)
    }

    // ─────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────

    private fun fmt(amount: Double) = "%.0f".format(amount)

    private fun pct(part: Double, total: Double): Int =
        if (total > 0) ((part / total) * 100).toInt() else 0

    private fun formatHour(hour: Int): String = when {
        hour < 12 -> "${hour}am"
        hour == 12 -> "12pm"
        else -> "${hour - 12}pm"
    }

    private fun daysElapsedThisWeek(): Int {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val firstDay = cal.firstDayOfWeek
        return ((dayOfWeek - firstDay + 7) % 7) + 1
    }

    private fun countGoodDays(
        recentExpenses: List<RecentExpense>,
        dailyBudget: Double
    ): Int {
        if (dailyBudget <= 0) return 0
        // Group by date, sum per day, count consecutive days under budget
        val byDay = recentExpenses
            .groupBy { it.dateKey }
            .map { (_, expenses) -> expenses.sumOf { it.amount } }
            .take(7) // look back max 7 days
        var streak = 0
        for (dayTotal in byDay) {
            if (dayTotal <= dailyBudget) streak++ else break
        }
        return streak
    }
}

// ─────────────────────────────────────────
// Models
// ─────────────────────────────────────────

data class Insight(
    val emoji: String,
    val title: String,
    val message: String,
    val type: InsightType,
    val priority: Int = 5   // higher = shown first
)

enum class InsightType {
    POSITIVE,   // green
    CAUTION,    // amber
    WARNING,    // red
    NEUTRAL     // gray/blue — informational
}

// Lightweight model passed from ViewModel for streak calculation
data class RecentExpense(
    val dateKey: String,  // "YYYY-MM-DD"
    val amount: Double
)