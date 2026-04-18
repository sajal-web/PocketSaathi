package com.sajalweb.pocketsaathi.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPrefsDataStore(private val context: Context) {

    companion object {
        val MONTHLY_LIMIT = doublePreferencesKey("monthly_limit")
        val BUDGET_PERCENTAGE = intPreferencesKey("budget_percentage")
        val SETUP_DONE = booleanPreferencesKey("setup_done")
    }

    val monthlyLimit: Flow<Double> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[MONTHLY_LIMIT] ?: 0.0 }

    val budgetPercentage: Flow<Int> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[BUDGET_PERCENTAGE] ?: 70 }

    val isSetupDone: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[SETUP_DONE] ?: false }

    suspend fun saveBudgetConfig(monthlyLimit: Double, percentage: Int) {
        context.dataStore.edit {
            it[MONTHLY_LIMIT] = monthlyLimit
            it[BUDGET_PERCENTAGE] = percentage
            it[SETUP_DONE] = true
        }
    }
}