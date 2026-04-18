package com.sajalweb.pocketsaathi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajalweb.pocketsaathi.data.db.DayTotal
import com.sajalweb.pocketsaathi.data.model.Category
import com.sajalweb.pocketsaathi.data.model.Expense
import com.sajalweb.pocketsaathi.data.model.ParsedExpense
import com.sajalweb.pocketsaathi.data.prefs.UserPrefsDataStore
import com.sajalweb.pocketsaathi.data.repository.ExpenseRepository
import com.sajalweb.pocketsaathi.domain.BudgetCalculator
import com.sajalweb.pocketsaathi.domain.Insight
import com.sajalweb.pocketsaathi.domain.InsightEngine
import com.sajalweb.pocketsaathi.domain.SmartParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val todayTotal: Double = 0.0,
    val dailyBudget: Double = 0.0,
    val monthlyLimit: Double = 0.0,
    val budgetPercentage: Int = 70,
    val remaining: Double = 0.0,
    val budgetPercentUsed: Float = 0f,
    val weekTotal: Double = 0.0,
    val monthTotal: Double = 0.0,
    val todayExpenses: List<Expense> = emptyList(),
    val recentExpenses: List<Expense> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val healthScore: Int = 75,
    val isSetupDone: Boolean = false,
    val weeklyBreakdown: List<DayTotal> = emptyList()
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
        prefs.monthlyLimit,      // now Double
        prefs.budgetPercentage,  // new Int
        prefs.isSetupDone,
        repository.getTodayExpenses(),
        repository.getRecentExpenses(),
        repository.getWeeklyBreakdown()
    ) { args ->
        val todayTotal = (args[0] as? Double) ?: 0.0
        val weekTotal = (args[1] as? Double) ?: 0.0
        val monthTotal = (args[2] as? Double) ?: 0.0
        val monthlyLimit = args[3] as Double
        val percentage = args[4] as Int
        val setupDone = args[5] as Boolean
        @Suppress("UNCHECKED_CAST")
        val todayExpenses = args[6] as List<Expense>
        @Suppress("UNCHECKED_CAST")
        val recent = args[7] as List<Expense>
        @Suppress("UNCHECKED_CAST")
        val weekly = args[8] as List<DayTotal>

        val dailyBudget = calculator.getDailyBudget(monthlyLimit, percentage)
        val avgSpend = repository.getAvgDailySpend() ?: 0.0

        DashboardUiState(
            todayTotal = todayTotal,
            dailyBudget = dailyBudget,
            monthlyLimit = monthlyLimit,   // you may rename this field later
            remaining = calculator.getRemainingToday(dailyBudget, todayTotal),
            budgetPercentUsed = calculator.getBudgetPercentUsed(dailyBudget, todayTotal),
            weekTotal = weekTotal,
            monthTotal = monthTotal,
            todayExpenses = todayExpenses,
            recentExpenses = recent,
            healthScore = insightEngine.getHealthScore(todayTotal, dailyBudget, avgSpend),
            insights = insightEngine.getInsights(todayTotal, dailyBudget, avgSpend, weekTotal),
            isSetupDone = setupDone,
            weeklyBreakdown = weekly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
    // --- Add Expense ---
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

    fun saveExpense(
        amount: Double,
        description: String,
        category: Category,
        note: String = ""
    ) {
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

    fun saveBudgetConfig(monthlyLimit: Double, percentage: Int) {
        viewModelScope.launch { prefs.saveBudgetConfig(monthlyLimit, percentage) }
    }

    fun resetAddState() {
        _addState.value = AddExpenseUiState()
    }
}