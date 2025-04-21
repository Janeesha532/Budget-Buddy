package com.example.budgebuddy3.repository

import androidx.lifecycle.LiveData
import com.example.budgebuddy3.database.BudgetDao
import com.example.budgebuddy3.model.Budget
import java.util.*

class BudgetRepository(private val budgetDao: BudgetDao) {
    fun getBudgetsForCurrentMonth(): LiveData<List<Budget>> {
        return budgetDao.getBudgetsForCurrentMonth()
    }

    suspend fun getBudgetsForCurrentMonthSync(): List<Budget> {
        val calendar = Calendar.getInstance()
        return budgetDao.getBudgetsByMonthSync(
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )
    }

    suspend fun insertBudget(budget: Budget) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun deleteAllBudgets() {
        budgetDao.deleteAllBudgets()
    }
} 