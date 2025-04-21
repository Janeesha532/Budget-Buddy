package com.example.budgebuddy3.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.budgebuddy3.model.Budget
import java.util.*

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getBudgetsByMonth(month: Int, year: Int): LiveData<List<Budget>>

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    suspend fun getBudgetsByMonthSync(month: Int, year: Int): List<Budget>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Update
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getBudgetsForCurrentMonth(month: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
                                year: Int = Calendar.getInstance().get(Calendar.YEAR)): LiveData<List<Budget>>
} 