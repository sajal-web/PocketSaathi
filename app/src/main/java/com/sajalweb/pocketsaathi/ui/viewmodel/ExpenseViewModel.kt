package com.sajalweb.pocketsaathi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajalweb.pocketsaathi.data.db.DayTotal
import com.sajalweb.pocketsaathi.data.model.Category
import com.sajalweb.pocketsaathi.data.model.Expense
import com.sajalweb.pocketsaathi.data.model.ParsedExpense
import com.sajalweb.pocketsaathi.data.model.ReportType
import com.sajalweb.pocketsaathi.data.prefs.UserPrefsDataStore
import com.sajalweb.pocketsaathi.data.repository.ExpenseRepository
import com.sajalweb.pocketsaathi.domain.BudgetCalculator
import com.sajalweb.pocketsaathi.domain.Insight
import com.sajalweb.pocketsaathi.domain.InsightEngine
import com.sajalweb.pocketsaathi.domain.SmartParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ReportUiState(
    val reportType: ReportType = ReportType.WEEKLY,
    val totalSpending: Double = 0.0,
    val budgetLimit: Double = 0.0,
    val savedAmount: Double = 0.0,
    val overspentAmount: Double = 0.0,
    val isSaved: Boolean = false,
    val insightMessage: String = ""
)

data class DashboardUiState(
    val todayTotal: Double = 0.0,
    val dailyBudget: Double = 0.0,
    val monthlyLimit: Double = 0.0,
    val avgDailySpend: Double = 0.0,
    val budgetPercentage: Int = 70,
    val budgetStartDate: Long = 0L,
    val budgetEndDate: Long = 0L,
    val remaining: Double = 0.0,
    val budgetPercentUsed: Float = 0f,
    val weekTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val yearTotal: Double = 0.0,
    val todayExpenses: List<Expense> = emptyList(),
    val recentExpenses: List<Expense> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val healthScore: Int = 75,
    val isSetupDone: Boolean = false,
    val weeklyBreakdown: List<DayTotal> = emptyList(),
    val isDataLoaded: Boolean = false,
    val report: ReportUiState = ReportUiState()
)

data class AddExpenseUiState(
    val inputText: String = "",
    val parsed: ParsedExpense? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val parser: SmartParser,
    private val calculator: BudgetCalculator,
    private val insightEngine: InsightEngine,
    private val prefs: UserPrefsDataStore
) : ViewModel() {

    private val _addState = MutableStateFlow(AddExpenseUiState())
    val addState = _addState.asStateFlow()

    val dashboardState: StateFlow<DashboardUiState> = combine(
        repository.getTodayTotal(),
        repository.getWeekTotal(),
        repository.getMonthTotal(),
        repository.getYearTotal(),
        prefs.monthlyLimit,
        prefs.budgetPercentage,
        prefs.isSetupDone,
        repository.getTodayExpenses(),
        repository.getRecentExpenses(),
        repository.getWeeklyBreakdown(),
        prefs.budgetStartDate,
        prefs.budgetEndDate
    ) { args ->
        val todayTotal    = (args[0] as? Double) ?: 0.0
        val weekTotal     = (args[1] as? Double) ?: 0.0
        val monthTotal    = (args[2] as? Double) ?: 0.0
        val yearTotal     = (args[3] as? Double) ?: 0.0
        val monthlyLimit  = args[4] as Double
        val percentage    = args[5] as Int
        val setupDone     = args[6] as Boolean
        @Suppress("UNCHECKED_CAST")
        val todayExpenses = args[7] as List<Expense>
        @Suppress("UNCHECKED_CAST")
        val recent        = args[8] as List<Expense>
        @Suppress("UNCHECKED_CAST")
        val weekly        = args[9] as List<DayTotal>
        val startDate     = args[10] as Long
        val endDate       = args[11] as Long

        val dailyBudget = if (startDate > 0 && endDate > 0) {
            calculator.getDailyBudget(monthlyLimit, percentage, startDate, endDate)
        } else {
            calculator.getDailyBudget(monthlyLimit, percentage)
        }
        val avgSpend    = repository.getAvgDailySpend() ?: 0.0

        val daysInMonth  = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
        val weekBudget   = dailyBudget * 7
        val monthBudget  = dailyBudget * daysInMonth
        val yearBudget   = monthBudget * 12

        DashboardUiState(
            todayTotal        = todayTotal,
            dailyBudget       = dailyBudget,
            monthlyLimit      = monthlyLimit,
            budgetPercentage  = percentage,
            remaining         = calculator.getRemainingToday(dailyBudget, todayTotal),
            budgetPercentUsed = calculator.getBudgetPercentUsed(dailyBudget, todayTotal),
            weekTotal         = weekTotal,
            monthTotal        = monthTotal,
            yearTotal         = yearTotal,
            todayExpenses     = todayExpenses,
            recentExpenses    = recent,
            healthScore       = insightEngine.getHealthScore(todayTotal, dailyBudget, avgSpend),
            insights          = insightEngine.getInsights(todayTotal, dailyBudget, avgSpend, weekTotal),
            isSetupDone       = setupDone,
            weeklyBreakdown   = weekly,
            isDataLoaded      = true,
            report = buildReport(ReportType.WEEKLY, weekTotal, weekBudget)
        )
    }.combine(
        combine(
            prefs.selectedReportType as Flow<Any?>,
            repository.getWeekTotal(),
            repository.getMonthTotal(),
            repository.getYearTotal(),
            prefs.monthlyLimit,
            prefs.budgetPercentage,
        ) { args ->
            val reportType   = args[0] as ReportType
            val weekTotal    = (args[1] as? Double) ?: 0.0
            val monthTotal   = (args[2] as? Double) ?: 0.0
            val yearTotal    = (args[3] as? Double) ?: 0.0
            val monthlyLimit = args[4] as Double
            val percentage   = args[5] as Int

            val dailyBudget  = calculator.getDailyBudget(monthlyLimit, percentage)
            val daysInMonth  = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
            val weekBudget   = dailyBudget * 7
            val monthBudget  = dailyBudget * daysInMonth
            val yearBudget   = monthBudget * 12

            val (spending, budget) = when (reportType) {
                ReportType.WEEKLY  -> weekTotal  to weekBudget
                ReportType.MONTHLY -> monthTotal to monthBudget
                ReportType.YEARLY  -> yearTotal  to yearBudget
            }
            buildReport(reportType, spending, budget)
        }
    ) { dashState, report ->
        dashState.copy(report = report)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardUiState()
    )

    private fun buildReport(
        type: ReportType,
        spending: Double,
        budget: Double
    ): ReportUiState {
        val diff = budget - spending
        val isSaved = diff >= 0
        val insightMessage = when {
            budget <= 0   -> "Set your income to see report"
            isSaved       -> "You saved ₹${"%.0f".format(diff)} this ${type.label.lowercase()}"
            else          -> "Overspent by ₹${"%.0f".format(-diff)} this ${type.label.lowercase()}"
        }
        return ReportUiState(
            reportType      = type,
            totalSpending   = spending,
            budgetLimit     = budget,
            savedAmount     = if (isSaved) diff else 0.0,
            overspentAmount = if (!isSaved) -diff else 0.0,
            isSaved         = isSaved,
            insightMessage  = insightMessage
        )
    }

    fun selectReportType(type: ReportType) {
        viewModelScope.launch { prefs.saveReportType(type) }
    }

    fun onInputChanged(text: String) {
        _addState.update { it.copy(inputText = text, parsed = null) }
        if (text.length > 3) parseInput(text)
    }

    private fun parseInput(text: String) {
        viewModelScope.launch {
            val result = parser.parse(text)
            _addState.update { it.copy(parsed = result) }
        }
    }

    fun saveExpense(amount: Double, description: String, category: Category, note: String = "") {
        viewModelScope.launch {
            repository.addExpense(
                Expense(amount = amount, description = description, category = category, note = note)
            )
            _addState.value = AddExpenseUiState(isSaved = true)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch { repository.updateExpense(expense) }
    }

    fun saveBudgetConfig(monthlyLimit: Double, percentage: Int, startDate: Long, endDate: Long) {
        viewModelScope.launch {
            prefs.saveBudgetConfig(monthlyLimit, percentage, startDate, endDate)
        }
    }

    fun resetAddState() {
        _addState.value = AddExpenseUiState()
    }
}